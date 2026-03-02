import api from '@/api/axios'

export interface AdminClass {
  id: number
  code: string
  name: string
  departmentId?: number | null
  departmentName?: string | null
  intakeYear?: number | null
  program?: string | null
  active: boolean
  createdAt?: string
  updatedAt?: string
}

export interface StudentProfile {
  id: number
  studentCode: string
  user: {
    id: number
    username: string
    email?: string | null
  }
  department?: {
    id: number
    code: string
    name: string
  } | null
  active: boolean
}

export const adminClassService = {
  getAll(params: { keyword?: string; departmentId?: number; intakeYear?: number; active?: boolean; page?: number; size?: number }) {
    return api.get('/v1/admin-classes', { params })
  },
  getStudents(adminClassId: number) {
    return api.get(`/v1/admin-classes/${adminClassId}/students`)
  },
  create(payload: any) {
    return api.post('/v1/admin-classes', payload)
  },
  update(id: number, payload: any) {
    return api.put(`/v1/admin-classes/${id}`, payload)
  },
  delete(id: number) {
    return api.delete(`/v1/admin-classes/${id}`)
  }
}
