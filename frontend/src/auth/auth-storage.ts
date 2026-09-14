import type { AuthSession, LoginResponse } from '../types/auth'

const AUTH_STORAGE_KEY = 'hospital-auth-session'

export function createSession(
  response: LoginResponse,
): AuthSession {
  return {
    token: response.accessToken,
    user: {
      id: response.userId,
      email: response.email,
      role: response.role,
    },
  }
}

export function getStoredSession(): AuthSession | null {
  const storedSession =
    sessionStorage.getItem(AUTH_STORAGE_KEY)

  if (!storedSession) {
    return null
  }

  try {
    return JSON.parse(storedSession) as AuthSession
  } catch {
    sessionStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

export function storeSession(session: AuthSession): void {
  sessionStorage.setItem(
    AUTH_STORAGE_KEY,
    JSON.stringify(session),
  )
}

export function clearStoredSession(): void {
  sessionStorage.removeItem(AUTH_STORAGE_KEY)
}
