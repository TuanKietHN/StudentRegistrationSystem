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

export interface Enrollment {
  id: number
  course: EnrollmentCourse
  status: string
  grade?: number | null
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
  }
}

