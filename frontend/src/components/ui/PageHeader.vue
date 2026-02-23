<template>
  <div class="d-flex align-center justify-space-between mb-4">
    <div>
      <div class="text-h6">{{ title }}</div>
      <div v-if="subtitle" class="text-body-2 text-medium-emphasis mt-1">{{ subtitle }}</div>
    </div>
    <div class="d-flex align-center ga-2">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

