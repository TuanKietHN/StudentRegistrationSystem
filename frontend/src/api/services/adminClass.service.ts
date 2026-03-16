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
  userId?: number | null
  username?: string | null
  email?: string | null
  departmentId?: number | null
  departmentName?: string | null
  phone?: string | null
  active: boolean
}

export const adminClassService = {
  getAll(params: { keyword?: string; departmentId?: number; intakeYear?: number; active?: boolean; page?: number; size?: number }) {
    return api.get('/v1/admin-classes', { params })
  },
  getById(id: number) {
    return api.get(`/v1/admin-classes/${id}`)
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
