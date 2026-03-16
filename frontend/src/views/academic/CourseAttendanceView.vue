<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="courseTitle">
        <template #actions>
          <v-btn variant="text" :loading="loadingSessions" @click="reload">Tải lại</v-btn>
          <v-btn color="primary" variant="flat" :loading="opening" @click="openToday">Mở điểm danh hôm nay</v-btn>
        </template>
      </PageHeader>

      <v-row class="mt-2">
        <v-col cols="12" md="4">
          <v-text-field v-model.number="periods" type="number" min="1" label="Số tiết của buổi" />
          <v-alert v-if="selectedSession" class="mt-2" density="compact" variant="tonal" color="info">
            Buổi: {{ selectedSession.sessionDate }} ({{ selectedSession.periods }} tiết).
            Mở lúc: {{ selectedSession.openedAt || '-' }}. Khóa 'Có mặt' sau: {{ selectedSession.closesAt || '-' }}.
          </v-alert>

          <v-list class="mt-3" density="compact" nav>
            <v-list-subheader>Buổi điểm danh</v-list-subheader>
            <v-list-item
              v-for="s in sessions"
              :key="s.id"
              :active="s.id === selectedSessionId"
              :title="`${s.sessionDate} (${s.periods} tiết)`"
              @click="selectSession(s.id)"
            />
            <div v-if="sessions.length === 0" class="text-caption px-4 py-3">Chưa có buổi điểm danh</div>
          </v-list>
        </v-col>

        <v-col cols="12" md="8">
          <v-progress-linear v-if="loadingRoster" indeterminate class="mb-3" />
          <v-table v-if="selectedSession && roster.length">
            <thead>
              <tr>
                <th>MSSV</th>
                <th>Sinh viên</th>
                <th>Email</th>
                <th>SĐT</th>
                <th>Khoa</th>
                <th>Lớp HC</th>
                <th>Điểm danh</th>
                <th>Cấm thi</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in roster" :key="r.enrollmentId">
                <td>{{ r.studentCode || '-' }}</td>
                <td>{{ r.username || '-' }}</td>
                <td>{{ r.email || '-' }}</td>
                <td>{{ r.phone || '-' }}</td>
                <td>{{ r.departmentCode || '-' }}</td>
                <td>{{ r.adminClassCode || '-' }}</td>
                <td style="min-width: 170px">
                  <v-select
                    :items="statusOptions"
                    item-title="title"
                    item-value="value"
                    density="compact"
                    variant="outlined"
                    hide-details
                    :model-value="r.attendanceStatus"
                    @update:model-value="(v) => mark(r.enrollmentId, String(v) as any)"
                  />
                </td>
                <td style="white-space: nowrap">
                  <v-chip v-if="r.bannedExam" color="error" variant="tonal" size="small">Cấm thi</v-chip>
                  <span v-else>-</span>
                </td>
              </tr>
            </tbody>
          </v-table>

          <v-alert v-else-if="selectedSession && !loadingRoster" variant="tonal" density="compact" color="info">
            Chưa có dữ liệu sinh viên cho buổi này.
          </v-alert>
          <v-alert v-else variant="tonal" density="compact" color="info">
            Chọn một buổi điểm danh để xem danh sách.
          </v-alert>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { unwrapApiResponse } from '@/api/response'
import { cohortService } from '@/api/services/cohort.service'
import { attendanceService, type AttendanceRosterRow, type AttendanceSession, type AttendanceStatus } from '@/api/services/attendance.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUiStore()
const route = useRoute()
const cohortId = Number(route.params.cohortId)

const courseTitle = ref(`Điểm danh (Cohort #${cohortId})`)
const sessions = ref<AttendanceSession[]>([])
const selectedSessionId = ref<number | null>(null)
const selectedSession = computed(() => sessions.value.find((s) => s.id === selectedSessionId.value) || null)
const roster = ref<AttendanceRosterRow[]>([])

const periods = ref(3)
const opening = ref(false)
const loadingSessions = ref(false)
const loadingRoster = ref(false)

const statusOptions = computed(() => {
  const lockedPresent = isPresentLocked.value
  return [
    { title: lockedPresent ? 'Có mặt (đã khóa)' : 'Có mặt', value: 'PRESENT', disabled: lockedPresent },
    { title: 'Muộn', value: 'LATE' },
    { title: 'Vắng', value: 'ABSENT' },
    { title: 'Vắng có phép', value: 'EXCUSED' }
  ]
})

const isPresentLocked = computed(() => {
  const s = selectedSession.value
  if (!s?.closesAt) return false
  return new Date().getTime() > new Date(s.closesAt).getTime()
})

const loadCourse = async () => {
  try {
    const res = await cohortService.getById(cohortId)
    const c = unwrapApiResponse<any>(res)
    courseTitle.value = c?.code && c?.name ? `Điểm danh - ${c.code} - ${c.name}` : courseTitle.value
  } catch {
    courseTitle.value = `Điểm danh (Cohort #${cohortId})`
  }
}

const fetchSessions = async () => {
  loadingSessions.value = true
  try {
    const res = await attendanceService.listCohortSessions(cohortId)
    sessions.value = unwrapApiResponse<AttendanceSession[]>(res) || []
    if (!selectedSessionId.value && sessions.value.length) {
      selectedSessionId.value = sessions.value[0].id
    }
  } catch (err: any) {
    sessions.value = []
    uiStore.notify(err?.response?.data?.message || 'Không tải được danh sách buổi điểm danh', 'error', 4000)
  } finally {
    loadingSessions.value = false
  }
}

const fetchRoster = async (sessionId: number) => {
  loadingRoster.value = true
  try {
    const res = await attendanceService.getSessionRoster(sessionId)
    const data = unwrapApiResponse<any>(res)
    roster.value = (data?.students || []) as AttendanceRosterRow[]
  } catch (err: any) {
    roster.value = []
    uiStore.notify(err?.response?.data?.message || 'Không tải được roster điểm danh', 'error', 4000)
  } finally {
    loadingRoster.value = false
  }
}

const selectSession = async (id: number) => {
  selectedSessionId.value = id
  await fetchRoster(id)
}

const openToday = async () => {
  opening.value = true
  try {
    const today = new Date().toISOString().slice(0, 10)
    const res = await attendanceService.openSession({ cohortId, sessionDate: today, periods: periods.value })
    const s = unwrapApiResponse<AttendanceSession>(res)
    await fetchSessions()
    selectedSessionId.value = s.id
    await fetchRoster(s.id)
    uiStore.notify('Mở điểm danh thành công', 'success')
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Mở điểm danh thất bại', 'error', 4000)
  } finally {
    opening.value = false
  }
}

const mark = async (enrollmentId: number, status: AttendanceStatus) => {
  if (!selectedSessionId.value) return
  try {
    const res = await attendanceService.markAttendance(selectedSessionId.value, enrollmentId, { status })
    const data = unwrapApiResponse<any>(res)
    roster.value = (data?.students || []) as AttendanceRosterRow[]
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Cập nhật điểm danh thất bại', 'error', 4000)
  }
}

const reload = async () => {
  await Promise.allSettled([fetchSessions(), loadCourse()])
  if (selectedSessionId.value) {
    await fetchRoster(selectedSessionId.value)
  }
}

onMounted(async () => {
  await reload()
})
</script>
