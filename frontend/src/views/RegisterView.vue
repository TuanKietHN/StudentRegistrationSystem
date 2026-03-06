<template>
  <v-container class="auth-layout fill-height" fluid>
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="10" md="7" lg="5">
        <v-card class="admin-panel auth-card" variant="flat">
          <v-card-text class="pa-8 auth-card-scroll">
            <div class="d-flex justify-center mb-6">
              <div class="auth-brand-icon d-flex align-center justify-center">
                <v-icon icon="mdi-account-plus-outline" color="primary" size="26" />
              </div>
            </div>

            <div class="text-h5 font-weight-bold text-secondary text-center">Tạo tài khoản</div>
            <div class="text-body-2 text-center mt-2" style="color: #64748b">
              Đăng ký để bắt đầu sử dụng
            </div>

            <v-alert v-if="success" type="success" variant="tonal" class="mt-6" closable>
              {{ success }}
            </v-alert>
            <v-alert v-if="error" type="error" variant="tonal" class="mt-4" closable>
              {{ error }}
            </v-alert>

            <v-form class="mt-6" @submit.prevent="handleRegister">
              <v-text-field
                v-model="username"
                class="admin-input"
                label="Username"
                placeholder="Nhập tên đăng nhập"
                autocomplete="username"
                variant="solo-filled"
                bg-color="#F7F6F8"
                prepend-inner-icon="mdi-account"
                density="comfortable"
                required
                hide-details="auto"
                :rules="[(v) => !!v || 'Trường này không được để trống']"
              />

              <v-text-field
                v-model="email"
                class="admin-input mt-4"
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

              <v-text-field
                v-model="password"
                class="admin-input mt-4"
                label="Mật khẩu"
                placeholder="Nhập mật khẩu mạnh"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="new-password"
                variant="solo-filled"
                bg-color="#F7F6F8"
                prepend-inner-icon="mdi-lock-outline"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                density="comfortable"
                required
                hide-details="auto"
                :rules="[(v) => !!v || 'Trường này không được để trống']"
                @click:append-inner="showPassword = !showPassword"
              />

              <v-text-field
                v-model="confirmPassword"
                class="admin-input mt-4"
                label="Xác nhận mật khẩu"
                placeholder="Nhập lại mật khẩu"
                :type="showConfirmPassword ? 'text' : 'password'"
                autocomplete="new-password"
                variant="solo-filled"
                bg-color="#F7F6F8"
                prepend-inner-icon="mdi-lock-check-outline"
                :append-inner-icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'"
                density="comfortable"
                required
                hide-details="auto"
                :rules="[(v) => !!v || 'Trường này không được để trống']"
                @click:append-inner="showConfirmPassword = !showConfirmPassword"
              />

              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                :loading="loading"
                class="mt-6"
              >
                Tạo tài khoản
              </v-btn>
            </v-form>

            <v-divider class="my-6" />

            <div class="text-body-2 text-center" style="color: #64748b">
              Đã có tài khoản?
              <router-link to="/login" class="auth-link ml-1">Đăng nhập</router-link>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
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
    setTimeout(() => router.push({ name: 'Login' }), 600)
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
