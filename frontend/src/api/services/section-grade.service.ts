import api from '@/api/axios'

export interface SectionGradeResponse {
  id: number
  enrollmentId: number
  section: any
  student?: any
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

export const sectionGradeService = {
  getSectionGrades(sectionId: number) {
    return api.get(`/v1/sections/${sectionId}/grades`)
  },
  updateGrade(
    enrollmentId: number,
    payload: { status?: string; grade?: number | null; processScore?: number | null; examScore?: number | null; overrideReason?: string }
  ) {
    return api.put(`/v1/sections/grades/${enrollmentId}`, payload)
  },
  importSectionGrades(sectionId: number, file: File) {
    const form = new FormData()
    form.append('file', file)
    return api.post(`/v1/sections/${sectionId}/grades/import`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  downloadTemplate(sectionId: number) {
    return api.get(`/v1/sections/${sectionId}/grades/template`, { responseType: 'blob' })
  }
}
