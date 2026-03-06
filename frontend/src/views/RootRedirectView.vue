<template>
  <div />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const resolveTargetName = () => {
  if (!authStore.isLoggedIn) return 'Login'
  if (authStore.activeRole) return authStore.defaultRouteNameForRole(authStore.activeRole)
  if (authStore.roles.includes('ADMIN')) return 'AdminHome'
  if (authStore.roles.includes('STUDENT')) return 'StudentHome'
  if (authStore.roles.includes('TEACHER')) return 'TeacherHome'
  return authStore.defaultRouteNameForRole(authStore.activeRole)
}

onMounted(() => {
  router.replace({ name: resolveTargetName() })
})
</script>
