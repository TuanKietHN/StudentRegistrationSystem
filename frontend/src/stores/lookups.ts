import { defineStore } from 'pinia'
import { unwrapApiResponse, unwrapPageResponse } from '@/api/response'
import { departmentService, type Department } from '@/api/services/department.service'
import { semesterService, type Semester } from '@/api/services/semester.service'
import { classService, type Class } from '@/api/services/class.service'

type Option = { title: string; value: number }

interface LookupsState {
  semesterOptions: Option[]
  classOptions: Option[]
  departmentOptions: Option[]
  activeSemesterId: number | null
  activeSemesterLabel: string
  loaded: {
    semesters: boolean
    classes: boolean
    departments: boolean
    activeSemester: boolean
  }
}

export const useLookupsStore = defineStore('lookups', {
  state: (): LookupsState => ({
    semesterOptions: [],
    classOptions: [],
    departmentOptions: [],
    activeSemesterId: null,
    activeSemesterLabel: '',
    loaded: {
      semesters: false,
      classes: false,
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

    async loadClasses(force = false) {
      if (this.loaded.classes && !force) return
      const res = await classService.getAll({ page: 1, size: 500 })
      const page = unwrapPageResponse<Class>(res)
      this.classOptions = (page.data || []).map((s) => ({
        title: `${s.code} - ${s.name}`,
        value: s.id
      }))
      this.loaded.classes = true
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
      await Promise.all([this.loadSemesters(), this.loadClasses(), this.loadActiveSemester()])
    }
  }
})
