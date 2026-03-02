import api from '@/api/axios'

export interface Subject {
  id: number
  name: string
  code: string
  credit: number
  description?: string
  active: boolean
  processWeight?: number
  examWeight?: number
}

export interface SubjectParams {
  page?: number
  size?: number
  keyword?: string
  active?: boolean
}

export const subjectService = {
  getAll(params?: SubjectParams) {
    return api.get('/v1/subjects', { params })
  },
  getById(id: number) {
    return api.get(`/v1/subjects/${id}`)
  },
  create(payload: any) {
    return api.post('/v1/subjects', payload)
  },
  update(id: number, payload: any) {
    return api.put(`/v1/subjects/${id}`, payload)
  },
  delete(id: number) {
    return api.delete(`/v1/subjects/${id}`)
  }
}
