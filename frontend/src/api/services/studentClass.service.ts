import api from '@/api/axios'

export interface StudentClass {
    id: number
    code: string
    name: string
    departmentId?: number | null
    departmentName?: string | null
    cohortId?: number | null
    cohortCode?: string | null
    cohortName?: string | null
    advisorTeacherId?: number | null
    intakeYear?: number | null
    program?: string | null
    active: boolean
    createdAt?: string
    updatedAt?: string
}

export interface StudentProfile {
    id: number
    studentCode: string
    userId?: number | null
    username?: string | null
    email?: string | null
    departmentId?: number | null
    departmentName?: string | null
    phone?: string | null
    active: boolean
}

export const studentClassService = {
    getAll(params: { keyword?: string; departmentId?: number; cohortId?: number; active?: boolean; page?: number; size?: number }) {
        return api.get('/v1/student-classes', { params })
    },
    getById(id: number) {
        return api.get(`/v1/student-classes/${id}`)
    },
    getStudents(studentClassId: number) {
        return api.get(`/v1/student-classes/${studentClassId}/students`)
    },
    create(payload: any) {
        return api.post('/v1/student-classes', payload)
    },
    update(id: number, payload: any) {
        return api.put(`/v1/student-classes/${id}`, payload)
    },
    delete(id: number) {
        return api.delete(`/v1/student-classes/${id}`)
    }
}
