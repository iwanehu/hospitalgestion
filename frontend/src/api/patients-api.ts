import { http } from './http'
import type { PageResponse } from '../types/pagination'
import type {
  CreatePatientRequest,
  Patient,
  PatientFilters,
  PatientUser,
  UpdatePatientRequest,
} from '../types/patient'

interface UserResponse extends PatientUser {
  role: string
  isActive: boolean
}

export async function getPatients(
  filters: PatientFilters,
): Promise<PageResponse<Patient>> {
  const response = await http.get<PageResponse<Patient>>('/patients/page', {
    params: {
      text: filters.text || undefined,
      bloodType: filters.bloodType,
      hasHealthInsurance: filters.hasHealthInsurance,
      insuranceProvider: filters.insuranceProvider || undefined,
      isActive: filters.isActive,
      birthDateFrom: filters.birthDateFrom || undefined,
      birthDateTo: filters.birthDateTo || undefined,
      page: filters.page,
      size: filters.size,
      sort: 'user.lastName,asc',
    },
  })
  return response.data
}

export async function createPatient(request: CreatePatientRequest): Promise<Patient> {
  const response = await http.post<Patient>('/patients', request)
  return response.data
}

export async function updatePatient(
  id: number,
  request: UpdatePatientRequest,
): Promise<Patient> {
  const response = await http.patch<Patient>(`/patients/${id}`, request)
  return response.data
}

export async function deletePatient(id: number): Promise<void> {
  await http.delete(`/patients/${id}`)
}

export async function getAvailablePatientUsers(): Promise<PatientUser[]> {
  const response = await http.get<PageResponse<UserResponse>>('/users/page', {
    params: { role: 'PATIENT', isActive: true, page: 0, size: 100, sort: 'lastName,asc' },
  })

  const availability = await Promise.all(
    response.data.content.map(async (user) => {
      const exists = await http.get<boolean>(`/patients/exists/user/${user.id}`)
      return exists.data ? null : user
    }),
  )

  return availability.filter((user): user is UserResponse => user !== null)
}
