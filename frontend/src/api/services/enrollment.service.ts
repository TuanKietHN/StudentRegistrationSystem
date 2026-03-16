import api from '@/api/axios'

export interface EnrollmentCohort {
  id: number
  name: string
  code: string
  status?: string
  semester?: any
  clazz?: any
  teacher?: any
  enrollmentStartDate?: string | null
  enrollmentEndDate?: string | null
  registrationEnabled?: boolean
  timeSlots?: Array<{
    id: number
    dayOfWeek: number
    startTime: string
    endTime: string
  }>
}

export interface EnrollmentStudent {
  id: number
  username: string
  email?: string | null
  role?: string | null
}

export interface Enrollment {
  id: number
  cohort: EnrollmentCohort
  student?: EnrollmentStudent
  studentCode?: string | null
  studentPhone?: string | null
  studentActive?: boolean | null
  studentDepartmentCode?: string | null
  studentDepartmentName?: string | null
  studentAdminClassCode?: string | null
  studentAdminClassName?: string | null
  status: string
  grade?: number | null
  processScore?: number | null
  examScore?: number | null
  finalScore?: number | null
  scoreLocked?: boolean
  scoreOverridden?: boolean
  createdAt?: string
  updatedAt?: string
}

export const enrollmentService = {
  enrollSelf(cohortId: number) {
    return api.post('/v1/enrollments/self', { courseId: cohortId })
  },
  getMyEnrollments() {
    return api.get('/v1/enrollments/me')
  },
  cancelEnrollment(id: number) {
    return api.delete(`/v1/enrollments/${id}`)
  },
  getCohortEnrollments(cohortId: number) {
    return api.get(`/v1/enrollments/course/${cohortId}`)
  },
  updateEnrollment(
    id: number,
    payload: { status?: string; grade?: number | null; processScore?: number | null; examScore?: number | null; overrideReason?: string }
  ) {
    return api.put(`/v1/enrollments/${id}`, payload)
  },
  importCohortGrades(cohortId: number, file: File) {
    const form = new FormData()
    form.append('file', file)
    return api.post(`/v1/enrollments/course/${cohortId}/grades/import`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
