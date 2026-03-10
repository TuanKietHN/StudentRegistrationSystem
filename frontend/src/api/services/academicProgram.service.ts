import axios from '../axios'
import type { ApiResponse } from '../response'

export interface DepartmentResponse {
  id: number
  code: string
  name: string
  description: string
  parentId: number | null
  parentName: string | null
  headTeacherId: number | null
  headTeacherName: string | null
  active: boolean
}

export interface AcademicProgramResponse {
  id: number
  code: string
  name: string
  department: DepartmentResponse
  totalCredits: number
  description: string
  active: boolean
}

export interface AcademicProgramCreateRequest {
  code: string
  name: string
  departmentId: number
  totalCredits: number
  description?: string
}

export interface AcademicProgramUpdateRequest {
  name?: string
  departmentId?: number
  totalCredits?: number
  description?: string
  active?: boolean
}

export interface ProgramSubjectRequest {
  subjectId: number
  semester: number
  subjectType: string // COMPULSORY, ELECTIVE
  passScore?: number
}

export interface ProgramSubjectResponse {
  id: number
  programId: number
  subject: any // SubjectResponse
  semester: number
  subjectType: string
  passScore: number
}

const academicProgramService = {
  getAll: async (): Promise<ApiResponse<AcademicProgramResponse[]>> => {
    const response = await axios.get('/api/v1/academic-programs')
    return response.data
  },

  getById: async (id: number): Promise<ApiResponse<AcademicProgramResponse>> => {
    const response = await axios.get(`/api/v1/academic-programs/${id}`)
    return response.data
  },

  create: async (data: AcademicProgramCreateRequest): Promise<ApiResponse<AcademicProgramResponse>> => {
    const response = await axios.post('/api/v1/academic-programs', data)
    return response.data
  },

  update: async (id: number, data: AcademicProgramUpdateRequest): Promise<ApiResponse<AcademicProgramResponse>> => {
    const response = await axios.put(`/api/v1/academic-programs/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<void> => {
    await axios.delete(`/api/v1/academic-programs/${id}`)
  },

  addSubject: async (programId: number, data: ProgramSubjectRequest): Promise<ApiResponse<ProgramSubjectResponse>> => {
    const response = await axios.post(`/api/v1/academic-programs/${programId}/subjects`, data)
    return response.data
  },

  removeSubject: async (programSubjectId: number): Promise<void> => {
    await axios.delete(`/api/v1/academic-programs/subjects/${programSubjectId}`)
  },

  getSubjects: async (programId: number): Promise<ApiResponse<ProgramSubjectResponse[]>> => {
    const response = await axios.get(`/api/v1/academic-programs/${programId}/subjects`)
    return response.data
  }
}

export default academicProgramService
