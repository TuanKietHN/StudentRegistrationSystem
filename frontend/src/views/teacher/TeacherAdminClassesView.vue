<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Lớp hành chính">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-row>
        <v-col cols="12" md="4">
          <v-text-field v-model="keyword" label="Tìm kiếm mã, tên..." @update:model-value="handleSearch" />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
              v-model="departmentId"
              :items="departmentOptions"
              item-title="title"
              item-value="value"
              label="Khoa"
              clearable
              @update:model-value="handleSearch"
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
              v-model="cohortId"
              :items="cohortOptions"
              item-title="title"
              item-value="value"
              label="Niên khóa"
              clearable
              no-data-text="Không có dữ liệu"
              @update:model-value="handleSearch"
          />
        </v-col>
      </v-row>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
        <thead>
        <tr>
          <th>Mã lớp</th>
          <th>Tên lớp</th>
          <th>Khoa</th>
          <th>Niên khóa</th>
          <th>Khóa</th>
          <th>Chương trình</th>
          <th>Cố vấn</th>
          <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="c in adminClasses" :key="c.id">
          <td>{{ c.code }}</td>
          <td>{{ c.name }}</td>
          <td>{{ c.departmentName || '-' }}</td>
          <td>{{ c.cohortName || '-' }}</td>
          <td>{{ c.intakeYear ?? '-' }}</td>
          <td>{{ c.program || '-' }}</td>
          <td>{{ c.advisorTeacherId ? (teacherLabelById.get(c.advisorTeacherId) || `#${c.advisorTeacherId}`) : '-' }}</td>
          <td>
            <v-btn size="small" color="primary" variant="flat" class="mr-2" @click="goStudents(c.id)">Sinh viên</v-btn>
            <v-btn size="small" color="secondary" variant="flat" @click="goGrades(c.id)">Bảng điểm</v-btn>
          </td>
        </tr>
        <tr v-if="adminClasses.length === 0">
          <td colspan="8" class="text-center py-6">Không có dữ liệu</td>
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
import { unwrapPageResponse } from '@/api/response'
import { studentClassService, type StudentClass } from '@/api/services/studentClass.service'
import { useLookupsStore } from '@/stores/lookups'
import { cohortService, type Cohort } from '@/api/services/cohort.service'
import { teacherService, type Teacher } from '@/api/services/teacher.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const router = useRouter()
const lookupsStore = useLookupsStore()
const uiStore = useUiStore()

const adminClasses = ref<StudentClass[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const departmentId = ref<number | null>(null)
const cohortId = ref<number | null>(null)

const departmentOptions = computed(() => lookupsStore.departmentOptions)
const cohortOptions = ref<{ title: string; value: number }[]>([])
const teacherLabelById = ref(new Map<number, string>())

const fetchAdminClasses = async () => {
  loading.value = true
  try {
    const res = await studentClassService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      departmentId: departmentId.value || undefined,
      cohortId: cohortId.value || undefined,
      active: true
    })
    // @ts-ignore
    const data = res.data?.data
    if (data) {
        adminClasses.value = data.data || []
        totalPages.value = data.totalPages || 1
        
        // If teacher has only one admin class, it might be filtered by backend for security
        // But if no data returned, it means either no classes assigned or filter issue.
        // Let's try to fetch without 'active=true' if empty, just in case.
        if (adminClasses.value.length === 0 && page.value === 1) {
            // Optional: retry logic or just leave it empty
        }
    }
  } catch (err: any) {
    adminClasses.value = []
    totalPages.value = 1
    // Suppress 403 error notification if it's just empty list
    if (err?.response?.status !== 403) {
        uiStore.notify(err?.response?.data?.message || 'Không lấy được danh sách lớp hành chính', 'error', 4000)
    }
  } finally {
    loading.value = false
  }
}

const reload = async () => {
  await fetchAdminClasses()
}

const changePage = (p: number) => {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  fetchAdminClasses()
}

let timeout: any
const handleSearch = () => {
    clearTimeout(timeout)
    timeout = setTimeout(() => {
        page.value = 1
        fetchAdminClasses()
    }, 300)
}

const goStudents = (adminClassId: number) => {
  router.push({ name: 'TeacherAdminClassStudents', params: { adminClassId } })
}

const goGrades = (adminClassId: number) => {
  router.push({ name: 'TeacherAdminClassGrades', params: { adminClassId } })
}

const loadCohorts = async () => {
  try {
    const res = await cohortService.getAll({ page: 1, size: 500, active: true })
    // @ts-ignore
    const page = res.data?.data
    if (page) {
        cohortOptions.value = (page.data || []).map((c: any) => ({
        title: `${c.code} - ${c.name}`,
        value: c.id
        }))
    }
  } catch {
    cohortOptions.value = []
  }
}

const loadTeacherMap = async () => {
  try {
    const res = await teacherService.getAll({ page: 1, size: 500, active: true })
    // @ts-ignore
    const page = res.data?.data
    if (page) {
        teacherLabelById.value = new Map((page.data || []).map((t: any) => [t.id, `${t.username}${t.email ? ` (${t.email})` : ''}`]))
    }
  } catch {
    teacherLabelById.value = new Map()
  }
}

onMounted(async () => {
  try {
    await Promise.all([lookupsStore.loadDepartments(), loadCohorts(), loadTeacherMap()])
  } catch {}
  await reload()
})
</script>
