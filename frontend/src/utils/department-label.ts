import {
  departmentTypeLabels,
  type DepartmentType,
} from '../types/department'

export function getDepartmentLabel(
  value: string,
): string {
  if (value in departmentTypeLabels) {
    return departmentTypeLabels[
      value as DepartmentType
    ]
  }

  return value
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/^./, (letter) => letter.toUpperCase())
}