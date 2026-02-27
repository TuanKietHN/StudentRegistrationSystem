<template>
  <v-card>
    <v-card-text class="py-8">
      <PageHeader title="Trang chủ giảng viên">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-row>
        <v-col cols="12" md="6">
          <v-card variant="outlined">
            <v-card-title class="text-subtitle-1">Lịch làm việc</v-card-title>
            <v-card-text>
              <v-progress-linear v-if="loading" indeterminate class="mb-4" />
              <div v-if="!loading && scheduleDays.length === 0" class="text-body-2 text-medium-emphasis">
                Chưa có lịch giảng dạy
              </div>
              <div v-for="d in scheduleDays" :key="d.day" class="mb-4">
                <div class="text-subtitle-2 mb-2">{{ d.label }}</div>
                <v-table density="compact">
                  <thead>
                    <tr>
                      <th>Giờ</th>
                      <th>Lớp</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="it in d.items" :key="it.key">
                      <td style="white-space: nowrap">{{ it.time }}</td>
                      <td>{{ it.title }}</td>
                    </tr>
                  </tbody>
                </v-table>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card variant="outlined">
            <v-card-title class="text-subtitle-1">Thao tác nhanh</v-card-title>
            <v-card-text class="d-flex flex-wrap ga-2">
              <v-btn color="primary" variant="flat" :to="{ name: 'TeacherCourses' }">Lớp tôi dạy</v-btn>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { unwrapPageResponse } from '@/api/response'
import { courseService, type Course } from '@/api/services/course.service'
import { vnDayLabelFromIso } from '@/utils/schedule'
import PageHeader from '@/components/ui/PageHeader.vue'

type ScheduleItem = { key: string; day: number; time: string; title: string; start: string }
type ScheduleDay = { day: number; label: string; items: ScheduleItem[] }

const authStore = useAuthStore()
const loading = ref(false)
const courses = ref<Course[]>([])

const fetchAllActiveCourses = async () => {
  const all: Course[] = []
  let page = 1
  while (true) {
    const res = await courseService.getAll({ page, size: 200, active: true })
    const data = unwrapPageResponse<Course>(res)
    all.push(...(data.data || []))
    const totalPages = data.totalPages || 1
    if (page >= totalPages) break
    page++
  }
  return all
}

const reload = async () => {
  loading.value = true
  try {
    courses.value = await fetchAllActiveCourses()
  } finally {
    loading.value = false
  }
}

const scheduleDays = computed<ScheduleDay[]>(() => {
  const username = authStore.currentUser?.username
  if (!username) return []

  const items: ScheduleItem[] = []
  for (const c of courses.value) {
    if ((c.teacher?.username || '') !== username) continue
    const slots = c.timeSlots || []
    for (const s of slots) {
      const time = `${s.startTime.slice(0, 5)}-${s.endTime.slice(0, 5)}`
      items.push({
        key: `${c.id}-${s.id}`,
        day: s.dayOfWeek,
        time,
        title: `${c.code || ''} - ${c.name || ''}`.trim(),
        start: s.startTime
      })
    }
  }

  const byDay = new Map<number, ScheduleItem[]>()
  for (const it of items) {
    const arr = byDay.get(it.day) || []
    arr.push(it)
    byDay.set(it.day, arr)
  }
  return Array.from(byDay.entries())
    .map(([day, arr]) => ({
      day,
      label: vnDayLabelFromIso(day),
      items: arr.sort((a, b) => a.start.localeCompare(b.start))
    }))
    .sort((a, b) => a.day - b.day)
})

onMounted(() => reload())
</script>
