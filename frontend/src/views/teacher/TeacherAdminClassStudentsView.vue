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
          </tr>
          <tr v-if="students.length === 0">
            <td colspan="6" class="text-center py-6">Không có dữ liệu</td>
          </tr>
        </tbody>
      </v-table>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { unwrapApiResponse } from '@/api/response'
import { adminClassService, type StudentProfile } from '@/api/services/adminClass.service'
import { useUiStore } from '@/stores/ui'
import PageHeader from '@/components/ui/PageHeader.vue'

const uiStore = useUiStore()
const route = useRoute()
const adminClassId = Number(route.params.adminClassId)

const loading = ref(false)
const students = ref<StudentProfile[]>([])
const classTitle = ref(`Danh sách sinh viên (Lớp #${adminClassId})`)

const loadAdminClass = async () => {
  try {
    const res = await adminClassService.getById(adminClassId)
    const c = unwrapApiResponse<any>(res)
    classTitle.value = c?.code && c?.name ? `Danh sách sinh viên - ${c.code} - ${c.name}` : classTitle.value
  } catch {
    classTitle.value = `Danh sách sinh viên (Lớp #${adminClassId})`
  }
}

const reload = async () => {
  loading.value = true
  try {
    const res = await adminClassService.getStudents(adminClassId)
    students.value = unwrapApiResponse<StudentProfile[]>(res) || []
  } catch (err: any) {
    uiStore.notify(err?.response?.data?.message || 'Không tải được danh sách sinh viên', 'error', 4000)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadAdminClass()
  await reload()
})
</script>
