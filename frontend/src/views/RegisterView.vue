<template>
  <div class="register-background">
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="register-card" elevation="8">
          <!-- Header với gradient -->
          <div class="register-card-header">
            <v-icon size="48" color="white">mdi-account-plus</v-icon>
            <h1 class="register-title">Tạo Tài Khoản</h1>
          </div>

          <v-card-text class="register-content">
            <p class="register-subtitle">Đăng ký để bắt đầu sử dụng</p>

            <!-- Alerts -->
            <v-alert v-if="success" type="success" variant="tonal" class="mb-4" closable>
              {{ success }}
            </v-alert>
            <v-alert v-if="error" type="error" variant="tonal" class="mb-4" closable>
              {{ error }}
            </v-alert>

            <!-- Register Form -->
            <v-form @submit.prevent="handleRegister">
              <div class="form-group">
                <label class="form-label">Username</label>
                <v-text-field
                    v-model="username"
                    placeholder="Nhập tên đăng nhập"
                    autocomplete="username"
                    variant="outlined"
                    prepend-inner-icon="mdi-account"
                    density="comfortable"
                    required
                    :rules="[v => !!v || 'Trường này không được để trống']"
                />
              </div>

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

              <div class="form-group">
                <label class="form-label">Mật khẩu</label>
                <v-text-field
                    v-model="password"
                    placeholder="Nhập mật khẩu mạnh"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    variant="outlined"
                    prepend-inner-icon="mdi-lock"
                    :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append-inner="showPassword = !showPassword"
                    density="comfortable"
                    required
                    :rules="[v => !!v || 'Trường này không được để trống']"
                />
              </div>

              <div class="form-group">
                <label class="form-label">Xác nhận mật khẩu</label>
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
                  class="register-button"
              >
                Tạo Tài Khoản
              </v-btn>
            </v-form>
          </v-card-text>

          <!-- Divider -->
          <v-divider class="my-2" />

          <!-- Footer -->
          <v-card-text class="register-footer">
            <p class="footer-text">
              Đã có tài khoản?
              <router-link to="/login" class="link">Đăng nhập</router-link>
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
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
const showPassword = ref(false)
const showConfirmPassword = ref(false)

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
      password: password.value
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

<style scoped>
.register-background {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  position: relative;
  overflow: hidden;
}

.register-background::before {
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

.register-card {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.register-card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px;
  text-align: center;
  color: white;
}

.register-title {
  font-size: 24px;
  font-weight: 700;
  margin: 12px 0 0 0;
  letter-spacing: -0.5px;
}

.register-content {
  padding: 28px 24px;
  max-height: 75vh;
  overflow-y: auto;
}

.register-subtitle {
  text-align: center;
  color: #6b7280;
  margin-bottom: 20px;
  font-size: 14px;
}

.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.register-button {
  height: 48px;
  font-size: 15px;
  font-weight: 700;
  text-transform: none !important;
  letter-spacing: 0.5px;
  margin-top: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.register-button:hover {
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6) !important;
}

.register-footer {
  text-align: center;
  padding: 16px 24px;
}

.footer-text {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
  transition: all 0.2s ease;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}

:deep(.v-text-field),
:deep(.v-select) {
  font-size: 14px;
}

:deep(.v-field) {
  border-radius: 12px;
}

:deep(.v-divider) {
  opacity: 0.1;
}
</style>
