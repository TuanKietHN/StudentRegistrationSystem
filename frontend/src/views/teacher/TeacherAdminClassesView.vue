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
          <v-text-field v-model.number="intakeYear" type="number" label="Khóa (năm nhập học)" @update:model-value="handleSearch" />
        </v-col>
      </v-row>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
        <thead>
          <tr>
            <th>Mã lớp</th>
            <th>Tên lớp</th>
            <th>Khoa</th>
            <th>Khóa</th>
            <th>Chương trình</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in adminClasses" :key="c.id">
            <td>{{ c.code }}</td>
            <td>{{ c.name }}</td>
            <td>{{ c.departmentName || '-' }}</td>
            <td>{{ c.intakeYear ?? '-' }}</td>
            <td>{{ c.program || '-' }}</td>
            <td>
              <v-btn size="small" color="primary" variant="flat" @click="goStudents(c.id)">Danh sách sinh viên</v-btn>
            </td>
          </tr>
          <tr v-if="adminClasses.length === 0">
            <td colspan="6" class="text-center py-6">Không có dữ liệu</td>
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
import { adminClassService, type AdminClass } from '@/api/services/adminClass.service'
import { useLookupsStore } from '@/stores/lookups'
import { useDebounceFn } from '@/composables/useDebounceFn'
import PageHeader from '@/components/ui/PageHeader.vue'

const router = useRouter()
const lookupsStore = useLookupsStore()

const adminClasses = ref<AdminClass[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const departmentId = ref<number | null>(null)
const intakeYear = ref<number | null>(null)

const departmentOptions = computed(() => lookupsStore.departmentOptions)

const fetchAdminClasses = async () => {
  loading.value = true
  try {
    const res = await adminClassService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      departmentId: departmentId.value || undefined,
      intakeYear: intakeYear.value || undefined,
      active: true
    })
    const data = unwrapPageResponse<AdminClass>(res)
    adminClasses.value = data.data || []
    totalPages.value = data.totalPages || 1
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

const { debounced: debouncedSearch } = useDebounceFn(() => {
  page.value = 1
  fetchAdminClasses()
}, 350)

const handleSearch = () => debouncedSearch()

const goStudents = (adminClassId: number) => {
  router.push({ name: 'TeacherAdminClassStudents', params: { adminClassId } })
}

onMounted(async () => {
  await lookupsStore.loadDepartments()
  await reload()
})
</script>

