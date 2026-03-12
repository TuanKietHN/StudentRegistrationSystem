import axios from '../axios'
import type { ApiResponse } from '../response'

export interface SubjectProgressDTO {
  subjectCode: string
  subjectName: string
  credits: number
  finalScore: number | null
  letterGrade: string | null
  grade4: number | null
  status: 'PASSED' | 'NOT_PASSED' | 'NOT_STARTED' | 'IN_PROGRESS'
  type: string
  semester: number | null
  isExtra: boolean
}

export interface StudentProgressResponse {
  studentId: number
  studentName: string
  programName: string
  progressPercentage: number
  totalCredits: number
  earnedCredits: number
  gpa10: number
  gpa4: number
  subjects: SubjectProgressDTO[]
}

const studentProgressService = {
  getProgress: async (studentId: number): Promise<ApiResponse<StudentProgressResponse>> => {
    const response = await axios.get(`/v1/student-progress/${studentId}`)
    return response.data
  }
}

export default studentProgressService
