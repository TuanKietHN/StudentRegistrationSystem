<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="`Bảng điểm lớp: ${studentClass?.name || ''}`">
        <template #actions>
          <v-btn variant="text" @click="router.back()">Quay lại</v-btn>
          <v-btn variant="text" :loading="loading" @click="fetchGrades">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table v-else>
        <thead>
          <tr>
            <th>MSSV</th>
            <th>Họ tên</th>
            <th v-for="subject in allSubjects" :key="subject" class="text-center">
              {{ subject }}
            </th>
            <th class="text-center font-weight-bold">GPA</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in gradeRows" :key="row.studentId">
            <td>{{ row.studentCode }}</td>
            <td>{{ row.studentName }}</td>
            <td v-for="subject in allSubjects" :key="subject" class="text-center">
              {{ row.grades[subject] || '-' }}
            </td>
            <td class="text-center font-weight-bold" :class="getGpaColor(row.gpa)">
              {{ row.gpa?.toFixed(2) || '0.00' }}
            </td>
          </tr>
          <tr v-if="gradeRows.length === 0">
            <td :colspan="allSubjects.length + 3" class="text-center py-6">Không có dữ liệu điểm</td>
          </tr>
        </tbody>
      </v-table>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { unwrapApiResponse } from '@/api/response'
import { studentClassService, type StudentClass } from '@/api/services/studentClass.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const uiStore = useUiStore()
const studentClassId = Number(route.params.adminClassId)

const studentClass = ref<StudentClass | null>(null)
const loading = ref(false)
const rawGrades = ref<any[]>([])

const allSubjects = computed(() => {
  const subjects = new Set<string>()
  rawGrades.value.forEach(student => {
    student.grades?.forEach((g: any) => {
      subjects.add(g.subjectCode)
    })
  })
  return Array.from(subjects).sort()
})

const gradeRows = computed(() => {
  return rawGrades.value.map(student => {
    const gradesMap: Record<string, string> = {}
    student.grades?.forEach((g: any) => {
      gradesMap[g.subjectCode] = g.finalScore !== null ? g.finalScore.toString() : '-'
    })
    return {
      studentId: student.studentId,
      studentCode: student.studentCode,
      studentName: student.studentName,
      grades: gradesMap,
      gpa: student.gpa
    }
  })
})

const getGpaColor = (gpa: number) => {
  if (gpa >= 3.6) return 'text-success font-weight-bold'
  if (gpa >= 3.2) return 'text-primary'
  if (gpa < 2.0) return 'text-error'
  return ''
}

const fetchGrades = async () => {
  loading.value = true
  try {
    const [classRes, gradesRes] = await Promise.all([
      studentClassService.getById(studentClassId),
      studentClassService.getGrades(studentClassId)
    ])
    studentClass.value = unwrapApiResponse<StudentClass>(classRes)
    rawGrades.value = unwrapApiResponse<any[]>(gradesRes)
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Không lấy được bảng điểm', 'error')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchGrades()
})
</script>
