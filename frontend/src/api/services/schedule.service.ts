import http from '@/api/axios'

export interface ScheduleEvent {
  sectionId: number
  sectionName: string
  subjectName: string
  subjectCode: string
  teacherName: string
  dayOfWeek: number
  startTime: string
  endTime: string
  room: string
}

export const scheduleService = {
  getMySchedule(semesterId?: number) {
    return http.get<ScheduleEvent[]>('/schedules/mine', {
      params: { semesterId }
    })
  }
}
