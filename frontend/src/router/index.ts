import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const HomeView = () => import('../views/HomeView.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const ForgotPasswordView = () => import('../views/ForgotPasswordView.vue')
const ResetPasswordView = () => import('../views/ResetPasswordView.vue')
const DepartmentList = () => import('../views/academic/DepartmentList.vue')
const TeacherList = () => import('../views/academic/TeacherList.vue')
const SemesterList = () => import('../views/academic/SemesterList.vue')
const SubjectList = () => import('../views/academic/SubjectList.vue')
const CourseList = () => import('../views/academic/CourseList.vue')
const UserList = () => import('../views/iam/UserList.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: ForgotPasswordView
    },
    {
      path: '/reset-password',
      name: 'reset-password',
      component: ResetPasswordView
    },
    {
      path: '/departments',
      name: 'departments',
      component: DepartmentList,
      meta: { requiresAuth: true }
    },
    {
      path: '/teachers',
      name: 'teachers',
      component: TeacherList,
      meta: { requiresAuth: true }
    },
    {
      path: '/semesters',
      name: 'semesters',
      component: SemesterList,
      meta: { requiresAuth: true }
    },
    {
      path: '/subjects',
      name: 'subjects',
      component: SubjectList,
      meta: { requiresAuth: true }
    },
    {
      path: '/courses',
      name: 'courses',
      component: CourseList,
      meta: { requiresAuth: true }
    },
    {
      path: '/users',
      name: 'users',
      component: UserList,
      meta: { requiresAuth: true, roles: ['ADMIN'] }
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  const roles = (authStore.currentUser?.role || '').split(',').map((r) => r.trim()).filter(Boolean)

  if (authStore.isLoggedIn && (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password' || to.path === '/reset-password')) {
    next('/')
    return
  }
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    next('/login')
    return
  }

  const requiredRoles = (to.meta as any)?.roles as string[] | undefined
  if (requiredRoles?.length) {
    const hasRole = requiredRoles.some((r) => roles.includes(r))
    if (!hasRole) {
      window.dispatchEvent(
        new CustomEvent('api:notify', {
          detail: { text: 'Không có quyền truy cập trang này', color: 'warning', timeout: 3000 }
        })
      )
      next('/')
      return
    }
  }

  next()
})

export default router
