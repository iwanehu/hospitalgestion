import { http } from './http'
import type {
  CreateDepartmentRequest,
  Department,
  DepartmentFilters,
} from '../types/department'
import type { PageResponse } from '../types/pagination'





export async function getDepartments(
  filters: DepartmentFilters,
): Promise<PageResponse<Department>> {
  const response = await http.get<PageResponse<Department>>(
    '/departments/page',
    {
      params: {
        departmentType: filters.departmentType,
        isActive: filters.isActive,
        location: filters.location || undefined,
        description: filters.description || undefined,
        page: filters.page,
        size: filters.size,
        sort: 'departmentType,asc',
      },
    },
  )

  return response.data
}

export async function activateDepartment(
  id: number,
): Promise<void> {
  await http.patch(`/departments/${id}/activate`)
}

export async function deactivateDepartment(
  id: number,
): Promise<void> {
  await http.patch(`/departments/${id}/deactivate`)
}


export async function createDepartment(
  request: CreateDepartmentRequest,
): Promise<Department> {
  const response = await http.post<Department>(
    '/departments',
    request,
  )

  return response.data
}