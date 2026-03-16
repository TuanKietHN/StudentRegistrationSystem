import api from '@/api/axios'

export interface Semester {
  id: number
  name: string
  code: string
  startDate: string
  endDate: string
  active: boolean
  secondaryActive?: boolean
}

export interface SemesterParams {
  page?: number
  size?: number
  keyword?: string
  active?: boolean
}

export const semesterService = {
  getAll(params?: SemesterParams) {
    return api.get('/v1/semesters', { params })
  },
  getById(id: number) {
    return api.get(`/v1/semesters/${id}`)
  },
  getActive() {
    return api.get('/v1/semesters/active')
  },
  getActiveSecondary() {
    return api.get('/v1/semesters/active-secondary')
  },
  create(payload: any) {
    return api.post('/v1/semesters', payload)
  },
  update(id: number, payload: any) {
    return api.put(`/v1/semesters/${id}`, payload)
  },
  delete(id: number) {
    return api.delete(`/v1/semesters/${id}`)
  }
}
