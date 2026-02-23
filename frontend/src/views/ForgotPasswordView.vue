<template>
  <v-row class="fill-height" align="center" justify="center">
    <v-col cols="12" sm="8" md="6" lg="5">
      <v-card elevation="2">
        <v-card-title class="text-h5">Quên mật khẩu</v-card-title>
        <v-card-text>
          <v-alert v-if="success" type="success" variant="tonal" class="mb-4">
            {{ success }}
          </v-alert>
          <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
            {{ error }}
          </v-alert>
          <v-form @submit.prevent="handleSubmit">
            <v-text-field v-model="email" label="Email" autocomplete="email" required />
            <v-btn type="submit" color="primary" block :loading="loading">
              Gửi yêu cầu
            </v-btn>
          </v-form>
          <div class="d-flex justify-space-between mt-4">
            <v-btn to="/login" variant="text" density="comfortable">Quay lại đăng nhập</v-btn>
            <v-btn to="/reset-password" variant="text" density="comfortable">Đặt lại mật khẩu</v-btn>
          </div>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { authService } from '@/api/services/auth.service'
import { useUiStore } from '@/stores/ui'

const email = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')
const uiStore = useUiStore()

const handleSubmit = async () => {
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    const res = await authService.forgotPassword({ email: email.value })
    const token = res?.data?.data?.resetToken
    success.value = token
      ? `Token đặt lại mật khẩu (dev): ${token}`
      : 'Nếu email tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi.'
    uiStore.notify(success.value, 'success', 5000)
  } catch (err: any) {
    if (err?.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Gửi yêu cầu thất bại. Vui lòng thử lại.'
    }
    uiStore.notify(error.value, 'error', 5000)
  } finally {
    loading.value = false
  }
}
</script>
