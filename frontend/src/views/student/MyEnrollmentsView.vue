<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Lớp đã đăng ký">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã lớp</th>
            <th>Tên lớp</th>
            <th>Học kỳ</th>
            <th>Thời gian học</th>
            <th>Trạng thái</th>
            <th>Điểm</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in enrollments" :key="e.id">
            <td>{{ e.id }}</td>
            <td>{{ e.course?.code || '-' }}</td>
            <td>{{ e.course?.name || '-' }}</td>
            <td>{{ e.course?.semester?.code || '-' }}</td>
            <td>{{ formatTimeSlots(e.course?.timeSlots) }}</td>
            <td>
              <v-chip color="blue" variant="tonal" size="small">{{ e.status }}</v-chip>
            </td>
            <td>{{ e.grade ?? '-' }}</td>
            <td>
              <v-btn
                size="small"
                color="error"
                variant="text"
                :loading="cancelingId === e.id"
                :disabled="!canCancel(e)"
                @click="cancel(e)"
              >
                Hủy
              </v-btn>
            </td>
          </tr>
          <tr v-if="enrollments.length === 0">
            <td colspan="8" class="text-center py-6">Chưa có lớp đã đăng ký</td>
          </tr>
        </tbody>
      </v-table>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { unwrapApiResponse } from '@/api/response'
import { enrollmentService, type Enrollment } from '@/api/services/enrollment.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUiStore()

const enrollments = ref<Enrollment[]>([])
const loading = ref(false)
const cancelingId = ref<number | null>(null)

const todayStr = () => new Date().toISOString().slice(0, 10)

const isWindowOpen = (start?: string | null, end?: string | null) => {
  if (!start || !end) return false
  const now = todayStr()
  return now >= start && now <= end
}

const canCancel = (e: Enrollment) => {
  if (e.status !== 'ENROLLED') return false
  return isWindowOpen(e.course?.enrollmentStartDate, e.course?.enrollmentEndDate)
}

const formatTimeSlots = (slots?: Array<{ dayOfWeek: number; startTime: string; endTime: string }> | null) => {
  if (!slots?.length) return '-'
  return slots.map((s) => `T${s.dayOfWeek} ${s.startTime.slice(0, 5)}-${s.endTime.slice(0, 5)}`).join(', ')
}

const reload = async () => {
  loading.value = true
  try {
    const res = await enrollmentService.getMyEnrollments()
    enrollments.value = unwrapApiResponse<Enrollment[]>(res) || []
  } finally {
    loading.value = false
  }
}

const cancel = async (e: Enrollment) => {
  cancelingId.value = e.id
  try {
    await enrollmentService.cancelEnrollment(e.id)
    uiStore.notify('Hủy đăng ký thành công', 'success')
    await reload()
  } catch (err: any) {
    const msg = err?.response?.data?.message || 'Hủy đăng ký thất bại'
    uiStore.notify(msg, 'error', 4000)
  } finally {
    cancelingId.value = null
  }
}

onMounted(async () => {
  await reload()
})
</script>
