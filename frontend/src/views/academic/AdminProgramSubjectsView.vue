<template>
  <v-container fluid>
    <PageHeader 
      :title="pageTitle" 
      :subtitle="pageSubtitle"
      back-to="/admin/academic-programs"
    >
      <template #actions>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openAddSubjectDialog">
          Thêm môn học
        </v-btn>
      </template>
    </PageHeader>

    <v-row class="mt-4">
      <v-col cols="12" md="4">
        <v-card class="elevation-2 rounded-lg">
          <v-card-item title="Thông tin chương trình">
            <template #prepend>
              <v-icon color="primary" icon="mdi-information-outline"></v-icon>
            </template>
          </v-card-item>
          <v-divider></v-divider>
          <v-list density="compact">
            <v-list-item title="Mã chương trình" :subtitle="program?.code"></v-list-item>
            <v-list-item title="Tên chương trình" :subtitle="program?.name"></v-list-item>
            <v-list-item title="Khoa quản lý" :subtitle="program?.department?.name"></v-list-item>
            <v-list-item title="Tổng tín chỉ yêu cầu" :subtitle="`${program?.totalCredits} tín chỉ`"></v-list-item>
            <v-list-item title="Trạng thái">
              <template #subtitle>
                <v-chip
                  :color="program?.active ? 'success' : 'grey'"
                  size="x-small"
                  variant="flat"
                >
                  {{ program?.active ? 'Đang áp dụng' : 'Ngưng áp dụng' }}
                </v-chip>
              </template>
            </v-list-item>
          </v-list>
        </v-card>
      </v-col>

      <v-col cols="12" md="8">
        <v-card class="elevation-2 rounded-lg scroll-card">
          <v-data-table
            :headers="headers"
            :items="programSubjects"
            :loading="loading"
            class="fill-height"
            hover
          >
            <template v-slot:item.subjectType="{ item }">
              <v-chip 
                :color="item.subjectType === 'COMPULSORY' ? 'error' : 'info'" 
                size="small"
                variant="tonal"
              >
                {{ item.subjectType === 'COMPULSORY' ? 'Bắt buộc' : 'Tự chọn' }}
              </v-chip>
            </template>
            
            <template v-slot:item.semester="{ item }">
              <v-avatar color="primary" size="24" class="text-caption text-white">
                {{ item.semester }}
              </v-avatar>
            </template>

            <template v-slot:item.actions="{ item }">
              <v-btn 
                icon="mdi-delete-outline" 
                size="small" 
                variant="text" 
                color="error" 
                @click="confirmRemove(item)"
                v-tooltip="'Xóa khỏi chương trình'"
              ></v-btn>
            </template>

            <template v-slot:no-data>
              <div class="pa-10 text-center">
                <v-icon size="64" color="grey-lighten-1" icon="mdi-book-off-outline" class="mb-4"></v-icon>
                <div class="text-h6 text-grey-darken-1">Chưa có môn học nào</div>
                <div class="text-body-2 text-grey">Bắt đầu bằng cách thêm môn học vào chương trình này</div>
              </div>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>

    <!-- Add Subject Dialog -->
    <v-dialog v-model="addSubjectDialog" max-width="500px" persistent>
      <v-card class="rounded-lg">
        <v-card-title class="pa-4 bg-primary text-white">
          <v-icon icon="mdi-library-plus" class="mr-2"></v-icon>
          Thêm môn học vào chương trình
        </v-card-title>
        <v-card-text class="pa-6">
          <v-form ref="form" v-model="formValid">
            <v-row>
              <v-col cols="12">
                <v-autocomplete
                  v-model="newSubject.subjectId"
                  :items="availableSubjects"
                  item-title="name"
                  item-value="id"
                  label="Tìm kiếm môn học"
                  placeholder="Nhập tên hoặc mã môn học"
                  variant="outlined"
                  :rules="[v => !!v || 'Bắt buộc chọn môn học']"
                  required
                >
                  <template v-slot:item="{ props, item }">
                    <v-list-item v-bind="props" :subtitle="item.raw.code">
                      <template #prepend>
                        <v-icon icon="mdi-book-outline" size="small"></v-icon>
                      </template>
                    </v-list-item>
                  </template>
                </v-autocomplete>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model.number="newSubject.semester"
                  label="Học kỳ dự kiến"
                  type="number"
                  min="1"
                  max="12"
                  variant="outlined"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="newSubject.subjectType"
                  :items="[{title: 'Bắt buộc', value: 'COMPULSORY'}, {title: 'Tự chọn', value: 'ELECTIVE'}]"
                  label="Loại môn"
                  variant="outlined"
                  required
                ></v-select>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model.number="newSubject.passScore"
                  label="Điểm đạt (thang 10)"
                  type="number"
                  step="0.1"
                  min="0"
                  max="10"
                  variant="outlined"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="addSubjectDialog = false">Hủy</v-btn>
          <v-btn color="primary" variant="flat" :disabled="!formValid" @click="saveSubject">Xác nhận thêm</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <ConfirmDialog 
      v-model="confirmState.show" 
      :title="confirmState.title" 
      :text="confirmState.text" 
      :loading="confirmState.loading"
      @confirm="executeConfirm"
    />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'
import academicProgramService, { type AcademicProgramResponse, type ProgramSubjectResponse } from '@/api/services/academicProgram.service'
import { subjectService } from '@/api/services/subject.service'
import { useUiStore } from '@/stores/ui'
import { unwrapApiResponse } from '@/api/response'

const uiStore = useUiStore()
const route = useRoute()
const programId = Number(route.params.programId)

const loading = ref(false)
const program = ref<AcademicProgramResponse | null>(null)
const programSubjects = ref<ProgramSubjectResponse[]>([])
const availableSubjects = ref<any[]>([])

const addSubjectDialog = ref(false)
const formValid = ref(false)

const pageTitle = computed(() => program.value ? `CTĐT: ${program.value.name}` : 'Chi tiết Chương trình đào tạo')
const pageSubtitle = computed(() => program.value ? `Quản lý môn học thuộc chương trình ${program.value.code}` : 'Đang tải...')

const newSubject = reactive({
  subjectId: null as number | null,
  semester: 1,
  passScore: 4.0,
  subjectType: 'COMPULSORY'
})

const confirmState = reactive({
  show: false,
  title: '',
  text: '',
  loading: false,
  action: null as (() => Promise<void>) | null
})

const headers = [
  { title: 'Học kỳ', key: 'semester', width: '100px' },
  { title: 'Mã môn', key: 'subject.code', width: '120px' },
  { title: 'Tên học phần', key: 'subject.name' },
  { title: 'Số tín chỉ', key: 'subject.credit', width: '100px', align: 'center' as const },
  { title: 'Phân loại', key: 'subjectType', width: '120px' },
  { title: 'Điểm liệt', key: 'passScore', width: '100px', align: 'center' as const },
  { title: '', key: 'actions', sortable: false, align: 'end' as const },
]

const loadProgram = async () => {
  try {
    const res = await academicProgramService.getById(programId)
    program.value = unwrapApiResponse<AcademicProgramResponse>(res)
  } catch (err: any) {
    uiStore.notify('Không tìm thấy chương trình đào tạo', 'error')
  }
}

const loadProgramSubjects = async () => {
  loading.value = true
  try {
    const res = await academicProgramService.getSubjects(programId)
    programSubjects.value = res.data || []
  } catch (err: any) {
    uiStore.notify('Lỗi tải danh sách môn học', 'error')
  } finally {
    loading.value = false
  }
}

const openAddSubjectDialog = async () => {
  try {
    const res = await subjectService.getAll({ page: 1, size: 2000, active: true })
    const subjectData = (res.data as any)?.data
    if (subjectData && Array.isArray(subjectData.data)) {
        // Filter out subjects already in the program
        const existingIds = programSubjects.value.map(ps => ps.subject.id)
        availableSubjects.value = subjectData.data.filter((s: any) => !existingIds.includes(s.id))
    }
    
    addSubjectDialog.value = true
  } catch (err: any) {
    uiStore.notify('Lỗi tải danh sách môn học nhà trường', 'error')
  }
}

const saveSubject = async () => {
  if (!newSubject.subjectId) return
  
  try {
    await academicProgramService.addSubject(programId, {
      subjectId: newSubject.subjectId,
      semester: Number(newSubject.semester),
      subjectType: newSubject.subjectType,
      passScore: Number(newSubject.passScore)
    })
    uiStore.notify('Đã thêm môn học vào chương trình', 'success')
    addSubjectDialog.value = false
    await loadProgramSubjects()
  } catch (err: any) {
    uiStore.notify(err.response?.data?.message || 'Lỗi khi thêm môn học', 'error')
  }
}

const confirmRemove = (item: ProgramSubjectResponse) => {
  confirmState.title = 'Xác nhận gỡ môn'
  confirmState.text = `Bạn có chắc muốn gỡ môn "${item.subject.name}" khỏi chương trình này không?`
  confirmState.action = async () => {
    try {
      await academicProgramService.removeSubject(item.id)
      uiStore.notify('Đã gỡ môn học thành công', 'success')
      await loadProgramSubjects()
    } catch (err: any) {
      uiStore.notify('Lỗi khi thực hiện gỡ môn', 'error')
    }
  }
  confirmState.show = true
}

const executeConfirm = async () => {
  if (confirmState.action) {
    confirmState.loading = true
    await confirmState.action()
    confirmState.loading = false
    confirmState.show = false
  }
}

onMounted(() => {
  loadProgram()
  loadProgramSubjects()
})
</script>

<style scoped>
.scroll-card {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}
</style>
