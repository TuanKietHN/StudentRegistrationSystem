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
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in enrollments" :key="e.id">
            <td>{{ e.id }}</td>
            <td>{{ e.section?.code || '-' }}</td>
            <td>{{ e.section?.name || '-' }}</td>
            <td>{{ e.section?.semester?.code || '-' }}</td>
            <td>{{ formatTimeSlots(e.section?.timeSlots) }}</td>
            <td>
              <v-chip color="blue" variant="tonal" size="small">{{ e.status }}</v-chip>
            </td>
            <td>
              <v-btn
                size="small"
                color="error"
                variant="text"
                :loading="cancelingId === e.id"
                :disabled="!canCancel(e)"
                @click="openCancelDialog(e)"
              >
                Hủy
              </v-btn>
            </td>
          </tr>
          <tr v-if="enrollments.length === 0">
            <td colspan="7" class="text-center py-6">Chưa có lớp đã đăng ký</td>
          </tr>
        </tbody>
      </v-table>

      <ConfirmDialog
        v-model="confirmState.show"
        :title="confirmState.title"
        :text="confirmState.text"
        :loading="confirmState.loading"
        confirm-text="Xác nhận hủy"
        @confirm="executeConfirm"
      />
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { unwrapApiResponse } from '@/api/response'
import { enrollmentService, type Enrollment } from '@/api/services/enrollment.service'
import { useUiStore } from '@/stores/ui'
import { formatTimeSlotsVn } from '@/utils/schedule'
import PageHeader from '@/components/ui/PageHeader.vue'
import ConfirmDialog from '@/components/ui/ConfirmDialog.vue'

const uiStore = useUiStore()

const enrollments = ref<Enrollment[]>([])
const loading = ref(false)
const cancelingId = ref<number | null>(null)

// Confirm Dialog State
const confirmState = reactive({
  show: false,
  title: '',
  text: '',
  loading: false,
  action: null as (() => Promise<void>) | null
})

const todayStr = () => new Date().toISOString().slice(0, 10)

const isWindowOpen = (start?: string | null, end?: string | null) => {
  if (!start || !end) return false
  const now = todayStr()
  return now >= start && now <= end
}

const canCancel = (e: Enrollment) => {
  if (e.status !== 'ENROLLED') return false
  return isWindowOpen(e.section?.enrollmentStartDate, e.section?.enrollmentEndDate)
}

const formatTimeSlots = (slots?: Array<{ dayOfWeek: number; startTime: string; endTime: string }> | null) =>
  formatTimeSlotsVn(slots)

const reload = async () => {
  loading.value = true
  try {
    const res = await enrollmentService.getMyEnrollments()
    enrollments.value = unwrapApiResponse<Enrollment[]>(res) || []
  } finally {
    loading.value = false
  }
}

const openCancelDialog = (e: Enrollment) => {
  confirmState.title = 'Xác nhận hủy đăng ký'
  confirmState.text = `Bạn có chắc chắn muốn hủy đăng ký lớp "${e.section?.name || e.section?.code}" không?`
  confirmState.action = async () => {
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

onMounted(async () => {
  await reload()
})
</script>
