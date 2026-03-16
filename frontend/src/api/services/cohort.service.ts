import api from '@/api/axios';

export type CohortLifecycleStatus = 'OPEN' | 'CLOSED' | 'CANCELED' | 'MERGED';

export interface CohortTimeSlot {
  id: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}

export interface Cohort {
  id: number;
  name: string;
  code: string;
  maxStudents: number;
  currentStudents: number;
  active: boolean;
  status?: CohortLifecycleStatus;
  enrollmentStartDate?: string | null;
  enrollmentEndDate?: string | null;
  registrationEnabled?: boolean;
  timeSlots?: CohortTimeSlot[];
  clazz?: any;
  semester?: any;
  teacher?: any;
  createdAt?: string;
  updatedAt?: string;
}

export interface CohortParams {
  page?: number;
  size?: number;
  keyword?: string;
  semesterId?: number;
  classId?: number;
  teacherId?: number;
  active?: boolean;
  status?: CohortLifecycleStatus;
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

