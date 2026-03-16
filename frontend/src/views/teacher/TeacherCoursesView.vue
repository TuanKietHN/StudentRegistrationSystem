<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Lớp tôi đang dạy">
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
            v-model="classFilterId"
            :items="classOptions"
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
            <th>Sĩ số</th>
            <th>Lịch học</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in myCourses" :key="c.id">
            <td>{{ c.code }}</td>
            <td>{{ c.name }}</td>
            <td>{{ c.semester?.code || '-' }}</td>
            <td>{{ c.clazz?.code || '-' }}</td>
            <td>{{ c.currentStudents }} / {{ c.maxStudents }}</td>
            <td>{{ formatTimeSlots(c.timeSlots) }}</td>
            <td>
              <v-btn size="small" color="primary" variant="flat" @click="goEnrollments(c.id)">
                DS sinh viên / Nhập điểm
              </v-btn>
              <v-btn size="small" variant="text" class="ml-2" @click="goAttendance(c.id)">Điểm danh</v-btn>
            </td>
          </tr>
          <tr v-if="myCourses.length === 0">
            <td colspan="7" class="text-center py-6">Không có dữ liệu</td>
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLookupsStore } from '@/stores/lookups'
import { unwrapPageResponse } from '@/api/response'
import { cohortService, type Cohort, type CohortTimeSlot } from '@/api/services/cohort.service'
import { useDebounceFn } from '@/composables/useDebounceFn'
import { formatTimeSlotsVn } from '@/utils/schedule'
import PageHeader from '@/components/ui/PageHeader.vue'

const router = useRouter()
const authStore = useAuthStore()
const lookupsStore = useLookupsStore()

const courses = ref<Cohort[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const semesterFilterId = ref<number | null>(null)
const classFilterId = ref<number | null>(null)

const semesterOptions = computed(() => lookupsStore.semesterOptions)
const classOptions = computed(() => lookupsStore.classOptions)

const myCourses = computed(() => {
  const username = authStore.currentUser?.username
  if (!username) return []
  return (courses.value || []).filter((c) => (c.teacher?.username || '') === username)
})

const formatTimeSlots = (slots?: CohortTimeSlot[] | null) => formatTimeSlotsVn(slots)

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await cohortService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      semesterId: semesterFilterId.value || undefined,
      classId: classFilterId.value || undefined,
      active: true
    })
    const data = unwrapPageResponse<Cohort>(res)
    courses.value = data.data || []
    totalPages.value = data.totalPages || 1
  } finally {
    loading.value = false
  }
}

const reload = async () => {
  await fetchCourses()
}

const changePage = (p: number) => {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  fetchCourses()
}

const { debounced: debouncedSearch } = useDebounceFn(() => {
  page.value = 1
  fetchCourses()
}, 350)

const handleSearch = () => debouncedSearch()

const goEnrollments = (cohortId: number) => {
  router.push({ name: 'TeacherCohortEnrollments', params: { cohortId } })
}

const goAttendance = (cohortId: number) => {
  router.push({ name: 'TeacherCohortAttendance', params: { cohortId } })
}

onMounted(async () => {
  await lookupsStore.ensureAcademicLookups()
  await reload()
})
</script>
