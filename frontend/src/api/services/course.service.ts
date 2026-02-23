import api from '@/api/axios'

export interface Course {
  id: number
  name: string
  code: string
  maxStudents: number
  active: boolean
  subject?: any
  semester?: any
  teacher?: any
}

export interface CourseParams {
  page?: number
  size?: number
  keyword?: string
  semesterId?: number
  subjectId?: number
  teacherId?: number
  active?: boolean
}

export const courseService = {
  getAll(params?: CourseParams) {
    return api.get('/v1/courses', { params })
  },
  getById(id: number) {
    return api.get(`/v1/courses/${id}`)
  },
  create(payload: any) {
    return api.post('/v1/courses', payload)
  },
  update(id: number, payload: any) {
    return api.put(`/v1/courses/${id}`, payload)
  },
  delete(id: number) {
    return api.delete(`/v1/courses/${id}`)
  }
}

