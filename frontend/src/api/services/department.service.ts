import api from '@/api/axios';

export interface Department {
  id: number;
  code: string;
  name: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  headTeacherId?: number;
  headTeacherName?: string;
  active: boolean;
}

export interface DepartmentParams {
  page?: number;
  size?: number;
  keyword?: string;
  active?: boolean;
}

export const departmentService = {
  getAll(params?: DepartmentParams) {
    return api.get('/v1/departments', { params });
  },

  getById(id: number) {
    return api.get(`/v1/departments/${id}`);
  },

  create(data: any) {
    return api.post('/v1/departments', data);
  },

  update(id: number, data: any) {
    return api.put(`/v1/departments/${id}`, data);
  },

  delete(id: number) {
    return api.delete(`/v1/departments/${id}`);
  }
};
