<template>
  <v-row class="fill-height" align="center" justify="center">
    <v-col cols="12" sm="8" md="6" lg="5">
      <v-card elevation="2">
        <v-card-title class="text-h5">Đặt lại mật khẩu</v-card-title>
        <v-card-text>
          <v-alert v-if="success" type="success" variant="tonal" class="mb-4">
            {{ success }}
          </v-alert>
          <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
            {{ error }}
          </v-alert>
          <v-form @submit.prevent="handleSubmit">
            <v-text-field v-model="token" label="Token" required />
            <v-text-field v-model="newPassword" label="Mật khẩu mới" type="password" autocomplete="new-password" required />
            <v-text-field
              v-model="confirmPassword"
              label="Nhập lại mật khẩu mới"
              type="password"
              autocomplete="new-password"
              required
            />
            <v-btn type="submit" color="primary" block :loading="loading">
              Đặt lại mật khẩu
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authService } from '@/api/services/auth.service'
import { useUiStore } from '@/stores/ui'

const route = useRoute()
const router = useRouter()
const uiStore = useUiStore()

const token = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const loading = ref(false)
const error = ref('')
const success = ref('')

onMounted(() => {
  const t = route.query.token
  if (typeof t === 'string' && t) token.value = t
})

const handleSubmit = async () => {
  error.value = ''
  success.value = ''

  if (newPassword.value !== confirmPassword.value) {
    error.value = 'Mật khẩu nhập lại không khớp'
    uiStore.notify(error.value, 'error', 4000)
    return
  }

  loading.value = true
  try {
    await authService.resetPassword({ token: token.value, newPassword: newPassword.value })
    success.value = 'Đặt lại mật khẩu thành công. Vui lòng đăng nhập.'
    uiStore.notify(success.value, 'success')
    setTimeout(() => router.push('/login'), 600)
  } catch (err: any) {
    if (err?.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Đặt lại mật khẩu thất bại. Vui lòng thử lại.'
    }
    uiStore.notify(error.value, 'error', 5000)
  } finally {
    loading.value = false
  }
}
</script>
