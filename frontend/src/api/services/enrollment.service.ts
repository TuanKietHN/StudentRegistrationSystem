import api from '@/api/axios'

export interface EnrollmentSection {
  id: number
  name: string
  code: string
  status?: string
  semester?: any
  subject?: any
  teacher?: any
  enrollmentStartDate?: string | null
  enrollmentEndDate?: string | null
  registrationEnabled?: boolean
  timeSlots?: Array<{ id: number; dayOfWeek: number; startTime: string; endTime: string }>
}

export interface EnrollmentStudent {
  id: number
  username: string
  email?: string | null
  role?: string | null
}

export interface Enrollment {
  id: number
  section: EnrollmentSection
  student?: EnrollmentStudent
  studentCode?: string | null
  studentPhone?: string | null
  studentActive?: boolean | null
  studentDepartmentCode?: string | null
  studentDepartmentName?: string | null
  studentClassCode?: string | null
  studentClassName?: string | null
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
  enrollSelf(sectionId: number) {
    return api.post('/v1/enrollments/self', { sectionId })
  },
  getMyEnrollments() {
    return api.get('/v1/enrollments/me')
  },
  cancelEnrollment(id: number) {
    return api.delete(`/v1/enrollments/${id}`)
  },
  getSectionEnrollments(sectionId: number) {
    return api.get(`/v1/enrollments/sections/${sectionId}`)
  },
  updateEnrollment(
    id: number,
    payload: { status?: string; grade?: number | null; processScore?: number | null; examScore?: number | null; overrideReason?: string }
  ) {
    return api.put(`/v1/enrollments/${id}`, payload)
  },
  importSectionGrades(sectionId: number, file: File) {
    const form = new FormData()
    form.append('file', file)
    return api.post(`/v1/enrollments/sections/${sectionId}/grades/import`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
