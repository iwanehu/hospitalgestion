import axios from 'axios'
import {
  clearStoredSession,
  getStoredSession,
} from '../auth/auth-storage'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15_000,
})

http.interceptors.request.use((config) => {
  const session = getStoredSession()

  if (session?.token) {
    config.headers.Authorization =
      `Bearer ${session.token}`
  }

  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      getStoredSession()
    ) {
      clearStoredSession()

      window.dispatchEvent(
        new CustomEvent('hospital:unauthorized'),
      )
    }

    return Promise.reject(error)
  },
)
