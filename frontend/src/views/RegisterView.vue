<template>
  <v-row class="fill-height" align="center" justify="center">
    <v-col cols="12" sm="8" md="6" lg="5">
      <v-card elevation="2">
        <v-card-title class="text-h5">Đăng ký</v-card-title>
        <v-card-text>
          <v-alert v-if="success" type="success" variant="tonal" class="mb-4">
            {{ success }}
          </v-alert>
          <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
            {{ error }}
          </v-alert>
          <v-form @submit.prevent="handleRegister">
            <v-text-field v-model="username" label="Username" autocomplete="username" required />
            <v-text-field v-model="email" label="Email" autocomplete="email" required />
            <v-text-field v-model="password" label="Mật khẩu" type="password" autocomplete="new-password" required />
            <v-text-field
              v-model="confirmPassword"
              label="Nhập lại mật khẩu"
              type="password"
              autocomplete="new-password"
              required
            />
            <v-select
              v-model="role"
              :items="roles"
              label="Vai trò (tuỳ chọn)"
              clearable
            />
            <v-btn type="submit" color="primary" block :loading="loading">
              Tạo tài khoản
            </v-btn>
          </v-form>
          <div class="d-flex justify-space-between mt-4">
            <v-btn to="/login" variant="text" density="comfortable">Quay lại đăng nhập</v-btn>
          </div>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '@/api/services/auth.service'
import { useUiStore } from '@/stores/ui'

const router = useRouter()
const uiStore = useUiStore()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const role = ref<string | null>(null)
const roles = ['STUDENT', 'TEACHER', 'ADMIN']

const loading = ref(false)
const error = ref('')
const success = ref('')

const handleRegister = async () => {
  error.value = ''
  success.value = ''

  if (password.value !== confirmPassword.value) {
    error.value = 'Mật khẩu nhập lại không khớp'
    uiStore.notify(error.value, 'error', 4000)
    return
  }

  loading.value = true
  try {
    await authService.register({
      username: username.value,
      email: email.value,
      password: password.value,
      role: role.value || undefined
    })
    success.value = 'Đăng ký thành công. Vui lòng đăng nhập.'
    uiStore.notify(success.value, 'success')
    setTimeout(() => router.push('/login'), 600)
  } catch (err: any) {
    const msg = err?.response?.data?.message
    const details = err?.response?.data?.data
    if (msg && details && typeof details === 'object') {
      const lines = Object.entries(details).map(([k, v]) => `${k}: ${v}`)
      error.value = `${msg}\n${lines.join('\n')}`
    } else if (msg) {
      error.value = msg
    } else {
      error.value = 'Đăng ký thất bại. Vui lòng thử lại.'
    }
    uiStore.notify(error.value, 'error', 5000)
  } finally {
    loading.value = false
  }
}
</script>
