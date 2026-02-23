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
import { courseService, type Course } from '@/api/services/course.service'
import { semesterService } from '@/api/services/semester.service'
import { subjectService } from '@/api/services/subject.service'
import { teacherService } from '@/api/services/teacher.service'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const authStore = useAuthStore()
const uiStore = useUiStore()
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
let searchTimeout: any = null

const activeSemesterId = ref<number | null>(null)
const activeSemesterLabel = ref('')

const semesterOptions = ref<Array<{ title: string; value: number }>>([])
const subjectOptions = ref<Array<{ title: string; value: number }>>([])
const teacherOptions = ref<Array<{ title: string; value: number }>>([])

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
  code: [(v: string) => !!v || 'Code is required'],
  name: [(v: string) => !!v || 'Name is required'],
  semesterId: [(v: number | null) => !!v || 'Semester is required'],
  subjectId: [(v: number | null) => !!v || 'Subject is required'],
  maxStudents: [(v: number) => v >= 1 || 'Max students must be >= 1']
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
    const data = res.data.data
    courses.value = data.data
    totalPages.value = data.totalPages
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    page.value = 1
    fetchCourses()
  }, 350)
}

const changePage = (newPage: number) => {
  if (newPage > 0 && newPage <= totalPages.value) {
    page.value = newPage
    fetchCourses()
  }
}

const fetchSelectOptions = async () => {
  try {
    const [sem, sub, tea, activeSem] = await Promise.all([
      semesterService.getAll({ page: 1, size: 200 }),
      subjectService.getAll({ page: 1, size: 200 }),
      teacherService.getAll({ page: 1, size: 200 }),
      semesterService.getActive()
    ])
    semesterOptions.value = (sem.data.data.data || []).map((s: any) => ({ title: `${s.code} - ${s.name}`, value: s.id }))
    subjectOptions.value = (sub.data.data.data || []).map((s: any) => ({ title: `${s.code} - ${s.name}`, value: s.id }))
    teacherOptions.value = (tea.data.data.data || []).map((t: any) => ({
      title: `${t.username} (${t.employeeCode})`,
      value: t.userId
    }))

    const active = activeSem?.data?.data
    activeSemesterId.value = active?.id ?? null
    activeSemesterLabel.value = active ? `${active.code} - ${active.name}` : ''
    if (!semesterFilterId.value && activeSemesterId.value) {
      semesterFilterId.value = activeSemesterId.value
    }
  } catch {
    semesterOptions.value = []
    subjectOptions.value = []
    teacherOptions.value = []
    activeSemesterId.value = null
    activeSemesterLabel.value = ''
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
