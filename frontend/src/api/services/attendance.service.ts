import api from '@/api/axios'

export type AttendanceStatus = 'PRESENT' | 'LATE' | 'ABSENT' | 'EXCUSED'

export interface AttendanceSession {
  id: number
  cohortId: number
  sessionDate: string
  periods: number
  openedAt?: string | null
  closesAt?: string | null
  createdAt?: string
  updatedAt?: string
}

export interface AttendanceRosterRow {
  enrollmentId: number
  studentId: number
  studentCode?: string | null
  username?: string | null
  email?: string | null
  phone?: string | null
  departmentCode?: string | null
  departmentName?: string | null
  adminClassCode?: string | null
  adminClassName?: string | null
  attendanceStatus: AttendanceStatus
  markedAt?: string | null
  note?: string | null
  absentEquivalentPeriods?: number | null
  absentLimitPeriods?: number | null
  bannedExam?: boolean
}

export interface AttendanceRosterResponse {
  session: AttendanceSession
  students: AttendanceRosterRow[]
}

export const attendanceService = {
  openSession(payload: { cohortId: number; sessionDate?: string; periods?: number }) {
    return api.post('/v1/attendance/sessions/open', payload)
  },
  listCohortSessions(cohortId: number) {
    return api.get(`/v1/attendance/cohorts/${cohortId}/sessions`)
  },
  getSessionRoster(sessionId: number) {
    return api.get(`/v1/attendance/sessions/${sessionId}/roster`)
  },
  markAttendance(sessionId: number, enrollmentId: number, payload: { status: AttendanceStatus; note?: string }) {
    return api.put(`/v1/attendance/sessions/${sessionId}/records/${enrollmentId}`, payload)
  }
}
