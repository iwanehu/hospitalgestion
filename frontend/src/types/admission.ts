import type { Bed } from './bed'
import type { Patient } from './patient'
import type { Doctor } from './staff'

export type AdmissionStatus =
  | 'ACTIVE'
  | 'DISCHARGED'
  | 'TRANSFERRED'
  | 'CANCELLED'

export interface Admission {
  id: number

  patientId: number
  patientName: string

  attendingDoctorId: number
  attendingDoctorName: string

  bedId: number
  bedNumber: string

  roomId: number
  roomNumber: string

  wardId: number
  wardName: string

  departmentId: number
  departmentType: string

  status: AdmissionStatus
  admissionReason: string

  admittedAt: string
  dischargedAt: string | null

  notes: string | null

  createdAt: string
  updatedAt: string
}

export interface AdmissionFilters {
  status?: AdmissionStatus
  patientId?: number
  doctorId?: number
  bedId?: number
  roomId?: number
  wardId?: number
  departmentId?: number
  admittedFrom?: string
  admittedTo?: string
  page: number
  size: number
}

export interface CreateAdmissionRequest {
  patientId: number
  bedId: number
  attendingDoctorId: number
  admissionReason: string
  notes?: string
}

export interface UpdateAdmissionRequest {
  attendingDoctorId: number
  admissionReason?: string
  notes?: string
}

export interface DischargeAdmissionRequest {
  notes?: string
}

export interface TransferAdmissionRequest {
  newBedId: number
  reason?: string
}

export interface AdmissionFormOptions {
  patients: Patient[]
  doctors: Doctor[]
  beds: Bed[]
}

export const admissionStatusLabels:
Record<AdmissionStatus, string> = {
  ACTIVE: 'Activo',
  DISCHARGED: 'Alta médica',
  TRANSFERRED: 'Trasladado',
  CANCELLED: 'Cancelado',
}