import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import {
  BedDouble,
  ChevronLeft,
  ChevronRight,
  DoorOpen,
  Layers3,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Trash2,
} from 'lucide-react'

import { DeleteWardModal } from '../components/DeleteWardModal'
import { useState } from 'react'
import { getActiveDepartments } from '../api/departments-api'
import {
  activateWard,
  deactivateWard,
  getWards,
} from '../api/wards-api'
import { useAuth } from '../auth/useAuth'
import { WardFormModal } from '../components/WardFormModal'
import { departmentTypeLabels } from '../types/department'
import type {
  Ward,
  WardFilters,
} from '../types/ward'

type FacilityTab = 'wards' | 'rooms' | 'beds'

const tabs = [
  {
    id: 'wards',
    label: 'Salas',
    icon: Layers3,
  },
  {
    id: 'rooms',
    label: 'Habitaciones',
    icon: DoorOpen,
  },
  {
    id: 'beds',
    label: 'Camas',
    icon: BedDouble,
  },
] satisfies Array<{
  id: FacilityTab
  label: string
  icon: typeof Layers3
}>

export function FacilitiesPage() {
  const [activeTab, setActiveTab] =
    useState<FacilityTab>('wards')

  return (
    <div className="mx-auto max-w-7xl">
      <section className="mb-7">
        <h2 className="text-2xl font-semibold text-slate-950">
          Infraestructura hospitalaria
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          Administra salas, habitaciones y camas.
        </p>
      </section>

      <nav className="mb-6 flex gap-2 overflow-x-auto rounded-2xl border border-slate-200 bg-white p-2 shadow-sm">
        {tabs.map((tab) => {
          const Icon = tab.icon
          const selected = activeTab === tab.id

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => {
                setActiveTab(tab.id)
              }}
              className={`flex min-w-fit flex-1 items-center justify-center gap-2 rounded-xl px-4 py-3 text-sm font-medium transition ${
                selected
                  ? 'bg-cyan-700 text-white shadow-sm'
                  : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'
              }`}
            >
              <Icon className="size-5" />
              {tab.label}
            </button>
          )
        })}
      </nav>

      {activeTab === 'wards' ? (
        <WardsPanel />
      ) : (
        <PendingFacilityPanel type={activeTab} />
      )}
    </div>
  )
}

function WardsPanel() {
  const { user } = useAuth()
  const queryClient = useQueryClient()

  const [page, setPage] = useState(0)
  const [name, setName] = useState('')
  const [searchName, setSearchName] = useState('')


  const [wardToDelete, setWardToDelete] =
    useState<Ward | null>(null)

  const [departmentId, setDepartmentId] =
    useState<number | undefined>()

  const [activeStatus, setActiveStatus] =
    useState<'all' | 'active' | 'inactive'>('all')

  const [formOpen, setFormOpen] = useState(false)

  const [selectedWard, setSelectedWard] =
    useState<Ward | undefined>()

  const isAdmin = user?.role === 'ADMIN'

  const filters: WardFilters = {
    page,
    size: 10,
    name: searchName || undefined,
    departmentId,
    isActive:
      activeStatus === 'all'
        ? undefined
        : activeStatus === 'active',
  }

  const departmentsQuery = useQuery({
    queryKey: ['active-departments'],
    queryFn: getActiveDepartments,
  })

  const wardsQuery = useQuery({
    queryKey: ['wards', filters],
    queryFn: () => getWards(filters),
    placeholderData: keepPreviousData,
  })

  const statusMutation = useMutation({
    mutationFn: async (ward: Ward) => {
      if (ward.isActive) {
        await deactivateWard(ward.id)
        return
      }

      await activateWard(ward.id)
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['wards'],
      })
    },
  })

  const handleSearch = () => {
    setPage(0)
    setSearchName(name.trim())
  }

  const handleReset = () => {
    setName('')
    setSearchName('')
    setDepartmentId(undefined)
    setActiveStatus('all')
    setPage(0)
  }

  const openCreateForm = () => {
    setSelectedWard(undefined)
    setFormOpen(true)
  }

  const openEditForm = (ward: Ward) => {
    setSelectedWard(ward)
    setFormOpen(true)
  }

  const closeForm = () => {
    setFormOpen(false)
    setSelectedWard(undefined)
  }

  const data = wardsQuery.data

  return (
    <section>
      <div className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h3 className="text-xl font-semibold text-slate-950">
            Salas hospitalarias
          </h3>

          <p className="mt-1 text-sm text-slate-500">
            Organiza las salas asociadas a cada departamento.
          </p>
        </div>

        {isAdmin && (
          <button
            type="button"
            onClick={openCreateForm}
            className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white transition hover:bg-cyan-800"
          >
            <Plus className="size-5" />
            Nueva sala
          </button>
        )}
      </div>

      <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div className="xl:col-span-2">
            <label
              htmlFor="ward-name"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Nombre
            </label>

            <div className="flex gap-2">
              <input
                id="ward-name"
                value={name}
                onChange={(event) => {
                  setName(event.target.value)
                }}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    handleSearch()
                  }
                }}
                placeholder="Buscar por nombre"
                className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
              />

              <button
                type="button"
                onClick={handleSearch}
                className="rounded-xl bg-slate-900 px-4 text-white transition hover:bg-slate-700"
                aria-label="Buscar sala"
              >
                <Search className="size-5" />
              </button>
            </div>
          </div>

          <div>
            <label
              htmlFor="ward-department"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Departamento
            </label>

            <select
              id="ward-department"
              value={departmentId ?? ''}
              disabled={departmentsQuery.isPending}
              onChange={(event) => {
                const value = event.target.value

                setDepartmentId(
                  value ? Number(value) : undefined,
                )
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:ring-4 focus:ring-cyan-600/10 disabled:opacity-60"
            >
              <option value="">Todos</option>

              {departmentsQuery.data?.map(
                (department) => (
                  <option
                    key={department.id}
                    value={department.id}
                  >
                    {
                      departmentTypeLabels[
                        department.departmentType
                      ]
                    }
                  </option>
                ),
              )}
            </select>
          </div>

          <div>
            <label
              htmlFor="ward-status"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Estado
            </label>

            <select
              id="ward-status"
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
      </div>

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <header className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <div>
            <p className="font-medium text-slate-900">
              Listado de salas
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

          <Layers3 className="size-6 text-cyan-700" />
        </header>

        {wardsQuery.isPending && (
          <div className="space-y-3 p-6">
            {[1, 2, 3, 4].map((item) => (
              <div
                key={item}
                className="h-14 animate-pulse rounded-xl bg-slate-100"
              />
            ))}
          </div>
        )}

        {wardsQuery.isError && (
          <div className="p-10 text-center">
            <p className="font-medium text-red-700">
              No se pudieron cargar las salas.
            </p>

            <button
              type="button"
              onClick={() => {
                void wardsQuery.refetch()
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
              <table className="w-full min-w-[850px] text-left">
                <thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                  <tr>
                    <th className="px-5 py-4">
                      Sala
                    </th>

                    <th className="px-5 py-4">
                      Departamento
                    </th>

                    <th className="px-5 py-4">
                      Habitaciones
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
                  {data.content.map((ward) => (
                    <tr
                      key={ward.id}
                      className="transition hover:bg-slate-50"
                    >
                      <td className="px-5 py-4">
                        <p className="font-medium text-slate-900">
                          {ward.name}
                        </p>

                        <p className="mt-1 max-w-xs truncate text-sm text-slate-500">
                          {ward.description ||
                            'Sin descripción'}
                        </p>
                      </td>

                      <td className="px-5 py-4 text-sm text-slate-600">
                        {
                          departmentTypeLabels[
                            ward.departmentType
                          ]
                        }
                      </td>

                      <td className="px-5 py-4 text-sm text-slate-600">
                        {ward.totalRooms}
                      </td>

                      <td className="px-5 py-4">
                        <span
                          className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${
                            ward.isActive
                              ? 'bg-emerald-50 text-emerald-700'
                              : 'bg-slate-100 text-slate-600'
                          }`}
                        >
                          {ward.isActive
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
                                openEditForm(ward)
                              }}
                              className="rounded-lg border border-slate-200 p-2 text-slate-500 transition hover:border-cyan-300 hover:bg-cyan-50 hover:text-cyan-700"
                              aria-label="Editar sala"
                              title="Editar"
                            >
                              <Pencil className="size-4" />
                            </button>



                              <button
                                    type="button"
                                    onClick={() => setWardToDelete(ward)}
                                    aria-label={`Eliminar ${ward.name}`}
                                    title="Eliminar"
                                    className="inline-flex size-10 items-center justify-center rounded-xl border border-red-200 text-red-600 transition hover:bg-red-50"
                                  >
                                    <Trash2 className="size-4" />
                                  </button>


                            <button
                              type="button"
                              disabled={
                                statusMutation.isPending
                              }
                              onClick={() => {
                                statusMutation.mutate(ward)
                              }}
                              className="rounded-lg border border-slate-200 px-3 py-2 text-xs font-medium text-slate-600 transition hover:border-cyan-300 hover:text-cyan-700 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                              {ward.isActive
                                ? 'Desactivar'
                                : 'Activar'}
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
            <Layers3 className="mx-auto size-10 text-slate-300" />

            <p className="mt-4 font-medium text-slate-700">
              No se encontraron salas
            </p>

            <p className="mt-1 text-sm text-slate-500">
              Crea una sala o modifica los filtros.
            </p>
          </div>
        )}
      </div>

      {formOpen && (
        <WardFormModal
          ward={selectedWard}
          onClose={closeForm}
        />
      )}

      {wardToDelete && (
        <DeleteWardModal
          ward={wardToDelete}
          onClose={() => {
            setWardToDelete(null)
          }}
        />
      )}
    </section>
  )
}

interface PendingFacilityPanelProps {
  type: Exclude<FacilityTab, 'wards'>
}

function PendingFacilityPanel({
  type,
}: PendingFacilityPanelProps) {
  const rooms = type === 'rooms'
  const Icon = rooms ? DoorOpen : BedDouble

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-12 text-center shadow-sm">
      <Icon className="mx-auto size-12 text-slate-300" />

      <h3 className="mt-5 text-xl font-semibold text-slate-900">
        {rooms ? 'Habitaciones' : 'Camas'}
      </h3>

      <p className="mt-2 text-slate-500">
        Este módulo se habilitará después de configurar las
        salas hospitalarias.
      </p>
    </section>
  )
}
