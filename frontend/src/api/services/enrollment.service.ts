import api from '@/api/axios'

export interface EnrollmentCourse {
  id: number
  name: string
  code: string
  semester?: any
  subject?: any
  teacher?: any
  enrollmentStartDate?: string | null
  enrollmentEndDate?: string | null
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
}

export interface Enrollment {
  id: number
  course: EnrollmentCourse
  student?: EnrollmentStudent
  studentCode?: string | null
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
  enrollSelf(courseId: number) {
    return api.post('/v1/enrollments/self', { courseId })
  },
  getMyEnrollments() {
    return api.get('/v1/enrollments/me')
  },
  cancelEnrollment(id: number) {
    return api.delete(`/v1/enrollments/${id}`)
  },
  getCourseEnrollments(courseId: number) {
    return api.get(`/v1/enrollments/course/${courseId}`)
  },
  updateEnrollment(
    id: number,
    payload: { status?: string; grade?: number | null; processScore?: number | null; examScore?: number | null; overrideReason?: string }
  ) {
    return api.put(`/v1/enrollments/${id}`, payload)
  },
  importCourseGrades(courseId: number, file: File) {
    const form = new FormData()
    form.append('file', file)
    return api.post(`/v1/enrollments/course/${courseId}/grades/import`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
