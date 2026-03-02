import api from '@/api/axios';

export interface Class {
  id: number;
  name: string;
  code: string;
  credit: number;
  description?: string;
  active: boolean;
  processWeight?: number;
  examWeight?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ClassParams {
  page?: number;
  size?: number;
  keyword?: string;
  active?: boolean;
}

export const classService = {
  getAll(params?: ClassParams) {
    return api.get('/v1/classes', { params });
  },
  getById(id: number) {
    return api.get(`/v1/classes/${id}`);
  },
  create(payload: any) {
    return api.post('/v1/classes', payload);
  },
  update(id: number, payload: any) {
    return api.put(`/v1/classes/${id}`, payload);
  },
  delete(id: number) {
    return api.delete(`/v1/classes/${id}`);
  }
};

