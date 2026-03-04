<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Lớp hành chính">
        <template #actions>
          <v-btn color="primary" variant="flat" :disabled="!isAdmin" @click="openCreateDialog">Thêm mới</v-btn>
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
          <td>{{ advisorTeacherLabel(c.advisorTeacherId) }}</td>
          <td>
            <v-btn size="small" color="primary" variant="flat" @click="goStudents(c.id)">Danh sách sinh viên</v-btn>
            <v-btn size="small" variant="text" :disabled="!isAdmin" @click="openEditDialog(c)">Sửa</v-btn>
            <v-btn size="small" color="error" variant="text" :disabled="!isAdmin" @click="openDeleteDialog(c)">Xóa</v-btn>
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

  <v-dialog v-model="dialogOpen" max-width="760">
    <v-card>
      <v-card-title class="text-h6">
        {{ editingId ? 'Cập nhật lớp hành chính' : 'Thêm lớp hành chính' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="saveAdminClass">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.code" label="Mã lớp" :rules="rules.code" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.name" label="Tên lớp" :rules="rules.name" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                  v-model="form.departmentId"
                  :items="departmentOptions"
                  item-title="title"
                  item-value="value"
                  label="Khoa"
                  clearable
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                  v-model="form.cohortId"
                  :items="cohortOptions"
                  item-title="title"
                  item-value="value"
                  label="Niên khóa"
                  clearable
                  no-data-text="Không có dữ liệu"
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model.number="form.intakeYear" type="number" label="Khóa (năm nhập học)" />
            </v-col>
            <v-col cols="12" md="6">
              <v-select
                  v-model="form.advisorTeacherId"
                  :items="teacherOptions"
                  item-title="title"
                  item-value="value"
                  label="Giảng viên cố vấn"
                  clearable
                  no-data-text="Không có dữ liệu"
              />
            </v-col>
            <v-col cols="12">
              <v-text-field v-model="form.program" label="Chương trình" />
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
        <v-btn variant="text" @click="dialogOpen = false">Hủy</v-btn>
        <v-btn color="primary" variant="flat" :loading="saving" @click="saveAdminClass">Lưu</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <ConfirmDialog
      v-model="deleteOpen"
      title="Xóa lớp hành chính"
      :text="`Bạn có chắc chắn muốn xóa lớp ${deleting?.name || ''} (${deleting?.code || ''}) không?`"
      :loading="deletingLoading"
      @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { unwrapApiResponse, unwrapPageResponse } from '@/api/response'
import { studentClassService, type StudentClass } from '@/api/services/studentClass.service'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useLookupsStore } from '@/stores/lookups'
import { useDebounceFn } from '@/composables/useDebounceFn'
import { cohortService, type Cohort } from '@/api/services/cohort.service'
import { teacherService, type Teacher } from '@/api/services/teacher.service'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUiStore()
const lookupsStore = useLookupsStore()
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

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
const teacherOptions = ref<{ title: string; value: number }[]>([])
const teacherLabelById = ref(new Map<number, string>())

const dialogOpen = ref(false)
const deleteOpen = ref(false)
const saving = ref(false)
const deletingLoading = ref(false)
const editingId = ref<number | null>(null)
const deleting = ref<StudentClass | null>(null)
const formRef = ref<any>(null)

const form = ref({
  code: '',
  name: '',
  departmentId: null as number | null,
  cohortId: null as number | null,
  advisorTeacherId: null as number | null,
  intakeYear: null as number | null,
  program: '',
  active: true
})

const rules = {
  code: [(v: string) => !!v || 'Mã lớp là bắt buộc'],
  name: [(v: string) => !!v || 'Tên lớp là bắt buộc']
}

const fetchAdminClasses = async () => {
  loading.value = true
  try {
    const res = await studentClassService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      departmentId: departmentId.value || undefined,
      cohortId: cohortId.value || undefined
    })
    const data = unwrapPageResponse<StudentClass>(res)
    adminClasses.value = data.data || []
    totalPages.value = data.totalPages || 1
  } catch (err: any) {
    adminClasses.value = []
    totalPages.value = 1
    uiStore.notify(err?.response?.data?.message || 'Không lấy được danh sách lớp hành chính', 'error', 4000)
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
  router.push({ name: 'AdminAdminClassStudents', params: { adminClassId } })
}

const resetForm = () => {
  form.value = {
    code: '',
    name: '',
    departmentId: null,
    cohortId: null,
    advisorTeacherId: null,
    intakeYear: null,
    program: '',
    active: true
  }
}

const openCreateDialog = () => {
  if (!isAdmin.value) return
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

const fillFormFromClass = (c: StudentClass) => ({
  code: c.code,
  name: c.name,
  departmentId: c.departmentId ?? null,
  cohortId: c.cohortId ?? null,
  advisorTeacherId: c.advisorTeacherId ?? null,
  intakeYear: c.intakeYear ?? null,
  program: c.program || '',
  active: !!c.active
})

const openEditDialog = async (c: StudentClass) => {
  if (!isAdmin.value) return
  editingId.value = c.id
  dialogOpen.value = true
  try {
    const res = await studentClassService.getById(c.id)
    const full = unwrapApiResponse<StudentClass>(res)
    form.value = fillFormFromClass(full)
  } catch {
    form.value = fillFormFromClass(c)
  }
}

const saveAdminClass = async () => {
  if (!isAdmin.value) return
  const res = await formRef.value?.validate?.()
  if (res && res.valid === false) return

  saving.value = true
  try {
    const payload = {
      code: form.value.code,
      name: form.value.name,
      departmentId: form.value.departmentId,
      cohortId: form.value.cohortId,
      advisorTeacherId: form.value.advisorTeacherId,
      intakeYear: form.value.intakeYear,
      program: form.value.program || null,
      active: !!form.value.active
    }
    if (editingId.value) {
      await studentClassService.update(editingId.value, payload)
      uiStore.notify('Cập nhật lớp hành chính thành công', 'success')
    } else {
      await studentClassService.create(payload)
      uiStore.notify('Tạo lớp hành chính thành công', 'success')
    }
    dialogOpen.value = false
    await reload()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Thao tác thất bại', 'error', 4000)
  } finally {
    saving.value = false
  }
}

const openDeleteDialog = (c: StudentClass) => {
  if (!isAdmin.value) return
  deleting.value = c
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deleting.value) return
  deletingLoading.value = true
  try {
    await studentClassService.delete(deleting.value.id)
    uiStore.notify('Xóa lớp hành chính thành công', 'success')
    deleteOpen.value = false
    await reload()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Xóa thất bại', 'error', 4000)
  } finally {
    deletingLoading.value = false
  }
}

const advisorTeacherLabel = (id?: number | null) => {
  if (!id) return '-'
  return teacherLabelById.value.get(id) || `#${id}`
}

const loadCohorts = async () => {
  try {
    const res = await cohortService.getAll({ page: 1, size: 500, active: true })
    const page = unwrapPageResponse<Cohort>(res)
    cohortOptions.value = (page.data || []).map((c) => ({
      title: `${c.code} - ${c.name}`,
      value: c.id
    }))
  } catch {
    cohortOptions.value = []
  }
}

const loadTeachers = async () => {
  try {
    const res = await teacherService.getAll({ page: 1, size: 500, active: true })
    const page = unwrapPageResponse<Teacher>(res)
    const opts = (page.data || []).map((t) => ({
      title: `${t.username}${t.email ? ` (${t.email})` : ''}`,
      value: t.id
    }))
    teacherOptions.value = opts
    teacherLabelById.value = new Map(opts.map((o) => [o.value, o.title]))
  } catch {
    teacherOptions.value = []
    teacherLabelById.value = new Map()
  }
}

onMounted(async () => {
  try {
    await Promise.all([lookupsStore.loadDepartments(), loadCohorts(), loadTeachers()])
  } catch {}
  await reload()
})
</script>
