<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="classTitle">
        <template #actions>
          <v-btn variant="text" :loading="loading" @click="reload">Tải lại</v-btn>
        </template>
      </PageHeader>

      <v-progress-linear v-if="loading" indeterminate class="mb-4" />

      <v-table>
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

    <!-- Progress Dialog -->
    <v-dialog v-model="showProgressDialog" fullscreen transition="dialog-bottom-transition">
      <v-card>
        <v-toolbar color="primary">
          <v-btn icon="mdi-close" @click="showProgressDialog = false"></v-btn>
          <v-toolbar-title>Tiến độ học tập: {{ selectedStudent?.username }} ({{ selectedStudent?.studentCode }})</v-toolbar-title>
        </v-toolbar>
        <v-card-text class="bg-grey-lighten-4">
          <v-container>
            <StudentProgressView v-if="selectedStudent" :student-id="selectedStudent.id" />
          </v-container>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { unwrapApiResponse } from '@/api/response'
import { studentClassService, type StudentProfile } from '@/api/services/studentClass.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'
import StudentProgressView from '@/views/academic/StudentProgressView.vue'

const uiStore = useUiStore()
const route = useRoute()
const adminClassId = Number(route.params.adminClassId)

const loading = ref(false)
const students = ref<StudentProfile[]>([])
const classTitle = ref(`Danh sách sinh viên (Lớp #${adminClassId})`)

const showProgressDialog = ref(false)
const selectedStudent = ref<StudentProfile | null>(null)

const loadAdminClass = async () => {
  try {
    const res = await studentClassService.getById(adminClassId)
    const c = unwrapApiResponse<any>(res)
    classTitle.value = c?.code && c?.name ? `Danh sách sinh viên - ${c.code} - ${c.name}` : classTitle.value
  } catch {
    classTitle.value = `Danh sách sinh viên (Lớp #${adminClassId})`
  }
}

const reload = async () => {
  loading.value = true
  try {
    const res = await studentClassService.getStudents(adminClassId)
    students.value = unwrapApiResponse<StudentProfile[]>(res) || []
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Không tải được danh sách sinh viên', 'error', 4000)
  } finally {
    loading.value = false
  }
}

const viewProgress = (student: StudentProfile) => {
  selectedStudent.value = student
  showProgressDialog.value = true
}

onMounted(async () => {
  await loadAdminClass()
  await reload()
})
</script>
