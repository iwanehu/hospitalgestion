import { http } from './http'
import type { PageResponse } from '../types/pagination'
import type {
  Bed,
  BedAction,
  BedFilters,
  CreateBedRequest,
  UpdateBedRequest,
} from '../types/bed'

export async function getBeds(filters: BedFilters): Promise<PageResponse<Bed>> {
  const response = await http.get<PageResponse<Bed>>('/beds/page', {
    params: {
      bedNumber: filters.bedNumber || undefined,
      status: filters.status,
      roomId: filters.roomId,
      wardId: filters.wardId,
      departmentId: filters.departmentId,
      page: filters.page,
      size: filters.size,
      sort: 'bedNumber,asc',
    },
  })
  return response.data
}

export async function createBed(request: CreateBedRequest): Promise<Bed> {
  const response = await http.post<Bed>('/beds', request)
  return response.data
}

export async function updateBed(id: number, request: UpdateBedRequest): Promise<Bed> {
  const response = await http.put<Bed>(`/beds/${id}`, request)
  return response.data
}

export async function performBedAction(id: number, action: BedAction): Promise<Bed> {
  const response = await http.patch<Bed>(`/beds/${id}/${action}`)
  return response.data
}

export async function deleteBed(id: number): Promise<void> {
  await http.delete(`/beds/${id}`)
}
