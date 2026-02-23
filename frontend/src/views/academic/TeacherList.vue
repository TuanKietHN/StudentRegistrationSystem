<template>
  <v-card>
    <v-card-title class="d-flex align-center justify-space-between">
      <div class="text-h6">Danh sách Giảng viên</div>
      <v-btn color="primary" variant="flat" @click="createTeacher">Thêm mới</v-btn>
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
            <th>Mã NV</th>
            <th>Username</th>
            <th>Họ tên</th>
            <th>Khoa</th>
            <th>Chức danh</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="teacher in teachers" :key="teacher.id">
            <td>{{ teacher.id }}</td>
            <td>{{ teacher.employeeCode }}</td>
            <td>{{ teacher.username }}</td>
            <td>{{ teacher.fullName || '-' }}</td>
            <td>{{ teacher.departmentName || '-' }}</td>
            <td>{{ teacher.title || '-' }}</td>
            <td>
              <v-chip :color="teacher.active ? 'green' : 'red'" variant="tonal" size="small">
                {{ teacher.active ? 'Hoạt động' : 'Ngưng' }}
              </v-chip>
            </td>
            <td>
              <v-btn size="small" variant="text" @click="editTeacher(teacher)">Sửa</v-btn>
              <v-btn size="small" color="error" variant="text" @click="deleteTeacher(teacher.id)">Xóa</v-btn>
            </td>
          </tr>
          <tr v-if="teachers.length === 0">
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
import { ref, onMounted } from 'vue'
import { teacherService, type Teacher } from '@/api/services/teacher.service'

const teachers = ref<Teacher[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
let searchTimeout: any = null

const fetchTeachers = async () => {
  loading.value = true
  try {
    const response = await teacherService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value
    })
    const result = response.data.data
    teachers.value = result.data
    totalPages.value = result.totalPages
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    page.value = 1
    fetchTeachers()
  }, 500)
}

const changePage = (newPage: number) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    page.value = newPage
    fetchTeachers()
  }
}

const createTeacher = () => {
  alert('Chức năng thêm mới đang phát triển')
}

const editTeacher = (teacher: Teacher) => {
  alert(`Sửa giảng viên: ${teacher.username}`)
}

const deleteTeacher = async (id: number) => {
  if (confirm('Bạn có chắc chắn muốn xóa giảng viên này?')) {
    try {
      await teacherService.delete(id)
      fetchTeachers()
    } catch {
      alert('Xóa thất bại')
    }
  }
}

onMounted(() => {
  fetchTeachers()
})
</script>
