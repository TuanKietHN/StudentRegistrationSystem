<template>
  <v-app-bar class="admin-app-bar" color="surface" density="comfortable" elevation="0" height="64">
    <v-btn icon="mdi-menu" variant="text" class="d-md-none" @click="drawerOpen = !drawerOpen" />

    <div class="d-flex align-center ga-2">
      <v-icon icon="mdi-home-outline" size="18" color="secondary" />
      <div class="text-body-2 text-secondary">/</div>
      <div class="text-body-2 font-weight-semibold text-secondary">{{ currentSectionTitle }}</div>
    </div>

    <v-spacer />

    <v-select
      v-if="isDualRole"
      :model-value="activeRole"
      :items="roleOptions"
      item-title="title"
      item-value="value"
      hide-details
      density="compact"
      variant="solo-filled"
      bg-color="#F7F6F8"
      class="admin-input mr-2"
      style="max-width: 160px"
      @update:model-value="onSwitchRole"
    />

    <v-btn icon="mdi-bell-outline" variant="text" />
    <v-btn icon="mdi-logout" variant="text" @click="handleLogout" />
  </v-app-bar>

  <v-navigation-drawer
    v-model="drawerOpen"
    class="admin-drawer"
    color="surface"
    width="280"
    :temporary="isMobile"
  >
    <div class="pa-6 d-flex align-center ga-3">
      <div class="admin-brand-icon d-flex align-center justify-center">
        <v-icon icon="mdi-school-outline" color="primary" size="20" />
      </div>
      <div>
        <div class="text-body-2 font-weight-bold text-secondary">LMS</div>
        <div class="text-caption" style="color: #64748b">Sinh viên</div>
      </div>
    </div>

    <v-list nav density="comfortable" class="px-4">
      <v-list-item
        v-for="item in navItems"
        :key="item.name"
        :to="{ name: item.name }"
        :title="item.title"
        :prepend-icon="item.icon"
        rounded="lg"
        class="admin-nav-item"
        active-class="admin-nav-item--active"
      />
    </v-list>

    <div class="mt-auto">
      <v-divider class="mx-4 my-4" />
      <div class="px-4 pb-4">
        <div class="d-flex align-center ga-3 pa-2">
          <v-avatar size="32" color="#E2E8F0">
            <v-icon icon="mdi-account" color="secondary" size="18" />
          </v-avatar>
          <div class="flex-1">
            <div class="text-body-2 font-weight-semibold text-secondary">
              {{ currentUser?.username || 'Tài khoản' }}
            </div>
            <div class="text-caption" style="color: #64748b">{{ activeRoleLabel }}</div>
          </div>
        </div>
      </div>
    </div>
  </v-navigation-drawer>

  <v-main class="admin-layout">
    <v-container class="py-6 px-6" fluid>
      <RouterView />
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import { RouterView, useRoute, useRouter } from 'vue-router'
import { computed, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useDisplay } from 'vuetify'

const router = useRouter()
const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)
const activeRole = computed(() => authStore.activeRole)
const isDualRole = computed(() => authStore.isDualRole)

const drawerOpen = ref(true)
const { mdAndDown } = useDisplay()
const isMobile = computed(() => mdAndDown.value)

const navItems = [
  { name: 'StudentHome', title: 'Bảng điều khiển', icon: 'mdi-view-dashboard-outline' },
  { name: 'StudentSectionRegistration', title: 'Đăng ký lớp', icon: 'mdi-google-classroom' },
  { name: 'StudentMyEnrollments', title: 'Lớp đã đăng ký', icon: 'mdi-clipboard-text-outline' }
]

const route = useRoute()
const currentSectionTitle = computed(() => {
  const found = navItems.find((i) => i.name === route.name)
  return found?.title || 'Sinh viên'
})

const roleOptions = computed(() => {
  const roles = authStore.roles
  return [
    roles.includes('STUDENT') ? { title: 'Sinh viên', value: 'STUDENT' } : null,
    roles.includes('TEACHER') ? { title: 'Giảng viên', value: 'TEACHER' } : null
  ].filter(Boolean) as Array<{ title: string; value: 'STUDENT' | 'TEACHER' }>
})

const activeRoleLabel = computed(() => {
  if (activeRole.value === 'TEACHER') return 'Giảng viên'
  if (activeRole.value === 'STUDENT') return 'Sinh viên'
  return ''
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
