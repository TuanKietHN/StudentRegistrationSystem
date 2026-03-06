<template>
  <div>
    <PageHeader
      title="Quản lý Học kỳ"
      subtitle="Danh sách học kỳ và trạng thái hoạt động."
    >
      <template #actions>
        <v-btn
          v-if="isAdmin"
          color="primary"
          variant="flat"
          prepend-icon="mdi-plus"
          @click="openCreateDialog"
        >
          Thêm học kỳ
        </v-btn>
      </template>
    </PageHeader>

    <v-card class="admin-panel mb-6" variant="flat">
      <v-card-text class="pa-4">
        <v-row>
          <v-col cols="12" md="7">
            <v-text-field
              v-model="keyword"
              class="admin-input"
              placeholder="Tìm kiếm mã, tên học kỳ..."
              prepend-inner-icon="mdi-magnify"
              variant="solo-filled"
              bg-color="#F7F6F8"
              hide-details
              @update:model-value="handleSearch"
            />
          </v-col>
          <v-col cols="12" md="5">
            <v-select
              v-model="activeFilter"
              class="admin-input"
              :items="activeOptions"
              item-title="title"
              item-value="value"
              placeholder="Tất cả trạng thái"
              prepend-inner-icon="mdi-filter-variant"
              variant="solo-filled"
              bg-color="#F7F6F8"
              hide-details
              clearable
              @update:model-value="handleSearch"
            />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-card class="admin-panel" variant="flat">
      <v-progress-linear v-if="loading" indeterminate />
      <v-card-text class="pa-0">
        <v-table class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Mã</th>
              <th>Tên</th>
              <th>Bắt đầu</th>
              <th>Kết thúc</th>
              <th>HK chính</th>
              <th>HK phụ</th>
              <th class="text-right">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in semesters" :key="s.id">
              <td>{{ s.id }}</td>
              <td>{{ s.code }}</td>
              <td>{{ s.name }}</td>
              <td>{{ s.startDate }}</td>
              <td>{{ s.endDate }}</td>
              <td>
                <v-chip :color="s.active ? 'green' : 'red'" variant="tonal" size="small">
                  {{ s.active ? 'Hoạt động' : 'Ngưng' }}
                </v-chip>
              </td>
              <td>
                <v-chip :color="s.secondaryActive ? 'green' : 'red'" variant="tonal" size="small">
                  {{ s.secondaryActive ? 'Hoạt động' : 'Ngưng' }}
                </v-chip>
              </td>
              <td class="text-right">
                <v-btn size="small" variant="text" :disabled="!isAdmin" @click="openEditDialog(s)">Sửa</v-btn>
                <v-btn
                  size="small"
                  color="error"
                  variant="text"
                  :disabled="!isAdmin"
                  @click="openDeleteDialog(s)"
                >
                  Xóa
                </v-btn>
              </td>
            </tr>
            <tr v-if="semesters.length === 0">
              <td colspan="8" class="text-center py-6">Không có dữ liệu</td>
            </tr>
          </tbody>
        </v-table>

        <v-divider />
        <div class="d-flex align-center justify-space-between pa-4">
          <div class="text-caption" style="color: #64748b">Trang {{ page }} / {{ totalPages }}</div>
          <v-pagination
            :model-value="page"
            :length="totalPages"
            density="comfortable"
            rounded="lg"
            @update:model-value="changePage"
          />
        </div>
      </v-card-text>
    </v-card>
  </div>

  <v-dialog v-model="dialogOpen" max-width="760">
    <v-card>
      <v-card-title class="text-h6">
        {{ editingId ? 'Cập nhật học kỳ' : 'Thêm học kỳ' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="saveSemester">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.code" label="Mã học kỳ" :rules="rules.code" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.name" label="Tên học kỳ" :rules="rules.name" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                  v-model="form.startDate"
                  label="Ngày bắt đầu"
                  type="date"
                  :rules="rules.startDate"
                  required
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field
                  v-model="form.endDate"
                  label="Ngày kết thúc"
                  type="date"
                  :rules="rules.endDate"
                  required
              />
            </v-col>
            <v-col cols="12">
              <v-switch
                v-model="form.active"
                :label="form.active ? 'Học kỳ chính: Hoạt động' : 'Học kỳ chính: Ngưng'"
                inset
                density="comfortable"
                color="success"
                hide-details
              />
            </v-col>
            <v-col cols="12">
              <v-switch
                v-model="form.secondaryActive"
                :label="form.secondaryActive ? 'Học kỳ phụ: Hoạt động' : 'Học kỳ phụ: Ngưng'"
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
        <v-btn variant="text" @click="dialogOpen = false">Hủy</v-btn>
        <v-btn color="primary" variant="flat" :loading="saving" @click="saveSemester">Lưu</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <ConfirmDialog
      v-model="deleteOpen"
      title="Xóa học kỳ"
      :text="`Bạn có chắc chắn muốn xóa học kỳ ${deleting?.name || ''} (${deleting?.code || ''}) không?`"
      :loading="deletingLoading"
      @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { semesterService, type Semester } from '@/api/services/semester.service'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const authStore = useAuthStore()
const uiStore = useUiStore()
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

const semesters = ref<Semester[]>([])
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
const deleting = ref<Semester | null>(null)
const formRef = ref<any>(null)

const form = ref({
  code: '',
  name: '',
  startDate: '',
  endDate: '',
  active: false,
  secondaryActive: false
})

const rules = {
  code: [(v: string) => !!v || 'Code is required'],
  name: [(v: string) => !!v || 'Name is required'],
  startDate: [(v: string) => !!v || 'Start date is required'],
  endDate: [(v: string) => !!v || 'End date is required']
}

const fetchSemesters = async () => {
  loading.value = true
  try {
    const res = await semesterService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      active: activeFilter.value ?? undefined
    })
    const data = res.data.data
    semesters.value = data.data
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    page.value = 1
    fetchSemesters()
  }, 350)
}

const changePage = (newPage: number) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    page.value = newPage
    fetchSemesters()
  }
}

const resetForm = () => {
  form.value = {
    code: '',
    name: '',
    startDate: '',
    endDate: '',
    active: false,
    secondaryActive: false
  }
}

const openCreateDialog = () => {
  if (!isAdmin.value) return
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

const openEditDialog = (s: Semester) => {
  if (!isAdmin.value) return
  editingId.value = s.id
  form.value = {
    code: s.code,
    name: s.name,
    startDate: s.startDate,
    endDate: s.endDate,
    active: !!s.active,
    secondaryActive: !!s.secondaryActive
  }
  dialogOpen.value = true
}

const saveSemester = async () => {
  if (!isAdmin.value) return
  const res = await formRef.value?.validate?.()
  if (res && res.valid === false) return

  saving.value = true
  try {
    const payload = {
      code: form.value.code,
      name: form.value.name,
      startDate: form.value.startDate,
      endDate: form.value.endDate,
      active: !!form.value.active,
      secondaryActive: !!form.value.secondaryActive
    }
    if (editingId.value) {
      await semesterService.update(editingId.value, payload)
      uiStore.notify('Cập nhật học kỳ thành công', 'success')
    } else {
      await semesterService.create(payload)
      uiStore.notify('Tạo học kỳ thành công', 'success')
    }
    dialogOpen.value = false
    fetchSemesters()
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

const openDeleteDialog = (s: Semester) => {
  if (!isAdmin.value) return
  deleting.value = s
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deleting.value) return
  deletingLoading.value = true
  try {
    await semesterService.delete(deleting.value.id)
    uiStore.notify('Xóa học kỳ thành công', 'success')
    deleteOpen.value = false
    fetchSemesters()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Xóa thất bại', 'error', 4000)
  } finally {
    deletingLoading.value = false
  }
}

onMounted(fetchSemesters)
</script>
