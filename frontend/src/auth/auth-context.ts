import { createContext } from 'react'
import type {
  AuthUser,
  LoginRequest,
} from '../types/auth'

export interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  login: (credentials: LoginRequest) => Promise<void>
  logout: () => void
}

export const AuthContext =
  createContext<AuthContextValue | null>(null)

