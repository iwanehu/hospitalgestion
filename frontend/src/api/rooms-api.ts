import { http } from './http'
import type { PageResponse } from '../types/pagination'
import type {
  CreateRoomRequest,
  Room,
  RoomFilters,
  RoomStatus,
  UpdateRoomRequest,
} from '../types/room'

export async function getRooms(
  filters: RoomFilters,
): Promise<PageResponse<Room>> {
  const response = await http.get<PageResponse<Room>>('/rooms/page', {
    params: {
      number: filters.number || undefined,
      floor: filters.floor,
      roomType: filters.roomType,
      status: filters.status,
      wardId: filters.wardId,
      departmentId: filters.departmentId,
      page: filters.page,
      size: filters.size,
      sort: 'number,asc',
    },
  })
  return response.data
}

export async function createRoom(request: CreateRoomRequest): Promise<Room> {
  const response = await http.post<Room>('/rooms', request)
  return response.data
}

export async function updateRoom(
  id: number,
  request: UpdateRoomRequest,
): Promise<Room> {
  const response = await http.put<Room>(`/rooms/${id}`, request)
  return response.data
}

export async function updateRoomStatus(
  id: number,
  status: RoomStatus,
  notes?: string,
): Promise<Room> {
  const response = await http.patch<Room>(`/rooms/${id}/status`, {
    status,
    notes,
  })
  return response.data
}

export async function deleteRoom(id: number): Promise<void> {
  await http.delete(`/rooms/${id}`)
}
