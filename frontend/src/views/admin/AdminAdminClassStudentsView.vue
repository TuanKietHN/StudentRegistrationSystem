<template>
  <v-card>
    <v-card-text>
      <PageHeader :title="`Danh sách sinh viên (Lớp #${adminClassId})`">
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
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in students" :key="s.id">
            <td>{{ s.studentCode }}</td>
            <td>{{ s.user?.username }}</td>
            <td>{{ s.user?.email || '-' }}</td>
            <td>{{ s.department?.code || '-' }}</td>
            <td>
              <v-chip :color="s.active ? 'green' : 'grey'" variant="tonal" size="small">{{ s.active ? 'ACTIVE' : 'INACTIVE' }}</v-chip>
            </td>
          </tr>
          <tr v-if="students.length === 0">
            <td colspan="5" class="text-center py-6">Không có dữ liệu</td>
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
import PageHeader from '@/components/ui/PageHeader.vue'

const route = useRoute()
const adminClassId = Number(route.params.adminClassId)

const loading = ref(false)
const students = ref<StudentProfile[]>([])

const reload = async () => {
  loading.value = true
  try {
    const res = await adminClassService.getStudents(adminClassId)
    students.value = unwrapApiResponse<StudentProfile[]>(res) || []
  } finally {
    loading.value = false
  }
}

onMounted(() => reload())
</script>

