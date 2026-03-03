import api from '@/api/axios';

export interface Cohort {
  id: number;
  name: string;
  code: string;
  startYear: number;
  endYear: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CohortParams {
  page?: number;
  size?: number;
  keyword?: string;
  startYear?: number;
  endYear?: number;
  active?: boolean;
}

export const cohortService = {
  getAll(params?: CohortParams) {
    return api.get('/v1/cohorts', { params });
  },
  getById(id: number) {
    return api.get(`/v1/cohorts/${id}`);
  },
  create(payload: any) {
    return api.post('/v1/cohorts', payload);
  },
  update(id: number, payload: any) {
    return api.put(`/v1/cohorts/${id}`, payload);
  },
  delete(id: number) {
    return api.delete(`/v1/cohorts/${id}`);
  }
};
