import { createRouter, createWebHistory } from 'vue-router'
import { applyGuards } from './guards'

const HomeView = () => import('../views/HomeView.vue')
const RootRedirectView = () => import('../views/RootRedirectView.vue')
const LoginView = () => import('../views/LoginView.vue')
const ForgotPasswordView = () => import('../views/ForgotPasswordView.vue')
const ResetPasswordView = () => import('../views/ResetPasswordView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const TeacherLayout = () => import('../layouts/TeacherLayout.vue')
const StudentLayout = () => import('../layouts/StudentLayout.vue')
const DepartmentList = () => import('../views/academic/DepartmentList.vue')
const TeacherList = () => import('../views/academic/TeacherList.vue')
const SemesterList = () => import('../views/academic/SemesterList.vue')
const SubjectList = () => import('../views/academic/SubjectList.vue')
const CourseList = () => import('../views/academic/CourseList.vue')
const CourseAttendanceView = () => import('../views/academic/CourseAttendanceView.vue')
const UserList = () => import('../views/iam/UserList.vue')
const AdminAdminClassesView = () => import('../views/admin/AdminAdminClassesView.vue')
const AdminAdminClassStudentsView = () => import('../views/admin/AdminAdminClassStudentsView.vue')
const AdminCourseEnrollmentsView = () => import('../views/admin/AdminCourseEnrollmentsView.vue')
const StudentHomeView = () => import('../views/student/StudentHomeView.vue')
const StudentCourseRegistrationView = () => import('../views/student/CourseRegistrationView.vue')
const StudentMyEnrollmentsView = () => import('../views/student/MyEnrollmentsView.vue')
const TeacherHomeView = () => import('../views/teacher/TeacherHomeView.vue')
const TeacherCoursesView = () => import('../views/teacher/TeacherCoursesView.vue')
const TeacherCourseEnrollmentsView = () => import('../views/teacher/TeacherCourseEnrollmentsView.vue')
const TeacherAdminClassesView = () => import('../views/teacher/TeacherAdminClassesView.vue')
const TeacherAdminClassStudentsView = () => import('../views/teacher/TeacherAdminClassStudentsView.vue')
const NotFoundView = () => import('../views/NotFoundView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Root',
      component: RootRedirectView
    },
    {
      path: '/login',
      name: 'Login',
      component: LoginView,
      meta: { guestOnly: true }
    },
    {
      path: '/register',
      name: 'Register',
      component: RegisterView,
      meta: { guestOnly: true }
    },
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: ForgotPasswordView,
      meta: { guestOnly: true }
    },
    {
      path: '/reset-password',
      name: 'ResetPassword',
      component: ResetPasswordView,
      meta: { guestOnly: true }
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' },
      children: [
        { path: '', name: 'AdminHome', component: HomeView },
        { path: 'departments', name: 'AdminDepartments', component: DepartmentList },
        { path: 'teachers', name: 'AdminTeachers', component: TeacherList },
        { path: 'semesters', name: 'AdminSemesters', component: SemesterList },
        { path: 'classes', name: 'AdminClasses', component: SubjectList },
        { path: 'cohorts', name: 'AdminCohorts', component: CourseList },
        { path: 'cohorts/:cohortId/enrollments', name: 'AdminCohortEnrollments', component: AdminCourseEnrollmentsView },
        { path: 'cohorts/:cohortId/attendance', name: 'AdminCohortAttendance', component: CourseAttendanceView },
        { path: 'admin-classes', name: 'AdminAdminClasses', component: AdminAdminClassesView },
        { path: 'admin-classes/:adminClassId/students', name: 'AdminAdminClassStudents', component: AdminAdminClassStudentsView },
        { path: 'users', name: 'AdminUsers', component: UserList }
      ]
    },
    {
      path: '/teacher',
      component: TeacherLayout,
      meta: { requiresAuth: true, roles: ['TEACHER'], activeRoleRequired: 'TEACHER' },
      children: [
        { path: '', name: 'TeacherHome', component: TeacherHomeView },
        { path: 'cohorts', name: 'TeacherCohorts', component: TeacherCoursesView },
        { path: 'cohorts/:cohortId/enrollments', name: 'TeacherCohortEnrollments', component: TeacherCourseEnrollmentsView },
        { path: 'cohorts/:cohortId/attendance', name: 'TeacherCohortAttendance', component: CourseAttendanceView },
        { path: 'admin-classes', name: 'TeacherAdminClasses', component: TeacherAdminClassesView },
        { path: 'admin-classes/:adminClassId/students', name: 'TeacherAdminClassStudents', component: TeacherAdminClassStudentsView }
      ]
    },
    {
      path: '/student',
      component: StudentLayout,
      meta: { requiresAuth: true, roles: ['STUDENT'], activeRoleRequired: 'STUDENT' },
      children: [
        { path: '', name: 'StudentHome', component: StudentHomeView },
        { path: 'cohorts', name: 'StudentCohortRegistration', component: StudentCourseRegistrationView },
        { path: 'enrollments', name: 'StudentMyEnrollments', component: StudentMyEnrollmentsView }
      ]
    },
    { path: '/users', redirect: { name: 'AdminUsers' } },
    { path: '/departments', redirect: { name: 'AdminDepartments' } },
    { path: '/teachers', redirect: { name: 'AdminTeachers' } },
    { path: '/semesters', redirect: { name: 'AdminSemesters' } },
    { path: '/classes', redirect: { name: 'AdminClasses' } },
    { path: '/cohorts', redirect: { name: 'AdminCohorts' } },
    { path: '/subjects', redirect: { name: 'AdminClasses' } },
    { path: '/courses', redirect: { name: 'AdminCohorts' } },

    { path: '/app/cohorts', redirect: { name: 'StudentCohortRegistration' } },
    { path: '/app/enrollments', redirect: { name: 'StudentMyEnrollments' } },
    { path: '/app/teacher', redirect: { name: 'TeacherHome' } },
    { path: '/app/teacher/cohorts', redirect: { name: 'TeacherCohorts' } },
    { path: '/app/courses', redirect: { name: 'StudentCohortRegistration' } },
    { path: '/app/teacher/courses', redirect: { name: 'TeacherCohorts' } },

    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView }
  ]
})

applyGuards(router)

export default router
