import type { Patient } from './patient'
import type { Room } from './room'
import type { Doctor } from './staff'

export type AppointmentStatus =
  | 'SCHEDULED'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW'
  | 'RESCHEDULED'

export interface Appointment {
  id: number

  doctorId: number
  doctorName: string

  patientId: number
  patientName: string

  roomId: number | null
  roomNumber: string | null

  dateTime: string
  reason: string
  notes: string | null

  status: AppointmentStatus

  cancellationReason: string | null
  cancelledAt: string | null
  confirmedAt: string | null
  completedAt: string | null

  createdAt: string
  updatedAt: string
}

export interface AppointmentFilters {
  status?: AppointmentStatus
  patientId?: number
  doctorId?: number
  roomId?: number
  departmentId?: number
  dateTimeFrom?: string
  dateTimeTo?: string
  reason?: string
  page: number
  size: number
}

export interface CreateAppointmentRequest {
  doctorId: number
  patientId: number
  roomId?: number
  dateTime: string
  reason: string
  notes?: string
}

export interface UpdateAppointmentRequest {
  doctorId: number
  roomId?: number
  dateTime: string
  reason: string
  notes?: string
}

export interface CancelAppointmentRequest {
  reason: string
}

export interface AppointmentFormOptions {
  patients: Patient[]
  doctors: Doctor[]
  rooms: Room[]
}

export const appointmentStatusLabels:
Record<AppointmentStatus, string> = {
  SCHEDULED: 'Programada',
  CONFIRMED: 'Confirmada',
  IN_PROGRESS: 'En curso',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  NO_SHOW: 'No presentado',
  RESCHEDULED: 'Reprogramada',
}