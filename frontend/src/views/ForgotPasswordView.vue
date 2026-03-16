<template>
  <v-container class="auth-layout fill-height" fluid>
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="10" md="7" lg="5">
        <v-card class="admin-panel auth-card" variant="flat">
          <v-card-text class="pa-8">
            <div class="d-flex justify-center mb-6">
              <div class="auth-brand-icon d-flex align-center justify-center">
                <v-icon icon="mdi-lock-reset" color="primary" size="26" />
              </div>
            </div>

            <div class="text-h5 font-weight-bold text-secondary text-center">Quên mật khẩu</div>
            <div class="text-body-2 text-center mt-2" style="color: #64748b">
              Nhập email để nhận hướng dẫn đặt lại mật khẩu
            </div>

            <v-alert v-if="success" type="success" variant="tonal" class="mt-6" closable>
              {{ success }}
            </v-alert>
            <v-alert v-if="error" type="error" variant="tonal" class="mt-4" closable>
              {{ error }}
            </v-alert>

            <v-form class="mt-6" @submit.prevent="handleSubmit">
              <v-text-field
                v-model="email"
                class="admin-input"
                label="Email"
                placeholder="Nhập email của bạn"
                autocomplete="email"
                type="email"
                variant="solo-filled"
                bg-color="#F7F6F8"
                prepend-inner-icon="mdi-email-outline"
                density="comfortable"
                required
                hide-details="auto"
                :rules="[(v) => !!v || 'Trường này không được để trống']"
              />

              <v-btn type="submit" color="primary" size="large" block :loading="loading" class="mt-6">
                Gửi yêu cầu
              </v-btn>
            </v-form>

            <v-divider class="my-6" />

            <div class="d-flex align-center justify-center ga-2 text-body-2" style="color: #64748b">
              <router-link to="/login" class="auth-link">Quay lại đăng nhập</router-link>
              <span>•</span>
              <router-link to="/reset-password" class="auth-link">Có token?</router-link>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
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
    await authService.forgotPassword({ email: email.value })
    success.value = 'Nếu email tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi.'
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
