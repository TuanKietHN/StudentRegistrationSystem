import { createRouter, createWebHistory } from 'vue-router'
import { applyGuards } from './guards'

const HomeView = () => import('../views/HomeView.vue')
const LoginView = () => import('../views/LoginView.vue')
const ForgotPasswordView = () => import('../views/ForgotPasswordView.vue')
const ResetPasswordView = () => import('../views/ResetPasswordView.vue')
const DepartmentList = () => import('../views/academic/DepartmentList.vue')
const TeacherList = () => import('../views/academic/TeacherList.vue')
const SemesterList = () => import('../views/academic/SemesterList.vue')
const SubjectList = () => import('../views/academic/SubjectList.vue')
const CourseList = () => import('../views/academic/CourseList.vue')
const UserList = () => import('../views/iam/UserList.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const StudentCourseRegistrationView = () => import('../views/student/CourseRegistrationView.vue')
const StudentMyEnrollmentsView = () => import('../views/student/MyEnrollmentsView.vue')
const TeacherHomeView = () => import('../views/teacher/TeacherHomeView.vue')
const NotFoundView = () => import('../views/NotFoundView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: LoginView,
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
      path: '/app/courses',
      name: 'StudentCourseRegistration',
      component: StudentCourseRegistrationView,
      meta: { requiresAuth: true, roles: ['STUDENT'], activeRoleRequired: 'STUDENT' }
    },
    {
      path: '/app/enrollments',
      name: 'StudentMyEnrollments',
      component: StudentMyEnrollmentsView,
      meta: { requiresAuth: true, roles: ['STUDENT'], activeRoleRequired: 'STUDENT' }
    },
    {
      path: '/app/teacher',
      name: 'TeacherHome',
      component: TeacherHomeView,
      meta: { requiresAuth: true, roles: ['TEACHER'], activeRoleRequired: 'TEACHER' }
    },
    {
      path: '/admin/register',
      name: 'AdminRegister',
      component: RegisterView,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' }
    },
    {
      path: '/admin/departments',
      name: 'AdminDepartments',
      component: DepartmentList,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' }
    },
    {
      path: '/admin/teachers',
      name: 'AdminTeachers',
      component: TeacherList,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' }
    },
    {
      path: '/admin/semesters',
      name: 'AdminSemesters',
      component: SemesterList,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' }
    },
    {
      path: '/admin/subjects',
      name: 'AdminSubjects',
      component: SubjectList,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' }
    },
    {
      path: '/admin/courses',
      name: 'AdminCourses',
      component: CourseList,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' }
    },
    {
      path: '/admin/users',
      name: 'AdminUsers',
      component: UserList,
      meta: { requiresAuth: true, roles: ['ADMIN'], activeRoleRequired: 'ADMIN' }
    },

    { path: '/register', redirect: { name: 'AdminRegister' } },
    { path: '/users', redirect: { name: 'AdminUsers' } },
    { path: '/departments', redirect: { name: 'AdminDepartments' } },
    { path: '/teachers', redirect: { name: 'AdminTeachers' } },
    { path: '/semesters', redirect: { name: 'AdminSemesters' } },
    { path: '/subjects', redirect: { name: 'AdminSubjects' } },
    { path: '/courses', redirect: { name: 'AdminCourses' } },

    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView }
  ]
})

applyGuards(router)

export default router
