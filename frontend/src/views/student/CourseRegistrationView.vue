<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Đăng ký lớp học">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-row>
        <v-col cols="12" md="4">
          <v-text-field v-model="keyword" label="Tìm kiếm theo mã, tên..." @update:model-value="handleSearch" />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
            v-model="semesterFilterId"
            :items="semesterOptions"
            item-title="title"
            item-value="value"
            label="Lọc học kỳ"
            clearable
            @update:model-value="handleSearch"
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
            v-model="subjectFilterId"
            :items="subjectOptions"
            item-title="title"
            item-value="value"
            label="Lọc môn học"
            clearable
            @update:model-value="handleSearch"
          />
        </v-col>
      </v-row>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
        <thead>
          <tr>
            <th>Mã</th>
            <th>Tên</th>
            <th>Học kỳ</th>
            <th>Môn học</th>
            <th>Lịch học</th>
            <th>Thời gian đăng ký</th>
            <th>Sĩ số</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in courses" :key="c.id">
            <td>{{ c.code }}</td>
            <td>{{ c.name }}</td>
            <td>{{ c.semester?.code || '-' }}</td>
            <td>{{ c.subject?.code || '-' }}</td>
            <td>{{ formatTimeSlots(c.timeSlots) }}</td>
            <td>{{ formatWindow(c.enrollmentStartDate, c.enrollmentEndDate) }}</td>
            <td>{{ c.currentStudents }} / {{ c.maxStudents }}</td>
            <td>
              <v-chip :color="statusColor(c)" variant="tonal" size="small">
                {{ statusText(c) }}
              </v-chip>
            </td>
            <td>
              <v-btn
                size="small"
                color="primary"
                variant="flat"
                :loading="enrollingId === c.id"
                :disabled="isDisabled(c)"
                @click="enroll(c)"
              >
                Đăng ký
              </v-btn>
            </td>
          </tr>
          <tr v-if="courses.length === 0">
            <td colspan="9" class="text-center py-6">Không có dữ liệu</td>
          </tr>
        </tbody>
      </v-table>

      <div class="d-flex align-center justify-center mt-4">
        <v-btn :disabled="page === 1" variant="text" @click="changePage(page - 1)">Trước</v-btn>
        <div class="mx-4">Trang {{ page }} / {{ totalPages }}</div>
        <v-btn :disabled="page === totalPages" variant="text" @click="changePage(page + 1)">Sau</v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useUiStore } from '@/stores/ui'
import { useLookupsStore } from '@/stores/lookups'
import { unwrapApiResponse, unwrapPageResponse } from '@/api/response'
import { courseService, type Course, type CourseTimeSlot } from '@/api/services/course.service'
import { enrollmentService, type Enrollment } from '@/api/services/enrollment.service'
import { useDebounceFn } from '@/composables/useDebounceFn'
import { formatTimeSlotsVn } from '@/utils/schedule'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUiStore()
const lookupsStore = useLookupsStore()

const courses = ref<Course[]>([])
const myEnrollments = ref<Enrollment[]>([])
const enrollingId = ref<number | null>(null)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const semesterFilterId = ref<number | null>(null)
const subjectFilterId = ref<number | null>(null)

const semesterOptions = computed(() => lookupsStore.semesterOptions)
const subjectOptions = computed(() => lookupsStore.subjectOptions)

const todayStr = () => new Date().toISOString().slice(0, 10)

const isWindowOpen = (start?: string | null, end?: string | null) => {
  if (!start || !end) return false
  const now = todayStr()
  return now >= start && now <= end
}

const isFull = (c: Course) => (c.currentStudents || 0) >= (c.maxStudents || 0)

const isAlreadyEnrolled = (courseId: number) => myEnrollments.value.some((e) => e.course?.id === courseId && e.status === 'ENROLLED')

const toMinutes = (time: string) => {
  const [h, m] = time.split(':')
  return Number(h) * 60 + Number(m)
}

const overlap = (a: CourseTimeSlot, b: CourseTimeSlot) => {
  if (a.dayOfWeek !== b.dayOfWeek) return false
  const aStart = toMinutes(a.startTime)
  const aEnd = toMinutes(a.endTime)
  const bStart = toMinutes(b.startTime)
  const bEnd = toMinutes(b.endTime)
  return aStart < bEnd && aEnd > bStart
}

const isScheduleConflict = (c: Course) => {
  const slots = c.timeSlots || []
  if (!slots.length) return false
  const semesterId = c.semester?.id
  if (!semesterId) return false

  for (const e of myEnrollments.value) {
    if (e.status !== 'ENROLLED') continue
    if (e.course?.semester?.id !== semesterId) continue
    const enrolledSlots = e.course?.timeSlots || []
    for (const s of slots) {
      for (const es of enrolledSlots) {
        if (overlap(s, es)) return true
      }
    }
  }
  return false
}

const statusText = (c: Course) => {
  if (isAlreadyEnrolled(c.id)) return 'Đã đăng ký'
  if (!c.active) return 'Ngưng'
  if (!isWindowOpen(c.enrollmentStartDate, c.enrollmentEndDate)) return 'Hết hạn'
  if (isScheduleConflict(c)) return 'Trùng lịch'
  if (isFull(c)) return 'Đầy'
  return 'Có thể đăng ký'
}

const statusColor = (c: Course) => {
  const text = statusText(c)
  if (text === 'Có thể đăng ký') return 'green'
  if (text === 'Đã đăng ký') return 'blue'
  return 'orange'
}

const isDisabled = (c: Course) => {
  if (isAlreadyEnrolled(c.id)) return true
  if (!c.active) return true
  if (!isWindowOpen(c.enrollmentStartDate, c.enrollmentEndDate)) return true
  if (isFull(c)) return true
  if (isScheduleConflict(c)) return true
  return false
}

const formatWindow = (start?: string | null, end?: string | null) => {
  if (!start || !end) return '-'
  return `${start} → ${end}`
}

const formatTimeSlots = (slots?: CourseTimeSlot[] | null) => formatTimeSlotsVn(slots)

const fetchMyEnrollments = async () => {
  try {
    const res = await enrollmentService.getMyEnrollments()
    myEnrollments.value = unwrapApiResponse<Enrollment[]>(res) || []
  } catch (err: any) {
    myEnrollments.value = []
    uiStore.notify(err?.response?.data?.message || 'Không lấy được danh sách đăng ký của bạn', 'error', 4000)
  }
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await courseService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      semesterId: semesterFilterId.value || undefined,
      subjectId: subjectFilterId.value || undefined,
      active: true
    })
    const pageRes = unwrapPageResponse<Course>(res)
    courses.value = pageRes.data || []
    totalPages.value = pageRes.totalPages || 1
  } finally {
    loading.value = false
  }
}

const reload = async () => {
  await Promise.allSettled([fetchMyEnrollments(), fetchCourses()])
}

const changePage = async (p: number) => {
  page.value = p
  await fetchCourses()
}

const { debounced: debouncedSearch } = useDebounceFn(async () => {
  page.value = 1
  await reload()
}, 350)

const handleSearch = () => debouncedSearch()

const enroll = async (c: Course) => {
  enrollingId.value = c.id
  try {
    await enrollmentService.enrollSelf(c.id)
    uiStore.notify('Đăng ký thành công', 'success')
    await reload()
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Đăng ký thất bại'
    uiStore.notify(msg, 'error', 4000)
  } finally {
    enrollingId.value = null
  }
}

onMounted(async () => {
  await lookupsStore.ensureAcademicLookups()
  await reload()
})
</script>
