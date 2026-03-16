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
            <th>Sĩ số</th>
            <th>Lịch học</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in courses" :key="c.id">
            <td>{{ c.code }}</td>
            <td>{{ c.name }}</td>
            <td>{{ c.semester?.code || '-' }}</td>
            <td>{{ c.subject?.code || '-' }}</td>
            <td>{{ c.currentStudents }} / {{ c.maxStudents }}</td>
            <td>{{ formatTimeSlots(c.timeSlots) }}</td>
            <td>
              <v-btn size="small" color="primary" variant="flat" @click="goEnrollments(c.id)">
                DS sinh viên / Nhập điểm
              </v-btn>
            </td>
          </tr>
          <tr v-if="courses.length === 0">
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useLookupsStore } from '@/stores/lookups'
import { sectionService, type Section, type SectionTimeSlot } from '@/api/services/section.service'
import { formatTimeSlotsVn } from '@/utils/schedule'
import PageHeader from '@/components/ui/PageHeader.vue'

const router = useRouter()
const authStore = useAuthStore()
const lookupsStore = useLookupsStore()

const courses = ref<Section[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const semesterFilterId = ref<number | null>(null)
const subjectFilterId = ref<number | null>(null)

const semesterOptions = computed(() => lookupsStore.semesterOptions)
const subjectOptions = computed(() => lookupsStore.subjectOptions)

const formatTimeSlots = (slots?: any) => {
    if (!slots) return '-'
    // If it's a string, try to parse or display as is
    if (typeof slots === 'string') return slots
    return formatTimeSlotsVn(slots as SectionTimeSlot[])
}

const fetchCourses = async () => {
  loading.value = true
  try {
    // Teacher only sees their own sections.
    // The backend SectionController automatically filters by current user if ROLE_TEACHER (and not ADMIN).
    // So we don't need to pass teacherId explicitly if the backend handles it.
    // However, if we want to be explicit, we could pass teacherId if we knew it.
    // But let's rely on the backend logic first.
    const res = await sectionService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      semesterId: semesterFilterId.value || undefined,
      subjectId: subjectFilterId.value || undefined,
      // We don't filter by active=true because teacher might want to see past courses
    })
    // @ts-ignore
    const data = res.data?.data
    if (data) {
        courses.value = data.data || []
        totalPages.value = data.totalPages || 1
    }
  } catch (e) {
      console.error(e)
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

let timeout: any
const handleSearch = () => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
        page.value = 1
        fetchCourses()
    }, 300)
}

const goEnrollments = (sectionId: number) => {
  router.push({ name: 'TeacherSectionEnrollments', params: { sectionId } })
}

onMounted(async () => {
  await lookupsStore.ensureAcademicLookups() // Ensure lookups are loaded
  await reload()
})
</script>
