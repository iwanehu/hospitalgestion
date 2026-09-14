import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import {
  type FormEvent,
  useState,
} from 'react'
import {
  createNurse,
  getActiveDepartments,
  getAvailableNurseUsers,
  updateNurse,
} from '../api/staff-api'
import {
  nurseSpecialtyLabels,
  shiftTypeLabels,
  type Nurse,
  type NurseSpecialty,
  type ShiftType,
} from '../types/staff'


import { getDepartmentLabel } from '../utils/department-label'

interface Props {
  nurse?: Nurse
  onClose: () => void
}

interface ApiError {
  message?: string
}

const specialties = Object.keys(
  nurseSpecialtyLabels,
) as NurseSpecialty[]

const shifts = Object.keys(
  shiftTypeLabels,
) as ShiftType[]

export function NurseFormModal({
  nurse,
  onClose,
}: Props) {
  const queryClient = useQueryClient()
  const editing = nurse !== undefined

  const [userId, setUserId] = useState('')
  const [departmentId, setDepartmentId] = useState(
    nurse?.departmentId.toString() ?? '',
  )
  const [license, setLicense] = useState(
    nurse?.licenseNumber ?? '',
  )
  const [specialty, setSpecialty] =
    useState<NurseSpecialty>(
      nurse?.specialty ?? 'GENERAL',
    )
  const [shiftType, setShiftType] = useState<ShiftType>(
    nurse?.shiftType ?? 'MORNING',
  )
  const [experience, setExperience] = useState(
    nurse?.yearsOfExperience.toString() ?? '0',
  )
  const [hireDate, setHireDate] = useState(
    nurse?.hireDate ?? '',
  )
  const [biography, setBiography] = useState(
    nurse?.biography ?? '',
  )
  const [contactName, setContactName] = useState(
    nurse?.emergencyContactName ?? '',
  )
  const [contactPhone, setContactPhone] = useState(
    nurse?.emergencyContactPhone ?? '',
  )
  const [contactRelationship, setContactRelationship] =
    useState(
      nurse?.emergencyContactRelationship ?? '',
    )
  const [maxPatients, setMaxPatients] = useState(
    nurse?.maxPatientsPerShift.toString() ?? '10',
  )
  const [chargeNurse, setChargeNurse] = useState(
    nurse?.isChargeNurse ?? false,
  )
  const [vacationDays, setVacationDays] = useState(
    nurse?.vacationDaysAvailable.toString() ?? '30',
  )
  const [error, setError] = useState('')

  const departmentsQuery = useQuery({
    queryKey: ['departments', 'active', 'staff'],
    queryFn: getActiveDepartments,
  })

  const usersQuery = useQuery({
    queryKey: ['users', 'available-nurses'],
    queryFn: getAvailableNurseUsers,
    enabled: !editing,
  })

  const mutation = useMutation({
    mutationFn: () => {
      const commonData = {
        departmentId: Number(departmentId),
        specialty,
        shiftType,
        yearsOfExperience: Number(experience),
        hireDate: hireDate || undefined,
        biography: biography.trim() || undefined,
        emergencyContactName: contactName.trim(),
        emergencyContactPhone: contactPhone.trim(),
        emergencyContactRelationship:
          contactRelationship.trim(),
        maxPatientsPerShift: Number(maxPatients),
        isChargeNurse: chargeNurse,
        vacationDaysAvailable: Number(vacationDays),
      }

      if (nurse) {
        return updateNurse(nurse.id, commonData)
      }

      return createNurse({
        ...commonData,
        userId: Number(userId),
        licenseNumber: license.trim(),
      })
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ['nurses'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['users', 'available-nurses'],
        }),
      ])

      onClose()
    },

    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(
          cause.response?.data?.message ??
            'No se pudo guardar el perfil de enfermería.',
        )
        return
      }

      setError(
        'No se pudo guardar el perfil de enfermería.',
      )
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (!editing && !userId) {
      setError('Selecciona un usuario con rol NURSE.')
      return
    }

    if (!departmentId) {
      setError('Selecciona un departamento.')
      return
    }

    if (!editing && !license.trim()) {
      setError('Escribe el número de licencia.')
      return
    }

    const experienceNumber = Number(experience)
    const maxPatientsNumber = Number(maxPatients)
    const vacationDaysNumber = Number(vacationDays)

    if (
      !Number.isInteger(experienceNumber) ||
      experienceNumber < 0 ||
      experienceNumber > 60
    ) {
      setError(
        'La experiencia debe estar entre 0 y 60 años.',
      )
      return
    }

    if (
      !Number.isInteger(maxPatientsNumber) ||
      maxPatientsNumber < 0 ||
      maxPatientsNumber > 20
    ) {
      setError(
        'El máximo de pacientes debe estar entre 0 y 20.',
      )
      return
    }

    if (
      !Number.isInteger(vacationDaysNumber) ||
      vacationDaysNumber < 0 ||
      vacationDaysNumber > 60
    ) {
      setError(
        'Los días de vacaciones deben estar entre 0 y 60.',
      )
      return
    }

    if (
      !contactName.trim() ||
      !contactPhone.trim() ||
      !contactRelationship.trim()
    ) {
      setError(
        'Completa todos los datos del contacto de emergencia.',
      )
      return
    }

    mutation.mutate()
  }

  const inputClass =
    'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-cyan-600'

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="nurse-form-title"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <div className="max-h-[92vh] w-full max-w-3xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <header className="sticky top-0 z-10 flex items-start justify-between border-b border-slate-200 bg-white px-6 py-5">
          <div>
            <h2
              id="nurse-form-title"
              className="text-xl font-semibold text-slate-950"
            >
              {editing
                ? 'Editar enfermero'
                : 'Nuevo enfermero'}
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              {editing
                ? 'Actualiza la información profesional.'
                : 'Asocia un usuario NURSE con su perfil profesional.'}
            </p>
          </div>

          <button
            type="button"
            onClick={onClose}
            aria-label="Cerrar"
            className="rounded-lg p-2 text-slate-400 hover:bg-slate-100"
          >
            <X className="size-5" />
          </button>
        </header>

        <form onSubmit={handleSubmit}>
          <div className="grid gap-5 px-6 py-6 md:grid-cols-2">
            {!editing && (
              <label className="block md:col-span-2">
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Usuario de enfermería
                </span>

                <select
                  value={userId}
                  onChange={(event) =>
                    setUserId(event.target.value)
                  }
                  className={inputClass}
                >
                  <option value="">
                    {usersQuery.isPending
                      ? 'Cargando usuarios...'
                      : 'Selecciona un usuario'}
                  </option>

                  {usersQuery.data?.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.firstName} {user.lastName} ·{' '}
                      {user.email}
                    </option>
                  ))}
                </select>
              </label>
            )}

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Departamento
              </span>

              <select
                value={departmentId}
                onChange={(event) =>
                  setDepartmentId(event.target.value)
                }
                className={inputClass}
              >
                <option value="">
                  Selecciona un departamento
                </option>

                {departmentsQuery.data?.map((department) => (
                  <option
                    key={department.id}
                    value={department.id}
                  >
                    {getDepartmentLabel(department.departmentType)} ·{' '}
{department.location}
                  </option>
                ))}
              </select>
            </label>

            {!editing && (
              <label>
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Número de licencia
                </span>

                <input
                  value={license}
                  onChange={(event) =>
                    setLicense(event.target.value)
                  }
                  maxLength={50}
                  placeholder="Ejemplo: ENF-12345"
                  className={inputClass}
                />
              </label>
            )}

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Especialidad
              </span>

              <select
                value={specialty}
                onChange={(event) =>
                  setSpecialty(
                    event.target.value as NurseSpecialty,
                  )
                }
                className={inputClass}
              >
                {specialties.map((value) => (
                  <option key={value} value={value}>
                    {nurseSpecialtyLabels[value]}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Turno
              </span>

              <select
                value={shiftType}
                onChange={(event) =>
                  setShiftType(
                    event.target.value as ShiftType,
                  )
                }
                className={inputClass}
              >
                {shifts.map((value) => (
                  <option key={value} value={value}>
                    {shiftTypeLabels[value]}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Años de experiencia
              </span>

              <input
                type="number"
                min={0}
                max={60}
                value={experience}
                onChange={(event) =>
                  setExperience(event.target.value)
                }
                className={inputClass}
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Fecha de contratación
              </span>

              <input
                type="date"
                max={new Date().toISOString().slice(0, 10)}
                value={hireDate}
                onChange={(event) =>
                  setHireDate(event.target.value)
                }
                className={inputClass}
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Máximo de pacientes por turno
              </span>

              <input
                type="number"
                min={0}
                max={20}
                value={maxPatients}
                onChange={(event) =>
                  setMaxPatients(event.target.value)
                }
                className={inputClass}
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Días de vacaciones disponibles
              </span>

              <input
                type="number"
                min={0}
                max={60}
                value={vacationDays}
                onChange={(event) =>
                  setVacationDays(event.target.value)
                }
                className={inputClass}
              />
            </label>

            <label className="flex items-center gap-3 rounded-xl border border-slate-200 p-4 md:col-span-2">
              <input
                type="checkbox"
                checked={chargeNurse}
                onChange={(event) =>
                  setChargeNurse(event.target.checked)
                }
                className="size-4"
              />

              <span className="text-sm font-medium text-slate-700">
                Es responsable de enfermería
              </span>
            </label>

            <div className="md:col-span-2">
              <h3 className="font-semibold text-slate-900">
                Contacto de emergencia
              </h3>
            </div>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Nombre
              </span>

              <input
                value={contactName}
                onChange={(event) =>
                  setContactName(event.target.value)
                }
                maxLength={150}
                className={inputClass}
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Teléfono
              </span>

              <input
                value={contactPhone}
                onChange={(event) =>
                  setContactPhone(event.target.value)
                }
                maxLength={20}
                className={inputClass}
              />
            </label>

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Relación
              </span>

              <input
                value={contactRelationship}
                onChange={(event) =>
                  setContactRelationship(event.target.value)
                }
                maxLength={50}
                placeholder="Ejemplo: Madre"
                className={inputClass}
              />
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Biografía
              </span>

              <textarea
                value={biography}
                onChange={(event) =>
                  setBiography(event.target.value)
                }
                maxLength={500}
                rows={5}
                className={`${inputClass} resize-none`}
              />
            </label>

            {error && (
              <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 md:col-span-2">
                {error}
              </div>
            )}
          </div>

          <footer className="sticky bottom-0 flex justify-end gap-3 border-t border-slate-200 bg-white px-6 py-5">
            <button
              type="button"
              onClick={onClose}
              disabled={mutation.isPending}
              className="rounded-xl border border-slate-200 px-4 py-2.5"
            >
              Cancelar
            </button>

            <button
              type="submit"
              disabled={mutation.isPending}
              className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-4 py-2.5 text-white hover:bg-cyan-800 disabled:opacity-50"
            >
              {mutation.isPending && (
                <LoaderCircle className="size-4 animate-spin" />
              )}

              {mutation.isPending
                ? 'Guardando...'
                : editing
                  ? 'Guardar cambios'
                  : 'Crear enfermero'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}