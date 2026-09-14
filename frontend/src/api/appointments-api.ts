import type {
  Appointment,
  AppointmentFilters,
  AppointmentStatus,
  CancelAppointmentRequest,
  CreateAppointmentRequest,
  UpdateAppointmentRequest,
} from '../types/appointment'
import type { PageResponse } from '../types/pagination'
import type { Patient } from '../types/patient'
import type { Room } from '../types/room'
import type { Doctor } from '../types/staff'
import { http } from './http'

export async function getAppointments(
  filters: AppointmentFilters,
): Promise<PageResponse<Appointment>> {
  const response =
    await http.get<PageResponse<Appointment>>(
      '/appointments/page',
      {
        params: {
          status: filters.status,
          patientId: filters.patientId,
          doctorId: filters.doctorId,
          roomId: filters.roomId,
          departmentId: filters.departmentId,
          dateTimeFrom: filters.dateTimeFrom,
          dateTimeTo: filters.dateTimeTo,
          reason: filters.reason || undefined,
          page: filters.page,
          size: filters.size,
          sort: 'dateTime,asc',
        },
      },
    )

  return response.data
}

export async function createAppointment(
  request: CreateAppointmentRequest,
): Promise<Appointment> {
  const response = await http.post<Appointment>(
    '/appointments',
    request,
  )

  return response.data
}

export async function updateAppointment(
  id: number,
  request: UpdateAppointmentRequest,
): Promise<Appointment> {
  const response = await http.put<Appointment>(
    `/appointments/${id}`,
    request,
  )

  return response.data
}

export async function updateAppointmentStatus(
  id: number,
  status: AppointmentStatus,
): Promise<Appointment> {
  const response = await http.patch<Appointment>(
    `/appointments/${id}/status`,
    { status },
  )

  return response.data
}

export async function cancelAppointment(
  id: number,
  request: CancelAppointmentRequest,
): Promise<Appointment> {
  const response = await http.patch<Appointment>(
    `/appointments/${id}/cancel`,
    request,
  )

  return response.data
}

export async function deleteAppointment(
  id: number,
): Promise<void> {
  await http.delete(`/appointments/${id}`)
}

export async function getAppointmentPatients():
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

export async function getAppointmentDoctors():
Promise<Doctor[]> {
  const response = await http.get<Doctor[]>(
    '/doctors/active/ordered',
  )

  return response.data
}

export async function getAppointmentRooms():
Promise<Room[]> {
  const response = await http.get<PageResponse<Room>>(
    '/rooms/page',
    {
      params: {
        page: 0,
        size: 100,
        sort: 'number,asc',
      },
    },
  )

  return response.data.content
}