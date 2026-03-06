import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: string[]
    permissions?: string[]
    activeRoleRequired?: 'ADMIN' | 'TEACHER' | 'STUDENT'
    guestOnly?: boolean
    title?: string
  }
}
