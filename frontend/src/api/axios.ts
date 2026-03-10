import axios from 'axios';

const api = axios.create({
  baseURL: '/api', // Proxied by Vite or direct URL
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    const activeRole = localStorage.getItem('activeRole');
    if (activeRole) {
      config.headers['X-Active-Role'] = activeRole;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

type NotifyColor = 'success' | 'error' | 'info' | 'warning'

function dispatchNotify(text: string, color: NotifyColor = 'info', timeout = 2500) {
  window.dispatchEvent(new CustomEvent('api:notify', { detail: { text, color, timeout } }))
}

function clearSession() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('user');
}

const refreshClient = axios.create({
  baseURL: '/api',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json',
  },
})

let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  const response = await refreshClient.post('/v1/auth/refresh', {})
  const data = response.data?.data

  if (!data?.accessToken) {
    throw new Error('invalid_refresh_response')
  }

  localStorage.setItem('accessToken', data.accessToken)
  return data.accessToken
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error?.response?.status
    const originalRequest = error?.config as any

    if (status === 401) {
      const url: string = originalRequest?.url || ''
      const isAuthCall = url.includes('/v1/auth/login') || url.includes('/v1/auth/refresh')

      if (!isAuthCall && originalRequest && !originalRequest._retry) {
        originalRequest._retry = true
        try {
          refreshPromise ||= refreshAccessToken().finally(() => {
            refreshPromise = null
          })
          const newAccessToken = await refreshPromise
          originalRequest.headers = originalRequest.headers || {}
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return api(originalRequest)
        } catch {
          clearSession()
          if (!window.location.pathname.includes('/login')) {
            window.location.href = '/login'
          }
          return Promise.reject(error)
        }
      }

      clearSession()
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login'
      }
      return Promise.reject(error)
    }

    if (status === 403) {
      dispatchNotify('Không có quyền thực hiện thao tác này', 'warning', 3000)
    }

    return Promise.reject(error);
  }
);

export default api;
