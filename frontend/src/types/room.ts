export type RoomType =
  | 'EMERGENCY_OBSERVATION'
  | 'EMERGENCY_ISOLATION'
  | 'CARDIOLOGY_ROOM'
  | 'CARDIOLOGY_ICU'
  | 'PEDIATRIC_ROOM'
  | 'PEDIATRIC_ICU'
  | 'RADIOLOGY_PROCEDURE_ROOM'
  | 'MRI_ROOM'
  | 'CT_ROOM'
  | 'SURGERY_PREOP'
  | 'SURGERY_POSTOP_RECOVERY'
  | 'SURGERY_ISOLATION'
  | 'INTERNAL_MEDICINE_ROOM'
  | 'LABORATORY_PROCEDURE_ROOM'
  | 'TRAUMATOLOGY_ROOM'
  | 'TRAUMATOLOGY_ICU'
  | 'INDIVIDUAL'
  | 'DOUBLE'
  | 'SHARED'
  | 'ISOLATION'

export type RoomStatus =
  | 'AVAILABLE'
  | 'OCCUPIED'
  | 'RESERVED'
  | 'CLEANING'
  | 'MAINTENANCE'

export interface Room {
  id: number
  number: string
  floor: number
  roomType: RoomType
  status: RoomStatus
  capacity: number
  totalBeds: number
  availableBeds: number
  occupiedBeds: number
  wardId: number
  wardName: string
  departmentId: number
  departmentType: string
  notes: string | null
  createdAt: string
  updatedAt: string
}

export interface RoomFilters {
  number?: string
  floor?: number
  roomType?: RoomType
  status?: RoomStatus
  wardId?: number
  departmentId?: number
  page: number
  size: number
}

export interface CreateRoomRequest {
  number: string
  floor: number
  roomType: RoomType
  capacity: number
  wardId: number
  notes: string
}

export interface UpdateRoomRequest {
  number: string
  floor: number
  roomType: RoomType
  capacity: number
  notes: string
}

export const roomTypeLabels: Record<RoomType, string> = {
  EMERGENCY_OBSERVATION: 'Observación de urgencias',
  EMERGENCY_ISOLATION: 'Aislamiento de urgencias',
  CARDIOLOGY_ROOM: 'Habitación de cardiología',
  CARDIOLOGY_ICU: 'UCI de cardiología',
  PEDIATRIC_ROOM: 'Habitación pediátrica',
  PEDIATRIC_ICU: 'UCI pediátrica',
  RADIOLOGY_PROCEDURE_ROOM: 'Sala de radiología',
  MRI_ROOM: 'Resonancia magnética',
  CT_ROOM: 'Tomografía',
  SURGERY_PREOP: 'Preoperatorio',
  SURGERY_POSTOP_RECOVERY: 'Recuperación posoperatoria',
  SURGERY_ISOLATION: 'Aislamiento quirúrgico',
  INTERNAL_MEDICINE_ROOM: 'Medicina interna',
  LABORATORY_PROCEDURE_ROOM: 'Procedimientos de laboratorio',
  TRAUMATOLOGY_ROOM: 'Habitación de traumatología',
  TRAUMATOLOGY_ICU: 'UCI de traumatología',
  INDIVIDUAL: 'Individual',
  DOUBLE: 'Doble',
  SHARED: 'Compartida',
  ISOLATION: 'Aislamiento',
}

export const roomStatusLabels: Record<RoomStatus, string> = {
  AVAILABLE: 'Disponible',
  OCCUPIED: 'Ocupada',
  RESERVED: 'Reservada',
  CLEANING: 'Limpieza',
  MAINTENANCE: 'Mantenimiento',
}
