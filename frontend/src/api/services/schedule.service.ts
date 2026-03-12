import http from '@/api/axios'
import type { ApiResponse } from '@/api/response'

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
  async getMySchedule(semesterId?: number): Promise<ApiResponse<ScheduleEvent[]>> {
    const response = await http.get<ApiResponse<ScheduleEvent[]>>('/schedules/mine', {
      params: { semesterId }
    })
    return response.data
  }
}
