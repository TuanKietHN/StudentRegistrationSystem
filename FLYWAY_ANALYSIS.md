# Flyway Migration Analysis

## 1. Overview
The Flyway migrations are standardized to support an **internal course management system** where:
- `courses` represents **lớp học phần** (V1 model).
- Teacher is a **User** with an optional **Teacher profile** (`teachers.user_id`).
- Flyway is the **single source of schema truth**.

## 2. Consistency with Architecture & Principles

### 2.1. Pros (Good Practices)
- **Modular Structure:** The migrations are split by feature (Auth, Academic, …) and are easy to reason about.
- **Naming Conventions:** Consistent naming (plural table names, snake_case columns).
- **Audit Columns:** `created_at` and `updated_at` are consistently used (with triggers in V2).
- **Constraints:** Good use of UNIQUE, FOREIGN KEY, and NOT NULL constraints to ensure data integrity.
- **RBAC:** V2 migration implements a solid Role-Based Access Control system with `roles`, `permissions`, `user_roles`, and `role_permissions`.

### 2.2. Cons & Discrepancies (Risks)
- **Duplicate Data Seeding:** Both Flyway (V1, V2) and `DataSeeder.java` attempt to seed initial data (Admin, Teacher, Student). This causes conflicts or redundant checks.
  - *Recommendation:* Remove data seeding from `DataSeeder.java` or Flyway (preferably keep structural reference data in Flyway and test/demo data in `DataSeeder`).
- **LMS/Online schema removed:**
  - V4–V6 migrations introduced lesson/assessment/attendance tables.
  - The project scope removed online learning; V9 drops those tables and related views/functions.
- **Course normalization:**
  - V10 removes e-learning oriented columns added to `courses` in V3 and fixes `v_courses_with_teacher` to treat `courses.teacher_id` as `users.id` and join teacher profile via `teachers.user_id`.

## 3. Alignment with Recent Code Refactoring
- **Multi-Role Support:**
  - The V2 migration **supports** multi-role via `user_roles` (Many-to-Many). This aligns perfectly with the recent Java refactoring (`Set<RoleType>`).
- **Enum vs DB:**
  - Java Enum `RoleType` (ADMIN, TEACHER, STUDENT) matches the initial roles inserted in V2.

## 4. Conclusion
Flyway is now the single source of schema truth. The remaining key action item is to keep seeding strategy consistent:
- Use Flyway for deterministic baseline reference data (roles, permissions, optional demo users).
- Use an application seeder only for dev/demo data and guard it by profile/property.
