<template>
  <v-row class="fill-height" align="center" justify="center">
    <v-col cols="12" sm="8" md="5" lg="4">
      <v-card elevation="2">
        <v-card-title class="text-h5">Đăng nhập</v-card-title>
        <v-card-text>
          <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
            {{ error }}
          </v-alert>
          <v-form @submit.prevent="handleLogin">
            <v-text-field
              v-model="username"
              label="Username hoặc Email"
              autocomplete="username"
              required
            />
            <v-text-field
              v-model="password"
              label="Mật khẩu"
              type="password"
              autocomplete="current-password"
              required
            />
            <v-btn type="submit" color="primary" block :loading="loading">
              Đăng nhập
            </v-btn>
          </v-form>
          <div class="d-flex justify-space-between mt-4">
            <v-btn to="/register" variant="text" density="comfortable">Đăng ký</v-btn>
            <v-btn to="/forgot-password" variant="text" density="comfortable">Quên mật khẩu</v-btn>
          </div>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  error.value = ''

  try {
    await authStore.login({
      username: username.value,
      password: password.value
    })
    router.push('/')
  } catch (err: any) {
    if (err?.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Đăng nhập thất bại. Vui lòng thử lại.'
    }
  } finally {
    loading.value = false
  }
}
</script>
