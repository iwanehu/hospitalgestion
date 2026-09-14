export type BedStatus =
  | 'AVAILABLE'
  | 'OCCUPIED'
  | 'CLEANING'
  | 'MAINTENANCE'
  | 'RESERVED'

export interface Bed {
  id: number
  bedNumber: string
  status: BedStatus
  isOccupied: boolean
  roomId: number
  roomNumber: string
  roomFloor: number
  roomType: string
  wardId: number
  wardName: string
  departmentId: number
  departmentType: string
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface BedFilters {
  bedNumber?: string
  status?: BedStatus
  roomId?: number
  wardId?: number
  departmentId?: number
  page: number
  size: number
}

export interface CreateBedRequest {
  bedNumber: string
  roomId: number
  notes: string
}

export interface UpdateBedRequest {
  bedNumber: string
  notes: string
}

export type BedAction =
  | 'reserve'
  | 'occupy'
  | 'release'
  | 'finish-cleaning'
  | 'maintenance'
  | 'finish-maintenance'
  | 'cancel-reservation'

export const bedStatusLabels: Record<BedStatus, string> = {
  AVAILABLE: 'Disponible',
  OCCUPIED: 'Ocupada',
  CLEANING: 'Limpieza',
  MAINTENANCE: 'Mantenimiento',
  RESERVED: 'Reservada',
}
