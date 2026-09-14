import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import { type FormEvent, useState } from 'react'
import {
  createAdmission,
  getAdmissionDoctors,
  getAdmissionPatients,
  getAvailableAdmissionBeds,
  updateAdmission,
} from '../api/admissions-api'
import type { Admission } from '../types/admission'

interface Props {
  admission?: Admission
  onClose: () => void
}

interface ApiError {
  message?: string
}

export function AdmissionFormModal({
  admission,
  onClose,
}: Props) {
  const queryClient = useQueryClient()
  const editing = admission !== undefined

  const [patientId, setPatientId] = useState(
    admission?.patientId.toString() ?? '',
  )
  const [doctorId, setDoctorId] = useState(
    admission?.attendingDoctorId.toString() ?? '',
  )
  const [bedId, setBedId] = useState(
    admission?.bedId.toString() ?? '',
  )
  const [reason, setReason] = useState(
    admission?.admissionReason ?? '',
  )
  const [notes, setNotes] = useState(
    admission?.notes ?? '',
  )
  const [error, setError] = useState('')

  const patientsQuery = useQuery({
    queryKey: ['admission-options', 'patients'],
    queryFn: getAdmissionPatients,
    enabled: !editing,
  })

  const doctorsQuery = useQuery({
    queryKey: ['admission-options', 'doctors'],
    queryFn: getAdmissionDoctors,
  })

  const bedsQuery = useQuery({
    queryKey: ['admission-options', 'beds'],
    queryFn: getAvailableAdmissionBeds,
    enabled: !editing,
  })

  const mutation = useMutation({
    mutationFn: () => {
      const commonData = {
        attendingDoctorId: Number(doctorId),
        admissionReason: reason.trim(),
        notes: notes.trim() || undefined,
      }

      if (admission) {
        return updateAdmission(admission.id, commonData)
      }

      return createAdmission({
        ...commonData,
        patientId: Number(patientId),
        bedId: Number(bedId),
      })
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ['admissions'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['beds'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['rooms'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['admission-options'],
        }),
      ])

      onClose()
    },

    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(
          cause.response?.data?.message ??
            'No se pudo guardar la admisión.',
        )
        return
      }

      setError('No se pudo guardar la admisión.')
    },
  })

  function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()
    setError('')

    if (!editing && !patientId) {
      setError('Selecciona un paciente.')
      return
    }

    if (!doctorId) {
      setError('Selecciona el médico responsable.')
      return
    }

    if (!editing && !bedId) {
      setError('Selecciona una cama disponible.')
      return
    }

    if (!reason.trim()) {
      setError('Escribe el motivo del ingreso.')
      return
    }

    mutation.mutate()
  }

  const inputClass =
    'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-cyan-600'

  const loadingOptions =
    patientsQuery.isPending ||
    doctorsQuery.isPending ||
    bedsQuery.isPending

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="admission-form-title"
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
              id="admission-form-title"
              className="text-xl font-semibold text-slate-950"
            >
              {editing
                ? 'Editar admisión'
                : 'Nueva admisión'}
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              {editing
                ? 'Actualiza el médico, motivo y notas.'
                : 'Asigna un paciente a una cama disponible.'}
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
              <label className="md:col-span-2">
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Paciente
                </span>

                <select
                  value={patientId}
                  onChange={(event) =>
                    setPatientId(event.target.value)
                  }
                  className={inputClass}
                >
                  <option value="">
                    {patientsQuery.isPending
                      ? 'Cargando pacientes...'
                      : 'Selecciona un paciente'}
                  </option>

                  {patientsQuery.data?.map((patient) => (
                    <option
                      key={patient.id}
                      value={patient.id}
                    >
                      {patient.fullName} ·{' '}
                      {patient.documentId}
                    </option>
                  ))}
                </select>
              </label>
            )}

            <label className={editing ? 'md:col-span-2' : ''}>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Médico responsable
              </span>

              <select
                value={doctorId}
                onChange={(event) =>
                  setDoctorId(event.target.value)
                }
                className={inputClass}
              >
                <option value="">
                  {doctorsQuery.isPending
                    ? 'Cargando médicos...'
                    : 'Selecciona un médico'}
                </option>

                {doctorsQuery.data?.map((doctor) => (
                  <option
                    key={doctor.id}
                    value={doctor.id}
                  >
                    {doctor.fullName}
                  </option>
                ))}
              </select>
            </label>

            {!editing && (
              <label>
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Cama disponible
                </span>

                <select
                  value={bedId}
                  onChange={(event) =>
                    setBedId(event.target.value)
                  }
                  className={inputClass}
                >
                  <option value="">
                    {bedsQuery.isPending
                      ? 'Cargando camas...'
                      : 'Selecciona una cama'}
                  </option>

                  {bedsQuery.data?.map((bed) => (
                    <option key={bed.id} value={bed.id}>
                      {bed.bedNumber} · Habitación{' '}
                      {bed.roomNumber} · {bed.wardName}
                    </option>
                  ))}
                </select>

                {bedsQuery.data?.length === 0 && (
                  <p className="mt-2 text-sm text-amber-700">
                    No existen camas disponibles.
                  </p>
                )}
              </label>
            )}

            <label className="md:col-span-2">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Motivo del ingreso
              </span>

              <input
                value={reason}
                onChange={(event) =>
                  setReason(event.target.value)
                }
                maxLength={255}
                placeholder="Describe el motivo principal"
                className={inputClass}
              />
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Notas
              </span>

              <textarea
                value={notes}
                onChange={(event) =>
                  setNotes(event.target.value)
                }
                maxLength={1000}
                rows={6}
                placeholder="Observaciones adicionales"
                className={`${inputClass} resize-none`}
              />
            </label>

            {loadingOptions && (
              <p className="text-sm text-slate-500 md:col-span-2">
                Cargando opciones del formulario...
              </p>
            )}

            {(patientsQuery.isError ||
              doctorsQuery.isError ||
              bedsQuery.isError) && (
              <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800 md:col-span-2">
                No se pudieron cargar todas las opciones.
                Comprueba pacientes, médicos y camas.
              </div>
            )}

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
              className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-4 py-2.5 text-white hover:bg-cyan-800 disabled:opacity-50"
            >
              {mutation.isPending && (
                <LoaderCircle className="size-4 animate-spin" />
              )}

              {mutation.isPending
                ? 'Guardando...'
                : editing
                  ? 'Guardar cambios'
                  : 'Crear admisión'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}