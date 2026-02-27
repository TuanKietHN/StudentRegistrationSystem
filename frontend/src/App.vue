<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted, onUnmounted } from 'vue'
import { useUiStore } from '@/stores/ui'

const uiStore = useUiStore()

const onApiNotify = (event: Event) => {
  const detail = (event as CustomEvent<{ text: string; color?: any; timeout?: number }>).detail
  if (!detail?.text) return
  uiStore.notify(detail.text, detail.color, detail.timeout)
}

onMounted(() => {
  window.addEventListener('api:notify', onApiNotify as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('api:notify', onApiNotify as EventListener)
})
</script>

<template>
  <v-app>
    <RouterView />

    <v-snackbar
      v-model="uiStore.snackbar.open"
      :color="uiStore.snackbar.color"
      :timeout="uiStore.snackbar.timeout"
      location="bottom right"
    >
      {{ uiStore.snackbar.text }}
      <template #actions>
        <v-btn icon="mdi-close" variant="text" @click="uiStore.closeSnackbar" />
      </template>
    </v-snackbar>
  </v-app>
</template>
