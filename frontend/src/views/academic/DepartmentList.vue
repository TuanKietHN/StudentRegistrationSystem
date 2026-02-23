<template>
  <v-card>
    <v-card-title class="d-flex align-center justify-space-between">
      <div class="text-h6">Danh sách Khoa</div>
      <v-btn color="primary" variant="flat" @click="createDepartment">Thêm mới</v-btn>
    </v-card-title>
    <v-card-text>
      <v-text-field
        v-model="keyword"
        label="Tìm kiếm theo mã, tên..."
        density="comfortable"
        variant="outlined"
        @update:model-value="handleSearch"
      />

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã Khoa</th>
            <th>Tên Khoa</th>
            <th>Trưởng Khoa</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="dept in departments" :key="dept.id">
            <td>{{ dept.id }}</td>
            <td>{{ dept.code }}</td>
            <td>{{ dept.name }}</td>
            <td>{{ dept.headTeacherName || '-' }}</td>
            <td>
              <v-chip :color="dept.active ? 'green' : 'red'" variant="tonal" size="small">
                {{ dept.active ? 'Hoạt động' : 'Ngưng' }}
              </v-chip>
            </td>
            <td>
              <v-btn size="small" variant="text" @click="editDepartment(dept)">Sửa</v-btn>
              <v-btn size="small" color="error" variant="text" @click="deleteDepartment(dept.id)">Xóa</v-btn>
            </td>
          </tr>
          <tr v-if="departments.length === 0">
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
import { ref, onMounted } from 'vue'
import { departmentService, type Department } from '@/api/services/department.service'

const departments = ref<Department[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
let searchTimeout: any = null

const fetchDepartments = async () => {
  loading.value = true
  try {
    const response = await departmentService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value
    })
    const result = response.data.data
    departments.value = result.data
    totalPages.value = result.totalPages
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    page.value = 1
    fetchDepartments()
  }, 500)
}

const changePage = (newPage: number) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    page.value = newPage
    fetchDepartments()
  }
}

const createDepartment = () => {
  alert('Chức năng thêm mới đang phát triển')
}

const editDepartment = (dept: Department) => {
  alert(`Sửa khoa: ${dept.name}`)
}

const deleteDepartment = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa khoa này?')) return
  try {
    await departmentService.delete(id)
    fetchDepartments()
  } catch {
    alert('Xóa thất bại')
  }
}

onMounted(fetchDepartments)
</script>
