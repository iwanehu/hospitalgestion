import type { DepartmentType } from './department'

export interface Ward {
  id: number
  name: string
  description: string | null
  isActive: boolean
  departmentId: number
  departmentType: DepartmentType
  totalRooms: number
  createdAt: string
  updatedAt: string
}

export interface WardFilters {
  name?: string
  description?: string
  isActive?: boolean
  departmentId?: number
  page: number
  size: number
}

export interface CreateWardRequest {
  name: string
  description: string
  departmentId: number
}

export interface UpdateWardRequest {
  name: string
  description: string
  isActive: boolean
}
