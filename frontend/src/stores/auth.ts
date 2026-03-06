import { defineStore } from 'pinia'
import router from '@/router'
import { authService, type LoginPayload, type TokenResponse } from '@/api/services/auth.service'
import { unwrapApiResponse } from '@/api/response'

function base64UrlDecode(input: string): string {
  const padded = input.replace(/-/g, '+').replace(/_/g, '/').padEnd(Math.ceil(input.length / 4) * 4, '=')
  return atob(padded)
}

function authoritiesFromToken(token: string | null): string[] {
  if (!token) return []
  const parts = token.split('.')
  if (parts.length < 2) return []
  try {
    const payloadJson = base64UrlDecode(parts[1])
    const payload = JSON.parse(payloadJson) as { scope?: unknown }
    const scope = typeof payload.scope === 'string' ? payload.scope : ''
    return scope
      .split(' ')
      .map((s) => s.trim())
      .filter(Boolean)
  } catch {
    return []
  }
}

interface User {
  username: string
  role: string
}

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  activeRole: string | null
  lastActiveRole: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('accessToken') || null,
    isAuthenticated: !!localStorage.getItem('accessToken'),
    activeRole: localStorage.getItem('activeRole'),
    lastActiveRole: localStorage.getItem('lastActiveRole')
  }),

  getters: {
    currentUser: (state) => state.user,
    isLoggedIn: (state) => state.isAuthenticated,
    authorities: (): string[] => authoritiesFromToken(localStorage.getItem('accessToken')),
    permissions: (): string[] => authoritiesFromToken(localStorage.getItem('accessToken')).filter((a) => !a.startsWith('ROLE_')),
    roles: (state): string[] =>
      (state.user?.role || '')
        .split(',')
        .map((r) => r.trim())
        .filter(Boolean)
        .map((r) => r.replace(/^ROLE_/, '')),
    isDualRole: (state): boolean => {
      const roles = (state.user?.role || '')
        .split(',')
        .map((r) => r.trim())
        .filter(Boolean)
        .map((r) => r.replace(/^ROLE_/, ''))
      return roles.includes('STUDENT') && roles.includes('TEACHER')
    }
  },

  actions: {
    setSession(payload: Pick<TokenResponse, 'accessToken' | 'username' | 'role'>) {
      const { accessToken, username, role } = payload

      this.token = accessToken
      this.user = { username, role }
      this.isAuthenticated = true

      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('user', JSON.stringify(this.user))
      this.hydrateRoleContext()
    },

    async login(credentials: LoginPayload) {
      try {
        const response = await authService.login(credentials)
        const data = unwrapApiResponse<TokenResponse>(response)
        this.setSession(data)

        return true
      } catch (error) {
        throw error
      }
    },

    async logout() {
      try {
        await authService.logout()
      } catch {
      }
      this.token = null
      this.user = null
      this.isAuthenticated = false
      this.activeRole = null
      this.lastActiveRole = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      localStorage.removeItem('activeRole')
      localStorage.removeItem('lastActiveRole')
      router.push({ name: 'Login' })
    },

    hasAnyRole(required: string[]) {
      return required.some((r) => this.roles.includes(r))
    },

    hasPermissions(required: string[]) {
      if (!required?.length) return true
      const set = new Set(this.permissions)
      return required.every((p) => set.has(p))
    },

    hydrateRoleContext() {
      const roles = this.roles
      const last = localStorage.getItem('lastActiveRole')

      if (last && roles.includes(last)) {
        this.activeRole = last
        this.lastActiveRole = last
        localStorage.setItem('activeRole', last)
        localStorage.setItem('lastActiveRole', last)
        return
      }

      if (roles.length === 1) {
        this.setActiveRole(roles[0])
        return
      }

      if (roles.includes('STUDENT')) {
        this.setActiveRole('STUDENT')
        return
      }

      if (roles.length) {
        this.setActiveRole(roles[0])
      }
    },

    setActiveRole(role: string) {
      if (!this.roles.includes(role)) return
      this.activeRole = role
      this.lastActiveRole = role
      localStorage.setItem('activeRole', role)
      localStorage.setItem('lastActiveRole', role)
    },

    defaultRouteNameForRole(role: string | null): string {
      if (role === 'ADMIN') return 'AdminHome'
      if (role === 'STUDENT') return 'StudentHome'
      if (role === 'TEACHER') return 'TeacherHome'
      return 'Root'
    },

    redirectAfterLogin(redirect?: string | null) {
      if (redirect) {
        router.replace(redirect)
        return
      }
      const name = this.defaultRouteNameForRole(this.activeRole)
      router.replace({ name })
    },

    initialize() {
    }
  },
})
