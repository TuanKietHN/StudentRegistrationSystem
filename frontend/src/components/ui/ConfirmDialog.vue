<template>
  <v-dialog :model-value="modelValue" max-width="520" @update:model-value="emit('update:modelValue', $event)">
    <v-card>
      <v-card-title class="text-h6">{{ title }}</v-card-title>
      <v-card-text>{{ text }}</v-card-text>
      <v-card-actions class="justify-end">
        <v-btn variant="text" :disabled="loading" @click="emit('update:modelValue', false)">
          {{ cancelText }}
        </v-btn>
        <v-btn :color="confirmColor" variant="flat" :loading="loading" @click="emit('confirm')">
          {{ confirmText }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    text: string
    confirmText?: string
    cancelText?: string
    confirmColor?: string
    loading?: boolean
  }>(),
  {
    confirmText: 'Xóa',
    cancelText: 'Hủy',
    confirmColor: 'error',
    loading: false
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
}>()
</script>
