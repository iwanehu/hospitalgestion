export type BloodType =
  | 'A_POSITIVE'
  | 'A_NEGATIVE'
  | 'B_POSITIVE'
  | 'B_NEGATIVE'
  | 'AB_POSITIVE'
  | 'AB_NEGATIVE'
  | 'O_POSITIVE'
  | 'O_NEGATIVE'

export interface Patient {
  id: number
  userId: number
  fullName: string
  email: string
  documentId: string
  phone: string | null
  bloodType: BloodType | null
  birthDate: string
  emergencyContactName: string | null
  emergencyContactPhone: string | null
  emergencyContactRelationship: string | null
  allergies: string | null
  hasHealthInsurance: boolean
  healthInsuranceProvider: string | null
  healthInsuranceNumber: string | null
  medicalHistory: string | null
  createdAt: string
  updatedAt: string
}

export interface PatientFilters {
  text?: string
  bloodType?: BloodType
  hasHealthInsurance?: boolean
  insuranceProvider?: string
  isActive?: boolean
  birthDateFrom?: string
  birthDateTo?: string
  page: number
  size: number
}

export interface CreatePatientRequest {
  userId: number
  bloodType: BloodType | null
  birthDate: string
  emergencyContactName: string
  emergencyContactPhone: string
  emergencyContactRelationship: string
  allergies: string
  hasHealthInsurance: boolean
  healthInsuranceProvider: string
  healthInsuranceNumber: string
  medicalHistory: string
}

export type UpdatePatientRequest = Omit<
  CreatePatientRequest,
  'userId' | 'birthDate'
>

export interface PatientUser {
  id: number
  email: string
  documentId: string
  firstName: string
  lastName: string
  phone: string | null
}

export const bloodTypeLabels: Record<BloodType, string> = {
  A_POSITIVE: 'A+',
  A_NEGATIVE: 'A−',
  B_POSITIVE: 'B+',
  B_NEGATIVE: 'B−',
  AB_POSITIVE: 'AB+',
  AB_NEGATIVE: 'AB−',
  O_POSITIVE: 'O+',
  O_NEGATIVE: 'O−',
}
