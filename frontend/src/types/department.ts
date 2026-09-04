export const departmentTypes = [
  'EMERGENCY',
  'CARDIOLOGY',
  'PEDIATRICS',
  'RADIOLOGY',
  'SURGERY',
  'INTERNAL_MEDICINE',
  'LABORATORY',
  'TRAUMATOLOGY',
] as const

export type DepartmentType =
  (typeof departmentTypes)[number]

export interface Department {
  id: number
  departmentType: DepartmentType
  location: string
  phoneExtension: string | null
  description: string | null
  isActive: boolean
  totalWards: number
  createdAt: string
  updatedAt: string
}

export interface DepartmentFilters {
  departmentType?: DepartmentType
  isActive?: boolean
  location?: string
  description?: string
  page: number
  size: number
}

export const departmentTypeLabels:
  Record<DepartmentType, string> = {
    EMERGENCY: 'Urgencias',
    CARDIOLOGY: 'Cardiología',
    PEDIATRICS: 'Pediatría',
    RADIOLOGY: 'Radiología',
    SURGERY: 'Cirugía',
    INTERNAL_MEDICINE: 'Medicina interna',
    LABORATORY: 'Laboratorio',
    TRAUMATOLOGY: 'Traumatología',
  }


  export interface CreateDepartmentRequest {
  departmentType: DepartmentType
  location: string
  phoneExtension: string
  description: string
}