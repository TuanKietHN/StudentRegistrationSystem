<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Danh sách Giảng viên">
        <template #actions>
          <v-btn v-if="isAdmin" color="primary" variant="flat" @click="openCreateDialog">Thêm mới</v-btn>
        </template>
      </PageHeader>
      <v-row>
        <v-col cols="12" md="6">
          <v-text-field
              v-model="keyword"
              label="Tìm kiếm theo mã, tên..."
              @update:model-value="handleSearch"
          />
        </v-col>
        <v-col cols="12" md="6">
          <v-select
              v-model="departmentFilterId"
              :items="departmentOptions"
              item-title="title"
              item-value="value"
              label="Lọc theo khoa"
              clearable
              @update:model-value="handleSearch"
          />
        </v-col>
      </v-row>

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
            <v-btn size="small" variant="text" :disabled="!isAdmin" @click="openEditDialog(teacher)">Sửa</v-btn>
            <v-btn size="small" color="error" variant="text" :disabled="!isAdmin" @click="openDeleteDialog(teacher)">Xóa</v-btn>
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

  <v-dialog v-model="dialogOpen" max-width="760">
    <v-card>
      <v-card-title class="text-h6">
        {{ editingId ? 'Cập nhật giảng viên' : 'Thêm giảng viên' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="saveTeacher">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.employeeCode" label="Mã nhân viên" :rules="rules.employeeCode" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-autocomplete
                  v-model="form.userId"
                  :items="userOptions"
                  item-title="title"
                  item-value="value"
                  :loading="userLoading"
                  :disabled="!!editingId"
                  label="User (bắt buộc)"
                  density="comfortable"
                  variant="outlined"
                  :rules="editingId ? [] : rules.userId"
                  @update:search="onUserSearch"
                  clearable
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                  v-model="form.departmentId"
                  :items="departmentOptions"
                  item-title="title"
                  item-value="value"
                  label="Khoa"
                  density="comfortable"
                  variant="outlined"
                  clearable
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.title" label="Chức danh" density="comfortable" variant="outlined" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.specialization" label="Chuyên môn" density="comfortable" variant="outlined" />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.phone" label="Số điện thoại" density="comfortable" variant="outlined" />
            </v-col>
            <v-col cols="12">
              <v-switch v-model="form.active" label="Kích hoạt" inset />
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions class="justify-end">
        <v-btn variant="text" @click="dialogOpen = false">Hủy</v-btn>
        <v-btn color="primary" variant="flat" :loading="saving" @click="saveTeacher">Lưu</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <ConfirmDialog
      v-model="deleteOpen"
      title="Xóa giảng viên"
      :text="`Bạn có chắc chắn muốn xóa giảng viên ${deleting?.username || ''} (${deleting?.employeeCode || ''}) không?`"
      :loading="deletingLoading"
      @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { teacherService, type Teacher } from '@/api/services/teacher.service'
import { departmentService } from '@/api/services/department.service'
import { userService, type UserSummary } from '@/api/services/user.service'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const authStore = useAuthStore()
const uiStore = useUiStore()
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

const teachers = ref<Teacher[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
let searchTimeout: any = null

const departmentFilterId = ref<number | null>(null)
const departmentOptions = ref<Array<{ title: string; value: number }>>([])

const dialogOpen = ref(false)
const deleteOpen = ref(false)
const saving = ref(false)
const deletingLoading = ref(false)
const editingId = ref<number | null>(null)
const deleting = ref<Teacher | null>(null)
const formRef = ref<any>(null)

const userOptions = ref<Array<{ title: string; value: number }>>([])
const userLoading = ref(false)
let userSearchTimeout: any = null

const form = ref({
  userId: null as number | null,
  employeeCode: '',
  departmentId: null as number | null,
  specialization: '',
  title: '',
  phone: '',
  active: true
})

const rules = {
  userId: [(v: number | null) => !!v || 'User là bắt buộc'],
  employeeCode: [(v: string) => !!v || 'Mã nhân viên là bắt buộc']
}

const fetchTeachers = async () => {
  loading.value = true
  try {
    const response = await teacherService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      departmentId: departmentFilterId.value || undefined
    })
    const result = response.data.data
    teachers.value = result.data
    totalPages.value = result.totalPages
  } finally {
    loading.value = false
  }
}

const fetchDepartmentsForSelect = async () => {
  try {
    const res = await departmentService.getAll({ page: 1, size: 200 })
    const list = res.data.data.data || []
    departmentOptions.value = list.map((d: any) => ({ title: `${d.code} - ${d.name}`, value: d.id }))
  } catch {
    departmentOptions.value = []
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

const resetForm = () => {
  form.value = {
    userId: null,
    employeeCode: '',
    departmentId: null,
    specialization: '',
    title: '',
    phone: '',
    active: true
  }
}

const openCreateDialog = () => {
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

const openEditDialog = (teacher: Teacher) => {
  editingId.value = teacher.id
  form.value = {
    userId: teacher.userId,
    employeeCode: teacher.employeeCode,
    departmentId: teacher.departmentId ?? null,
    specialization: teacher.specialization || '',
    title: teacher.title || '',
    phone: teacher.phone || '',
    active: !!teacher.active
  }
  dialogOpen.value = true
}

const saveTeacher = async () => {
  if (!isAdmin.value) return
  const res = await formRef.value?.validate?.()
  if (res && res.valid === false) return

  saving.value = true
  try {
    const basePayload = {
      employeeCode: form.value.employeeCode,
      departmentId: form.value.departmentId || null,
      specialization: form.value.specialization || null,
      title: form.value.title || null,
      phone: form.value.phone || null,
      active: !!form.value.active
    }

    if (editingId.value) {
      await teacherService.update(editingId.value, basePayload)
      uiStore.notify('Cập nhật giảng viên thành công', 'success')
    } else {
      const payload = { ...basePayload, userId: form.value.userId }
      await teacherService.create(payload)
      uiStore.notify('Tạo giảng viên thành công', 'success')
    }

    dialogOpen.value = false
    fetchTeachers()
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Thao tác thất bại'
    const details = err?.response?.data?.data
    if (details && typeof details === 'object') {
      const lines = Object.entries(details).map(([k, v]) => `${k}: ${v}`)
      uiStore.notify(`${msg} - ${lines.join(', ')}`, 'error', 5000)
    } else {
      uiStore.notify(msg, 'error', 4000)
    }
  } finally {
    saving.value = false
  }
}

const openDeleteDialog = (teacher: Teacher) => {
  if (!isAdmin.value) return
  deleting.value = teacher
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deleting.value) return
  deletingLoading.value = true
  try {
    await teacherService.delete(deleting.value.id)
    uiStore.notify('Xóa giảng viên thành công', 'success')
    deleteOpen.value = false
    fetchTeachers()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Xóa thất bại', 'error', 4000)
  } finally {
    deletingLoading.value = false
  }
}

const onUserSearch = (q: string) => {
  if (userSearchTimeout) clearTimeout(userSearchTimeout)
  userSearchTimeout = setTimeout(async () => {
    const query = (q || '').trim()
    if (query.length < 2) {
      userOptions.value = []
      return
    }
    userLoading.value = true
    try {
      const res = await userService.getAll({ page: 1, size: 10, keyword: query })
      const list: UserSummary[] = res.data.data.data || []
      userOptions.value = list.map(u => ({ title: `${u.username} (${u.email})`, value: u.id }))
    } catch {
      userOptions.value = []
    } finally {
      userLoading.value = false
    }
  }, 350)
}

onMounted(() => {
  fetchDepartmentsForSelect()
  fetchTeachers()
})
</script>
