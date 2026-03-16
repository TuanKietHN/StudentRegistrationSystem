import { defineStore } from 'pinia'
import { unwrapApiResponse, unwrapPageResponse } from '@/api/response'
import { departmentService, type Department } from '@/api/services/department.service'
import { semesterService, type Semester } from '@/api/services/semester.service'
import { subjectService, type Subject } from '@/api/services/subject.service'

type Option = { title: string; value: number }

interface LookupsState {
  semesterOptions: Option[]
  subjectOptions: Option[]
  departmentOptions: Option[]
  activeSemesterId: number | null
  activeSemesterLabel: string
  loaded: {
    semesters: boolean
    subjects: boolean
    departments: boolean
    activeSemester: boolean
  }
}

export const useLookupsStore = defineStore('lookups', {
  state: (): LookupsState => ({
    semesterOptions: [],
    subjectOptions: [],
    departmentOptions: [],
    activeSemesterId: null,
    activeSemesterLabel: '',
    loaded: {
      semesters: false,
      subjects: false,
      departments: false,
      activeSemester: false
    }
  }),
  actions: {
    async loadSemesters(force = false) {
      if (this.loaded.semesters && !force) return
      const res = await semesterService.getAll({ page: 1, size: 500 })
      const page = unwrapPageResponse<Semester>(res)
      this.semesterOptions = (page.data || []).map((s) => ({
        title: `${s.code} - ${s.name}`,
        value: s.id
      }))
      this.loaded.semesters = true
    },

    async loadSubjects(force = false) {
      if (this.loaded.subjects && !force) return
      const res = await subjectService.getAll({ page: 1, size: 500 })
      const page = unwrapPageResponse<Subject>(res)
      this.subjectOptions = (page.data || []).map((s) => ({
        title: `${s.code} - ${s.name}`,
        value: s.id
      }))
      this.loaded.subjects = true
    },

    async loadDepartments(force = false) {
      if (this.loaded.departments && !force) return
      const res = await departmentService.getAll({ page: 1, size: 500 })
      const page = unwrapPageResponse<Department>(res)
      this.departmentOptions = (page.data || []).map((d) => ({
        title: `${d.code} - ${d.name}`,
        value: d.id
      }))
      this.loaded.departments = true
    },

    async loadActiveSemester(force = false) {
      if (this.loaded.activeSemester && !force) return
      try {
        const res = await semesterService.getActive()
        const active = unwrapApiResponse<Semester>(res)
        this.activeSemesterId = active?.id ?? null
        this.activeSemesterLabel = active ? `${active.code} - ${active.name}` : ''
      } catch {
        this.activeSemesterId = null
        this.activeSemesterLabel = ''
      }
      this.loaded.activeSemester = true
    },

    async ensureAcademicLookups() {
      await Promise.all([this.loadSemesters(), this.loadSubjects(), this.loadActiveSemester()])
    },

    reset() {
      this.semesterOptions = []
      this.subjectOptions = []
      this.departmentOptions = []
      this.activeSemesterId = null
      this.activeSemesterLabel = ''
      this.loaded = {
        semesters: false,
        subjects: false,
        departments: false,
        activeSemester: false
      }
    }
  }
})
