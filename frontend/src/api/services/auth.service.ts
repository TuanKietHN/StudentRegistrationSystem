import api from '@/api/axios'

export interface LoginPayload {
  username: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken?: string | null
  tokenType: string
  expiresIn: number
  username: string
  role: string
}

export interface RegisterPayload {
  username: string
  email: string
  password: string
  role?: string
}

export interface ForgotPasswordPayload {
  email: string
}

export interface ResetPasswordPayload {
  token: string
  newPassword: string
}

export const authService = {
  login(payload: LoginPayload) {
    return api.post('/v1/auth/login', payload)
  },
  refresh() {
    return api.post('/v1/auth/refresh', {})
  },
  logout() {
    return api.post('/v1/auth/logout', {})
  },
  register(payload: RegisterPayload) {
    return api.post('/v1/auth/register', payload)
  },
  forgotPassword(payload: ForgotPasswordPayload) {
    return api.post('/v1/auth/forgot-password', payload)
  },
  resetPassword(payload: ResetPasswordPayload) {
    return api.post('/v1/auth/reset-password', payload)
  }
}
