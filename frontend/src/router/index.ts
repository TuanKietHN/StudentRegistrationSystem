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
const ScheduleView = () => import('../views/ScheduleView.vue')
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
        { path: 'departments', name: 'AdminDepartments', component: DepartmentList, meta: { permissions: ['DEPARTMENT:READ'] } },
        { path: 'teachers', name: 'AdminTeachers', component: TeacherList, meta: { permissions: ['TEACHER:READ'] } },
        { path: 'semesters', name: 'AdminSemesters', component: SemesterList, meta: { permissions: ['SEMESTER:READ'] } },
        { path: 'subjects', name: 'AdminSubjects', component: SubjectList, meta: { permissions: ['SUBJECT:READ'] } },
        { path: 'sections', name: 'AdminSections', component: CourseList, meta: { permissions: ['SECTION:READ', 'SEMESTER:READ', 'SUBJECT:READ', 'TEACHER:READ'] } },
        { path: 'sections/:sectionId/enrollments', name: 'AdminSectionEnrollments', component: AdminCourseEnrollmentsView, meta: { permissions: ['SECTION:READ', 'ENROLLMENT:READ'] } },
        { path: 'admin-classes', name: 'AdminAdminClasses', component: AdminAdminClassesView, meta: { permissions: ['STUDENT_CLASS:READ', 'COHORT:READ', 'DEPARTMENT:READ', 'TEACHER:READ'] } },
        { path: 'admin-classes/:adminClassId/students', name: 'AdminAdminClassStudents', component: AdminAdminClassStudentsView, meta: { permissions: ['STUDENT_CLASS:READ', 'COHORT:READ', 'DEPARTMENT:READ', 'TEACHER:READ'] } },
        { path: 'users', name: 'AdminUsers', component: UserList, meta: { permissions: ['USER:READ'] } }
      ]
    },
    {
      path: '/teacher',
      component: TeacherLayout,
      meta: { requiresAuth: true, roles: ['TEACHER'], activeRoleRequired: 'TEACHER' },
      children: [
        { path: '', name: 'TeacherHome', component: TeacherHomeView },
        { path: 'sections', name: 'TeacherSections', component: TeacherCoursesView, meta: { permissions: ['SECTION:READ', 'SEMESTER:READ', 'SUBJECT:READ'] } },
        { path: 'sections/:sectionId/enrollments', name: 'TeacherSectionEnrollments', component: TeacherCourseEnrollmentsView, meta: { permissions: ['SECTION:READ', 'ENROLLMENT:READ', 'ENROLLMENT:UPDATE'] } },
        { path: 'admin-classes', name: 'TeacherAdminClasses', component: TeacherAdminClassesView, meta: { permissions: ['STUDENT_CLASS:READ', 'COHORT:READ', 'DEPARTMENT:READ', 'TEACHER:READ'] } },
        { path: 'admin-classes/:adminClassId/students', name: 'TeacherAdminClassStudents', component: TeacherAdminClassStudentsView, meta: { permissions: ['STUDENT_CLASS:READ', 'COHORT:READ', 'DEPARTMENT:READ', 'TEACHER:READ'] } },
        { path: 'schedule', name: 'TeacherSchedule', component: ScheduleView, meta: { permissions: ['SECTION:READ'] } }
      ]
    },
    {
      path: '/student',
      component: StudentLayout,
      meta: { requiresAuth: true, roles: ['STUDENT'], activeRoleRequired: 'STUDENT' },
      children: [
        { path: '', name: 'StudentHome', component: StudentHomeView },
        { path: 'sections', name: 'StudentSectionRegistration', component: StudentCourseRegistrationView, meta: { permissions: ['SEMESTER:READ', 'SUBJECT:READ', 'SECTION:READ', 'ENROLLMENT:READ', 'ENROLLMENT:CREATE'] } },
        { path: 'enrollments', name: 'StudentMyEnrollments', component: StudentMyEnrollmentsView, meta: { permissions: ['ENROLLMENT:READ', 'ENROLLMENT:DELETE'] } },
        { path: 'schedule', name: 'StudentSchedule', component: ScheduleView, meta: { permissions: ['SECTION:READ', 'ENROLLMENT:READ'] } }
      ]
    },
    { path: '/users', redirect: { name: 'AdminUsers' } },
    { path: '/departments', redirect: { name: 'AdminDepartments' } },
    { path: '/teachers', redirect: { name: 'AdminTeachers' } },
    { path: '/semesters', redirect: { name: 'AdminSemesters' } },
    { path: '/classes', redirect: { name: 'AdminSubjects' } },
    { path: '/subjects', redirect: { name: 'AdminSubjects' } },
    { path: '/cohorts', redirect: { name: 'AdminSections' } },
    { path: '/courses', redirect: { name: 'AdminSections' } },
    { path: '/sections', redirect: { name: 'AdminSections' } },

    { path: '/app/cohorts', redirect: { name: 'StudentSectionRegistration' } },
    { path: '/app/sections', redirect: { name: 'StudentSectionRegistration' } },
    { path: '/app/enrollments', redirect: { name: 'StudentMyEnrollments' } },
    { path: '/app/teacher', redirect: { name: 'TeacherHome' } },
    { path: '/app/teacher/cohorts', redirect: { name: 'TeacherSections' } },
    { path: '/app/teacher/sections', redirect: { name: 'TeacherSections' } },
    { path: '/app/courses', redirect: { name: 'StudentSectionRegistration' } },
    { path: '/app/teacher/courses', redirect: { name: 'TeacherSections' } },

    { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFoundView }
  ]
})

applyGuards(router)

export default router
