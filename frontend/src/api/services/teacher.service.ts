import api from '@/api/axios';

export interface Teacher {
  id: number;
  userId: number;
  username: string;
  fullName?: string;
  employeeCode: string;
  departmentId?: number;
  departmentName?: string;
  specialization?: string;
  title?: string;
  phone?: string;
  active: boolean;
}

export interface TeacherParams {
  page?: number;
  size?: number;
  keyword?: string;
  departmentId?: number;
  active?: boolean;
}

export const teacherService = {
  getAll(params?: TeacherParams) {
    return api.get('/v1/teachers', { params });
  },

  getById(id: number) {
    return api.get(`/v1/teachers/${id}`);
  },

  create(data: any) {
    return api.post('/v1/teachers', data);
  },

  update(id: number, data: any) {
    return api.put(`/v1/teachers/${id}`, data);
  },

  delete(id: number) {
    return api.delete(`/v1/teachers/${id}`);
  }
};
