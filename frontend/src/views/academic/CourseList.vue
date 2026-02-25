<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Danh sách Lớp học">
        <template #actions>
          <v-btn v-if="isAdmin" color="primary" variant="flat" @click="openCreateDialog">Thêm mới</v-btn>
        </template>
      </PageHeader>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
              v-model="keyword"
              label="Tìm kiếm theo mã, tên..."
              @update:model-value="handleSearch"
          />
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
          <div v-if="activeSemesterLabel" class="text-caption mt-1">
            Học kỳ đang active: {{ activeSemesterLabel }}
          </div>
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
      <v-row>
        <v-col cols="12" md="4">
          <v-select
              v-model="teacherFilterUserId"
              :items="teacherOptions"
              item-title="title"
              item-value="value"
              label="Lọc giảng viên"
              clearable
              @update:model-value="handleSearch"
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-select
              v-model="activeFilter"
              :items="activeOptions"
              item-title="title"
              item-value="value"
              label="Lọc trạng thái"
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
          <th>Học kỳ</th>
          <th>Môn học</th>
          <th>Giảng viên</th>
          <th>Sĩ số tối đa</th>
          <th>Trạng thái</th>
          <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="c in courses" :key="c.id">
          <td>{{ c.id }}</td>
          <td>{{ c.code }}</td>
          <td>{{ c.name }}</td>
          <td>{{ c.semester?.code || '-' }}</td>
          <td>{{ c.subject?.code || '-' }}</td>
          <td>{{ c.teacher?.username || '-' }}</td>
          <td>{{ c.maxStudents }}</td>
          <td>
            <v-chip :color="c.active ? 'green' : 'red'" variant="tonal" size="small">
              {{ c.active ? 'Hoạt động' : 'Ngưng' }}
            </v-chip>
          </td>
          <td>
            <v-btn size="small" variant="text" :disabled="!isAdmin" @click="openEditDialog(c)">Sửa</v-btn>
            <v-btn size="small" color="error" variant="text" :disabled="!isAdmin" @click="openDeleteDialog(c)">Xóa</v-btn>
          </td>
        </tr>
        <tr v-if="courses.length === 0">
          <td colspan="9" class="text-center py-6">Không có dữ liệu</td>
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

  <v-dialog v-model="dialogOpen" max-width="920">
    <v-card>
      <v-card-title class="text-h6">
        {{ editingId ? 'Cập nhật lớp học' : 'Thêm lớp học' }}
      </v-card-title>
      <v-card-text>
        <v-form ref="formRef" @submit.prevent="saveCourse">
          <v-row>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.code" label="Mã lớp" :rules="rules.code" required />
            </v-col>
            <v-col cols="12" md="6">
              <v-text-field v-model="form.name" label="Tên lớp" :rules="rules.name" required />
            </v-col>
            <v-col cols="12" md="4">
              <v-select
                  v-model="form.semesterId"
                  :items="semesterOptions"
                  item-title="title"
                  item-value="value"
                  label="Học kỳ"
                  :rules="rules.semesterId"
                  density="comfortable"
                  variant="outlined"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-select
                  v-model="form.subjectId"
                  :items="subjectOptions"
                  item-title="title"
                  item-value="value"
                  label="Môn học"
                  :rules="rules.subjectId"
                  density="comfortable"
                  variant="outlined"
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-select
                  v-model="form.teacherUserId"
                  :items="teacherOptions"
                  item-title="title"
                  item-value="value"
                  label="Giảng viên (tuỳ chọn)"
                  density="comfortable"
                  variant="outlined"
                  clearable
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                  v-model.number="form.maxStudents"
                  label="Sĩ số tối đa"
                  type="number"
                  min="1"
                  :rules="rules.maxStudents"
                  required
              />
            </v-col>
            <v-col cols="12" md="8">
              <v-switch v-model="form.active" label="Kích hoạt" inset />
            </v-col>
          </v-row>
        </v-form>
      </v-card-text>
      <v-card-actions class="justify-end">
        <v-btn variant="text" @click="dialogOpen = false">Hủy</v-btn>
        <v-btn color="primary" variant="flat" :loading="saving" @click="saveCourse">Lưu</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <ConfirmDialog
      v-model="deleteOpen"
      title="Xóa lớp học"
      :text="`Bạn có chắc chắn muốn xóa lớp ${deleting?.name || ''} (${deleting?.code || ''}) không?`"
      :loading="deletingLoading"
      @confirm="confirmDelete"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useLookupsStore } from '@/stores/lookups'
import { useDebounceFn } from '@/composables/useDebounceFn'
import { unwrapPageResponse } from '@/api/response'
import { courseService, type Course } from '@/api/services/course.service'
import { teacherService } from '@/api/services/teacher.service'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const authStore = useAuthStore()
const uiStore = useUiStore()
const lookupsStore = useLookupsStore()
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

const courses = ref<Course[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const totalPages = ref(1)
const keyword = ref('')
const semesterFilterId = ref<number | null>(null)
const subjectFilterId = ref<number | null>(null)
const teacherFilterUserId = ref<number | null>(null)
const activeFilter = ref<boolean | null>(null)
const teacherOptions = ref<Array<{ title: string; value: number }>>([])
const semesterOptions = computed(() => lookupsStore.semesterOptions)
const subjectOptions = computed(() => lookupsStore.subjectOptions)
const activeSemesterId = computed(() => lookupsStore.activeSemesterId)
const activeSemesterLabel = computed(() => lookupsStore.activeSemesterLabel)

const activeOptions = [
  { title: 'Hoạt động', value: true },
  { title: 'Ngưng', value: false }
]

const dialogOpen = ref(false)
const deleteOpen = ref(false)
const saving = ref(false)
const deletingLoading = ref(false)
const editingId = ref<number | null>(null)
const deleting = ref<Course | null>(null)
const formRef = ref<any>(null)

const form = ref({
  code: '',
  name: '',
  semesterId: null as number | null,
  subjectId: null as number | null,
  teacherUserId: null as number | null,
  maxStudents: 1,
  active: true
})

const rules = {
  code: [(v: string) => !!v || 'Mã lớp là bắt buộc'],
  name: [(v: string) => !!v || 'Tên lớp là bắt buộc'],
  semesterId: [(v: number | null) => !!v || 'Học kỳ là bắt buộc'],
  subjectId: [(v: number | null) => !!v || 'Môn học là bắt buộc'],
  maxStudents: [(v: number) => v >= 1 || 'Sĩ số tối đa phải >= 1']
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await courseService.getAll({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      semesterId: semesterFilterId.value || undefined,
      subjectId: subjectFilterId.value || undefined,
      teacherId: teacherFilterUserId.value || undefined,
      active: activeFilter.value ?? undefined
    })
    const data = unwrapPageResponse<Course>(res)
    courses.value = data.data
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

const { debounced: debouncedSearch } = useDebounceFn(() => {
  page.value = 1
  fetchCourses()
}, 350)

const handleSearch = () => debouncedSearch()

const changePage = (newPage: number) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    page.value = newPage
    fetchCourses()
  }
}

const fetchSelectOptions = async () => {
  try {
    const [tea] = await Promise.all([teacherService.getAll({ page: 1, size: 200 }), lookupsStore.ensureAcademicLookups()])
    const teachers = unwrapPageResponse<any>(tea)
    teacherOptions.value = (teachers.data || []).map((t: any) => ({
      title: `${t.username} (${t.employeeCode})`,
      value: t.userId
    }))
    if (!semesterFilterId.value && activeSemesterId.value) semesterFilterId.value = activeSemesterId.value
  } catch {
    teacherOptions.value = []
  }
}

const resetForm = () => {
  form.value = {
    code: '',
    name: '',
    semesterId: activeSemesterId.value || semesterFilterId.value || null,
    subjectId: null,
    teacherUserId: null,
    maxStudents: 1,
    active: true
  }
}

const openCreateDialog = () => {
  if (!isAdmin.value) return
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

const openEditDialog = (c: Course) => {
  if (!isAdmin.value) return
  editingId.value = c.id
  form.value = {
    code: c.code,
    name: c.name,
    semesterId: c.semester?.id ?? null,
    subjectId: c.subject?.id ?? null,
    teacherUserId: c.teacher?.id ?? null,
    maxStudents: c.maxStudents,
    active: !!c.active
  }
  dialogOpen.value = true
}

const saveCourse = async () => {
  if (!isAdmin.value) return
  const res = await formRef.value?.validate?.()
  if (res && res.valid === false) return

  saving.value = true
  try {
    const payload: any = {
      code: form.value.code,
      name: form.value.name,
      semesterId: form.value.semesterId,
      subjectId: form.value.subjectId,
      maxStudents: form.value.maxStudents,
      active: !!form.value.active
    }
    if (form.value.teacherUserId) payload.teacherId = form.value.teacherUserId

    if (editingId.value) {
      await courseService.update(editingId.value, payload)
      uiStore.notify('Cập nhật lớp học thành công', 'success')
    } else {
      await courseService.create(payload)
      uiStore.notify('Tạo lớp học thành công', 'success')
    }
    dialogOpen.value = false
    fetchCourses()
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

const openDeleteDialog = (c: Course) => {
  if (!isAdmin.value) return
  deleting.value = c
  deleteOpen.value = true
}

const confirmDelete = async () => {
  if (!deleting.value) return
  deletingLoading.value = true
  try {
    await courseService.delete(deleting.value.id)
    uiStore.notify('Xóa lớp học thành công', 'success')
    deleteOpen.value = false
    fetchCourses()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Xóa thất bại', 'error', 4000)
  } finally {
    deletingLoading.value = false
  }
}

onMounted(async () => {
  await fetchSelectOptions()
  await fetchCourses()
})
</script>
