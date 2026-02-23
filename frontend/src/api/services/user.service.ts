import api from '@/api/axios'

export interface UserSummary {
  id: number
  username: string
  email: string
  avatar?: string
  role: string
}

export interface UserParams {
  page?: number
  size?: number
  keyword?: string
  role?: string
}

export const userService = {
  getAll(params?: UserParams) {
    return api.get('/v1/users', { params })
  },
  getById(id: number) {
    return api.get(`/v1/users/${id}`)
  },
  create(payload: any) {
    return api.post('/v1/users', payload)
  },
  update(id: number, payload: any) {
    return api.put(`/v1/users/${id}`, payload)
  },
  delete(id: number) {
    return api.delete(`/v1/users/${id}`)
  }
}
