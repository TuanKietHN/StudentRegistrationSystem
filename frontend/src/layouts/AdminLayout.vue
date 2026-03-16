<template>
  <v-app-bar class="admin-app-bar" color="surface" density="comfortable" elevation="0" height="64">
    <v-btn icon="mdi-menu" variant="text" class="d-md-none" @click="drawerOpen = !drawerOpen" />

    <div class="d-flex align-center ga-2">
      <v-icon icon="mdi-home-outline" size="18" color="secondary" />
      <div class="text-body-2 text-secondary">/</div>
      <div class="text-body-2 font-weight-semibold text-secondary">{{ currentSectionTitle }}</div>
    </div>

    <v-spacer />

    <v-btn icon="mdi-bell-outline" variant="text" />

    <v-menu location="bottom end">
      <template #activator="{ props }">
        <v-btn v-bind="props" variant="text" class="text-secondary">
          <span class="text-body-2 font-weight-semibold">VN</span>
          <v-icon icon="mdi-chevron-down" size="18" class="ml-1" />
        </v-btn>
      </template>
      <v-list density="compact">
        <v-list-item title="Tiếng Việt" />
      </v-list>
    </v-menu>

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
        <div class="text-body-2 font-weight-bold text-secondary">LMS Admin</div>
        <div class="text-caption" style="color: #64748b">Quản trị hệ thống</div>
      </div>
    </div>

    <v-list nav density="comfortable" class="px-4">
      <v-list-item
        v-for="item in navItems"
        :key="item.name"
        :to="{ name: item.name }"
        :prepend-icon="item.icon"
        rounded="lg"
        class="admin-nav-item"
        active-class="admin-nav-item--active"
      >
        <v-list-item-title v-text="item.title"></v-list-item-title>
      </v-list-item>
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
          <v-btn icon="mdi-chevron-right" variant="text" size="small" />
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
import { RouterView } from 'vue-router'
import { computed, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useDisplay } from 'vuetify'
import { useRoute } from 'vue-router'

const authStore = useAuthStore()
const currentUser = computed(() => authStore.currentUser)

const drawerOpen = ref(true)
const { mdAndDown } = useDisplay()
const isMobile = computed(() => mdAndDown.value)

const navItems = [
  { name: 'AdminHome', title: 'Bảng điều khiển', icon: 'mdi-view-dashboard-outline' },
  { name: 'AdminUsers', title: 'Quản lý người dùng', icon: 'mdi-account-group-outline' },
  { name: 'AdminDepartments', title: 'Quản lý khoa', icon: 'mdi-domain' },
  { name: 'AdminTeachers', title: 'Quản lý giảng viên', icon: 'mdi-account-tie-outline' },
  { name: 'AdminAdminClasses', title: 'Quản lý lớp hành chính', icon: 'mdi-account-multiple-outline' },
  { name: 'AdminPrograms', title: 'Chương trình đào tạo', icon: 'mdi-school-outline' },
  { name: 'AdminSemesters', title: 'Quản lý học kỳ', icon: 'mdi-calendar-range-outline' },
  { name: 'AdminSubjects', title: 'Quản lý môn học', icon: 'mdi-book-open-page-variant-outline' },
  { name: 'AdminSections', title: 'Quản lý lớp học phần', icon: 'mdi-google-classroom' }
]

const route = useRoute()
const currentSectionTitle = computed(() => {
  const found = navItems.find((i) => i.name === route.name)
  return found?.title || 'Quản trị'
})

const activeRoleLabel = computed(() => {
  const roles = authStore.roles
  if (roles.includes('ADMIN')) return 'Quản trị viên'
  return roles[0] || ''
})

const handleLogout = () => {
  authStore.logout()
}
</script>

<style scoped>
.admin-brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 9999px;
  background: rgba(116, 28, 233, 0.1);
}

.admin-nav-item--active {
  background: rgba(116, 28, 233, 0.1);
}

.admin-nav-item--active :deep(.v-list-item-title) {
  color: #741ce9;
  font-weight: 600;
}
</style>
