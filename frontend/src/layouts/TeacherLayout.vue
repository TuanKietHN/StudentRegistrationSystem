<template>
  <v-app-bar color="primary" density="comfortable">
    <v-app-bar-title>CMS Academic (Giảng viên)</v-app-bar-title>
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

  <v-navigation-drawer :model-value="drawer" width="280">
    <v-list density="comfortable" nav>
      <v-list-subheader>Chức năng</v-list-subheader>
      <v-list-item title="Trang chủ" :to="{ name: 'TeacherHome' }" prepend-icon="mdi-view-dashboard" />
      <v-list-item title="Lớp tôi dạy" :to="{ name: 'TeacherSections' }" prepend-icon="mdi-google-classroom" />
      <v-list-item title="Lớp hành chính" :to="{ name: 'TeacherAdminClasses' }" prepend-icon="mdi-account-multiple" />
    </v-list>
  </v-navigation-drawer>

  <v-main>
    <v-container class="py-6" fluid>
      <RouterView />
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
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
  get: () => authStore.isLoggedIn,
  set: () => {}
})

const handleLogout = () => {
  authStore.logout()
}

const onSwitchRole = (role: string) => {
  if (role !== 'STUDENT' && role !== 'TEACHER') return
  authStore.setActiveRole(role)
  router.replace({ name: authStore.defaultRouteNameForRole(role) })
}
</script>
