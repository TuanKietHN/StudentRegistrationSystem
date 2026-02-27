<template>
  <v-card>
    <v-card-text>
      <PageHeader title="Trang chủ sinh viên" />

      <v-row>
        <v-col cols="12" md="6">
          <v-card variant="outlined">
            <v-card-title class="text-subtitle-1">Lịch học</v-card-title>
            <v-card-text>
              <v-progress-linear v-if="loading" indeterminate class="mb-4" />
              <div
                v-if="!loading && scheduleDays.length === 0"
                class="text-body-2 text-medium-emphasis"
              >
                Chưa có lịch học
              </div>
              <div v-for="d in scheduleDays" :key="d.day" class="mb-4">
                <div class="text-subtitle-2 mb-2">{{ d.label }}</div>
                <v-table density="compact">
                  <thead>
                    <tr>
                      <th>Giờ</th>
                      <th>Lớp</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="it in d.items" :key="it.key">
                      <td style="white-space: nowrap">{{ it.time }}</td>
                      <td>{{ it.title }}</td>
                    </tr>
                  </tbody>
                </v-table>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { unwrapApiResponse } from "@/api/response";
import {
  enrollmentService,
  type Enrollment,
} from "@/api/services/enrollment.service";
import PageHeader from "@/components/ui/PageHeader.vue";

type ScheduleItem = {
  key: string;
  day: number;
  time: string;
  title: string;
  start: string;
};
type ScheduleDay = { day: number; label: string; items: ScheduleItem[] };

const loading = ref(false);
const enrollments = ref<Enrollment[]>([]);

const dayLabel = (day: number) => {
  if (day === 8) return "Chủ nhật";
  return `Thứ ${day}`;
};

const reload = async () => {
  loading.value = true;
  try {
    const res = await enrollmentService.getMyEnrollments();
    enrollments.value = unwrapApiResponse<Enrollment[]>(res) || [];
  } finally {
    loading.value = false;
  }
};

const scheduleDays = computed<ScheduleDay[]>(() => {
  const items: ScheduleItem[] = [];
  for (const e of enrollments.value) {
    if (e.status !== "ENROLLED") continue;
    const slots = e.course?.timeSlots || [];
    for (const s of slots) {
      const time = `${s.startTime.slice(0, 5)}-${s.endTime.slice(0, 5)}`;
      items.push({
        key: `${e.id}-${s.id}`,
        day: s.dayOfWeek,
        time,
        title: `${e.course?.code || ""} - ${e.course?.name || ""}`.trim(),
        start: s.startTime,
      });
    }
  }
  const byDay = new Map<number, ScheduleItem[]>();
  for (const it of items) {
    const arr = byDay.get(it.day) || [];
    arr.push(it);
    byDay.set(it.day, arr);
  }
  return Array.from(byDay.entries())
    .map(([day, arr]) => ({
      day,
      label: dayLabel(day),
      items: arr.sort((a, b) => a.start.localeCompare(b.start)),
    }))
    .sort((a, b) => a.day - b.day);
});

onMounted(() => reload());
</script>
