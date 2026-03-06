<template>
  <v-container class="auth-layout fill-height" fluid>
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="9" md="6" lg="4">
        <v-card class="admin-panel auth-card" variant="flat">
          <v-card-text class="pa-8">
            <div class="d-flex justify-center mb-6">
              <div class="auth-brand-icon d-flex align-center justify-center">
                <v-icon icon="mdi-school-outline" color="primary" size="26" />
              </div>
            </div>

            <div class="text-h5 font-weight-bold text-secondary text-center">Đăng nhập</div>
            <div class="text-body-2 text-center mt-2" style="color: #64748b">
              Truy cập hệ thống quản trị
            </div>

            <v-alert v-if="error" type="error" variant="tonal" class="mt-6" closable>
              {{ error }}
            </v-alert>

            <v-form class="mt-6" @submit.prevent="handleLogin">
              <v-text-field
                v-model="username"
                class="admin-input"
                label="Username hoặc Email"
                placeholder="Nhập tài khoản của bạn"
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
                v-model="password"
                class="admin-input mt-4"
                label="Mật khẩu"
                placeholder="Nhập mật khẩu của bạn"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                variant="solo-filled"
                bg-color="#F7F6F8"
                prepend-inner-icon="mdi-lock"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                density="comfortable"
                required
                hide-details="auto"
                :rules="[(v) => !!v || 'Trường này không được để trống']"
                @click:append-inner="showPassword = !showPassword"
              />

              <div class="d-flex justify-end mt-3">
                <router-link to="/forgot-password" class="auth-link">Quên mật khẩu?</router-link>
              </div>

              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                :loading="loading"
                class="mt-6"
              >
                Đăng nhập
              </v-btn>
            </v-form>

            <v-divider class="my-6" />

            <div class="text-body-2 text-center" style="color: #64748b">
              Chưa có tài khoản?
              <router-link to="/register" class="auth-link ml-1">Tạo tài khoản</router-link>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
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
