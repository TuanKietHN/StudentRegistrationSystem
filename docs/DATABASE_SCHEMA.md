# Thiết kế Cơ sở dữ liệu (Database Schema)

Tài liệu này mô tả cấu trúc cơ sở dữ liệu cho hệ thống CMS theo phạm vi **quản lý khóa học nội bộ**:
- `courses` là **lớp học phần** (V1), không triển khai LMS/online trong cùng schema.
- Teacher là **profile tuỳ chọn** của `users` (User + Optional Role Profile).
- Schema được quản trị bằng Flyway (nguồn sự thật duy nhất).

## 1. Sơ đồ Quan hệ Thực thể (ER Diagram)

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : grants

    SEMESTERS ||--o{ COURSES : opens
    SUBJECTS ||--o{ COURSES : offered_as
    TEACHERS ||--o{ COURSES : teaches
    USERS ||--o{ ENROLLMENTS : studies
    COURSES ||--o{ ENROLLMENTS : has

    USERS ||--o| TEACHERS : optional_profile
    DEPARTMENTS ||--o{ TEACHERS : contains
    DEPARTMENTS ||--o| DEPARTMENTS : parent
    TEACHERS ||--o| DEPARTMENTS : heads

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password
        varchar avatar
        timestamp created_at
        timestamp updated_at
    }

    ROLES {
        bigint id PK
        varchar name UK
        varchar description
        timestamp created_at
        timestamp updated_at
    }

    USER_ROLES {
        bigint user_id FK
        bigint role_id FK
        timestamp assigned_at
    }

    SEMESTERS {
        bigint id PK
        varchar name
        varchar code UK
        date start_date
        date end_date
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    SUBJECTS {
        bigint id PK
        varchar name
        varchar code UK
        int credits
        text description
        boolean active
        bigint department_id FK
        int theory_hours
        int practice_hours
        bigint[] prerequisite_subject_ids
        timestamp created_at
        timestamp updated_at
    }

    COURSES {
        bigint id PK
        varchar name
        varchar code UK
        boolean active
        int max_students
        int current_students
        bigint semester_id FK
        bigint subject_id FK
        bigint teacher_id FK "teachers.id"
        timestamp created_at
        timestamp updated_at
    }

    ENROLLMENTS {
        bigint id PK
        bigint course_id FK
        bigint student_id FK "users.id"
        varchar status
        double grade
        timestamp created_at
        timestamp updated_at
    }

    DEPARTMENTS {
        bigint id PK
        varchar code UK
        varchar name
        text description
        bigint parent_id FK
        bigint head_teacher_id FK "teachers.id"
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    TEACHERS {
        bigint id PK
        bigint user_id FK UK
        varchar employee_code UK
        bigint department_id FK
        varchar specialization
        varchar title
        text bio
        varchar office_location
        varchar office_hours
        varchar phone
        boolean active
        timestamp created_at
        timestamp updated_at
    }
```

## 2. Redis Schema (Auth)

Hệ thống có thể dùng Redis cho caching hoặc token nếu cần. Hiện tại refresh token/password reset token đang được quản trị theo migrations V2 (DB tables), không bắt buộc Redis.
