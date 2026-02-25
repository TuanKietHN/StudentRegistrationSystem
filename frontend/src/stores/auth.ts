import { defineStore } from 'pinia';
import router from '@/router';
import { authService, type LoginPayload, type TokenResponse } from '@/api/services/auth.service';
import { unwrapApiResponse } from '@/api/response';

interface User {
  username: string;
  role: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: localStorage.getItem('accessToken') || null,
    isAuthenticated: !!localStorage.getItem('accessToken'),
  }),

  getters: {
    currentUser: (state) => state.user,
    isLoggedIn: (state) => state.isAuthenticated,
  },

  actions: {
    setSession(payload: Pick<TokenResponse, 'accessToken' | 'username' | 'role'>) {
      const { accessToken, username, role } = payload;

      this.token = accessToken;
      this.user = { username, role };
      this.isAuthenticated = true;

      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('user', JSON.stringify(this.user));
    },

    async login(credentials: LoginPayload) {
      try {
        const response = await authService.login(credentials);
        const data = unwrapApiResponse<TokenResponse>(response);
        this.setSession(data);

        return true;
      } catch (error) {
        throw error;
      }
    },

    async logout() {
      try {
        await authService.logout()
      } catch {
      }
      this.token = null;
      this.user = null;
      this.isAuthenticated = false;
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      router.push('/login');
    },

    initialize() {
    }
  },
});
