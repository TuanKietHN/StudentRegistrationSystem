<template>
  <div class="home-view">
    <!-- Header -->
    <div class="home-header">
      <h1 class="header-title">Tổng Quan Hệ Thống</h1>
      <p class="header-subtitle">Quản lý toàn bộ cơ sở dữ liệu giáo dục</p>
    </div>

    <!-- Module Cards Grid -->
    <v-row class="modules-grid">
      <!-- Departments -->
      <v-col cols="12" sm="6" md="4">
        <router-link to="/departments" class="module-link">
          <v-card class="module-card department-card">
            <div class="module-icon department-icon">
              <v-icon size="40" color="white">mdi-domain</v-icon>
            </div>
            <v-card-title class="module-title">Khoa</v-card-title>
            <v-card-text class="module-description">
              Quản lý danh sách khoa, phân cấp và trưởng khoa
            </v-card-text>
            <div class="module-action">
              <v-btn size="small" variant="text" color="primary" append-icon="mdi-arrow-right">
                Quản lý
              </v-btn>
            </div>
          </v-card>
        </router-link>
      </v-col>

      <!-- Teachers -->
      <v-col cols="12" sm="6" md="4">
        <router-link to="/teachers" class="module-link">
          <v-card class="module-card teacher-card">
            <div class="module-icon teacher-icon">
              <v-icon size="40" color="white">mdi-account-tie</v-icon>
            </div>
            <v-card-title class="module-title">Giảng Viên</v-card-title>
            <v-card-text class="module-description">
              Quản lý hồ sơ giảng viên, khoa phụ trách và trạng thái
            </v-card-text>
            <div class="module-action">
              <v-btn size="small" variant="text" color="primary" append-icon="mdi-arrow-right">
                Quản lý
              </v-btn>
            </div>
          </v-card>
        </router-link>
      </v-col>

      <!-- Semesters -->
      <v-col cols="12" sm="6" md="4">
        <router-link to="/semesters" class="module-link">
          <v-card class="module-card semester-card">
            <div class="module-icon semester-icon">
              <v-icon size="40" color="white">mdi-calendar-range</v-icon>
            </div>
            <v-card-title class="module-title">Học Kỳ</v-card-title>
            <v-card-text class="module-description">
              Quản lý học kỳ, thời gian và trạng thái hoạt động
            </v-card-text>
            <div class="module-action">
              <v-btn size="small" variant="text" color="primary" append-icon="mdi-arrow-right">
                Quản lý
              </v-btn>
            </div>
          </v-card>
        </router-link>
      </v-col>

      <!-- Subjects -->
      <v-col cols="12" sm="6" md="4">
        <router-link to="/classes" class="module-link">
          <v-card class="module-card subject-card">
            <div class="module-icon subject-icon">
              <v-icon size="40" color="white">mdi-book-open-page-variant</v-icon>
            </div>
            <v-card-title class="module-title">Môn Học</v-card-title>
            <v-card-text class="module-description">
              Quản lý môn học, số tín chỉ và trạng thái
            </v-card-text>
            <div class="module-action">
              <v-btn size="small" variant="text" color="primary" append-icon="mdi-arrow-right">
                Quản lý
              </v-btn>
            </div>
          </v-card>
        </router-link>
      </v-col>

      <!-- Courses -->
      <v-col cols="12" sm="6" md="4">
        <router-link to="/cohorts" class="module-link">
          <v-card class="module-card course-card">
            <div class="module-icon course-icon">
              <v-icon size="40" color="white">mdi-google-classroom</v-icon>
            </div>
            <v-card-title class="module-title">Lớp Học Phần</v-card-title>
            <v-card-text class="module-description">
              Quản lý lớp học phần theo học kỳ, môn và giảng viên
            </v-card-text>
            <div class="module-action">
              <v-btn size="small" variant="text" color="primary" append-icon="mdi-arrow-right">
                Quản lý
              </v-btn>
            </div>
          </v-card>
        </router-link>
      </v-col>

      <!-- Users -->
      <v-col cols="12" sm="6" md="4">
        <router-link to="/users" class="module-link">
          <v-card class="module-card user-card">
            <div class="module-icon user-icon">
              <v-icon size="40" color="white">mdi-account-multiple</v-icon>
            </div>
            <v-card-title class="module-title">Quản Trị User</v-card-title>
            <v-card-text class="module-description">
              Quản lý người dùng, vai trò và quyền hạn
            </v-card-text>
            <div class="module-action">
              <v-btn size="small" variant="text" color="primary" append-icon="mdi-arrow-right">
                Quản lý
              </v-btn>
            </div>
          </v-card>
        </router-link>
      </v-col>
    </v-row>
  </div>
</template>

<style scoped>
.home-view {
  padding: 20px 0;
}

.home-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 48px 32px;
  border-radius: 16px;
  color: white;
  margin-bottom: 40px;
}

.header-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
}

.header-subtitle {
  font-size: 16px;
  margin-top: 8px;
  opacity: 0.9;
  font-weight: 400;
  margin-bottom: 0;
}

.modules-grid {
  margin-bottom: 20px;
}

.module-link {
  text-decoration: none;
  display: block;
  height: 100%;
  color: inherit;
}

.module-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 24px;
  height: 100%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.module-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.module-card:hover {
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.08);
  border-color: #d1d5db;
  transform: translateY(-4px);
}

.module-card:hover::before {
  transform: scaleX(1);
}

.department-card::before {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.teacher-card::before {
  background: linear-gradient(90deg, #764ba2 0%, #f093fb 100%);
}

.semester-card::before {
  background: linear-gradient(90deg, #f093fb 0%, #4facfe 100%);
}

.subject-card::before {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
}

.course-card::before {
  background: linear-gradient(90deg, #00f2fe 0%, #43e97b 100%);
}

.user-card::before {
  background: linear-gradient(90deg, #43e97b 0%, #38f9d7 100%);
}

.module-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  margin-bottom: 16px;
}

.department-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.teacher-icon {
  background: linear-gradient(135deg, #764ba2 0%, #f093fb 100%);
}

.semester-icon {
  background: linear-gradient(135deg, #f093fb 0%, #4facfe 100%);
}

.subject-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.course-icon {
  background: linear-gradient(135deg, #00f2fe 0%, #43e97b 100%);
}

.user-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.module-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  padding: 0 !important;
  line-height: 1.3;
  margin-bottom: 8px;
}

.module-description {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  padding: 0 !important;
  margin-bottom: 16px;
  font-weight: 400;
}

.module-action {
  padding: 0 !important;
}

.module-action :deep(.v-btn) {
  padding: 0;
  text-transform: none;
  font-weight: 500;
  font-size: 14px;
}

@media (max-width: 960px) {
  .home-header {
    padding: 40px 24px;
  }

  .header-title {
    font-size: 28px;
  }

  .header-subtitle {
    font-size: 14px;
  }
}

@media (max-width: 600px) {
  .home-header {
    padding: 32px 16px;
    margin-bottom: 24px;
  }

  .header-title {
    font-size: 24px;
  }

  .header-subtitle {
    font-size: 13px;
  }

  .module-card {
    padding: 20px 16px;
  }

  .module-icon {
    width: 56px;
    height: 56px;
  }

  .module-title {
    font-size: 16px;
  }

  .module-description {
    font-size: 13px;
  }
}
</style>
