export type UserRole =
  | 'ADMIN'
  | 'DOCTOR'
  | 'NURSE'
  | 'RECEPTIONIST'
  | 'PATIENT'

export interface LoginRequest {
  email: string
  password: string
}

export interface AuthUser {
  id: number
  email: string
  role: UserRole
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: number
  email: string
  role: UserRole
}

export interface AuthSession {
  token: string
  user: AuthUser
}
