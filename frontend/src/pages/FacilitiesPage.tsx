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
import { DeleteRoomModal } from '../components/DeleteRoomModal'
import { DeleteBedModal } from '../components/DeleteBedModal'
import { useState, type ReactNode } from 'react'
import { getActiveDepartments } from '../api/departments-api'
import { getRooms, updateRoomStatus } from '../api/rooms-api'
import { getBeds, performBedAction } from '../api/beds-api'
import {
  activateWard,
  deactivateWard,
  getWards,
} from '../api/wards-api'
import { useAuth } from '../auth/useAuth'
import { WardFormModal } from '../components/WardFormModal'
import { RoomFormModal } from '../components/RoomFormModal'
import { BedFormModal } from '../components/BedFormModal'
import { departmentTypeLabels } from '../types/department'
import type {
  Ward,
  WardFilters,
} from '../types/ward'
import {
  roomStatusLabels,
  roomTypeLabels,
  type Room,
  type RoomFilters,
  type RoomStatus,
  type RoomType,
} from '../types/room'
import {
  bedStatusLabels,
  type Bed,
  type BedAction,
  type BedFilters,
  type BedStatus,
} from '../types/bed'

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

      {activeTab === 'wards' && <WardsPanel />}
      {activeTab === 'rooms' && <RoomsPanel />}
      {activeTab === 'beds' && <BedsPanel />}
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

const roomTypes = Object.keys(roomTypeLabels) as RoomType[]
const roomStatuses = Object.keys(roomStatusLabels) as RoomStatus[]

function RoomsPanel() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const isAdmin = user?.role === 'ADMIN'
  const canChangeStatus = isAdmin || user?.role === 'NURSE'
  const [page, setPage] = useState(0)
  const [number, setNumber] = useState('')
  const [searchNumber, setSearchNumber] = useState('')
  const [wardId, setWardId] = useState<number | undefined>()
  const [roomType, setRoomType] = useState<RoomType | undefined>()
  const [status, setStatus] = useState<RoomStatus | undefined>()
  const [formOpen, setFormOpen] = useState(false)
  const [selectedRoom, setSelectedRoom] = useState<Room | undefined>()
  const [roomToDelete, setRoomToDelete] = useState<Room | null>(null)

  const filters: RoomFilters = {
    page,
    size: 10,
    number: searchNumber || undefined,
    wardId,
    roomType,
    status,
  }

  const wardsQuery = useQuery({
    queryKey: ['wards', 'active-room-filter'],
    queryFn: () => getWards({ page: 0, size: 100, isActive: true }),
  })

  const roomsQuery = useQuery({
    queryKey: ['rooms', filters],
    queryFn: () => getRooms(filters),
    placeholderData: keepPreviousData,
  })

  const statusMutation = useMutation({
    mutationFn: ({ room, nextStatus }: { room: Room; nextStatus: RoomStatus }) =>
      updateRoomStatus(room.id, nextStatus, room.notes ?? undefined),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['rooms'] })
    },
  })

  const data = roomsQuery.data
  const closeForm = () => {
    setFormOpen(false)
    setSelectedRoom(undefined)
  }

  return (
    <section>
      <div className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h3 className="text-xl font-semibold text-slate-950">Habitaciones</h3>
          <p className="mt-1 text-sm text-slate-500">Gestiona las habitaciones asociadas a cada sala.</p>
        </div>
        {isAdmin && (
          <button type="button" onClick={() => { setSelectedRoom(undefined); setFormOpen(true) }} className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white hover:bg-cyan-800">
            <Plus className="size-5" /> Nueva habitación
          </button>
        )}
      </div>

      <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <label className="block">
            <span className="mb-2 block text-sm font-medium text-slate-700">Número</span>
            <div className="flex gap-2">
              <input value={number} onChange={(e) => setNumber(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') { setPage(0); setSearchNumber(number.trim()) } }} placeholder="Buscar por número" className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none focus:border-cyan-600" />
              <button type="button" onClick={() => { setPage(0); setSearchNumber(number.trim()) }} aria-label="Buscar habitación" className="rounded-xl bg-slate-900 px-4 text-white"><Search className="size-5" /></button>
            </div>
          </label>
          <FilterSelect label="Sala" value={wardId ?? ''} onChange={(value) => { setWardId(value ? Number(value) : undefined); setPage(0) }}>
            <option value="">Todas</option>
            {wardsQuery.data?.content.map((ward) => <option key={ward.id} value={ward.id}>{ward.name}</option>)}
          </FilterSelect>
          <FilterSelect label="Tipo" value={roomType ?? ''} onChange={(value) => { setRoomType((value || undefined) as RoomType | undefined); setPage(0) }}>
            <option value="">Todos</option>
            {roomTypes.map((type) => <option key={type} value={type}>{roomTypeLabels[type]}</option>)}
          </FilterSelect>
          <FilterSelect label="Estado" value={status ?? ''} onChange={(value) => { setStatus((value || undefined) as RoomStatus | undefined); setPage(0) }}>
            <option value="">Todos</option>
            {roomStatuses.map((item) => <option key={item} value={item}>{roomStatusLabels[item]}</option>)}
          </FilterSelect>
        </div>
        <button type="button" onClick={() => { setNumber(''); setSearchNumber(''); setWardId(undefined); setRoomType(undefined); setStatus(undefined); setPage(0) }} className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-cyan-700"><RefreshCw className="size-4" /> Limpiar filtros</button>
      </div>

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <header className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <div><p className="font-medium text-slate-900">Listado de habitaciones</p><p className="text-sm text-slate-500">{data ? `${data.totalElements} ${data.totalElements === 1 ? 'resultado' : 'resultados'}` : 'Cargando resultados'}</p></div>
          <DoorOpen className="size-6 text-cyan-700" />
        </header>

        {roomsQuery.isPending && <div className="space-y-3 p-6">{[1, 2, 3].map((item) => <div key={item} className="h-14 animate-pulse rounded-xl bg-slate-100" />)}</div>}
        {roomsQuery.isError && <div className="p-10 text-center text-red-700">No se pudieron cargar las habitaciones.</div>}
        {data && !data.empty && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1050px] text-left">
                <thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500"><tr><th className="px-5 py-4">Habitación</th><th className="px-5 py-4">Sala</th><th className="px-5 py-4">Planta</th><th className="px-5 py-4">Tipo</th><th className="px-5 py-4">Camas</th><th className="px-5 py-4">Estado</th>{(isAdmin || canChangeStatus) && <th className="px-5 py-4 text-right">Acciones</th>}</tr></thead>
                <tbody className="divide-y divide-slate-100">
                  {data.content.map((room) => (
                    <tr key={room.id} className="hover:bg-slate-50">
                      <td className="px-5 py-4"><p className="font-medium text-slate-900">{room.number}</p><p className="mt-1 max-w-xs truncate text-sm text-slate-500">{room.notes || 'Sin notas'}</p></td>
                      <td className="px-5 py-4 text-sm text-slate-600">{room.wardName}</td>
                      <td className="px-5 py-4 text-sm text-slate-600">{room.floor}</td>
                      <td className="px-5 py-4 text-sm text-slate-600">{roomTypeLabels[room.roomType]}</td>
                      <td className="px-5 py-4 text-sm text-slate-600">{room.totalBeds}/{room.capacity}</td>
                      <td className="px-5 py-4">
                        {canChangeStatus ? (
                          <select value={room.status} disabled={statusMutation.isPending} onChange={(e) => statusMutation.mutate({ room, nextStatus: e.target.value as RoomStatus })} className="rounded-lg border border-slate-200 bg-white px-2 py-1.5 text-xs font-medium text-slate-700">
                            {roomStatuses.map((item) => <option key={item} value={item}>{roomStatusLabels[item]}</option>)}
                          </select>
                        ) : <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs text-slate-700">{roomStatusLabels[room.status]}</span>}
                      </td>
                      {(isAdmin || canChangeStatus) && (
                        <td className="px-5 py-4"><div className="flex justify-end gap-2">
                          {isAdmin && <button type="button" onClick={() => { setSelectedRoom(room); setFormOpen(true) }} aria-label={`Editar ${room.number}`} className="rounded-lg border border-slate-200 p-2 text-slate-500 hover:border-cyan-300 hover:text-cyan-700"><Pencil className="size-4" /></button>}
                          {isAdmin && <button type="button" onClick={() => setRoomToDelete(room)} aria-label={`Eliminar ${room.number}`} className="rounded-lg border border-red-200 p-2 text-red-600 hover:bg-red-50"><Trash2 className="size-4" /></button>}
                        </div></td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <footer className="flex items-center justify-between border-t border-slate-200 px-5 py-4"><p className="text-sm text-slate-500">Página {data.page + 1} de {Math.max(data.totalPages, 1)}</p><div className="flex gap-2"><button type="button" disabled={!data.hasPrevious} onClick={() => setPage((current) => current - 1)} aria-label="Página anterior" className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"><ChevronLeft className="size-5" /></button><button type="button" disabled={!data.hasNext} onClick={() => setPage((current) => current + 1)} aria-label="Página siguiente" className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"><ChevronRight className="size-5" /></button></div></footer>
          </>
        )}
        {data?.empty && <div className="p-12 text-center"><DoorOpen className="mx-auto size-10 text-slate-300" /><p className="mt-4 font-medium text-slate-700">No se encontraron habitaciones</p><p className="mt-1 text-sm text-slate-500">Crea una habitación o modifica los filtros.</p></div>}
      </div>

      {formOpen && <RoomFormModal room={selectedRoom} onClose={closeForm} />}
      {roomToDelete && <DeleteRoomModal room={roomToDelete} onClose={() => setRoomToDelete(null)} />}
    </section>
  )
}

function FilterSelect({ label, value, onChange, children }: { label: string; value: string | number; onChange: (value: string) => void; children: ReactNode }) {
  return <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">{label}</span><select value={value} onChange={(event) => onChange(event.target.value)} className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none focus:border-cyan-600">{children}</select></label>
}

const bedStatuses = Object.keys(bedStatusLabels) as BedStatus[]
const bedActionLabels: Record<BedAction, string> = {
  reserve: 'Reservar',
  occupy: 'Ocupar',
  release: 'Liberar',
  'finish-cleaning': 'Finalizar limpieza',
  maintenance: 'Enviar a mantenimiento',
  'finish-maintenance': 'Finalizar mantenimiento',
  'cancel-reservation': 'Cancelar reserva',
}

function allowedBedActions(status: BedStatus, role?: string): BedAction[] {
  const admin = role === 'ADMIN'
  const nurse = role === 'NURSE'
  const receptionist = role === 'RECEPTIONIST'
  if (status === 'AVAILABLE') {
    return [
      ...(admin || nurse || receptionist ? ['reserve' as const] : []),
      ...(admin ? ['occupy' as const] : []),
      ...(admin || nurse ? ['maintenance' as const] : []),
    ]
  }
  if (status === 'RESERVED') {
    return [
      ...(admin ? ['occupy' as const] : []),
      ...(admin || nurse || receptionist ? ['cancel-reservation' as const] : []),
    ]
  }
  if (status === 'OCCUPIED') {
    return admin ? ['release'] : []
  }
  if (status === 'CLEANING') {
    return admin || nurse
      ? ['finish-cleaning', 'maintenance']
      : []
  }
  return admin || nurse ? ['finish-maintenance'] : []
}

function BedsPanel() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const isAdmin = user?.role === 'ADMIN'
  const [page, setPage] = useState(0)
  const [number, setNumber] = useState('')
  const [searchNumber, setSearchNumber] = useState('')
  const [roomId, setRoomId] = useState<number | undefined>()
  const [status, setStatus] = useState<BedStatus | undefined>()
  const [formOpen, setFormOpen] = useState(false)
  const [selectedBed, setSelectedBed] = useState<Bed | undefined>()
  const [bedToDelete, setBedToDelete] = useState<Bed | null>(null)

  const filters: BedFilters = { page, size: 10, bedNumber: searchNumber || undefined, roomId, status }
  const roomsQuery = useQuery({ queryKey: ['rooms', 'bed-filter'], queryFn: () => getRooms({ page: 0, size: 100 }) })
  const bedsQuery = useQuery({ queryKey: ['beds', filters], queryFn: () => getBeds(filters), placeholderData: keepPreviousData })
  const actionMutation = useMutation({
    mutationFn: ({ bed, action }: { bed: Bed; action: BedAction }) => performBedAction(bed.id, action),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['beds'] }),
        queryClient.invalidateQueries({ queryKey: ['rooms'] }),
      ])
    },
  })
  const data = bedsQuery.data
  const closeForm = () => { setFormOpen(false); setSelectedBed(undefined) }

  return (
    <section>
      <div className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div><h3 className="text-xl font-semibold text-slate-950">Camas hospitalarias</h3><p className="mt-1 text-sm text-slate-500">Controla disponibilidad, reservas, ocupación y mantenimiento.</p></div>
        {isAdmin && <button type="button" onClick={() => { setSelectedBed(undefined); setFormOpen(true) }} className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white hover:bg-cyan-800"><Plus className="size-5" /> Nueva cama</button>}
      </div>

      <div className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 md:grid-cols-3">
          <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Número</span><div className="flex gap-2"><input value={number} onChange={(e) => setNumber(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') { setPage(0); setSearchNumber(number.trim()) } }} placeholder="Buscar por número" className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none focus:border-cyan-600" /><button type="button" onClick={() => { setPage(0); setSearchNumber(number.trim()) }} aria-label="Buscar cama" className="rounded-xl bg-slate-900 px-4 text-white"><Search className="size-5" /></button></div></label>
          <FilterSelect label="Habitación" value={roomId ?? ''} onChange={(value) => { setRoomId(value ? Number(value) : undefined); setPage(0) }}><option value="">Todas</option>{roomsQuery.data?.content.map((room) => <option key={room.id} value={room.id}>{room.number} · {room.wardName}</option>)}</FilterSelect>
          <FilterSelect label="Estado" value={status ?? ''} onChange={(value) => { setStatus((value || undefined) as BedStatus | undefined); setPage(0) }}><option value="">Todos</option>{bedStatuses.map((item) => <option key={item} value={item}>{bedStatusLabels[item]}</option>)}</FilterSelect>
        </div>
        <button type="button" onClick={() => { setNumber(''); setSearchNumber(''); setRoomId(undefined); setStatus(undefined); setPage(0) }} className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-cyan-700"><RefreshCw className="size-4" /> Limpiar filtros</button>
      </div>

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <header className="flex items-center justify-between border-b border-slate-200 px-5 py-4"><div><p className="font-medium text-slate-900">Listado de camas</p><p className="text-sm text-slate-500">{data ? `${data.totalElements} ${data.totalElements === 1 ? 'resultado' : 'resultados'}` : 'Cargando resultados'}</p></div><BedDouble className="size-6 text-cyan-700" /></header>
        {bedsQuery.isPending && <div className="space-y-3 p-6">{[1, 2, 3].map((item) => <div key={item} className="h-14 animate-pulse rounded-xl bg-slate-100" />)}</div>}
        {bedsQuery.isError && <div className="p-10 text-center text-red-700">No se pudieron cargar las camas.</div>}
        {data && !data.empty && <><div className="overflow-x-auto"><table className="w-full min-w-[1050px] text-left"><thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500"><tr><th className="px-5 py-4">Cama</th><th className="px-5 py-4">Habitación</th><th className="px-5 py-4">Sala</th><th className="px-5 py-4">Planta</th><th className="px-5 py-4">Estado</th><th className="px-5 py-4 text-right">Acciones</th></tr></thead><tbody className="divide-y divide-slate-100">{data.content.map((bed) => {
          const actions = allowedBedActions(bed.status, user?.role)
          return <tr key={bed.id} className="hover:bg-slate-50"><td className="px-5 py-4"><p className="font-medium text-slate-900">{bed.bedNumber}</p><p className="mt-1 max-w-xs truncate text-sm text-slate-500">{bed.notes || 'Sin notas'}</p></td><td className="px-5 py-4 text-sm text-slate-600">{bed.roomNumber}</td><td className="px-5 py-4 text-sm text-slate-600">{bed.wardName}</td><td className="px-5 py-4 text-sm text-slate-600">{bed.roomFloor}</td><td className="px-5 py-4"><span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-700">{bedStatusLabels[bed.status]}</span></td><td className="px-5 py-4"><div className="flex justify-end gap-2">{actions.length > 0 && <select defaultValue="" disabled={actionMutation.isPending} onChange={(e) => { const action = e.target.value as BedAction; if (action) actionMutation.mutate({ bed, action }); e.target.value = '' }} className="rounded-lg border border-slate-200 bg-white px-2 py-2 text-xs text-slate-700"><option value="" disabled>Cambiar estado</option>{actions.map((action) => <option key={action} value={action}>{bedActionLabels[action]}</option>)}</select>}{isAdmin && <button type="button" onClick={() => { setSelectedBed(bed); setFormOpen(true) }} aria-label={`Editar ${bed.bedNumber}`} className="rounded-lg border border-slate-200 p-2 text-slate-500 hover:text-cyan-700"><Pencil className="size-4" /></button>}{isAdmin && <button type="button" onClick={() => setBedToDelete(bed)} aria-label={`Eliminar ${bed.bedNumber}`} className="rounded-lg border border-red-200 p-2 text-red-600 hover:bg-red-50"><Trash2 className="size-4" /></button>}</div></td></tr>
        })}</tbody></table></div><footer className="flex items-center justify-between border-t border-slate-200 px-5 py-4"><p className="text-sm text-slate-500">Página {data.page + 1} de {Math.max(data.totalPages, 1)}</p><div className="flex gap-2"><button type="button" disabled={!data.hasPrevious} onClick={() => setPage((current) => current - 1)} aria-label="Página anterior" className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"><ChevronLeft className="size-5" /></button><button type="button" disabled={!data.hasNext} onClick={() => setPage((current) => current + 1)} aria-label="Página siguiente" className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"><ChevronRight className="size-5" /></button></div></footer></>}
        {data?.empty && <div className="p-12 text-center"><BedDouble className="mx-auto size-10 text-slate-300" /><p className="mt-4 font-medium text-slate-700">No se encontraron camas</p><p className="mt-1 text-sm text-slate-500">Crea una cama o modifica los filtros.</p></div>}
      </div>
      {formOpen && <BedFormModal bed={selectedBed} onClose={closeForm} />}
      {bedToDelete && <DeleteBedModal bed={bedToDelete} onClose={() => setBedToDelete(null)} />}
    </section>
  )
}
