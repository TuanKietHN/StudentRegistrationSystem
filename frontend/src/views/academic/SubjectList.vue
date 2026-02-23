<template>
  <v-card>
    <v-card-title class="d-flex align-center justify-space-between">
      <div class="text-h6">Danh sách Môn học</div>
      <v-btn v-if="isAdmin" color="primary" variant="flat" @click="openCreateDialog">Thêm mới</v-btn>
    </v-card-title>
    <v-card-text>
      <v-row>
        <v-col cols="12" md="6">
          <v-text-field
            v-model="keyword"
            label="Tìm kiếm theo mã, tên..."
            density="comfortable"
            variant="outlined"
            @update:model-value="handleSearch"
          />
        </v-col>
        <v-col cols="12" md="6">
          <v-select
            v-model="activeFilter"
            :items="activeOptions"
            item-title="title"
            item-value="value"
            label="Lọc trạng thái"
            density="comfortable"
            variant="outlined"
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
            <th>Mã</th>
            <th>Tên</th>
            <th>Số tín chỉ</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in subjects" :key="s.id">
            <td>{{ s.id }}</td>
            <td>{{ s.code }}</td>
            <td>{{ s.name }}</td>
            <td>{{ s.credit }}</td>
            <td>
              <v-chip :color="s.active ? 'green' : 'red'" variant="tonal" size="small">
                {{ s.active ? 'Hoạt động' : 'Ngưng' }}
              </v-chip>
            </td>
            <td>
              <v-btn size="small" variant="text" :disabled="!isAdmin" @click="openEditDialog(s)">Sửa</v-btn>
              <v-btn size="small" color="error" variant="text" :disabled="!isAdmin" @click="openDeleteDialog(s)">Xóa</v-btn>
            </td>
          </tr>
          <tr v-if="subjects.length === 0">
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

  <v-dialog v-model="dialogOpen" max-width="760">
    <v-card>
      <v-card-title class="text-h6">
        {{ editingId ? 'Cập nhật môn học' : 'Thêm môn học' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="saveSubject">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.code" label="Mã môn học" :rules="rules.code" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.name" label="Tên môn học" :rules="rules.name" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                v-model.number="form.credit"
                label="Số tín chỉ"
                type="number"
                min="0"
                :rules="rules.credit"
                required
              />
            </v-col>
            <v-col cols="12">
              <v-textarea v-model="form.description" label="Mô tả" rows="3" auto-grow />
            </v-col>
            <v-col cols="12">
              <v-switch v-model="form.active" label="Kích hoạt" inset />
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions class="justify-end">
        <v-btn variant="text" @click="dialogOpen = false">Hủy</v-btn>
        <v-btn color="primary" variant="flat" :loading="saving" @click="saveSubject">Lưu</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="deleteOpen" max-width="520">
    <v-card>
      <v-card-title class="text-h6">Xóa môn học</v-card-title>
      <v-card-text>
        Bạn có chắc chắn muốn xóa môn học
        <b>{{ deleting?.name }}</b>
        ({{ deleting?.code }}) không?
      </v-card-text>
      <v-card-actions class="justify-end">
        <v-btn variant="text" @click="deleteOpen = false">Hủy</v-btn>
        <v-btn color="error" variant="flat" :loading="deletingLoading" @click="confirmDelete">Xóa</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { subjectService, type Subject } from '@/api/services/subject.service'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'

const authStore = useAuthStore()
const uiStore = useUiStore()
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

const subjects = ref<Subject[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const activeFilter = ref<boolean | null>(null)
let searchTimeout: any = null

const activeOptions = [
  { title: 'Hoạt động', value: true },
  { title: 'Ngưng', value: false }
]

const dialogOpen = ref(false)
const deleteOpen = ref(false)
const saving = ref(false)
const deletingLoading = ref(false)
const editingId = ref<number | null>(null)
const deleting = ref<Subject | null>(null)
const formRef = ref<any>(null)

const form = ref({
  code: '',
  name: '',
  credit: 0,
  description: '',
  active: true
})

const rules = {
  code: [(v: string) => !!v || 'Code is required'],
  name: [(v: string) => !!v || 'Name is required'],
  credit: [(v: number) => v >= 0 || 'Credit must be >= 0']
}

const fetchSubjects = async () => {
  loading.value = true
  try {
    const res = await subjectService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      active: activeFilter.value ?? undefined
    })
    const data = res.data.data
    subjects.value = data.data
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    page.value = 1
    fetchSubjects()
  }, 350)
}

const changePage = (newPage: number) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    page.value = newPage
    fetchSubjects()
  }
}

const resetForm = () => {
  form.value = {
    code: '',
    name: '',
    credit: 0,
    description: '',
    active: true
  }
}

const openCreateDialog = () => {
  if (!isAdmin.value) return
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

const openEditDialog = (s: Subject) => {
  if (!isAdmin.value) return
  editingId.value = s.id
  form.value = {
    code: s.code,
    name: s.name,
    credit: s.credit,
    description: s.description || '',
    active: !!s.active
  }
  dialogOpen.value = true
}

const saveSubject = async () => {
  if (!isAdmin.value) return
  const res = await formRef.value?.validate?.()
  if (res && res.valid === false) return

  saving.value = true
  try {
    const payload = {
      code: form.value.code,
      name: form.value.name,
      credit: form.value.credit,
      description: form.value.description || null,
      active: !!form.value.active
    }
    if (editingId.value) {
      await subjectService.update(editingId.value, payload)
      uiStore.notify('Cập nhật môn học thành công', 'success')
    } else {
      await subjectService.create(payload)
      uiStore.notify('Tạo môn học thành công', 'success')
    }
    dialogOpen.value = false
    fetchSubjects()
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

const openDeleteDialog = (s: Subject) => {
  if (!isAdmin.value) return
  deleting.value = s
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deleting.value) return
  deletingLoading.value = true
  try {
    await subjectService.delete(deleting.value.id)
    uiStore.notify('Xóa môn học thành công', 'success')
    deleteOpen.value = false
    fetchSubjects()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Xóa thất bại', 'error', 4000)
  } finally {
    deletingLoading.value = false
  }
}

onMounted(fetchSubjects)
</script>

