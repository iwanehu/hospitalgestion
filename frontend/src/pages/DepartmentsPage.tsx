import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import {
  Building2,
  ChevronLeft,
  ChevronRight,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Trash2,
} from 'lucide-react'
import { useState } from 'react'
import {
  activateDepartment,
  deactivateDepartment,
  getDepartments,
} from '../api/departments-api'
import { useAuth } from '../auth/useAuth'
import { DepartmentFormModal } from '../components/DepartmentFormModal'
import {
  departmentTypeLabels,
  departmentTypes,
} from '../types/department'
import type {
  Department,
  DepartmentFilters,
  DepartmentType,
} from '../types/department'



import { DeleteDepartmentModal } from '../components/DeleteDepartmentModal'

export function DepartmentsPage() {
  const { user } = useAuth()
  const queryClient = useQueryClient()

  const [page, setPage] = useState(0)
  const [location, setLocation] = useState('')
  const [searchLocation, setSearchLocation] = useState('')

  const [departmentType, setDepartmentType] =
    useState<DepartmentType | ''>('')

  const [activeStatus, setActiveStatus] =
    useState<'all' | 'active' | 'inactive'>('all')

  const [formOpen, setFormOpen] = useState(false)

  const [selectedDepartment, setSelectedDepartment] =
    useState<Department | undefined>()



    const [departmentToDelete, setDepartmentToDelete] =
  useState<Department | undefined>()

  const isAdmin = user?.role === 'ADMIN'

  const filters: DepartmentFilters = {
    page,
    size: 10,
    location: searchLocation || undefined,
    departmentType: departmentType || undefined,
    isActive:
      activeStatus === 'all'
        ? undefined
        : activeStatus === 'active',
  }

  const departmentsQuery = useQuery({
    queryKey: ['departments', filters],
    queryFn: () => getDepartments(filters),
    placeholderData: keepPreviousData,
  })

  const statusMutation = useMutation({
    mutationFn: async (department: Department) => {
      if (department.isActive) {
        await deactivateDepartment(department.id)
        return
      }

      await activateDepartment(department.id)
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['departments'],
      })
    },
  })

  const handleSearch = () => {
    setPage(0)
    setSearchLocation(location.trim())
  }

  const handleReset = () => {
    setLocation('')
    setSearchLocation('')
    setDepartmentType('')
    setActiveStatus('all')
    setPage(0)
  }

  const openCreateForm = () => {
    setSelectedDepartment(undefined)
    setFormOpen(true)
  }

  const openEditForm = (department: Department) => {
    setSelectedDepartment(department)
    setFormOpen(true)
  }

  const closeForm = () => {
    setFormOpen(false)
    setSelectedDepartment(undefined)
  }

  const data = departmentsQuery.data

  return (
    <div className="mx-auto max-w-7xl">
      <section className="mb-7 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h2 className="text-2xl font-semibold text-slate-950">
            Departamentos
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Gestiona las áreas y servicios del hospital.
          </p>
        </div>

        {isAdmin && (
          <button
            type="button"
            onClick={openCreateForm}
            className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white transition hover:bg-cyan-800"
          >
            <Plus className="size-5" />
            Nuevo departamento
          </button>
        )}
      </section>

      <section className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div className="xl:col-span-2">
            <label
              htmlFor="department-location"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Ubicación
            </label>

            <div className="flex gap-2">
              <input
                id="department-location"
                value={location}
                onChange={(event) => {
                  setLocation(event.target.value)
                }}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    handleSearch()
                  }
                }}
                placeholder="Buscar por ubicación"
                className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
              />

              <button
                type="button"
                onClick={handleSearch}
                className="rounded-xl bg-slate-900 px-4 text-white transition hover:bg-slate-700"
                aria-label="Buscar"
              >
                <Search className="size-5" />
              </button>
            </div>
          </div>

          <div>
            <label
              htmlFor="department-type"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Tipo
            </label>

            <select
              id="department-type"
              value={departmentType}
              onChange={(event) => {
                setDepartmentType(
                  event.target.value as DepartmentType | '',
                )
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:ring-4 focus:ring-cyan-600/10"
            >
              <option value="">Todos</option>

              {departmentTypes.map((type) => (
                <option key={type} value={type}>
                  {departmentTypeLabels[type]}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label
              htmlFor="department-status"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Estado
            </label>

            <select
              id="department-status"
              value={activeStatus}
              onChange={(event) => {
                setActiveStatus(
                  event.target.value as
                    | 'all'
                    | 'active'
                    | 'inactive',
                )
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:ring-4 focus:ring-cyan-600/10"
            >
              <option value="all">Todos</option>
              <option value="active">Activos</option>
              <option value="inactive">Inactivos</option>
            </select>
          </div>
        </div>

        <button
          type="button"
          onClick={handleReset}
          className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-cyan-700"
        >
          <RefreshCw className="size-4" />
          Limpiar filtros
        </button>
      </section>

      <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <div>
            <p className="font-medium text-slate-900">
              Listado de departamentos
            </p>

            <p className="text-sm text-slate-500">
              {data
                ? `${data.totalElements} ${
                    data.totalElements === 1
                      ? 'resultado'
                      : 'resultados'
                  }`
                : 'Cargando resultados'}
            </p>
          </div>

          <Building2 className="size-6 text-cyan-700" />
        </div>

        {departmentsQuery.isPending && (
          <div className="space-y-3 p-6">
            {[1, 2, 3, 4].map((item) => (
              <div
                key={item}
                className="h-14 animate-pulse rounded-xl bg-slate-100"
              />
            ))}
          </div>
        )}

        {departmentsQuery.isError && (
          <div className="p-10 text-center">
            <p className="font-medium text-red-700">
              No se pudieron cargar los departamentos.
            </p>

            <button
              type="button"
              onClick={() => {
                void departmentsQuery.refetch()
              }}
              className="mt-4 rounded-xl bg-slate-900 px-4 py-2 text-sm text-white"
            >
              Intentar nuevamente
            </button>
          </div>
        )}

        {data && !data.empty && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[900px] text-left">
                <thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                  <tr>
                    <th className="px-5 py-4">
                      Departamento
                    </th>

                    <th className="px-5 py-4">
                      Ubicación
                    </th>

                    <th className="px-5 py-4">
                      Extensión
                    </th>

                    <th className="px-5 py-4">
                      Salas
                    </th>

                    <th className="px-5 py-4">
                      Estado
                    </th>

                    {isAdmin && (
                      <th className="px-5 py-4 text-right">
                        Acciones
                      </th>
                    )}
                  </tr>
                </thead>

                <tbody className="divide-y divide-slate-100">
                  {data.content.map((department) => (
                    <tr
                      key={department.id}
                      className="transition hover:bg-slate-50"
                    >
                      <td className="px-5 py-4">
                        <p className="font-medium text-slate-900">
                          {
                            departmentTypeLabels[
                              department.departmentType
                            ]
                          }
                        </p>

                        <p className="mt-1 max-w-xs truncate text-sm text-slate-500">
                          {department.description ||
                            'Sin descripción'}
                        </p>
                      </td>

                      <td className="px-5 py-4 text-sm text-slate-600">
                        {department.location}
                      </td>

                      <td className="px-5 py-4 text-sm text-slate-600">
                        {department.phoneExtension || '—'}
                      </td>

                      <td className="px-5 py-4 text-sm text-slate-600">
                        {department.totalWards}
                      </td>

                      <td className="px-5 py-4">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
                            department.isActive
                              ? 'bg-emerald-50 text-emerald-700'
                              : 'bg-slate-100 text-slate-600'
                          }`}
                        >
                          {department.isActive
                            ? 'Activo'
                            : 'Inactivo'}
                        </span>
                      </td>

                      {isAdmin && (
                        <td className="px-5 py-4">
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => {
                                openEditForm(department)
                              }}
                              className="rounded-lg border border-slate-200 p-2 text-slate-500 transition hover:border-cyan-300 hover:bg-cyan-50 hover:text-cyan-700"
                              aria-label="Editar departamento"
                              title="Editar"
                            >
                              <Pencil className="size-4" />
                            </button>

                            <button
                              type="button"
                              disabled={
                                statusMutation.isPending
                              }
                              onClick={() => {
                                statusMutation.mutate(
                                  department,
                                )
                              }}
                              className="rounded-lg border border-slate-200 px-3 py-2 text-xs font-medium text-slate-600 transition hover:border-cyan-300 hover:text-cyan-700 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                              {department.isActive
                                ? 'Desactivar'
                                : 'Activar'}
                            </button>


                            <button
                                type="button"
                             onClick={() => {
                             setDepartmentToDelete(department)
                                     }}
                             className="rounded-lg border border-slate-200 p-2 text-slate-500 transition hover:border-red-300 hover:bg-red-50 hover:text-red-700"
                             aria-label="Eliminar departamento"
                             title="Eliminar"
                                                >
                        <Trash2 className="size-4" />
                            </button>

                          </div>
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <footer className="flex items-center justify-between border-t border-slate-200 px-5 py-4">
              <p className="text-sm text-slate-500">
                Página {data.page + 1} de{' '}
                {Math.max(data.totalPages, 1)}
              </p>

              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={!data.hasPrevious}
                  onClick={() => {
                    setPage((current) => current - 1)
                  }}
                  className="rounded-lg border border-slate-200 p-2 text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                  aria-label="Página anterior"
                >
                  <ChevronLeft className="size-5" />
                </button>

                <button
                  type="button"
                  disabled={!data.hasNext}
                  onClick={() => {
                    setPage((current) => current + 1)
                  }}
                  className="rounded-lg border border-slate-200 p-2 text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                  aria-label="Página siguiente"
                >
                  <ChevronRight className="size-5" />
                </button>
              </div>
            </footer>
          </>
        )}

        {data?.empty && (
          <div className="p-12 text-center">
            <Building2 className="mx-auto size-10 text-slate-300" />

            <p className="mt-4 font-medium text-slate-700">
              No se encontraron departamentos
            </p>

            <p className="mt-1 text-sm text-slate-500">
              Prueba modificando los filtros seleccionados.
            </p>
          </div>
        )}
      </section>

      {formOpen && (
        <DepartmentFormModal
          department={selectedDepartment}
          onClose={closeForm}
        />
      )}

              {departmentToDelete && (
                <DeleteDepartmentModal
                    department={departmentToDelete}
                    onClose={() => {
                    setDepartmentToDelete(undefined)
                    }}
                />
                )}

    </div>
  )
}