import type { PageResponse } from '../types/pagination'
import type {
  CreateDoctorRequest,
  CreateNurseRequest,
  Doctor,
  DoctorFilters,
  Nurse,
  NurseFilters,
  StaffDepartment,
  StaffUser,
  UpdateDoctorRequest,
  UpdateNurseRequest,
} from '../types/staff'
import { http } from './http'

interface UserResponse extends StaffUser {
  role: string
  isActive: boolean
}

export async function getDoctors(
  filters: DoctorFilters,
): Promise<PageResponse<Doctor>> {
  const response = await http.get<PageResponse<Doctor>>(
    '/doctors/page',
    {
      params: {
        text: filters.text || undefined,
        departmentId: filters.departmentId,
        specialty: filters.specialty,
        isActive: filters.isActive,
        minimumExperience: filters.minimumExperience,
        maximumExperience: filters.maximumExperience,
        page: filters.page,
        size: filters.size,
        sort: 'user.lastName,asc',
      },
    },
  )

  return response.data
}

export async function createDoctor(
  request: CreateDoctorRequest,
): Promise<Doctor> {
  const response = await http.post<Doctor>('/doctors', request)
  return response.data
}

export async function updateDoctor(
  id: number,
  request: UpdateDoctorRequest,
): Promise<Doctor> {
  const response = await http.put<Doctor>(
    `/doctors/${id}`,
    request,
  )

  return response.data
}

export async function deleteDoctor(id: number): Promise<void> {
  await http.delete(`/doctors/${id}`)
}

export async function getNurses(
  filters: NurseFilters,
): Promise<PageResponse<Nurse>> {
  const response = await http.get<PageResponse<Nurse>>(
    '/nurses/page',
    {
      params: {
        text: filters.text || undefined,
        departmentId: filters.departmentId,
        specialty: filters.specialty,
        shiftType: filters.shiftType,
        isActive: filters.isActive,
        isChargeNurse: filters.isChargeNurse,
        minimumExperience: filters.minimumExperience,
        maximumExperience: filters.maximumExperience,
        hiredFrom: filters.hiredFrom || undefined,
        hiredTo: filters.hiredTo || undefined,
        page: filters.page,
        size: filters.size,
        sort: 'user.lastName,asc',
      },
    },
  )

  return response.data
}

export async function createNurse(
  request: CreateNurseRequest,
): Promise<Nurse> {
  const response = await http.post<Nurse>('/nurses', request)
  return response.data
}

export async function updateNurse(
  id: number,
  request: UpdateNurseRequest,
): Promise<Nurse> {
  const response = await http.put<Nurse>(
    `/nurses/${id}`,
    request,
  )

  return response.data
}

export async function deleteNurse(id: number): Promise<void> {
  await http.delete(`/nurses/${id}`)
}

export async function getActiveDepartments():
Promise<StaffDepartment[]> {
  const response = await http.get<StaffDepartment[]>(
    '/departments/active/ordered',
  )

  return response.data
}

async function getAvailableUsers(
  role: 'DOCTOR' | 'NURSE',
  profilePath: 'doctors' | 'nurses',
): Promise<StaffUser[]> {
  const response = await http.get<PageResponse<UserResponse>>(
    '/users/page',
    {
      params: {
        role,
        isActive: true,
        page: 0,
        size: 100,
        sort: 'lastName,asc',
      },
    },
  )

  const availability = await Promise.all(
    response.data.content.map(async (user) => {
      const exists = await http.get<boolean>(
        `/${profilePath}/exists/user/${user.id}`,
      )

      return exists.data ? null : user
    }),
  )

  return availability.filter(
    (user): user is UserResponse => user !== null,
  )
}

export function getAvailableDoctorUsers(): Promise<StaffUser[]> {
  return getAvailableUsers('DOCTOR', 'doctors')
}

export function getAvailableNurseUsers(): Promise<StaffUser[]> {
  return getAvailableUsers('NURSE', 'nurses')
}