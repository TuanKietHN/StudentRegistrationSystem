import api from '@/api/axios';

export type SectionLifecycleStatus = 'OPEN' | 'CLOSED' | 'CANCELED' | 'MERGED';

export interface SectionTimeSlot {
  id: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}

export interface Section {
  id: number;
  name: string;
  code: string;
  maxStudents: number;
  currentStudents: number;
  active: boolean;
  status?: SectionLifecycleStatus;
  enrollmentStartDate?: string | null;
  enrollmentEndDate?: string | null;
  registrationEnabled?: boolean;
  timeSlots?: SectionTimeSlot[];
  subject?: any;
  semester?: any;
  teacher?: any;
  createdAt?: string;
  updatedAt?: string;
}

export interface SectionParams {
  page?: number;
  size?: number;
  keyword?: string;
  semesterId?: number;
  subjectId?: number;
  teacherId?: number;
  active?: boolean;
  status?: SectionLifecycleStatus;
}

export const sectionService = {
  getAll(params?: SectionParams) {
    return api.get('/v1/sections', { params });
  },
  getById(id: number) {
    return api.get(`/v1/sections/${id}`);
  },
  create(payload: any) {
    return api.post('/v1/sections', payload);
  },
  update(id: number, payload: any) {
    return api.put(`/v1/sections/${id}`, payload);
  },
  cancel(id: number, payload?: { reason?: string }) {
    return api.post(`/v1/sections/${id}/cancel`, payload || {});
  },
  merge(payload: { targetSectionId: number; sourceSectionIds: number[]; reason?: string }) {
    return api.post('/v1/sections/merge', payload);
  },
  delete(id: number) {
    return api.delete(`/v1/sections/${id}`);
  },
  getTimeSlots(id: number) {
    return api.get(`/v1/sections/${id}/time-slots`);
  },
  replaceTimeSlots(id: number, payload: Array<{ dayOfWeek: number; startTime: string; endTime: string }>) {
    return api.put(`/v1/sections/${id}/time-slots`, payload);
  }
};

