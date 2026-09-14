import {
  keepPreviousData,
  useQuery,
} from '@tanstack/react-query'
import {
  Ban,
  CalendarCheck,
  CalendarDays,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Clock3,
  Pencil,
  Play,
  Plus,
  RefreshCw,
  Search,
  Trash2,
  UserX,
} from 'lucide-react'
import { useState } from 'react'
import { getAppointments } from '../api/appointments-api'
import { useAuth } from '../auth/useAuth'
import { AppointmentFormModal } from '../components/AppointmentFormModal'
import { AppointmentStatusModal } from '../components/AppointmentStatusModal'
import { DeleteAppointmentModal } from '../components/DeleteAppointmentModal'
import {
  appointmentStatusLabels,
  type Appointment,
  type AppointmentFilters,
  type AppointmentStatus,
} from '../types/appointment'

const statuses = Object.keys(
  appointmentStatusLabels,
) as AppointmentStatus[]

const statusClasses: Record<AppointmentStatus, string> = {
  SCHEDULED: 'bg-blue-50 text-blue-700',
  CONFIRMED: 'bg-cyan-50 text-cyan-700',
  IN_PROGRESS: 'bg-amber-50 text-amber-700',
  COMPLETED: 'bg-emerald-50 text-emerald-700',
  CANCELLED: 'bg-red-50 text-red-700',
  NO_SHOW: 'bg-slate-100 text-slate-600',
  RESCHEDULED: 'bg-violet-50 text-violet-700',
}

interface StatusSelection {
  appointment: Appointment
  status: AppointmentStatus
}

export function AppointmentsPage() {
  const { user } = useAuth()
  const role = user?.role

  const [page, setPage] = useState(0)
  const [reason, setReason] = useState('')
  const [reasonSearch, setReasonSearch] = useState('')
  const [status, setStatus] =
    useState<AppointmentStatus | undefined>()
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')

  const [formOpen, setFormOpen] = useState(false)
  const [selectedAppointment, setSelectedAppointment] =
    useState<Appointment | undefined>()
  const [statusSelection, setStatusSelection] =
    useState<StatusSelection | null>(null)
  const [appointmentToDelete, setAppointmentToDelete] =
    useState<Appointment | null>(null)

  const canCreate =
    role === 'ADMIN' || role === 'RECEPTIONIST'

  const canEdit =
    role === 'ADMIN' || role === 'RECEPTIONIST'

  const canUpdateAnyStatus =
    role === 'ADMIN' || role === 'DOCTOR'

  const canConfirm =
    canUpdateAnyStatus || role === 'RECEPTIONIST'

  const canCancel =
    role === 'ADMIN' || role === 'RECEPTIONIST'

  const canDelete = role === 'ADMIN'

  const filters: AppointmentFilters = {
    page,
    size: 10,
    status,
    reason: reasonSearch || undefined,
    dateTimeFrom: dateFrom
      ? `${dateFrom}T00:00:00`
      : undefined,
    dateTimeTo: dateTo
      ? `${dateTo}T23:59:59`
      : undefined,
  }

  const appointmentsQuery = useQuery({
    queryKey: ['appointments', filters],
    queryFn: () => getAppointments(filters),
    placeholderData: keepPreviousData,
  })

  const data = appointmentsQuery.data

  function searchByReason() {
    setPage(0)
    setReasonSearch(reason.trim())
  }

  function resetFilters() {
    setReason('')
    setReasonSearch('')
    setStatus(undefined)
    setDateFrom('')
    setDateTo('')
    setPage(0)
  }

  function openCreateForm() {
    setSelectedAppointment(undefined)
    setFormOpen(true)
  }

  function openEditForm(appointment: Appointment) {
    setSelectedAppointment(appointment)
    setFormOpen(true)
  }

  function closeForm() {
    setFormOpen(false)
    setSelectedAppointment(undefined)
  }

  function selectStatus(
    appointment: Appointment,
    newStatus: AppointmentStatus,
  ) {
    setStatusSelection({
      appointment,
      status: newStatus,
    })
  }

  return (
    <div className="mx-auto max-w-7xl">
      <div className="mb-7 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <p className="text-sm font-medium text-cyan-700">
            AGENDA HOSPITALARIA
          </p>

          <h2 className="mt-1 text-2xl font-semibold text-slate-950">
            Citas
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Programa consultas y controla su evolución.
          </p>
        </div>

        {canCreate && (
          <button
            type="button"
            onClick={openCreateForm}
            className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white hover:bg-cyan-800"
          >
            <Plus className="size-5" />
            Nueva cita
          </button>
        )}
      </div>

      <section className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <label>
            <span className="mb-2 block text-sm font-medium text-slate-700">
              Buscar por motivo
            </span>

            <div className="flex gap-2">
              <input
                value={reason}
                onChange={(event) =>
                  setReason(event.target.value)
                }
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    searchByReason()
                  }
                }}
                placeholder="Motivo de la consulta"
                className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none focus:border-cyan-600"
              />

              <button
                type="button"
                onClick={searchByReason}
                aria-label="Buscar citas"
                className="rounded-xl bg-slate-900 px-4 text-white"
              >
                <Search className="size-5" />
              </button>
            </div>
          </label>

          <label>
            <span className="mb-2 block text-sm font-medium text-slate-700">
              Estado
            </span>

            <select
              value={status ?? ''}
              onChange={(event) => {
                setStatus(
                  (event.target.value ||
                    undefined) as
                    | AppointmentStatus
                    | undefined,
                )
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
            >
              <option value="">Todos</option>

              {statuses.map((value) => (
                <option key={value} value={value}>
                  {appointmentStatusLabels[value]}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span className="mb-2 block text-sm font-medium text-slate-700">
              Fecha desde
            </span>

            <input
              type="date"
              value={dateFrom}
              onChange={(event) => {
                setDateFrom(event.target.value)
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
            />
          </label>

          <label>
            <span className="mb-2 block text-sm font-medium text-slate-700">
              Fecha hasta
            </span>

            <input
              type="date"
              value={dateTo}
              onChange={(event) => {
                setDateTo(event.target.value)
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
            />
          </label>
        </div>

        <button
          type="button"
          onClick={resetFilters}
          className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-cyan-700"
        >
          <RefreshCw className="size-4" />
          Limpiar filtros
        </button>
      </section>

      <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <header className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
          <div>
            <p className="font-medium text-slate-900">
              Listado de citas
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

          <CalendarDays className="size-6 text-cyan-700" />
        </header>

        {appointmentsQuery.isPending && (
          <div className="space-y-3 p-6">
            {[1, 2, 3].map((item) => (
              <div
                key={item}
                className="h-16 animate-pulse rounded-xl bg-slate-100"
              />
            ))}
          </div>
        )}

        {appointmentsQuery.isError && (
          <div className="p-10 text-center">
            <p className="font-medium text-red-700">
              No se pudieron cargar las citas.
            </p>

            <button
              type="button"
              onClick={() =>
                void appointmentsQuery.refetch()
              }
              className="mt-4 rounded-xl bg-slate-900 px-4 py-2 text-sm text-white"
            >
              Intentar nuevamente
            </button>
          </div>
        )}

        {data && !data.empty && (
          <>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[1250px] text-left">
                <thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                  <tr>
                    <th className="px-5 py-4">
                      Paciente
                    </th>
                    <th className="px-5 py-4">
                      Médico
                    </th>
                    <th className="px-5 py-4">
                      Fecha y hora
                    </th>
                    <th className="px-5 py-4">
                      Habitación
                    </th>
                    <th className="px-5 py-4">
                      Motivo
                    </th>
                    <th className="px-5 py-4">
                      Estado
                    </th>
                    <th className="px-5 py-4 text-right">
                      Acciones
                    </th>
                  </tr>
                </thead>

                <tbody className="divide-y divide-slate-100">
                  {data.content.map((appointment) => {
                    const editable =
                      appointment.status === 'SCHEDULED' ||
                      appointment.status === 'CONFIRMED' ||
                      appointment.status === 'RESCHEDULED'

                    const final =
                      appointment.status === 'COMPLETED' ||
                      appointment.status === 'CANCELLED' ||
                      appointment.status === 'NO_SHOW'

                    return (
                      <tr
                        key={appointment.id}
                        className="hover:bg-slate-50"
                      >
                        <td className="px-5 py-4">
                          <p className="font-medium text-slate-900">
                            {appointment.patientName}
                          </p>

                          <p className="mt-1 text-xs text-slate-400">
                            Cita #{appointment.id}
                          </p>
                        </td>

                        <td className="px-5 py-4 text-sm text-slate-600">
                          {appointment.doctorName}
                        </td>

                        <td className="px-5 py-4">
                          <div className="flex items-start gap-2">
                            <Clock3 className="mt-0.5 size-4 shrink-0 text-cyan-700" />

                            <span className="text-sm text-slate-600">
                              {formatDateTime(
                                appointment.dateTime,
                              )}
                            </span>
                          </div>
                        </td>

                        <td className="px-5 py-4 text-sm text-slate-600">
                          {appointment.roomNumber
                            ? `Habitación ${appointment.roomNumber}`
                            : 'Sin asignar'}
                        </td>

                        <td className="max-w-64 px-5 py-4">
                          <p
                            title={appointment.reason}
                            className="truncate text-sm text-slate-600"
                          >
                            {appointment.reason}
                          </p>

                          {appointment.cancellationReason && (
                            <p
                              title={
                                appointment.cancellationReason
                              }
                              className="mt-1 truncate text-xs text-red-600"
                            >
                              {
                                appointment.cancellationReason
                              }
                            </p>
                          )}
                        </td>

                        <td className="px-5 py-4">
                          <span
                            className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                              statusClasses[
                                appointment.status
                              ]
                            }`}
                          >
                            {
                              appointmentStatusLabels[
                                appointment.status
                              ]
                            }
                          </span>
                        </td>

                        <td className="px-5 py-4">
                          <div className="flex justify-end gap-2">
                            {editable && canEdit && (
                              <ActionButton
                                title="Reprogramar"
                                label={`Reprogramar cita de ${appointment.patientName}`}
                                onClick={() =>
                                  openEditForm(appointment)
                                }
                              >
                                <Pencil className="size-4" />
                              </ActionButton>
                            )}

                            {(appointment.status ===
                              'SCHEDULED' ||
                              appointment.status ===
                                'RESCHEDULED') &&
                              canConfirm && (
                                <ActionButton
                                  title="Confirmar"
                                  label={`Confirmar cita de ${appointment.patientName}`}
                                  color="cyan"
                                  onClick={() =>
                                    selectStatus(
                                      appointment,
                                      'CONFIRMED',
                                    )
                                  }
                                >
                                  <CalendarCheck className="size-4" />
                                </ActionButton>
                              )}

                            {appointment.status ===
                              'CONFIRMED' &&
                              canUpdateAnyStatus && (
                                <>
                                  <ActionButton
                                    title="Iniciar consulta"
                                    label={`Iniciar cita de ${appointment.patientName}`}
                                    color="amber"
                                    onClick={() =>
                                      selectStatus(
                                        appointment,
                                        'IN_PROGRESS',
                                      )
                                    }
                                  >
                                    <Play className="size-4" />
                                  </ActionButton>

                                  <ActionButton
                                    title="No presentado"
                                    label={`Marcar ausencia de ${appointment.patientName}`}
                                    onClick={() =>
                                      selectStatus(
                                        appointment,
                                        'NO_SHOW',
                                      )
                                    }
                                  >
                                    <UserX className="size-4" />
                                  </ActionButton>
                                </>
                              )}

                            {appointment.status ===
                              'IN_PROGRESS' &&
                              canUpdateAnyStatus && (
                                <ActionButton
                                  title="Completar"
                                  label={`Completar cita de ${appointment.patientName}`}
                                  color="emerald"
                                  onClick={() =>
                                    selectStatus(
                                      appointment,
                                      'COMPLETED',
                                    )
                                  }
                                >
                                  <CheckCircle2 className="size-4" />
                                </ActionButton>
                              )}

                            {editable && canCancel && (
                              <ActionButton
                                title="Cancelar"
                                label={`Cancelar cita de ${appointment.patientName}`}
                                color="amber"
                                onClick={() =>
                                  selectStatus(
                                    appointment,
                                    'CANCELLED',
                                  )
                                }
                              >
                                <Ban className="size-4" />
                              </ActionButton>
                            )}

                            {final && canDelete && (
                              <ActionButton
                                title="Eliminar"
                                label={`Eliminar cita de ${appointment.patientName}`}
                                color="red"
                                onClick={() =>
                                  setAppointmentToDelete(
                                    appointment,
                                  )
                                }
                              >
                                <Trash2 className="size-4" />
                              </ActionButton>
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
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
                  onClick={() =>
                    setPage((current) => current - 1)
                  }
                  aria-label="Página anterior"
                  className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"
                >
                  <ChevronLeft className="size-5" />
                </button>

                <button
                  type="button"
                  disabled={!data.hasNext}
                  onClick={() =>
                    setPage((current) => current + 1)
                  }
                  aria-label="Página siguiente"
                  className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"
                >
                  <ChevronRight className="size-5" />
                </button>
              </div>
            </footer>
          </>
        )}

        {data?.empty && (
          <div className="p-12 text-center">
            <CalendarDays className="mx-auto size-10 text-slate-300" />

            <p className="mt-4 font-medium text-slate-700">
              No se encontraron citas
            </p>

            <p className="mt-1 text-sm text-slate-500">
              Programa una cita o modifica los filtros.
            </p>
          </div>
        )}
      </section>

      {formOpen && (
        <AppointmentFormModal
          appointment={selectedAppointment}
          onClose={closeForm}
        />
      )}

      {statusSelection && (
        <AppointmentStatusModal
          appointment={statusSelection.appointment}
          status={statusSelection.status}
          onClose={() => setStatusSelection(null)}
        />
      )}

      {appointmentToDelete && (
        <DeleteAppointmentModal
          appointment={appointmentToDelete}
          onClose={() =>
            setAppointmentToDelete(null)
          }
        />
      )}
    </div>
  )
}

interface ActionButtonProps {
  title: string
  label: string
  color?: 'slate' | 'cyan' | 'amber' | 'emerald' | 'red'
  onClick: () => void
  children: React.ReactNode
}

const actionButtonClasses = {
  slate:
    'border-slate-200 text-slate-500 hover:bg-slate-50',
  cyan:
    'border-cyan-200 text-cyan-700 hover:bg-cyan-50',
  amber:
    'border-amber-200 text-amber-700 hover:bg-amber-50',
  emerald:
    'border-emerald-200 text-emerald-700 hover:bg-emerald-50',
  red:
    'border-red-200 text-red-600 hover:bg-red-50',
}

function ActionButton({
  title,
  label,
  color = 'slate',
  onClick,
  children,
}: ActionButtonProps) {
  return (
    <button
      type="button"
      title={title}
      aria-label={label}
      onClick={onClick}
      className={`rounded-lg border p-2 transition ${actionButtonClasses[color]}`}
    >
      {children}
    </button>
  )
}

function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat('es-ES', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}