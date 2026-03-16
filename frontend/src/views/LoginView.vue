<template>
  <div class="login-background">
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="8" md="5" lg="4">
        <v-card class="login-card" elevation="8">
          <!-- Header với gradient -->
          <div class="login-card-header">
            <v-icon size="48" color="white">mdi-school</v-icon>
            <h1 class="login-title">Quản Lý Giáo Dục</h1>
          </div>

          <v-card-text class="login-content">
            <p class="login-subtitle">Đăng nhập vào hệ thống</p>

            <!-- Error Alert -->
            <v-alert v-if="error" type="error" variant="tonal" class="mb-4" closable>
              {{ error }}
            </v-alert>

            <!-- Login Form -->
            <v-form @submit.prevent="handleLogin">
              <div class="form-group">
                <label class="form-label">Username hoặc Email</label>
                <v-text-field
                    v-model="username"
                    placeholder="Nhập tài khoản của bạn"
                    autocomplete="username"
                    variant="outlined"
                    prepend-inner-icon="mdi-account"
                    density="comfortable"
                    required
                    :rules="[v => !!v || 'Trường này không được để trống']"
                />
              </div>

              <div class="form-group">
                <label class="form-label">Mật khẩu</label>
                <v-text-field
                    v-model="password"
                    placeholder="Nhập mật khẩu của bạn"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    variant="outlined"
                    prepend-inner-icon="mdi-lock"
                    :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append-inner="showPassword = !showPassword"
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
                  class="login-button"
              >
                Đăng Nhập
              </v-btn>
            </v-form>

            <!-- Links -->
            <div class="login-links">
              <router-link to="/forgot-password" class="link">Quên mật khẩu?</router-link>
            </div>
          </v-card-text>

          <!-- Divider -->
          <v-divider class="my-2" />

          <!-- Footer -->
          <v-card-text class="login-footer">
            <p class="footer-text">
              Chưa có tài khoản?
              <router-link to="/register" class="link">Tạo tài khoản</router-link>
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'

const route = useRoute()
const authStore = useAuthStore()
const uiStore = useUiStore()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
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
    uiStore.notify('Đăng nhập thành công', 'success')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null
    authStore.redirectAfterLogin(redirect)
  } catch (err: any) {
    if (err?.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'Đăng nhập thất bại. Vui lòng thử lại.'
    }
    uiStore.notify(error.value, 'error', 4000)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-background {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-background::before {
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

.login-card {
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.login-card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 24px;
  text-align: center;
  color: white;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  margin: 12px 0 0 0;
  letter-spacing: -0.5px;
}

.login-content {
  padding: 32px 24px;
}

.login-subtitle {
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

.login-button {
  height: 48px;
  font-size: 15px;
  font-weight: 700;
  text-transform: none !important;
  letter-spacing: 0.5px;
  margin-top: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.login-button:hover {
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6) !important;
}

.login-links {
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

.login-footer {
  text-align: center;
  padding: 16px 24px;
}

.footer-text {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.footer-text .link {
  margin-left: 4px;
}

:deep(.v-text-field) {
  font-size: 14px;
}

:deep(.v-field) {
  border-radius: 12px;
}

:deep(.v-field--rounded) {
  border-radius: 12px;
}

:deep(.v-divider) {
  opacity: 0.1;
}
</style>
