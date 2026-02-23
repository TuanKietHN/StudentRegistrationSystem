<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const currentUser = computed(() => authStore.currentUser)

const handleLogout = () => {
  authStore.logout()
}
</script>

<template>
  <v-app>
    <v-app-bar v-if="isLoggedIn" color="primary" density="comfortable">
      <v-app-bar-title>CMS Academic</v-app-bar-title>
      <v-spacer />
      <v-btn to="/" variant="text">Trang chủ</v-btn>
      <v-btn to="/departments" variant="text">Khoa</v-btn>
      <v-btn to="/teachers" variant="text">Giảng viên</v-btn>
      <v-divider vertical class="mx-3" />
      <div class="text-body-2 mr-3">Xin chào, {{ currentUser?.username }}</div>
      <v-btn color="secondary" variant="flat" @click="handleLogout">Đăng xuất</v-btn>
    </v-app-bar>

    <v-main>
      <v-container class="py-6">
        <RouterView />
      </v-container>
    </v-main>
  </v-app>
</template>
