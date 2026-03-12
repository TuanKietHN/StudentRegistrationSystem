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
const tab = ref('program')
const panels = ref<number[]>([0])

const groupedSubjects = computed(() => {
  if (!progress.value) return {}
  const groups: Record<number | string, any[]> = {}
  
  progress.value.subjects.forEach(subject => {
    const sem = subject.semester || 'extra'
    if (!groups[sem]) groups[sem] = []
    groups[sem].push(subject)
  })
  
  return groups
})

const sortedSemesters = computed(() => {
  const keys = Object.keys(groupedSubjects.value)
    .filter(k => k !== 'extra')
    .map(Number)
    .sort((a, b) => a - b)
  
  const result: (number | string)[] = [...keys]
  if (groupedSubjects.value['extra']) {
    result.push('extra')
  }
  return result
})

const getSemesterTitle = (sem: number | string) => {
  if (sem === 'extra') return 'Môn học ngoài chương trình'
  return `Học kỳ ${sem}`
}

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
    case 'NOT_STARTED': return 'grey-lighten-1'
    default: return 'grey'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PASSED': return 'Đã đạt'
    case 'NOT_PASSED': return 'Chưa đạt'
    case 'IN_PROGRESS': return 'Đang học'
    case 'NOT_STARTED': return 'Chưa học'
    default: return status
  }
}

onMounted(() => {
  fetchProgress()
})
</script>

<template>
  <div>
    <PageHeader 
      title="Chương trình đào tạo & Tiến độ" 
      subtitle="Theo dõi lộ trình học tập và kết quả theo học kỳ"
    />

    <v-row v-if="loading">
      <v-col cols="12" class="text-center py-10">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
        <div class="mt-4 text-grey">Đang tải dữ liệu tiến độ...</div>
      </v-col>
    </v-row>

    <div v-else-if="progress">
      <!-- Summary Cards -->
      <v-row>
        <v-col cols="12" md="4">
          <v-card class="h-100" color="primary" variant="tonal" border>
            <v-card-text class="text-center">
              <div class="text-overline mb-1">Tiến độ hoàn thành</div>
              <v-progress-circular
                :model-value="progress.progressPercentage"
                :size="110"
                :width="10"
                color="primary"
                class="mb-3"
              >
                <span class="text-h6 font-weight-bold">{{ progress.progressPercentage }}%</span>
              </v-progress-circular>
              <div class="text-subtitle-1">
                {{ progress.earnedCredits }} / {{ progress.totalCredits }} tín chỉ
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" md="4">
          <v-card class="h-100" color="success" variant="tonal" border>
            <v-card-text class="text-center d-flex flex-column justify-center h-100">
              <div class="text-overline mb-1">GPA (Hệ 10)</div>
              <div class="text-h2 font-weight-bold mb-2">
                {{ progress.gpa10 }}
              </div>
              <div class="text-caption text-uppercase font-weight-bold">Trung bình tích lũy</div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" md="4">
          <v-card class="h-100" color="info" variant="tonal" border>
            <v-card-text class="text-center d-flex flex-column justify-center h-100">
              <div class="text-overline mb-1">GPA (Hệ 4)</div>
              <div class="text-h2 font-weight-bold mb-2">
                {{ progress.gpa4 }}
              </div>
              <div class="text-caption text-uppercase font-weight-bold">Thang điểm 4</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Program Info -->
      <v-alert
        icon="mdi-school"
        variant="tonal"
        color="primary"
        class="mt-6 mb-4"
        :title="progress.programName"
      >
        Sinh viên: <strong>{{ progress.studentName }}</strong>
      </v-alert>

      <!-- Main Content -->
      <v-card class="mt-4">
        <v-tabs v-model="tab" color="primary">
          <v-tab value="program">
             <v-icon start icon="mdi-format-list-bulleted-type"></v-icon>
             Khung chương trình
          </v-tab>
          <v-tab value="all">
             <v-icon start icon="mdi-table"></v-icon>
             Bảng điểm chi tiết
          </v-tab>
        </v-tabs>

        <v-divider></v-divider>

        <v-card-text class="pa-0">
          <v-window v-model="tab">
            <!-- Grouped by Semester View -->
            <v-window-item value="program" class="pa-4">
              <v-expansion-panels v-model="panels" multiple variant="accordion">
                <v-expansion-panel
                  v-for="sem in sortedSemesters"
                  :key="sem"
                  elevation="0"
                  class="border-b"
                >
                  <v-expansion-panel-title>
                    <div class="d-flex align-center w-100">
                      <v-icon :icon="sem === 'extra' ? 'mdi-plus-circle-outline' : 'mdi-calendar-check'" 
                              :color="sem === 'extra' ? 'info' : 'primary'" class="mr-3"></v-icon>
                      <span class="text-subtitle-1 font-weight-bold">{{ getSemesterTitle(sem) }}</span>
                      <v-spacer></v-spacer>
                      <v-chip size="x-small" variant="flat" class="mr-2">
                        {{ groupedSubjects[sem].length }} môn
                      </v-chip>
                    </div>
                  </v-expansion-panel-title>
                  <v-expansion-panel-text class="pa-0">
                    <v-list lines="two" density="comfortable" class="pa-0">
                      <v-list-item
                        v-for="sub in groupedSubjects[sem]"
                        :key="sub.subjectCode"
                        class="border-b"
                      >
                        <template v-slot:prepend>
                          <v-avatar :color="getStatusColor(sub.status)" size="12" class="mr-2"></v-avatar>
                        </template>

                        <v-list-item-title class="font-weight-medium">
                          {{ sub.subjectCode }} - {{ sub.subjectName }}
                        </v-list-item-title>
                        
                        <v-list-item-subtitle>
                          {{ sub.credits }} tín chỉ • {{ sub.type === 'COMPULSORY' ? 'Bắt buộc' : 'Tự chọn' }}
                        </v-list-item-subtitle>

                        <template v-slot:append>
                          <div class="text-right">
                            <div v-if="sub.status !== 'NOT_STARTED' && sub.finalScore !== null" class="d-flex align-center justify-end">
                              <div class="mr-4">
                                <div class="text-caption text-grey">Điểm</div>
                                <div class="font-weight-bold" :class="sub.status === 'PASSED' ? 'text-success' : 'text-error'">
                                  {{ sub.finalScore }} / {{ sub.letterGrade }}
                                </div>
                              </div>
                              <v-chip :color="getStatusColor(sub.status)" size="small" variant="tonal">
                                {{ getStatusText(sub.status) }}
                              </v-chip>
                            </div>
                            <v-chip v-else :color="getStatusColor(sub.status)" size="small" variant="outlined">
                              {{ getStatusText(sub.status) }}
                            </v-chip>
                          </div>
                        </template>
                      </v-list-item>
                    </v-list>
                  </v-expansion-panel-text>
                </v-expansion-panel>
              </v-expansion-panels>
            </v-window-item>

            <!-- Flat List Table View -->
            <v-window-item value="all">
              <v-data-table
                :headers="headers"
                :items="progress.subjects"
                density="comfortable"
                class="elevation-0"
              >
                <template v-slot:item.status="{ item }">
                  <v-chip
                    :color="getStatusColor(item.status)"
                    size="small"
                    variant="tonal"
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
              </v-data-table>
            </v-window-item>
          </v-window>
        </v-card-text>
      </v-card>
    </div>
    
    <v-alert v-else type="info" variant="tonal" class="mt-4" icon="mdi-information-outline">
      Không tìm thấy thông tin tiến trình học tập của sinh viên này. 
      Vui lòng kiểm tra lại cấu trình đào tạo của lớp học tương ứng.
    </v-alert>
  </div>
</template>
