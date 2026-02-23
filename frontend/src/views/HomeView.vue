<template>
  <div>
    <div class="text-h5 mb-4">Tổng quan</div>
    <v-row>
      <v-col cols="12" md="4">
        <v-card to="/departments" class="h-100" variant="tonal">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-domain</v-icon>
            Khoa
          </v-card-title>
          <v-card-text>Quản lý danh sách khoa, phân cấp và trưởng khoa.</v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card to="/teachers" class="h-100" variant="tonal">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-account-tie</v-icon>
            Giảng viên
          </v-card-title>
          <v-card-text>Quản lý hồ sơ giảng viên, khoa phụ trách và trạng thái.</v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card to="/semesters" class="h-100" variant="tonal">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-calendar-range</v-icon>
            Học kỳ
          </v-card-title>
          <v-card-text>Quản lý học kỳ, thời gian và trạng thái hoạt động.</v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card to="/subjects" class="h-100" variant="tonal">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-book-open-page-variant</v-icon>
            Môn học
          </v-card-title>
          <v-card-text>Quản lý môn học, số tín chỉ và trạng thái.</v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card to="/courses" class="h-100" variant="tonal">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-google-classroom</v-icon>
            Lớp học
          </v-card-title>
          <v-card-text>Quản lý lớp học theo học kỳ, môn và giảng viên.</v-card-text>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card to="/forgot-password" class="h-100" variant="tonal">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-lock-reset</v-icon>
            Quên mật khẩu
          </v-card-title>
          <v-card-text>Yêu cầu token và đặt lại mật khẩu.</v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>
