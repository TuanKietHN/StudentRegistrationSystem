<template>
  <div class="reset-background">
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="reset-card" elevation="8">
          <!-- Header -->
          <div class="reset-header">
            <v-icon size="48" color="white">mdi-lock-open</v-icon>
            <h1 class="reset-title">Đặt Lại Mật Khẩu</h1>
          </div>

          <v-card-text class="reset-content">
            <p class="reset-subtitle">Nhập token và mật khẩu mới</p>

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
                <label class="form-label">Token</label>
                <v-text-field
                    v-model="token"
                    placeholder="Nhập token từ email"
                    variant="outlined"
                    prepend-inner-icon="mdi-key"
                    density="comfortable"
                    required
                    :rules="[v => !!v || 'Trường này không được để trống']"
                />
              </div>

              <div class="form-group">
                <label class="form-label">Mật Khẩu Mới</label>
                <v-text-field
                    v-model="newPassword"
                    placeholder="Nhập mật khẩu mới"
                    :type="showNewPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    variant="outlined"
                    prepend-inner-icon="mdi-lock"
                    :append-inner-icon="showNewPassword ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append-inner="showNewPassword = !showNewPassword"
                    density="comfortable"
                    required
                    :rules="[v => !!v || 'Trường này không được để trống']"
                />
              </div>

              <div class="form-group">
                <label class="form-label">Xác Nhận Mật Khẩu</label>
                <v-text-field
                    v-model="confirmPassword"
                    placeholder="Nhập lại mật khẩu"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    variant="outlined"
                    prepend-inner-icon="mdi-lock-check"
                    :append-inner-icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append-inner="showConfirmPassword = !showConfirmPassword"
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
                  class="reset-button"
              >
                Đặt Lại Mật Khẩu
              </v-btn>
            </v-form>

            <!-- Links -->
            <div class="reset-links">
              <router-link to="/login" class="link">Quay lại đăng nhập</router-link>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
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
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

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

<style scoped>
.reset-background {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.reset-background::before {
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

.reset-card {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.reset-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px;
  text-align: center;
  color: white;
}

.reset-title {
  font-size: 24px;
  font-weight: 700;
  margin: 12px 0 0 0;
  letter-spacing: -0.5px;
}

.reset-content {
  padding: 32px 24px;
}

.reset-subtitle {
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

.reset-button {
  height: 48px;
  font-size: 15px;
  font-weight: 700;
  text-transform: none !important;
  letter-spacing: 0.5px;
  margin-top: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.reset-button:hover {
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6) !important;
}

.reset-links {
  text-align: center;
  margin-top: 20px;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}

:deep(.v-text-field) {
  font-size: 14px;
}

:deep(.v-field) {
  border-radius: 12px;
}
</style>
