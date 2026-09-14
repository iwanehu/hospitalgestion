import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react'
import type { ReactNode } from 'react'
import { loginRequest } from '../api/auth-api'
import {
  clearStoredSession,
  createSession,
  getStoredSession,
  storeSession,
} from './auth-storage'
import {
  AuthContext,
  type AuthContextValue,
} from './auth-context'
import type {
  AuthSession,
  LoginRequest,
} from '../types/auth'

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({
  children,
}: AuthProviderProps) {
  const [session, setSession] =
    useState<AuthSession | null>(() => getStoredSession())

  const login = useCallback(
    async (credentials: LoginRequest) => {
      const response = await loginRequest(credentials)
      const newSession = createSession(response)

      storeSession(newSession)
      setSession(newSession)
    },
    [],
  )

  const logout = useCallback(() => {
    clearStoredSession()
    setSession(null)
  }, [])

  useEffect(() => {
    const handleUnauthorized = () => {
      setSession(null)
    }

    window.addEventListener(
      'hospital:unauthorized',
      handleUnauthorized,
    )

    return () => {
      window.removeEventListener(
        'hospital:unauthorized',
        handleUnauthorized,
      )
    }
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user: session?.user ?? null,
      token: session?.token ?? null,
      isAuthenticated: session !== null,
      login,
      logout,
    }),
    [session, login, logout],
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
