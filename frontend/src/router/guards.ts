import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export function applyGuards(router: Router) {
  router.beforeEach((to) => {
    const authStore = useAuthStore()

    if (authStore.isLoggedIn && (to.meta.guestOnly || to.name === 'Login' || to.name === 'ForgotPassword' || to.name === 'ResetPassword')) {
      return { name: authStore.defaultRouteNameForRole(authStore.activeRole) }
    }

    if (to.meta.requiresAuth && !authStore.isLoggedIn) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }

    const requiredPermissions = to.meta.permissions
    if (requiredPermissions?.length && !authStore.hasPermissions(requiredPermissions)) {
      window.dispatchEvent(
        new CustomEvent('api:notify', {
          detail: { text: 'Không có quyền truy cập trang này', color: 'warning', timeout: 3000 }
        })
      )
      return { name: authStore.defaultRouteNameForRole(authStore.activeRole) }
    }

    const requiredRoles = to.meta.roles
    if (requiredRoles?.length && !authStore.hasAnyRole(requiredRoles)) {
      window.dispatchEvent(
        new CustomEvent('api:notify', {
          detail: { text: 'Không có quyền truy cập trang này', color: 'warning', timeout: 3000 }
        })
      )
      return { name: authStore.defaultRouteNameForRole(authStore.activeRole) }
    }

    const requiredActiveRole = to.meta.activeRoleRequired
    if (requiredActiveRole && authStore.activeRole !== requiredActiveRole) {
      if (authStore.roles.includes(requiredActiveRole)) {
        authStore.setActiveRole(requiredActiveRole)
      } else {
        window.dispatchEvent(
          new CustomEvent('api:notify', {
            detail: { text: 'Không có quyền truy cập trang này', color: 'warning', timeout: 3000 }
          })
        )
        return { name: authStore.defaultRouteNameForRole(authStore.activeRole) }
      }
    }

    return true
  })
}
