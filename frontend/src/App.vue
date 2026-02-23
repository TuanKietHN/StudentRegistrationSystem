<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'
import { useUiStore } from '@/stores/ui'

const authStore = useAuthStore()
const uiStore = useUiStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const currentUser = computed(() => authStore.currentUser)
const isAdmin = computed(() => (authStore.currentUser?.role || '').split(',').includes('ADMIN'))

const drawer = computed({
  get: () => isLoggedIn.value,
  set: () => {}
})

const handleLogout = () => {
  authStore.logout()
}
</script>

<template>
  <v-app>
    <v-app-bar v-if="isLoggedIn" color="primary" density="comfortable">
      <v-app-bar-title>CMS Academic</v-app-bar-title>
      <v-spacer />
      <div class="text-body-2 mr-3">Xin chào, {{ currentUser?.username }}</div>
      <v-btn color="secondary" variant="flat" @click="handleLogout">Đăng xuất</v-btn>
    </v-app-bar>

    <v-navigation-drawer v-if="isLoggedIn" :model-value="drawer" width="280">
      <v-list density="comfortable" nav>
        <v-list-subheader>Chức năng</v-list-subheader>
        <v-list-item title="Trang chủ" to="/" prepend-icon="mdi-view-dashboard" />
        <v-list-item title="Khoa" to="/departments" prepend-icon="mdi-domain" />
        <v-list-item title="Giảng viên" to="/teachers" prepend-icon="mdi-account-tie" />
        <v-list-item title="Học kỳ" to="/semesters" prepend-icon="mdi-calendar-range" />
        <v-list-item title="Môn học" to="/subjects" prepend-icon="mdi-book-open-page-variant" />
        <v-list-item title="Lớp học" to="/courses" prepend-icon="mdi-google-classroom" />
        <v-divider class="my-2" />
        <v-list-subheader>Quản trị</v-list-subheader>
        <v-list-item
          v-if="isAdmin"
          title="Users"
          to="/users"
          prepend-icon="mdi-account-group"
        />
        <v-list-item
          v-if="isAdmin"
          title="Đăng ký tài khoản"
          to="/register"
          prepend-icon="mdi-account-plus"
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
    </v-snackbar>
  </v-app>
</template>
