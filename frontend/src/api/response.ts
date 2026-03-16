import type { AxiosResponse } from 'axios'

export interface ApiResponse<T> {
  status: number
  message: string
  data: T
}

export interface PageResponse<T> {
  page: number
  size: number
  totalElements: number
  totalPages: number
  data: T[]
}

export function unwrapApiResponse<T>(response: AxiosResponse<ApiResponse<T>>): T {
  return response.data.data
}

export function unwrapPageResponse<T>(
  response: AxiosResponse<ApiResponse<PageResponse<T>>>
): PageResponse<T> {
  return response.data.data
}
