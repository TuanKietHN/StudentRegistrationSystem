<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="`Danh sách sinh viên (Course #${courseId})`">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Sinh viên</th>
            <th>Email</th>
            <th>Trạng thái</th>
            <th>Điểm</th>
            <th>Lưu</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in enrollments" :key="e.id">
            <td>{{ e.id }}</td>
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
                :model-value="draftGrade[e.id] ?? (e.grade ?? '')"
                density="compact"
                variant="outlined"
                hide-details
                type="number"
                min="0"
                max="10"
                step="0.1"
                @update:model-value="(v) => (draftGrade[e.id] = v)"
              />
            </td>
            <td>
              <v-btn size="small" color="primary" variant="flat" :loading="savingId === e.id" @click="save(e.id)">
                Lưu
              </v-btn>
            </td>
          </tr>
          <tr v-if="enrollments.length === 0">
            <td colspan="6" class="text-center py-6">Không có dữ liệu</td>
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

const statusOptions = ['ENROLLED', 'COMPLETED', 'DROPPED', 'CANCELLED']

const draftStatus = reactive<Record<number, string>>({})
const draftGrade = reactive<Record<number, any>>({})

const reload = async () => {
  loading.value = true
  try {
    const res = await enrollmentService.getCourseEnrollments(courseId)
    enrollments.value = unwrapApiResponse<Enrollment[]>(res) || []
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
    const grade = toGradeOrNull(draftGrade[enrollmentId])
    await enrollmentService.updateEnrollment(enrollmentId, { status, grade })
    uiStore.notify('Lưu thành công', 'success')
    await reload()
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Lưu thất bại', 'error', 4000)
  } finally {
    savingId.value = null
  }
}

onMounted(() => reload())
</script>
