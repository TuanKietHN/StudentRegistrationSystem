<template>
  <v-card>
    <v-card-text class="py-10 text-center">
      <div class="text-h6 mb-2">Không tìm thấy trang</div>
      <v-btn color="primary" variant="flat" @click="goHome">Về trang chủ</v-btn>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push({ name: 'Home' })
}
</script>

