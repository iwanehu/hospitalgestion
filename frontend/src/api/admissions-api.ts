import type { Bed } from '../types/bed'
import type {
  Admission,
  AdmissionFilters,
  CreateAdmissionRequest,
  DischargeAdmissionRequest,
  TransferAdmissionRequest,
  UpdateAdmissionRequest,
} from '../types/admission'
import type { PageResponse } from '../types/pagination'
import type { Patient } from '../types/patient'
import type { Doctor } from '../types/staff'
import { http } from './http'

export async function getAdmissions(
  filters: AdmissionFilters,
): Promise<PageResponse<Admission>> {
  const response = await http.get<PageResponse<Admission>>(
    '/admissions/page',
    {
      params: {
        status: filters.status,
        patientId: filters.patientId,
        doctorId: filters.doctorId,
        bedId: filters.bedId,
        roomId: filters.roomId,
        wardId: filters.wardId,
        departmentId: filters.departmentId,
        admittedFrom: filters.admittedFrom,
        admittedTo: filters.admittedTo,
        page: filters.page,
        size: filters.size,
        sort: 'admittedAt,desc',
      },
    },
  )

  return response.data
}

export async function createAdmission(
  request: CreateAdmissionRequest,
): Promise<Admission> {
  const response = await http.post<Admission>(
    '/admissions',
    request,
  )

  return response.data
}

export async function updateAdmission(
  id: number,
  request: UpdateAdmissionRequest,
): Promise<Admission> {
  const response = await http.put<Admission>(
    `/admissions/${id}`,
    request,
  )

  return response.data
}

export async function dischargeAdmission(
  id: number,
  request: DischargeAdmissionRequest,
): Promise<Admission> {
  const response = await http.patch<Admission>(
    `/admissions/${id}/discharge`,
    request,
  )

  return response.data
}

export async function transferAdmission(
  id: number,
  request: TransferAdmissionRequest,
): Promise<Admission> {
  const response = await http.post<Admission>(
    `/admissions/${id}/transfer`,
    request,
  )

  return response.data
}

export async function cancelAdmission(
  id: number,
): Promise<Admission> {
  const response = await http.patch<Admission>(
    `/admissions/${id}/cancel`,
  )

  return response.data
}

export async function deleteAdmission(
  id: number,
): Promise<void> {
  await http.delete(`/admissions/${id}`)
}

export async function getAdmissionPatients():
Promise<Patient[]> {
  const response = await http.get<PageResponse<Patient>>(
    '/patients/page',
    {
      params: {
        isActive: true,
        page: 0,
        size: 100,
        sort: 'user.lastName,asc',
      },
    },
  )

  return response.data.content
}

export async function getAdmissionDoctors():
Promise<Doctor[]> {
  const response = await http.get<Doctor[]>(
    '/doctors/active/ordered',
  )

  return response.data
}

export async function getAvailableAdmissionBeds():
Promise<Bed[]> {
  const response = await http.get<Bed[]>(
    '/beds/status/AVAILABLE',
  )

  return response.data
}