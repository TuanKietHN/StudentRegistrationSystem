<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import studentProgressService, { type StudentProgressResponse } from '@/api/services/studentProgress.service'
import PageHeader from '@/components/ui/PageHeader.vue'

const props = defineProps<{
  studentId?: number
}>()

const authStore = useAuthStore()
const progress = ref<StudentProgressResponse | null>(null)
const loading = ref(false)
const tab = ref('all')

const headers = [
  { title: 'Mã HP', key: 'subjectCode', align: 'start' },
  { title: 'Tên học phần', key: 'subjectName', align: 'start' },
  { title: 'Tín chỉ', key: 'credits', align: 'center' },
  { title: 'Điểm hệ 10', key: 'finalScore', align: 'center' },
  { title: 'Điểm hệ 4', key: 'grade4', align: 'center' },
  { title: 'Điểm chữ', key: 'letterGrade', align: 'center' },
  { title: 'Trạng thái', key: 'status', align: 'center' },
]

const targetStudentId = computed(() => {
  return props.studentId || authStore.user?.studentId
})

const passedSubjects = computed(() => {
  return progress.value?.subjects.filter(s => s.status === 'PASSED') || []
})

const notPassedSubjects = computed(() => {
  return progress.value?.subjects.filter(s => s.status !== 'PASSED') || []
})

const fetchProgress = async () => {
  if (!targetStudentId.value) return

  loading.value = true
  try {
    const res = await studentProgressService.getProgress(targetStudentId.value)
    progress.value = res.data
  } catch (error) {
    console.error('Failed to fetch progress:', error)
  } finally {
    loading.value = false
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'PASSED': return 'success'
    case 'NOT_PASSED': return 'error'
    case 'IN_PROGRESS': return 'warning'
    default: return 'grey'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PASSED': return 'Đã qua'
    case 'NOT_PASSED': return 'Chưa qua'
    case 'IN_PROGRESS': return 'Đang học'
    default: return 'Chưa học'
  }
}

onMounted(() => {
  fetchProgress()
})
</script>

<template>
  <div>
    <PageHeader 
      title="Tiến độ học tập" 
      subtitle="Theo dõi kết quả và lộ trình học tập"
    />

    <v-row v-if="loading">
      <v-col cols="12" class="text-center">
        <v-progress-circular indeterminate color="primary"></v-progress-circular>
      </v-col>
    </v-row>

    <div v-else-if="progress">
      <!-- Summary Cards -->
      <v-row>
        <v-col cols="12" md="4">
          <v-card class="h-100" color="primary" variant="tonal">
            <v-card-text class="text-center">
              <div class="text-h6 mb-2">Tiến độ hoàn thành</div>
              <v-progress-circular
                :model-value="progress.progressPercentage"
                :size="100"
                :width="10"
                color="primary"
                class="mb-2"
              >
                {{ progress.progressPercentage }}%
              </v-progress-circular>
              <div class="text-subtitle-2">
                {{ progress.earnedCredits }} / {{ progress.totalCredits }} tín chỉ
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card class="h-100" color="success" variant="tonal">
            <v-card-text class="text-center d-flex flex-column justify-center h-100">
              <div class="text-h6 mb-2">GPA (Hệ 10)</div>
              <div class="text-h2 font-weight-bold mb-2">
                {{ progress.gpa10 }}
              </div>
              <div class="text-subtitle-2">Điểm trung bình tích lũy</div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="4">
          <v-card class="h-100" color="info" variant="tonal">
            <v-card-text class="text-center d-flex flex-column justify-center h-100">
              <div class="text-h6 mb-2">GPA (Hệ 4)</div>
              <div class="text-h2 font-weight-bold mb-2">
                {{ progress.gpa4 }}
              </div>
              <div class="text-subtitle-2">Quy đổi thang điểm 4</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Program Info -->
      <v-card class="mt-4 mb-4">
        <v-card-title>Thông tin chương trình</v-card-title>
        <v-card-text>
          <v-list density="compact">
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-school"></v-icon>
              </template>
              <v-list-item-title>Chương trình đào tạo</v-list-item-title>
              <v-list-item-subtitle>{{ progress.programName }}</v-list-item-subtitle>
            </v-list-item>
            <v-list-item>
              <template v-slot:prepend>
                <v-icon icon="mdi-account"></v-icon>
              </template>
              <v-list-item-title>Sinh viên</v-list-item-title>
              <v-list-item-subtitle>{{ progress.studentName }}</v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>

      <!-- Subject List -->
      <v-card>
        <v-tabs v-model="tab" color="primary">
          <v-tab value="all">Tất cả môn học</v-tab>
          <v-tab value="passed">Đã hoàn thành ({{ passedSubjects.length }})</v-tab>
          <v-tab value="not_passed">Chưa hoàn thành ({{ notPassedSubjects.length }})</v-tab>
        </v-tabs>

        <v-card-text>
          <v-window v-model="tab">
            <v-window-item value="all">
              <v-data-table
                :headers="headers"
                :items="progress.subjects"
                density="comfortable"
                class="elevation-1"
              >
                <template v-slot:item.status="{ item }">
                  <v-chip
                    :color="getStatusColor(item.status)"
                    size="small"
                    class="text-uppercase"
                  >
                    {{ getStatusText(item.status) }}
                  </v-chip>
                </template>
                <template v-slot:item.finalScore="{ item }">
                  <span v-if="item.finalScore !== null" :class="{'font-weight-bold': true, 'text-success': item.status === 'PASSED', 'text-error': item.status === 'NOT_PASSED'}">
                    {{ item.finalScore }}
                  </span>
                  <span v-else class="text-grey">-</span>
                </template>
                <template v-slot:item.letterGrade="{ item }">
                  <v-chip v-if="item.letterGrade" size="x-small" variant="outlined">
                    {{ item.letterGrade }}
                  </v-chip>
                  <span v-else>-</span>
                </template>
              </v-data-table>
            </v-window-item>
            
            <v-window-item value="passed">
               <v-data-table
                :headers="headers"
                :items="passedSubjects"
                density="comfortable"
                class="elevation-1"
              >
                 <template v-slot:item.status="{ item }">
                  <v-chip
                    :color="getStatusColor(item.status)"
                    size="small"
                    class="text-uppercase"
                  >
                    {{ getStatusText(item.status) }}
                  </v-chip>
                </template>
                <template v-slot:item.finalScore="{ item }">
                  <span v-if="item.finalScore !== null" :class="{'font-weight-bold': true, 'text-success': item.status === 'PASSED', 'text-error': item.status === 'NOT_PASSED'}">
                    {{ item.finalScore }}
                  </span>
                  <span v-else class="text-grey">-</span>
                </template>
                 <template v-slot:item.letterGrade="{ item }">
                  <v-chip v-if="item.letterGrade" size="x-small" variant="outlined">
                    {{ item.letterGrade }}
                  </v-chip>
                  <span v-else>-</span>
                </template>
              </v-data-table>
            </v-window-item>

             <v-window-item value="not_passed">
               <v-data-table
                :headers="headers"
                :items="notPassedSubjects"
                density="comfortable"
                class="elevation-1"
              >
                 <template v-slot:item.status="{ item }">
                  <v-chip
                    :color="getStatusColor(item.status)"
                    size="small"
                    class="text-uppercase"
                  >
                    {{ getStatusText(item.status) }}
                  </v-chip>
                </template>
                <template v-slot:item.finalScore="{ item }">
                  <span v-if="item.finalScore !== null" :class="{'font-weight-bold': true, 'text-success': item.status === 'PASSED', 'text-error': item.status === 'NOT_PASSED'}">
                    {{ item.finalScore }}
                  </span>
                  <span v-else class="text-grey">-</span>
                </template>
                 <template v-slot:item.letterGrade="{ item }">
                  <v-chip v-if="item.letterGrade" size="x-small" variant="outlined">
                    {{ item.letterGrade }}
                  </v-chip>
                  <span v-else>-</span>
                </template>
              </v-data-table>
            </v-window-item>
          </v-window>
        </v-card-text>
      </v-card>
    </div>
    
    <v-alert v-else type="info" class="mt-4">
      Không tìm thấy thông tin tiến độ học tập.
    </v-alert>
  </div>
</template>
