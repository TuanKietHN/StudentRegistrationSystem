<template>
  <v-app-bar color="primary" density="comfortable">
    <v-app-bar-title>CMS Academic (Admin)</v-app-bar-title>
    <v-spacer />
    <div class="text-body-2 mr-3">Xin chào, {{ currentUser?.username }}</div>
    <v-btn color="secondary" variant="flat" @click="handleLogout">Đăng xuất</v-btn>
  </v-app-bar>

  <v-navigation-drawer :model-value="drawer" width="280">
    <v-list density="comfortable" nav>
      <v-list-subheader>Quản trị</v-list-subheader>
      <v-list-item title="Tổng quan" :to="{ name: 'AdminHome' }" prepend-icon="mdi-view-dashboard" />
      <v-list-item title="Users" :to="{ name: 'AdminUsers' }" prepend-icon="mdi-account-group" />
      <v-list-item title="Khoa" :to="{ name: 'AdminDepartments' }" prepend-icon="mdi-domain" />
      <v-list-item title="Giảng viên" :to="{ name: 'AdminTeachers' }" prepend-icon="mdi-account-tie" />
      <v-list-item title="Học kỳ" :to="{ name: 'AdminSemesters' }" prepend-icon="mdi-calendar-range" />
      <v-list-item title="Môn học" :to="{ name: 'AdminSubjects' }" prepend-icon="mdi-book-open-page-variant" />
      <v-list-item title="Lớp học" :to="{ name: 'AdminCourses' }" prepend-icon="mdi-google-classroom" />
    </v-list>
  </v-navigation-drawer>

  <v-main>
    <v-container class="py-6" fluid>
      <RouterView />
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)

const drawer = computed({
  get: () => authStore.isLoggedIn,
  set: () => {}
})

const handleLogout = () => {
  authStore.logout()
}
</script>
