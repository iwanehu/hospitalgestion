import { http } from './http'
import type { PageResponse } from '../types/pagination'
import type {
  CreateWardRequest,
  UpdateWardRequest,
  Ward,
  WardFilters,
} from '../types/ward'

export async function getWards(
  filters: WardFilters,
): Promise<PageResponse<Ward>> {
  const response = await http.get<PageResponse<Ward>>(
    '/wards/page',
    {
      params: {
        name: filters.name || undefined,
        description: filters.description || undefined,
        isActive: filters.isActive,
        departmentId: filters.departmentId,
        page: filters.page,
        size: filters.size,
        sort: 'name,asc',
      },
    },
  )

  return response.data
}

export async function createWard(
  request: CreateWardRequest,
): Promise<Ward> {
  const response = await http.post<Ward>(
    '/wards',
    request,
  )

  return response.data
}

export async function updateWard(
  id: number,
  request: UpdateWardRequest,
): Promise<Ward> {
  const response = await http.put<Ward>(
    `/wards/${id}`,
    request,
  )

  return response.data
}

export async function activateWard(
  id: number,
): Promise<void> {
  await http.patch(`/wards/${id}/activate`)
}

export async function deactivateWard(
  id: number,
): Promise<void> {
  await http.patch(`/wards/${id}/deactivate`)
}

export async function deleteWard(
  id: number,
): Promise<void> {
  await http.delete(`/wards/${id}`)
}
