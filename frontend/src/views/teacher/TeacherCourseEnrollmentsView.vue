<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="courseTitle" back-to="/teacher/sections">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
          <v-btn variant="text" :disabled="!selectedFile" :loading="importing" @click="importGrades">Import Excel</v-btn>
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
              <v-btn size="small" color="primary" variant="flat" :loading="savingId === e.id" @click="save(e.id)">
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
const courseTitle = ref(`Danh sách sinh viên (Section #${sectionId})`)
const enrollments = ref<SectionGradeResponse[]>([])
const loading = ref(false)
const savingId = ref<number | null>(null)
const importing = ref(false)
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

const loadSection = async () => {
  try {
    const res = await sectionService.getById(sectionId)
    const s = unwrapApiResponse<any>(res)
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
    await reload()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Lưu thất bại', 'error', 4000)
  } finally {
    savingId.value = null
  }
}

const importGrades = async () => {
  if (!selectedFile.value) return
  importing.value = true
  try {
    const res = await sectionGradeService.importSectionGrades(sectionId, selectedFile.value)
    const data = unwrapApiResponse<any>(res)
    const imported = data?.imported ?? 0
    const skippedLocked = data?.skippedLocked ?? 0
    const skippedNotFound = data?.skippedNotFound ?? 0
    const skippedInvalid = data?.skippedInvalid ?? 0
    uiStore.notify(
      `Import xong: ${imported} dòng. Bỏ qua: khóa ${skippedLocked}, không tìm thấy ${skippedNotFound}, lỗi ${skippedInvalid}.`,
      'success',
      4500
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
