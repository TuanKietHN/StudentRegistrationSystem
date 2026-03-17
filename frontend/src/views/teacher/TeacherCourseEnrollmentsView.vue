<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="courseTitle" back-to="/teacher/sections">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
          <v-btn variant="text" :loading="downloading" @click="downloadTemplate" prepend-icon="mdi-download">Tải file điểm</v-btn>
          <v-btn variant="text" :loading="importing" @click="importGrades" prepend-icon="mdi-upload">Import Excel</v-btn>
        </template>
      </PageHeader>

      <v-file-input
        v-model="selectedFile"
        accept=".xlsx"
        label="File Excel điểm (xlsx)"
        prepend-icon="mdi-file-excel"
        density="comfortable"
        variant="outlined"
        class="mb-4"
      />

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-alert variant="tonal" color="info" density="compact" class="mb-4">
        Import Excel (.xlsx): cần cột định danh (studentCode/mssv hoặc username) và cột điểm (process/qt, exam/thi). Điểm theo thang 0–10.
      </v-alert>

      <v-table>
        <thead>
          <tr>
            <th>MSSV</th>
            <th>Sinh viên</th>
            <th>Email</th>
            <th>SĐT</th>
            <th>Khoa</th>
            <th>Lớp HC</th>
            <th>SV</th>
            <th>Trạng thái</th>
            <th>Điểm quá trình</th>
            <th>Điểm thi</th>
            <th>Điểm cuối</th>
            <th>Lưu</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in enrollments" :key="e.id" :class="e.scoreOverridden ? 'bg-yellow-lighten-5' : ''">
            <td>{{ e.studentCode || '-' }}</td>
            <td>{{ e.student?.username || '-' }}</td>
            <td>{{ e.student?.email || '-' }}</td>
            <td>{{ e.studentPhone || '-' }}</td>
            <td>{{ e.studentDepartmentCode || '-' }}</td>
            <td>{{ e.studentClassCode || '-' }}</td>
            <td>
              <v-chip :color="e.studentActive ? 'green' : 'grey'" variant="tonal" size="small">
                {{ e.studentActive ? 'Đang học' : 'Ngưng' }}
              </v-chip>
            </td>
            <td style="min-width: 160px">
              <v-select
                :items="statusOptions"
                :model-value="draftStatus[e.id] ?? e.status"
                density="compact"
                variant="outlined"
                hide-details
                @update:model-value="(v) => (draftStatus[e.id] = String(v))"
              />
            </td>
            <td style="max-width: 140px">
              <v-text-field
                :model-value="draftProcess[e.id] ?? (e.processScore ?? '')"
                density="compact"
                variant="outlined"
                hide-details
                type="number"
                min="0"
                max="10"
                step="0.1"
                :disabled="e.scoreLocked"
                @update:model-value="(v) => (draftProcess[e.id] = v)"
              />
            </td>
            <td style="max-width: 140px">
              <v-text-field
                :model-value="draftExam[e.id] ?? (e.examScore ?? '')"
                density="compact"
                variant="outlined"
                hide-details
                type="number"
                min="0"
                max="10"
                step="0.1"
                :disabled="e.scoreLocked"
                @update:model-value="(v) => (draftExam[e.id] = v)"
              />
            </td>
            <td style="white-space: nowrap">
              <span v-if="e.finalScore != null">{{ e.finalScore }}</span>
              <span v-else>{{ e.grade ?? '-' }}</span>
              <v-chip v-if="e.scoreLocked" class="ml-2" size="x-small" color="grey" variant="tonal">Khóa</v-chip>
              <v-chip v-if="e.scoreOverridden" class="ml-2" size="x-small" color="warning" variant="tonal">Đã sửa</v-chip>
            </td>
            <td>
              <v-btn 
                size="small" 
                color="primary" 
                variant="flat" 
                :loading="savingId === e.id" 
                :disabled="!isRowChanged(e)"
                @click="save(e.id)"
              >
                Lưu
              </v-btn>
            </td>
          </tr>
          <tr v-if="enrollments.length === 0">
            <td colspan="12" class="text-center py-6">Không có dữ liệu</td>
          </tr>
        </tbody>
      </v-table>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { unwrapApiResponse } from '@/api/response'
import { sectionService } from '@/api/services/section.service'
import { sectionGradeService, type SectionGradeResponse } from '@/api/services/section-grade.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUiStore()
const route = useRoute()

const sectionId = Number(route.params.sectionId)
const sectionCode = ref<string>('')
const courseTitle = ref(`Danh sách sinh viên (Section #${sectionId})`)
const enrollments = ref<SectionGradeResponse[]>([])
const loading = ref(false)
const savingId = ref<number | null>(null)
const importing = ref(false)
const downloading = ref(false)
const selectedFile = ref<File | null>(null)

const statusOptions = [
  { title: 'Đang tham gia', value: 'ENROLLED' },
  { title: 'Hoàn thành', value: 'COMPLETED' },
  { title: 'Rút lớp', value: 'DROPPED' },
  { title: 'Hủy', value: 'CANCELLED' }
]

const draftStatus = reactive<Record<number, string>>({})
const draftProcess = reactive<Record<number, any>>({})
const draftExam = reactive<Record<number, any>>({})

const isRowChanged = (e: SectionGradeResponse) => {
  const statusChanged = draftStatus[e.id] !== undefined && draftStatus[e.id] !== e.status
  const processChanged = draftProcess[e.id] !== undefined && draftProcess[e.id] !== (e.processScore ?? '').toString()
  const examChanged = draftExam[e.id] !== undefined && draftExam[e.id] !== (e.examScore ?? '').toString()
  
  return statusChanged || processChanged || examChanged
}

const loadSection = async () => {
  try {
    const res = await sectionService.getById(sectionId)
    const s = unwrapApiResponse<any>(res)
    if (s?.code) sectionCode.value = s.code
    courseTitle.value = s?.code && s?.name ? `Danh sách sinh viên - ${s.code} - ${s.name}` : courseTitle.value
  } catch {
    courseTitle.value = `Danh sách sinh viên (Section #${sectionId})`
  }
}

const reload = async () => {
  loading.value = true
  try {
    const res = await sectionGradeService.getSectionGrades(sectionId)
    enrollments.value = unwrapApiResponse<SectionGradeResponse[]>(res) || []
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Không tải được danh sách sinh viên', 'error', 4000)
  } finally {
    loading.value = false
  }
}

const toGradeOrNull = (v: any): number | null => {
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  if (Number.isNaN(n)) return null
  return n
}

const save = async (enrollmentId: number) => {
  savingId.value = enrollmentId
  try {
    const status = draftStatus[enrollmentId]
    const processScore = toGradeOrNull(draftProcess[enrollmentId])
    const examScore = toGradeOrNull(draftExam[enrollmentId])
    const payload: any = { status }
    if (processScore != null || examScore != null) {
      payload.processScore = processScore
      payload.examScore = examScore
    }
    await sectionGradeService.updateGrade(enrollmentId, payload)
    uiStore.notify('Lưu thành công', 'success')
    // Clear draft values after save
    delete draftStatus[enrollmentId]
    delete draftProcess[enrollmentId]
    delete draftExam[enrollmentId]
    await reload()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Lưu thất bại', 'error', 4000)
  } finally {
    savingId.value = null
  }
}

const downloadTemplate = async () => {
  downloading.value = true
  try {
    const res = await sectionGradeService.downloadTemplate(sectionId)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `Bang_diem_${sectionCode.value || sectionId}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    uiStore.notify('Tải file điểm thành công', 'success')
  } catch (err: any) {
    uiStore.notify('Không thể tải file điểm', 'error')
  } finally {
    downloading.value = false
  }
}

const importGrades = async () => {
  if (!selectedFile.value) {
    uiStore.notify('Vui lòng chọn file Excel trước khi Import', 'error')
    return
  }
  
  if (selectedFile.value.size > 5 * 1024 * 1024) {
    uiStore.notify('File quá lớn (tối đa 5MB)', 'error')
    return
  }
  
  if (!selectedFile.value.name.endsWith('.xlsx')) {
    uiStore.notify('Vui lòng chọn file định dạng .xlsx', 'error')
    return
  }

  importing.value = true
  try {
    const res = await sectionGradeService.importSectionGrades(sectionId, selectedFile.value)
    const data = unwrapApiResponse<any>(res)
    const imported = data?.imported ?? 0
    const skippedLocked = data?.skippedLocked ?? 0
    const skippedNotFound = data?.skippedNotFound ?? 0
    const skippedInvalid = data?.skippedInvalid ?? 0
    uiStore.notify(
      `Thành công: ${imported} SV. Bỏ qua: ${skippedLocked} SV đã khóa điểm, ${skippedNotFound} SV không thuộc lớp, ${skippedInvalid} dòng lỗi hoặc trống.`,
      'success',
      6000
    )
    await reload()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Import thất bại', 'error', 5000)
  } finally {
    importing.value = false
  }
}

onMounted(async () => {
  await loadSection()
  await reload()
})
</script>
