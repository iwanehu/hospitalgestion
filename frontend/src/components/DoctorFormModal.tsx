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
  createDoctor,
  getActiveDepartments,
  getAvailableDoctorUsers,
  updateDoctor,
} from '../api/staff-api'
import {
  specialtyLabels,
  type Doctor,
  type Specialty,
} from '../types/staff'

import { getDepartmentLabel } from '../utils/department-label'

interface Props {
  doctor?: Doctor
  onClose: () => void
}

interface ApiError {
  message?: string
}

const specialties = Object.keys(
  specialtyLabels,
) as Specialty[]

export function DoctorFormModal({
  doctor,
  onClose,
}: Props) {
  const queryClient = useQueryClient()
  const editing = doctor !== undefined

  const [userId, setUserId] = useState('')
  const [departmentId, setDepartmentId] = useState(
    doctor?.departmentId.toString() ?? '',
  )
  const [specialty, setSpecialty] = useState<Specialty>(
    doctor?.specialty ?? 'GENERAL_SURGERY',
  )
  const [license, setLicense] = useState(
    doctor?.medicalLicenseNumber ?? '',
  )
  const [experience, setExperience] = useState(
    doctor?.yearsOfExperience.toString() ?? '0',
  )
  const [biography, setBiography] = useState(
    doctor?.biography ?? '',
  )
  const [error, setError] = useState('')

  const departmentsQuery = useQuery({
    queryKey: ['departments', 'active', 'staff'],
    queryFn: getActiveDepartments,
  })

  const usersQuery = useQuery({
    queryKey: ['users', 'available-doctors'],
    queryFn: getAvailableDoctorUsers,
    enabled: !editing,
  })

  const mutation = useMutation({
    mutationFn: () => {
      const commonData = {
        departmentId: Number(departmentId),
        specialty,
        yearsOfExperience: Number(experience),
        biography: biography.trim() || undefined,
      }

      if (doctor) {
        return updateDoctor(doctor.id, commonData)
      }

      return createDoctor({
        ...commonData,
        userId: Number(userId),
        medicalLicenseNumber: license.trim(),
      })
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ['doctors'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['users', 'available-doctors'],
        }),
      ])

      onClose()
    },

    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(
          cause.response?.data?.message ??
            'No se pudo guardar el médico.',
        )
        return
      }

      setError('No se pudo guardar el médico.')
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (!editing && !userId) {
      setError('Selecciona un usuario médico.')
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

    if (
      !Number.isInteger(Number(experience)) ||
      Number(experience) < 0
    ) {
      setError(
        'Los años de experiencia deben ser un número entero positivo.',
      )
      return
    }

    mutation.mutate()
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="doctor-form-title"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <div className="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <header className="sticky top-0 z-10 flex items-start justify-between border-b border-slate-200 bg-white px-6 py-5">
          <div>
            <h2
              id="doctor-form-title"
              className="text-xl font-semibold text-slate-950"
            >
              {editing ? 'Editar médico' : 'Nuevo médico'}
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              {editing
                ? 'Actualiza la información profesional.'
                : 'Asocia un usuario DOCTOR con su perfil profesional.'}
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
                  Usuario médico
                </span>

                <select
                  value={userId}
                  onChange={(event) =>
                    setUserId(event.target.value)
                  }
                  disabled={
                    usersQuery.isPending ||
                    mutation.isPending
                  }
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3"
                >
                  <option value="">
                    {usersQuery.isPending
                      ? 'Cargando usuarios...'
                      : 'Selecciona un usuario'}
                  </option>

                  {usersQuery.data?.map((user) => (
                    <option
                      key={user.id}
                      value={user.id}
                    >
                      {user.firstName} {user.lastName} ·{' '}
                      {user.email}
                    </option>
                  ))}
                </select>

                {usersQuery.isError && (
                  <p className="mt-2 text-sm text-red-600">
                    No se pudieron cargar los usuarios.
                  </p>
                )}
              </label>
            )}

            <label className="block">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Departamento
              </span>

              <select
                value={departmentId}
                onChange={(event) =>
                  setDepartmentId(event.target.value)
                }
                disabled={mutation.isPending}
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3"
              >
                <option value="">
                  {departmentsQuery.isPending
                    ? 'Cargando departamentos...'
                    : 'Selecciona un departamento'}
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

            <label className="block">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Especialidad
              </span>

              <select
                value={specialty}
                onChange={(event) =>
                  setSpecialty(
                    event.target.value as Specialty,
                  )
                }
                disabled={mutation.isPending}
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3"
              >
                {specialties.map((value) => (
                  <option
                    key={value}
                    value={value}
                  >
                    {specialtyLabels[value]}
                  </option>
                ))}
              </select>
            </label>

            {!editing && (
              <label className="block">
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Número de licencia
                </span>

                <input
                  value={license}
                  onChange={(event) =>
                    setLicense(event.target.value)
                  }
                  disabled={mutation.isPending}
                  maxLength={50}
                  placeholder="Ejemplo: MED-12345"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-cyan-600"
                />
              </label>
            )}

            <label className="block">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Años de experiencia
              </span>

              <input
                type="number"
                min={0}
                step={1}
                value={experience}
                onChange={(event) =>
                  setExperience(event.target.value)
                }
                disabled={mutation.isPending}
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-cyan-600"
              />
            </label>

            <label className="block md:col-span-2">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Biografía
              </span>

              <textarea
                value={biography}
                onChange={(event) =>
                  setBiography(event.target.value)
                }
                disabled={mutation.isPending}
                maxLength={500}
                rows={5}
                placeholder="Experiencia, formación y funciones principales"
                className="w-full resize-none rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-cyan-600"
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
              className="rounded-xl border border-slate-200 px-4 py-2.5 text-slate-600 hover:bg-slate-50"
            >
              Cancelar
            </button>

            <button
              type="submit"
              disabled={mutation.isPending}
              className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-4 py-2.5 text-white hover:bg-cyan-800 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {mutation.isPending && (
                <LoaderCircle className="size-4 animate-spin" />
              )}

              {mutation.isPending
                ? 'Guardando...'
                : editing
                  ? 'Guardar cambios'
                  : 'Crear médico'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}