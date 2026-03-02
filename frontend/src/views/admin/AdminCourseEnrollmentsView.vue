<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="`Danh sách sinh viên (Course #${courseId})`">
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

      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>MSSV</th>
            <th>Sinh viên</th>
            <th>Email</th>
            <th>Trạng thái</th>
            <th>Điểm quá trình</th>
            <th>Điểm thi</th>
            <th>Điểm cuối</th>
            <th>Lý do sửa</th>
            <th>Lưu</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in enrollments" :key="e.id" :class="e.scoreOverridden ? 'bg-yellow-lighten-5' : ''">
            <td>{{ e.id }}</td>
            <td>{{ e.studentCode || '-' }}</td>
            <td>{{ e.student?.username || '-' }}</td>
            <td>{{ e.student?.email || '-' }}</td>
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
                @update:model-value="(v) => (draftExam[e.id] = v)"
              />
            </td>
            <td style="white-space: nowrap">
              <span v-if="e.finalScore != null">{{ e.finalScore }}</span>
              <span v-else>{{ e.grade ?? '-' }}</span>
              <v-chip v-if="e.scoreLocked" class="ml-2" size="x-small" color="grey" variant="tonal">Khóa</v-chip>
              <v-chip v-if="e.scoreOverridden" class="ml-2" size="x-small" color="warning" variant="tonal">Đã sửa</v-chip>
            </td>
            <td style="min-width: 220px">
              <v-text-field
                :model-value="draftReason[e.id] ?? ''"
                density="compact"
                variant="outlined"
                hide-details
                placeholder="Bắt buộc nếu sửa điểm đã khóa"
                @update:model-value="(v) => (draftReason[e.id] = String(v))"
              />
            </td>
            <td>
              <v-btn size="small" color="primary" variant="flat" :loading="savingId === e.id" @click="save(e)">
                Lưu
              </v-btn>
            </td>
          </tr>
          <tr v-if="enrollments.length === 0">
            <td colspan="10" class="text-center py-6">Không có dữ liệu</td>
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
import { enrollmentService, type Enrollment } from '@/api/services/enrollment.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUiStore()
const route = useRoute()

const courseId = Number(route.params.courseId)
const enrollments = ref<Enrollment[]>([])
const loading = ref(false)
const savingId = ref<number | null>(null)
const importing = ref(false)
const selectedFile = ref<File | null>(null)

const statusOptions = ['ENROLLED', 'COMPLETED', 'DROPPED', 'CANCELLED']

const draftStatus = reactive<Record<number, string>>({})
const draftProcess = reactive<Record<number, any>>({})
const draftExam = reactive<Record<number, any>>({})
const draftReason = reactive<Record<number, string>>({})

const reload = async () => {
  loading.value = true
  try {
    const res = await enrollmentService.getCourseEnrollments(courseId)
    enrollments.value = unwrapApiResponse<Enrollment[]>(res) || []
  } finally {
    loading.value = false
  }
}

const toScoreOrNull = (v: any): number | null => {
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  if (Number.isNaN(n)) return null
  return n
}

const save = async (e: Enrollment) => {
  savingId.value = e.id
  try {
    const status = draftStatus[e.id]
    const processScore = toScoreOrNull(draftProcess[e.id])
    const examScore = toScoreOrNull(draftExam[e.id])
    const overrideReason = (draftReason[e.id] || '').trim()

    const payload: any = { status }
    if (processScore != null || examScore != null) {
      payload.processScore = processScore
      payload.examScore = examScore
      if (e.scoreLocked && !overrideReason) {
        uiStore.notify('Cần nhập lý do khi sửa điểm đã khóa', 'warning', 4000)
        return
      }
      if (e.scoreLocked) payload.overrideReason = overrideReason
    }

    await enrollmentService.updateEnrollment(e.id, payload)
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
    const res = await enrollmentService.importCourseGrades(courseId, selectedFile.value)
    const data = unwrapApiResponse<any>(res)
    uiStore.notify(`Import xong: ${data?.imported ?? 0} dòng`, 'success')
    await reload()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Import thất bại', 'error', 5000)
  } finally {
    importing.value = false
  }
}

onMounted(() => reload())
</script>

