import {
  keepPreviousData,
  useQuery,
} from '@tanstack/react-query'
import {
  ArrowRightLeft,
  Ban,
  BedDouble,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  ClipboardPlus,
  Pencil,
  Plus,
  RefreshCw,
  Trash2,
} from 'lucide-react'
import { useState } from 'react'
import { getAdmissions } from '../api/admissions-api'
import { useAuth } from '../auth/useAuth'
import {
  AdmissionActionModal,
  type AdmissionAction,
} from '../components/AdmissionActionModal'
import { AdmissionFormModal } from '../components/AdmissionFormModal'
import { DeleteAdmissionModal } from '../components/DeleteAdmissionModal'
import {
  admissionStatusLabels,
  type Admission,
  type AdmissionFilters,
  type AdmissionStatus,
} from '../types/admission'


import { getDepartmentLabel } from '../utils/department-label'

const statuses = Object.keys(
  admissionStatusLabels,
) as AdmissionStatus[]

const statusClasses: Record<AdmissionStatus, string> = {
  ACTIVE: 'bg-emerald-50 text-emerald-700',
  DISCHARGED: 'bg-blue-50 text-blue-700',
  TRANSFERRED: 'bg-violet-50 text-violet-700',
  CANCELLED: 'bg-red-50 text-red-700',
}

interface SelectedAction {
  admission: Admission
  action: AdmissionAction
}

export function AdmissionsPage() {
  const { user } = useAuth()
  const role = user?.role

  const [page, setPage] = useState(0)
  const [status, setStatus] =
    useState<AdmissionStatus | undefined>()
  const [admittedFrom, setAdmittedFrom] = useState('')
  const [admittedTo, setAdmittedTo] = useState('')

  const [formOpen, setFormOpen] = useState(false)
  const [selectedAdmission, setSelectedAdmission] =
    useState<Admission | undefined>()
  const [selectedAction, setSelectedAction] =
    useState<SelectedAction | null>(null)
  const [admissionToDelete, setAdmissionToDelete] =
    useState<Admission | null>(null)

  const canCreate =
    role === 'ADMIN' ||
    role === 'DOCTOR' ||
    role === 'RECEPTIONIST'

  const canEdit =
    role === 'ADMIN' || role === 'DOCTOR'

  const canDischarge =
    role === 'ADMIN' || role === 'DOCTOR'

  const canTransfer =
    role === 'ADMIN' || role === 'NURSE'

  const canCancel =
    role === 'ADMIN' || role === 'RECEPTIONIST'

  const canDelete = role === 'ADMIN'

  const filters: AdmissionFilters = {
    page,
    size: 10,
    status,
    admittedFrom: admittedFrom
      ? `${admittedFrom}T00:00:00`
      : undefined,
    admittedTo: admittedTo
      ? `${admittedTo}T23:59:59`
      : undefined,
  }

  const admissionsQuery = useQuery({
    queryKey: ['admissions', filters],
    queryFn: () => getAdmissions(filters),
    placeholderData: keepPreviousData,
  })

  const data = admissionsQuery.data

  function openCreateForm() {
    setSelectedAdmission(undefined)
    setFormOpen(true)
  }

  function openEditForm(admission: Admission) {
    setSelectedAdmission(admission)
    setFormOpen(true)
  }

  function closeForm() {
    setFormOpen(false)
    setSelectedAdmission(undefined)
  }

  function openAction(
    admission: Admission,
    action: AdmissionAction,
  ) {
    setSelectedAction({
      admission,
      action,
    })
  }

  function resetFilters() {
    setStatus(undefined)
    setAdmittedFrom('')
    setAdmittedTo('')
    setPage(0)
  }

  return (
    <div className="mx-auto max-w-7xl">
      <div className="mb-7 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <p className="text-sm font-medium text-cyan-700">
            HOSPITALIZACIÓN
          </p>

          <h2 className="mt-1 text-2xl font-semibold text-slate-950">
            Admisiones
          </h2>

          <p className="mt-1 text-sm text-slate-500">
            Gestiona ingresos, altas, cancelaciones y
            traslados.
          </p>
        </div>

        {canCreate && (
          <button
            type="button"
            onClick={openCreateForm}
            className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white hover:bg-cyan-800"
          >
            <Plus className="size-5" />
            Nueva admisión
          </button>
        )}
      </div>

      <section className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 md:grid-cols-3">
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
                    | AdmissionStatus
                    | undefined,
                )
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
            >
              <option value="">Todos</option>

              {statuses.map((value) => (
                <option key={value} value={value}>
                  {admissionStatusLabels[value]}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span className="mb-2 block text-sm font-medium text-slate-700">
              Ingreso desde
            </span>

            <input
              type="date"
              value={admittedFrom}
              onChange={(event) => {
                setAdmittedFrom(event.target.value)
                setPage(0)
              }}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
            />
          </label>

          <label>
            <span className="mb-2 block text-sm font-medium text-slate-700">
              Ingreso hasta
            </span>

            <input
              type="date"
              value={admittedTo}
              onChange={(event) => {
                setAdmittedTo(event.target.value)
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
              Listado de admisiones
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

          <ClipboardPlus className="size-6 text-cyan-700" />
        </header>

        {admissionsQuery.isPending && (
          <div className="space-y-3 p-6">
            {[1, 2, 3].map((item) => (
              <div
                key={item}
                className="h-16 animate-pulse rounded-xl bg-slate-100"
              />
            ))}
          </div>
        )}

        {admissionsQuery.isError && (
          <div className="p-10 text-center">
            <p className="font-medium text-red-700">
              No se pudieron cargar las admisiones.
            </p>

            <button
              type="button"
              onClick={() =>
                void admissionsQuery.refetch()
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
                      Ubicación
                    </th>
                    <th className="px-5 py-4">
                      Motivo
                    </th>
                    <th className="px-5 py-4">
                      Ingreso
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
                  {data.content.map((admission) => {
                    const active =
                      admission.status === 'ACTIVE'

                    return (
                      <tr
                        key={admission.id}
                        className="hover:bg-slate-50"
                      >
                        <td className="px-5 py-4">
                          <p className="font-medium text-slate-900">
                            {admission.patientName}
                          </p>

                          <p className="mt-1 text-xs text-slate-400">
                            Admisión #{admission.id}
                          </p>
                        </td>

                        <td className="px-5 py-4 text-sm text-slate-600">
                          {
                            admission.attendingDoctorName
                          }
                        </td>

                        <td className="px-5 py-4">
                          <div className="flex items-start gap-2">
                            <BedDouble className="mt-0.5 size-4 shrink-0 text-cyan-700" />

                            <div>
                              <p className="text-sm text-slate-700">
                                Cama{' '}
                                {admission.bedNumber} ·
                                Habitación{' '}
                                {admission.roomNumber}
                              </p>

                              <p className="mt-1 text-xs text-slate-500">
                               {admission.wardName} ·{' '}
{getDepartmentLabel(admission.departmentType)}
                              </p>
                            </div>
                          </div>
                        </td>

                        <td className="max-w-64 px-5 py-4">
                          <p
                            className="truncate text-sm text-slate-600"
                            title={
                              admission.admissionReason
                            }
                          >
                            {
                              admission.admissionReason
                            }
                          </p>
                        </td>

                        <td className="px-5 py-4 text-sm text-slate-600">
                          {formatDateTime(
                            admission.admittedAt,
                          )}
                        </td>

                        <td className="px-5 py-4">
                          <span
                            className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                              statusClasses[
                                admission.status
                              ]
                            }`}
                          >
                            {
                              admissionStatusLabels[
                                admission.status
                              ]
                            }
                          </span>
                        </td>

                        <td className="px-5 py-4">
                          <div className="flex justify-end gap-2">
                            {active && canEdit && (
                              <ActionButton
                                label={`Editar admisión de ${admission.patientName}`}
                                title="Editar"
                                onClick={() =>
                                  openEditForm(admission)
                                }
                              >
                                <Pencil className="size-4" />
                              </ActionButton>
                            )}

                            {active && canDischarge && (
                              <ActionButton
                                label={`Dar de alta a ${admission.patientName}`}
                                title="Dar de alta"
                                color="emerald"
                                onClick={() =>
                                  openAction(
                                    admission,
                                    'discharge',
                                  )
                                }
                              >
                                <CheckCircle2 className="size-4" />
                              </ActionButton>
                            )}

                            {active && canTransfer && (
                              <ActionButton
                                label={`Trasladar a ${admission.patientName}`}
                                title="Trasladar"
                                color="cyan"
                                onClick={() =>
                                  openAction(
                                    admission,
                                    'transfer',
                                  )
                                }
                              >
                                <ArrowRightLeft className="size-4" />
                              </ActionButton>
                            )}

                            {active && canCancel && (
                              <ActionButton
                                label={`Cancelar admisión de ${admission.patientName}`}
                                title="Cancelar admisión"
                                color="amber"
                                onClick={() =>
                                  openAction(
                                    admission,
                                    'cancel',
                                  )
                                }
                              >
                                <Ban className="size-4" />
                              </ActionButton>
                            )}

                            {!active && canDelete && (
                              <ActionButton
                                label={`Eliminar admisión de ${admission.patientName}`}
                                title="Eliminar"
                                color="red"
                                onClick={() =>
                                  setAdmissionToDelete(
                                    admission,
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
            <ClipboardPlus className="mx-auto size-10 text-slate-300" />

            <p className="mt-4 font-medium text-slate-700">
              No se encontraron admisiones
            </p>

            <p className="mt-1 text-sm text-slate-500">
              Crea una admisión o modifica los filtros.
            </p>
          </div>
        )}
      </section>

      {formOpen && (
        <AdmissionFormModal
          admission={selectedAdmission}
          onClose={closeForm}
        />
      )}

      {selectedAction && (
        <AdmissionActionModal
          admission={selectedAction.admission}
          action={selectedAction.action}
          onClose={() => setSelectedAction(null)}
        />
      )}

      {admissionToDelete && (
        <DeleteAdmissionModal
          admission={admissionToDelete}
          onClose={() =>
            setAdmissionToDelete(null)
          }
        />
      )}
    </div>
  )
}

interface ActionButtonProps {
  label: string
  title: string
  color?: 'slate' | 'cyan' | 'emerald' | 'amber' | 'red'
  onClick: () => void
  children: React.ReactNode
}

const actionButtonClasses = {
  slate:
    'border-slate-200 text-slate-500 hover:bg-slate-50 hover:text-cyan-700',
  cyan:
    'border-cyan-200 text-cyan-700 hover:bg-cyan-50',
  emerald:
    'border-emerald-200 text-emerald-700 hover:bg-emerald-50',
  amber:
    'border-amber-200 text-amber-700 hover:bg-amber-50',
  red:
    'border-red-200 text-red-600 hover:bg-red-50',
}

function ActionButton({
  label,
  title,
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