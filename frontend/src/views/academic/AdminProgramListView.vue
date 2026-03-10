<template>
  <v-container fluid>
    <PageHeader 
      title="Chương trình đào tạo" 
      subtitle="Quản lý danh sách chương trình đào tạo và môn học"
    >
      <template #actions>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreateDialog">
          Tạo mới
        </v-btn>
      </template>
    </PageHeader>

    <v-card class="mt-4">
      <v-data-table
        :headers="headers"
        :items="programs"
        :loading="loading"
        class="elevation-1"
      >
        <template v-slot:item.active="{ item }">
          <v-chip
            :color="item.active ? 'success' : 'grey'"
            size="small"
            variant="tonal"
          >
            {{ item.active ? 'Hoạt động' : 'Vô hiệu' }}
          </v-chip>
        </template>
        
        <template v-slot:item.department="{ item }">
          {{ item.department?.name }}
        </template>

        <template v-slot:item.actions="{ item }">
          <v-btn icon="mdi-pencil" size="small" variant="text" color="primary" @click="editProgram(item)"></v-btn>
          <v-btn icon="mdi-book-open-variant" size="small" variant="text" color="info" @click="manageSubjects(item)"></v-btn>
          <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="confirmDelete(item)"></v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- Create/Edit Dialog -->
    <v-dialog v-model="dialog" max-width="600px">
      <v-card>
        <v-card-title>
          {{ editedIndex === -1 ? 'Tạo chương trình mới' : 'Cập nhật chương trình' }}
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedItem.code"
                  label="Mã chương trình"
                  :readonly="editedIndex !== -1"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedItem.totalCredits"
                  label="Tổng tín chỉ"
                  type="number"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="editedItem.name"
                  label="Tên chương trình"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-select
                  v-model="editedItem.departmentId"
                  :items="departments"
                  item-title="name"
                  item-value="id"
                  label="Khoa"
                  required
                ></v-select>
              </v-col>
              <v-col cols="12">
                <v-textarea
                  v-model="editedItem.description"
                  label="Mô tả"
                  rows="3"
                ></v-textarea>
              </v-col>
              <v-col cols="12" v-if="editedIndex !== -1">
                <v-switch
                  v-model="editedItem.active"
                  color="primary"
                  label="Trạng thái hoạt động"
                ></v-switch>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="closeDialog">Hủy</v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="saveProgram">Lưu</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Manage Subjects Dialog -->
    <v-dialog v-model="subjectDialog" fullscreen transition="dialog-bottom-transition">
      <v-card>
        <v-toolbar color="primary">
          <v-btn icon="mdi-close" @click="subjectDialog = false"></v-btn>
          <v-toolbar-title>Quản lý môn học: {{ selectedProgram?.name }}</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-toolbar-items>
            <v-btn variant="text" prepend-icon="mdi-plus" @click="openAddSubjectDialog">Thêm môn học</v-btn>
          </v-toolbar-items>
        </v-toolbar>
        
        <v-card-text>
          <v-container>
            <v-data-table
              :headers="subjectHeaders"
              :items="programSubjects"
              :loading="subjectsLoading"
              class="elevation-1"
            >
              <template v-slot:item.subjectType="{ item }">
                <v-chip :color="item.subjectType === 'COMPULSORY' ? 'error' : 'info'" size="small">
                  {{ item.subjectType === 'COMPULSORY' ? 'Bắt buộc' : 'Tự chọn' }}
                </v-chip>
              </template>
              <template v-slot:item.subject.credits="{ item }">
                {{ item.subject.credit }}
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="removeSubject(item)"></v-btn>
              </template>
            </v-data-table>
          </v-container>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Add Subject Dialog -->
    <v-dialog v-model="addSubjectDialog" max-width="500px">
      <v-card>
        <v-card-title>Thêm môn học vào chương trình</v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-autocomplete
                  v-model="newSubject.subjectId"
                  :items="availableSubjects"
                  item-title="name"
                  item-value="id"
                  label="Chọn môn học"
                  required
                >
                  <template v-slot:item="{ props, item }">
                    <v-list-item v-bind="props" :subtitle="item.raw.code"></v-list-item>
                  </template>
                </v-autocomplete>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model.number="newSubject.semester"
                  label="Học kỳ dự kiến"
                  type="number"
                  min="1"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model.number="newSubject.passScore"
                  label="Điểm đạt (thang 10)"
                  type="number"
                  step="0.1"
                  min="0"
                  max="10"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-select
                  v-model="newSubject.subjectType"
                  :items="[{title: 'Bắt buộc', value: 'COMPULSORY'}, {title: 'Tự chọn', value: 'ELECTIVE'}]"
                  label="Loại môn"
                  required
                ></v-select>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="addSubjectDialog = false">Hủy</v-btn>
          <v-btn color="primary" variant="text" @click="saveSubject">Thêm</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <ConfirmDialog ref="confirm" />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import academicProgramService, { type AcademicProgramResponse, type ProgramSubjectResponse } from '@/api/services/academicProgram.service'
import { departmentService } from '@/api/services/department.service'
import { subjectService } from '@/api/services/subject.service'
import { useUiStore } from '@/stores/ui'
import { unwrapApiResponse } from '@/api/response'

const uiStore = useUiStore()
const confirm = ref()

const loading = ref(false)
const programs = ref<AcademicProgramResponse[]>([])
const departments = ref<any[]>([])
const availableSubjects = ref<any[]>([])

// Dialog states
const dialog = ref(false)
const subjectDialog = ref(false)
const addSubjectDialog = ref(false)
const subjectsLoading = ref(false)

const editedIndex = ref(-1)
const selectedProgram = ref<AcademicProgramResponse | null>(null)
const programSubjects = ref<ProgramSubjectResponse[]>([])

const editedItem = reactive({
  id: -1,
  code: '',
  name: '',
  departmentId: null as number | null,
  totalCredits: 0,
  description: '',
  active: true
})

const newSubject = reactive({
  subjectId: null as number | null,
  semester: 1,
  passScore: 4.0,
  subjectType: 'COMPULSORY'
})

const headers = [
  { title: 'Mã CT', key: 'code' },
  { title: 'Tên chương trình', key: 'name' },
  { title: 'Khoa', key: 'department' },
  { title: 'Tổng tín chỉ', key: 'totalCredits' },
  { title: 'Trạng thái', key: 'active' },
  { title: 'Hành động', key: 'actions', sortable: false, align: 'end' },
]

const subjectHeaders = [
  { title: 'Học kỳ', key: 'semester' },
  { title: 'Mã HP', key: 'subject.code' },
  { title: 'Tên học phần', key: 'subject.name' },
  { title: 'Tín chỉ', key: 'subject.credits' },
  { title: 'Loại', key: 'subjectType' },
  { title: 'Điểm đạt', key: 'passScore' },
  { title: 'Hành động', key: 'actions', sortable: false, align: 'end' },
]

const loadData = async () => {
  loading.value = true
  try {
    const [progRes, deptRes] = await Promise.all([
      academicProgramService.getAll(),
      departmentService.getAll()
    ])
    programs.value = unwrapApiResponse(progRes) || []
    departments.value = unwrapApiResponse(deptRes) || []
  } catch (error) {
    uiStore.notify('Lỗi tải dữ liệu', 'error')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  editedIndex.value = -1
  Object.assign(editedItem, {
    id: -1,
    code: '',
    name: '',
    departmentId: null,
    totalCredits: 0,
    description: '',
    active: true
  })
  dialog.value = true
}

const editProgram = (item: AcademicProgramResponse) => {
  editedIndex.value = programs.value.indexOf(item)
  Object.assign(editedItem, {
    id: item.id,
    code: item.code,
    name: item.name,
    departmentId: item.department?.id,
    totalCredits: item.totalCredits,
    description: item.description,
    active: item.active
  })
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
}

const saveProgram = async () => {
  try {
    if (editedIndex.value > -1) {
      await academicProgramService.update(editedItem.id, {
        name: editedItem.name,
        departmentId: editedItem.departmentId!,
        totalCredits: Number(editedItem.totalCredits),
        description: editedItem.description,
        active: editedItem.active
      })
      uiStore.notify('Cập nhật thành công', 'success')
    } else {
      await academicProgramService.create({
        code: editedItem.code,
        name: editedItem.name,
        departmentId: editedItem.departmentId!,
        totalCredits: Number(editedItem.totalCredits),
        description: editedItem.description
      })
      uiStore.notify('Tạo mới thành công', 'success')
    }
    closeDialog()
    loadData()
  } catch (error: any) {
    uiStore.notify(error.response?.data?.message || 'Có lỗi xảy ra', 'error')
  }
}

const confirmDelete = async (item: AcademicProgramResponse) => {
  if (await confirm.value.open('Xác nhận xóa', `Bạn có chắc muốn xóa chương trình "${item.name}"?`)) {
    try {
      await academicProgramService.delete(item.id)
      uiStore.notify('Xóa thành công', 'success')
      loadData()
    } catch (error: any) {
      uiStore.notify(error.response?.data?.message || 'Không thể xóa chương trình', 'error')
    }
  }
}

// Subject Management
const manageSubjects = async (item: AcademicProgramResponse) => {
  selectedProgram.value = item
  subjectDialog.value = true
  await loadProgramSubjects(item.id)
}

const loadProgramSubjects = async (programId: number) => {
  subjectsLoading.value = true
  try {
    const res = await academicProgramService.getSubjects(programId)
    programSubjects.value = unwrapApiResponse(res) || []
  } catch (error) {
    uiStore.notify('Lỗi tải danh sách môn học', 'error')
  } finally {
    subjectsLoading.value = false
  }
}

const openAddSubjectDialog = async () => {
  try {
    // Load all subjects for selection
    // Note: Ideally should use a search/autocomplete API for better performance
    const res = await subjectService.search({ page: 1, size: 1000, active: true })
    availableSubjects.value = unwrapApiResponse(res)?.content || []
    
    // Reset form
    Object.assign(newSubject, {
      subjectId: null,
      semester: 1,
      passScore: 4.0,
      subjectType: 'COMPULSORY'
    })
    addSubjectDialog.value = true
  } catch (error) {
    uiStore.notify('Lỗi tải danh sách môn học', 'error')
  }
}

const saveSubject = async () => {
  if (!selectedProgram.value || !newSubject.subjectId) return
  
  try {
    await academicProgramService.addSubject(selectedProgram.value.id, {
      subjectId: newSubject.subjectId,
      semester: Number(newSubject.semester),
      subjectType: newSubject.subjectType,
      passScore: Number(newSubject.passScore)
    })
    uiStore.notify('Thêm môn học thành công', 'success')
    addSubjectDialog.value = false
    loadProgramSubjects(selectedProgram.value.id)
  } catch (error: any) {
    uiStore.notify(error.response?.data?.message || 'Có lỗi xảy ra', 'error')
  }
}

const removeSubject = async (item: ProgramSubjectResponse) => {
  if (await confirm.value.open('Xác nhận xóa', `Bạn có chắc muốn xóa môn "${item.subject.name}" khỏi chương trình?`)) {
    try {
      await academicProgramService.removeSubject(item.id)
      uiStore.notify('Xóa môn học thành công', 'success')
      if (selectedProgram.value) {
        loadProgramSubjects(selectedProgram.value.id)
      }
    } catch (error: any) {
      uiStore.notify('Lỗi khi xóa môn học', 'error')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>
