<template>
  <v-card>
    <v-card-text class="py-8">
      <div class="text-h6 mb-2">Chế độ giảng viên</div>
      <div class="text-body-2 mb-4">Chọn chức năng bên dưới.</div>
      <v-btn color="primary" variant="flat" :to="{ name: 'TeacherCourses' }">Lớp tôi dạy</v-btn>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts"></script>
