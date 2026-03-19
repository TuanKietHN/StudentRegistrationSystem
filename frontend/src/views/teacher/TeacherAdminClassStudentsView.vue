<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="`Danh sách sinh viên lớp: ${studentClass?.name || ''}`" back-to="/teacher/admin-classes">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table v-else>
        <thead>
        <tr>
          <th>MSSV</th>
          <th>Tài khoản</th>
          <th>Email</th>
          <th>Khoa</th>
          <th>SĐT</th>
          <th>Trạng thái</th>
          <th class="text-center">Hành động</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="s in students" :key="s.id">
          <td>{{ s.studentCode }}</td>
          <td>{{ s.username || '-' }}</td>
          <td>{{ s.email || '-' }}</td>
          <td>{{ s.departmentName || '-' }}</td>
          <td>{{ s.phone || '-' }}</td>
          <td>
            <v-chip :color="s.active ? 'green' : 'grey'" variant="tonal" size="small">{{ s.active ? 'Đang học' : 'Ngưng' }}</v-chip>
          </td>
          <td class="text-center">
            <v-btn
                color="primary"
                variant="text"
                size="small"
                prepend-icon="mdi-chart-bar"
                @click="viewProgress(s)"
            >
              Tiến độ
            </v-btn>
          </td>
        </tr>
        <tr v-if="students.length === 0">
          <td colspan="7" class="text-center py-6">Không có dữ liệu</td>
        </tr>
        </tbody>
      </v-table>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { unwrapApiResponse } from '@/api/response'
import { studentClassService, type StudentProfile } from '@/api/services/studentClass.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUiStore()
const route = useRoute()
const router = useRouter()
const adminClassId = Number(route.params.adminClassId)

// Define local type for StudentClass since we only need basic fields
interface StudentClass {
  id: number
  code: string
  name: string
}

const loading = ref(false)
const students = ref<StudentProfile[]>([])
const studentClass = ref<StudentClass | null>(null)

const reload = async () => {
  loading.value = true
  try {
    const [classRes, studentsRes] = await Promise.all([
      studentClassService.getById(adminClassId),
      studentClassService.getStudents(adminClassId)
    ])
    // @ts-ignore
    studentClass.value = unwrapApiResponse<StudentClass>(classRes)
    // @ts-ignore
    students.value = unwrapApiResponse<StudentProfile[]>(studentsRes) || []
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Không tải được danh sách sinh viên', 'error', 4000)
  } finally {
    loading.value = false
  }
}

const viewProgress = (student: StudentProfile) => {
  router.push({
    name: 'TeacherStudentProgress',
    params: { studentId: student.id },
    query: { backTo: `/teacher/admin-classes/${adminClassId}/students` }
  })
}

onMounted(async () => {
  await reload()
})
</script>