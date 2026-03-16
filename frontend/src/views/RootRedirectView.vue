<template>
  <div />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const resolveTargetName = () => {
  const token = localStorage.getItem('accessToken')
  if (!token) return 'Login'

  const activeRole = localStorage.getItem('activeRole')
  if (activeRole === 'ADMIN') return 'AdminHome'
  if (activeRole === 'TEACHER') return 'TeacherHome'
  if (activeRole === 'STUDENT') return 'StudentHome'

  const userRaw = localStorage.getItem('user')
  const roleStr = userRaw ? (JSON.parse(userRaw)?.role as string) : ''
  const roles = String(roleStr)
    .split(',')
    .map((r) => r.trim())
    .filter(Boolean)
    .map((r) => r.replace(/^ROLE_/, ''))

  if (roles.includes('ADMIN')) return 'AdminHome'
  if (roles.includes('STUDENT')) return 'StudentHome'
  if (roles.includes('TEACHER')) return 'TeacherHome'
  return 'Login'
}

onMounted(() => {
  router.replace({ name: resolveTargetName() })
})
</script>
