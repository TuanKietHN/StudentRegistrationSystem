<template>
  <div class="forgot-background">
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="forgot-card" elevation="8">
          <!-- Header -->
          <div class="forgot-header">
            <v-icon size="48" color="white">mdi-lock-reset</v-icon>
            <h1 class="forgot-title">Quên Mật Khẩu</h1>
          </div>

          <v-card-text class="forgot-content">
            <p class="forgot-subtitle">Nhập email để nhận hướng dẫn đặt lại mật khẩu</p>

            <!-- Alerts -->
            <v-alert v-if="success" type="success" variant="tonal" class="mb-4" closable>
              {{ success }}
            </v-alert>
            <v-alert v-if="error" type="error" variant="tonal" class="mb-4" closable>
              {{ error }}
            </v-alert>

            <!-- Form -->
            <v-form @submit.prevent="handleSubmit">
              <div class="form-group">
                <label class="form-label">Email</label>
                <v-text-field
                    v-model="email"
                    placeholder="Nhập email của bạn"
                    autocomplete="email"
                    type="email"
                    variant="outlined"
                    prepend-inner-icon="mdi-email"
                    density="comfortable"
                    required
                    :rules="[v => !!v || 'Trường này không được để trống']"
                />
              </div>

              <v-btn
                  type="submit"
                  color="primary"
                  size="large"
                  block
                  :loading="loading"
                  class="forgot-button"
              >
                Gửi Yêu Cầu
              </v-btn>
            </v-form>

            <!-- Links -->
            <div class="forgot-links">
              <router-link to="/login" class="link">Quay lại đăng nhập</router-link>
              <span class="link-separator">•</span>
              <router-link to="/reset-password" class="link">Có token?</router-link>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
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

<style scoped>
.forgot-background {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.forgot-background::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 150%;
  height: 150%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.forgot-card {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.forgot-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px;
  text-align: center;
  color: white;
}

.forgot-title {
  font-size: 24px;
  font-weight: 700;
  margin: 12px 0 0 0;
  letter-spacing: -0.5px;
}

.forgot-content {
  padding: 32px 24px;
}

.forgot-subtitle {
  text-align: center;
  color: #6b7280;
  margin-bottom: 24px;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.forgot-button {
  height: 48px;
  font-size: 15px;
  font-weight: 700;
  text-transform: none !important;
  letter-spacing: 0.5px;
  margin-top: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.forgot-button:hover {
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6) !important;
}

.forgot-links {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s ease;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.link-separator {
  color: #d1d5db;
  margin: 0 8px;
}

:deep(.v-text-field) {
  font-size: 14px;
}

:deep(.v-field) {
  border-radius: 12px;
}
</style>
