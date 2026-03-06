<template>
  <v-container class="auth-layout fill-height" fluid>
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="10" md="7" lg="5">
        <v-card class="admin-panel auth-card" variant="flat">
          <v-card-text class="pa-8">
            <div class="d-flex justify-center mb-6">
              <div class="auth-brand-icon d-flex align-center justify-center">
                <v-icon icon="mdi-lock-open-outline" color="primary" size="26" />
              </div>
            </div>

            <div class="text-h5 font-weight-bold text-secondary text-center">Đặt lại mật khẩu</div>
            <div class="text-body-2 text-center mt-2" style="color: #64748b">Nhập token và mật khẩu mới</div>

            <v-alert v-if="success" type="success" variant="tonal" class="mt-6" closable>
              {{ success }}
            </v-alert>
            <v-alert v-if="error" type="error" variant="tonal" class="mt-4" closable>
              {{ error }}
            </v-alert>

            <v-form class="mt-6" @submit.prevent="handleSubmit">
              <v-text-field
                v-model="token"
                class="admin-input"
                label="Token"
                placeholder="Nhập token từ email"
                variant="solo-filled"
                bg-color="#F7F6F8"
                prepend-inner-icon="mdi-key-outline"
                density="comfortable"
                required
                hide-details="auto"
                :rules="[(v) => !!v || 'Trường này không được để trống']"
              />

              <v-text-field
                v-model="newPassword"
                class="admin-input mt-4"
                label="Mật khẩu mới"
                placeholder="Nhập mật khẩu mới"
                :type="showNewPassword ? 'text' : 'password'"
                autocomplete="new-password"
                variant="solo-filled"
                bg-color="#F7F6F8"
                prepend-inner-icon="mdi-lock-outline"
                :append-inner-icon="showNewPassword ? 'mdi-eye-off' : 'mdi-eye'"
                density="comfortable"
                required
                hide-details="auto"
                :rules="[(v) => !!v || 'Trường này không được để trống']"
                @click:append-inner="showNewPassword = !showNewPassword"
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

              <v-btn type="submit" color="primary" size="large" block :loading="loading" class="mt-6">
                Đặt lại mật khẩu
              </v-btn>
            </v-form>

            <v-divider class="my-6" />

            <div class="text-body-2 text-center" style="color: #64748b">
              <router-link to="/login" class="auth-link">Quay lại đăng nhập</router-link>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
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
    setTimeout(() => router.push({ name: 'Login' }), 600)
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
