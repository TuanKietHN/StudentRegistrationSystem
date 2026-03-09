<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 mb-4">{{ pageTitle }}</h1>
      </v-col>
    </v-row>
    <v-row v-if="loading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary"></v-progress-circular>
      </v-col>
    </v-row>
    <v-row v-else>
      <v-col v-for="day in days" :key="day.value" cols="12" md>
        <v-card class="h-100" variant="outlined">
          <v-card-title class="bg-grey-lighten-4 text-center text-subtitle-1 font-weight-bold py-2">
            {{ day.text }}
          </v-card-title>
          <v-divider></v-divider>
          <v-card-text class="pa-2">
            <template v-if="getEventsForDay(day.value).length > 0">
              <v-card
                v-for="event in getEventsForDay(day.value)"
                :key="event.sectionId + '-' + event.startTime"
                class="mb-2"
                color="primary"
                variant="tonal"
                elevation="1"
              >
                <v-card-text class="pa-2">
                  <div class="font-weight-bold text-body-2">{{ event.subjectName }}</div>
                  <div class="text-caption font-weight-medium">{{ event.subjectCode }}</div>
                  <div class="text-caption mt-1">
                    <v-icon size="small" class="mr-1">mdi-door</v-icon>
                    {{ event.room || 'Chưa cập nhật' }}
                  </div>
                  <div class="text-caption">
                    <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
                    {{ formatTime(event.startTime) }} - {{ formatTime(event.endTime) }}
                  </div>
                  <div class="text-caption mt-1" v-if="event.teacherName && event.teacherName !== 'N/A'">
                    <v-icon size="small" class="mr-1">mdi-account</v-icon>
                    {{ event.teacherName }}
                  </div>
                </v-card-text>
              </v-card>
            </template>
            <div v-else class="text-center text-caption text-grey mt-4">
              Không có lịch
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { scheduleService, type ScheduleEvent } from '@/api/services/schedule.service'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const events = ref<ScheduleEvent[]>([])

const pageTitle = computed(() => {
  return authStore.activeRole === 'TEACHER' ? 'Lịch giảng dạy của tôi' : 'Lịch học của tôi'
})

// Mapping based on Java DayOfWeek (ISO-8601): 1=Monday, 7=Sunday
const days = [
  { text: 'Thứ 2', value: 1 },
  { text: 'Thứ 3', value: 2 },
  { text: 'Thứ 4', value: 3 },
  { text: 'Thứ 5', value: 4 },
  { text: 'Thứ 6', value: 5 },
  { text: 'Thứ 7', value: 6 },
  { text: 'Chủ Nhật', value: 7 },
]

const getEventsForDay = (dayValue: number) => {
  return events.value.filter(e => e.dayOfWeek === dayValue)
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
}

const formatTime = (time: string) => {
  if (!time) return ''
  // Handle HH:mm:ss format
  return time.split(':').slice(0, 2).join(':')
}

const fetchSchedule = async () => {
  loading.value = true
  try {
    const response = await scheduleService.getMySchedule()
    events.value = response.data
  } catch (error) {
    console.error('Failed to fetch schedule', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSchedule()
})
</script>

<style scoped>
.v-card-title {
  font-size: 0.9rem !important;
}
</style>
