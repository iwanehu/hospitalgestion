export type Specialty =
  | 'EMERGENCY_MEDICINE'
  | 'CARDIOLOGY'
  | 'PEDIATRICS'
  | 'RADIOLOGY'
  | 'LABORATORY'
  | 'GENERAL_SURGERY'
  | 'NEUROSURGERY'
  | 'CARDIAC_SURGERY'
  | 'PLASTIC_SURGERY'
  | 'INTERNAL_MEDICINE'
  | 'CLINICAL_PATHOLOGY'
  | 'MICROBIOLOGY'
  | 'TRAUMATOLOGY'

export type NurseSpecialty =
  | 'GENERAL'
  | 'EMERGENCY'
  | 'ICU'
  | 'PEDIATRICS'
  | 'CARDIOLOGY'
  | 'SURGERY'
  | 'TRAUMATOLOGY'
  | 'RADIOLOGY'
  | 'LABORATORY'
  | 'NEONATAL'
  | 'ONCOLOGY'
  | 'NEPHROLOGY'
  | 'GERIATRICS'
  | 'OBSTETRICS'
  | 'MENTAL_HEALTH'

export type ShiftType =
  | 'MORNING'
  | 'AFTERNOON'
  | 'NIGHT'
  | 'ROTATING'

export interface StaffUser {
  id: number
  email: string
  firstName: string
  lastName: string
  documentId: string
  role: string
  isActive: boolean
}

export interface StaffDepartment {
  id: number
  departmentType: string
  location: string
  isActive: boolean
}

export interface Doctor {
  id: number
  userId: number
  fullName: string
  email: string
  departmentId: number
  departmentType: string
  specialty: Specialty
  medicalLicenseNumber: string
  yearsOfExperience: number
  biography: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateDoctorRequest {
  userId: number
  departmentId: number
  specialty: Specialty
  medicalLicenseNumber: string
  yearsOfExperience: number
  biography?: string
}

export interface UpdateDoctorRequest {
  departmentId: number
  specialty: Specialty
  yearsOfExperience: number
  biography?: string
}

export interface DoctorFilters {
  page: number
  size: number
  text?: string
  departmentId?: number
  specialty?: Specialty
  isActive?: boolean
  minimumExperience?: number
  maximumExperience?: number
}

export interface Nurse {
  id: number
  userId: number
  fullName: string
  email: string
  departmentId: number
  departmentType: string
  licenseNumber: string
  specialty: NurseSpecialty
  shiftType: ShiftType
  yearsOfExperience: number
  hireDate: string | null
  biography: string | null
  emergencyContactName: string
  emergencyContactPhone: string
  emergencyContactRelationship: string
  maxPatientsPerShift: number
  isChargeNurse: boolean
  vacationDaysAvailable: number
  createdAt: string
  updatedAt: string
}

export interface CreateNurseRequest {
  userId: number
  departmentId: number
  licenseNumber: string
  specialty: NurseSpecialty
  shiftType: ShiftType
  yearsOfExperience: number
  hireDate?: string
  biography?: string
  emergencyContactName: string
  emergencyContactPhone: string
  emergencyContactRelationship: string
  maxPatientsPerShift: number
  isChargeNurse: boolean
  vacationDaysAvailable: number
}

export interface UpdateNurseRequest {
  departmentId: number
  specialty: NurseSpecialty
  shiftType: ShiftType
  yearsOfExperience: number
  hireDate?: string
  biography?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  emergencyContactRelationship?: string
  maxPatientsPerShift?: number
  isChargeNurse?: boolean
  vacationDaysAvailable?: number
}

export interface NurseFilters {
  page: number
  size: number
  text?: string
  departmentId?: number
  specialty?: NurseSpecialty
  shiftType?: ShiftType
  isActive?: boolean
  isChargeNurse?: boolean
  minimumExperience?: number
  maximumExperience?: number
  hiredFrom?: string
  hiredTo?: string
}

export const specialtyLabels: Record<Specialty, string> = {
  EMERGENCY_MEDICINE: 'Medicina de urgencias',
  CARDIOLOGY: 'Cardiología',
  PEDIATRICS: 'Pediatría',
  RADIOLOGY: 'Radiología',
  LABORATORY: 'Laboratorio',
  GENERAL_SURGERY: 'Cirugía general',
  NEUROSURGERY: 'Neurocirugía',
  CARDIAC_SURGERY: 'Cirugía cardíaca',
  PLASTIC_SURGERY: 'Cirugía plástica',
  INTERNAL_MEDICINE: 'Medicina interna',
  CLINICAL_PATHOLOGY: 'Patología clínica',
  MICROBIOLOGY: 'Microbiología',
  TRAUMATOLOGY: 'Traumatología',
}

export const nurseSpecialtyLabels: Record<NurseSpecialty, string> = {
  GENERAL: 'General',
  EMERGENCY: 'Urgencias',
  ICU: 'Cuidados intensivos',
  PEDIATRICS: 'Pediatría',
  CARDIOLOGY: 'Cardiología',
  SURGERY: 'Cirugía',
  TRAUMATOLOGY: 'Traumatología',
  RADIOLOGY: 'Radiología',
  LABORATORY: 'Laboratorio',
  NEONATAL: 'Neonatal',
  ONCOLOGY: 'Oncología',
  NEPHROLOGY: 'Nefrología',
  GERIATRICS: 'Geriatría',
  OBSTETRICS: 'Obstetricia',
  MENTAL_HEALTH: 'Salud mental',
}

export const shiftTypeLabels: Record<ShiftType, string> = {
  MORNING: 'Mañana · 06:00–14:00',
  AFTERNOON: 'Tarde · 14:00–22:00',
  NIGHT: 'Noche · 22:00–06:00',
  ROTATING: 'Turnos rotativos',
}