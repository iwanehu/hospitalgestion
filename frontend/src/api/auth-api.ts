import { http } from './http'
import type {
  LoginRequest,
  LoginResponse,
} from '../types/auth'

export async function loginRequest(
  credentials: LoginRequest,
): Promise<LoginResponse> {
  const response = await http.post<LoginResponse>(
    '/auth/login',
    credentials,
  )

  return response.data
}
