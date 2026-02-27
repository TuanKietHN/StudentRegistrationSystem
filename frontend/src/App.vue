<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { computed, onMounted, onUnmounted } from 'vue'
import { useUiStore } from '@/stores/ui'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const uiStore = useUiStore()
const router = useRouter()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const currentUser = computed(() => authStore.currentUser)
const activeRole = computed(() => authStore.activeRole)
const isDualRole = computed(() => authStore.isDualRole)

const roleOptions = computed(() => {
  const roles = authStore.roles
  return [
    roles.includes('STUDENT') ? { title: 'Sinh viên', value: 'STUDENT' } : null,
    roles.includes('TEACHER') ? { title: 'Giảng viên', value: 'TEACHER' } : null
  ].filter(Boolean) as Array<{ title: string; value: 'STUDENT' | 'TEACHER' }>
})

const drawer = computed({
  get: () => isLoggedIn.value,
  set: () => {}
})

const handleLogout = () => {
  authStore.logout()
}

const onApiNotify = (event: Event) => {
  const detail = (event as CustomEvent<{ text: string; color?: any; timeout?: number }>).detail
  if (!detail?.text) return
  uiStore.notify(detail.text, detail.color, detail.timeout)
}

const onSwitchRole = (role: string) => {
  if (role !== 'STUDENT' && role !== 'TEACHER') return
  authStore.setActiveRole(role)
  router.replace({ name: authStore.defaultRouteNameForRole(role) })
}

onMounted(() => {
  window.addEventListener('api:notify', onApiNotify as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('api:notify', onApiNotify as EventListener)
})
</script>

<template>
  <v-app>
    <v-app-bar v-if="isLoggedIn" color="primary" density="comfortable">
      <v-app-bar-title>CMS Academic</v-app-bar-title>
      <v-spacer />
      <v-select
        v-if="isDualRole"
        :model-value="activeRole"
        :items="roleOptions"
        item-title="title"
        item-value="value"
        hide-details
        density="compact"
        variant="solo"
        class="mr-3"
        style="max-width: 160px"
        @update:model-value="onSwitchRole"
      />
      <div class="text-body-2 mr-3">Xin chào, {{ currentUser?.username }}</div>
      <v-btn color="secondary" variant="flat" @click="handleLogout">Đăng xuất</v-btn>
    </v-app-bar>

    <v-navigation-drawer v-if="isLoggedIn" :model-value="drawer" width="280">
      <v-list density="comfortable" nav>
        <v-list-subheader>Chức năng</v-list-subheader>
        <v-list-item title="Trang chủ" :to="{ name: 'Home' }" prepend-icon="mdi-view-dashboard" />

        <template v-if="activeRole === 'STUDENT'">
          <v-list-item title="Đăng ký lớp" :to="{ name: 'StudentCourseRegistration' }" prepend-icon="mdi-google-classroom" />
          <v-list-item title="Lớp đã đăng ký" :to="{ name: 'StudentMyEnrollments' }" prepend-icon="mdi-clipboard-text" />
        </template>

        <template v-if="activeRole === 'TEACHER'">
          <v-list-item title="Giảng dạy" :to="{ name: 'TeacherHome' }" prepend-icon="mdi-teach" />
          <v-list-item title="Lớp tôi dạy" :to="{ name: 'TeacherCourses' }" prepend-icon="mdi-google-classroom" />
        </template>

        <v-divider class="my-2" />
        <v-list-subheader>Quản trị</v-list-subheader>
        <v-list-item
          v-if="activeRole === 'ADMIN'"
          title="Users"
          :to="{ name: 'AdminUsers' }"
          prepend-icon="mdi-account-group"
        />
        <v-list-item
          v-if="activeRole === 'ADMIN'"
          title="Khoa"
          :to="{ name: 'AdminDepartments' }"
          prepend-icon="mdi-domain"
        />
        <v-list-item
          v-if="activeRole === 'ADMIN'"
          title="Giảng viên"
          :to="{ name: 'AdminTeachers' }"
          prepend-icon="mdi-account-tie"
        />
        <v-list-item
          v-if="activeRole === 'ADMIN'"
          title="Học kỳ"
          :to="{ name: 'AdminSemesters' }"
          prepend-icon="mdi-calendar-range"
        />
        <v-list-item
          v-if="activeRole === 'ADMIN'"
          title="Môn học"
          :to="{ name: 'AdminSubjects' }"
          prepend-icon="mdi-book-open-page-variant"
        />
        <v-list-item
          v-if="activeRole === 'ADMIN'"
          title="Lớp học"
          :to="{ name: 'AdminCourses' }"
          prepend-icon="mdi-google-classroom"
        />
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container class="py-6" fluid>
        <RouterView />
      </v-container>
    </v-main>

    <v-snackbar
      v-model="uiStore.snackbar.open"
      :color="uiStore.snackbar.color"
      :timeout="uiStore.snackbar.timeout"
      location="bottom right"
    >
      {{ uiStore.snackbar.text }}
      <template #actions>
        <v-btn icon="mdi-close" variant="text" @click="uiStore.closeSnackbar" />
      </template>
    </v-snackbar>
  </v-app>
</template>
