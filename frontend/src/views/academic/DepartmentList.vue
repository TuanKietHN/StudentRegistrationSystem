<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Danh sách Khoa">
        <template #actions>
          <v-btn v-if="isAdmin" color="primary" variant="flat" @click="openCreateDialog">Thêm mới</v-btn>
        </template>
      </PageHeader>
      <!-- Search Section -->
      <div class="search-section">
        <v-text-field
            v-model="keyword"
            placeholder="Tìm kiếm theo mã, tên..."
            variant="outlined"
            prepend-inner-icon="mdi-magnify"
            density="comfortable"
            @update:model-value="handleSearch"
            class="search-field"
        />
      </div>

      <v-progress-linear v-if="loading" indeterminate class="mb-4 rounded-pill" />

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
            <v-btn size="small" variant="text" :disabled="!isAdmin" @click="openEditDialog(dept)">Sửa</v-btn>
            <v-btn size="small" color="error" variant="text" :disabled="!isAdmin" @click="openDeleteDialog(dept)">Xóa</v-btn>
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

  <v-dialog v-model="dialogOpen" max-width="680">
    <v-card>
      <v-card-title class="text-h6">
        {{ editingId ? 'Cập nhật khoa' : 'Thêm khoa' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="saveDepartment">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.code" label="Mã khoa" :rules="rules.code" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.name" label="Tên khoa" :rules="rules.name" required />
            </v-col>
            <v-col cols="12">
              <v-textarea v-model="form.description" label="Mô tả" rows="3" auto-grow />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                  v-model.number="form.parentId"
                  label="Parent ID (tuỳ chọn)"
                  type="number"
                  min="1"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                  v-model.number="form.headTeacherId"
                  label="Head Teacher ID (tuỳ chọn)"
                  type="number"
                  min="1"
              />
            </v-col>
            <v-col cols="12">
              <v-switch
                v-model="form.active"
                :label="form.active ? 'Trạng thái: Hoạt động' : 'Trạng thái: Ngưng'"
                inset
                density="comfortable"
                color="success"
                hide-details
              />
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions class="justify-end">
        <v-btn variant="text" @click="closeDialog">Hủy</v-btn>
        <v-btn color="primary" variant="flat" :loading="saving" @click="saveDepartment">
          Lưu
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <ConfirmDialog
      v-model="deleteOpen"
      title="Xóa khoa"
      :text="`Bạn có chắc chắn muốn xóa khoa ${deleting?.name || ''} (${deleting?.code || ''}) không?`"
      :loading="deletingLoading"
      @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { departmentService, type Department } from '@/api/services/department.service'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const authStore = useAuthStore()
const uiStore = useUiStore()
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

const departments = ref<Department[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
let searchTimeout: any = null

const dialogOpen = ref(false)
const deleteOpen = ref(false)
const saving = ref(false)
const deletingLoading = ref(false)
const editingId = ref<number | null>(null)
const deleting = ref<Department | null>(null)
const formRef = ref<any>(null)

const form = ref({
  code: '',
  name: '',
  description: '',
  parentId: null as number | null,
  headTeacherId: null as number | null,
  active: true
})

const rules = {
  code: [(v: string) => !!v || 'Mã khoa không được để trống'],
  name: [(v: string) => !!v || 'Tên khoa không được để trống']
}

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

const resetForm = () => {
  form.value = {
    code: '',
    name: '',
    description: '',
    parentId: null,
    headTeacherId: null,
    active: true
  }
}

const openCreateDialog = () => {
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

const openEditDialog = (dept: Department) => {
  editingId.value = dept.id
  form.value = {
    code: dept.code,
    name: dept.name,
    description: dept.description || '',
    parentId: dept.parentId ?? null,
    headTeacherId: dept.headTeacherId ?? null,
    active: !!dept.active
  }
  dialogOpen.value = true
}

const closeDialog = () => {
  dialogOpen.value = false
}

const saveDepartment = async () => {
  if (!isAdmin.value) return
  const res = await formRef.value?.validate?.()
  if (res && res.valid === false) return

  saving.value = true
  try {
    const payload = {
      code: form.value.code,
      name: form.value.name,
      description: form.value.description || null,
      parentId: form.value.parentId || null,
      headTeacherId: form.value.headTeacherId || null,
      active: !!form.value.active
    }

    if (editingId.value) {
      await departmentService.update(editingId.value, payload)
      uiStore.notify('Cập nhật khoa thành công', 'success')
    } else {
      await departmentService.create(payload)
      uiStore.notify('Tạo khoa thành công', 'success')
    }
    dialogOpen.value = false
    fetchDepartments()
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

const openDeleteDialog = (dept: Department) => {
  if (!isAdmin.value) return
  deleting.value = dept
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deleting.value) return
  deletingLoading.value = true
  try {
    await departmentService.delete(deleting.value.id)
    uiStore.notify('Xóa khoa thành công', 'success')
    deleteOpen.value = false
    fetchDepartments()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Xóa thất bại', 'error', 4000)
  } finally {
    deletingLoading.value = false
  }
}

onMounted(fetchDepartments)
</script>

<style scoped>
.search-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  margin-bottom: 20px;
}

.search-field {
  width: 100%;
}

.search-field :deep(.v-field) {
  border-radius: 12px;
}

:deep(.v-table) {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

:deep(.v-table thead tr) {
  background: linear-gradient(to right, #f9fafb, #f3f4f6);
  border-bottom: 2px solid #e5e7eb;
}

:deep(.v-table thead th) {
  font-weight: 700;
  color: #374151;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px 12px;
}

:deep(.v-table tbody tr) {
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.2s ease;
}

:deep(.v-table tbody tr:hover) {
  background-color: #f9fafb;
}

:deep(.v-table tbody td) {
  padding: 16px 12px;
  color: #1f2937;
  font-size: 14px;
}

:deep(.v-chip) {
  font-weight: 500;
  letter-spacing: 0.2px;
}

:deep(.v-progress-linear) {
  border-radius: 12px;
}
</style>
