import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import { type FormEvent, useState } from 'react'
import {
  createAppointment,
  getAppointmentDoctors,
  getAppointmentPatients,
  getAppointmentRooms,
  updateAppointment,
} from '../api/appointments-api'
import type { Appointment } from '../types/appointment'

interface Props {
  appointment?: Appointment
  onClose: () => void
}

interface ApiError {
  message?: string
}

function toDateTimeInput(value?: string): string {
  if (!value) {
    return ''
  }

  return value.slice(0, 16)
}

export function AppointmentFormModal({
  appointment,
  onClose,
}: Props) {
  const queryClient = useQueryClient()
  const editing = appointment !== undefined

  const [patientId, setPatientId] = useState(
    appointment?.patientId.toString() ?? '',
  )
  const [doctorId, setDoctorId] = useState(
    appointment?.doctorId.toString() ?? '',
  )
  const [roomId, setRoomId] = useState(
    appointment?.roomId?.toString() ?? '',
  )
  const [dateTime, setDateTime] = useState(
    toDateTimeInput(appointment?.dateTime),
  )
  const [reason, setReason] = useState(
    appointment?.reason ?? '',
  )
  const [notes, setNotes] = useState(
    appointment?.notes ?? '',
  )
  const [error, setError] = useState('')

  const patientsQuery = useQuery({
    queryKey: ['appointment-options', 'patients'],
    queryFn: getAppointmentPatients,
    enabled: !editing,
  })

  const doctorsQuery = useQuery({
    queryKey: ['appointment-options', 'doctors'],
    queryFn: getAppointmentDoctors,
  })

  const roomsQuery = useQuery({
    queryKey: ['appointment-options', 'rooms'],
    queryFn: getAppointmentRooms,
  })

  const mutation = useMutation({
    mutationFn: () => {
      const commonData = {
        doctorId: Number(doctorId),
        roomId: roomId ? Number(roomId) : undefined,
        dateTime,
        reason: reason.trim(),
        notes: notes.trim() || undefined,
      }

      if (appointment) {
        return updateAppointment(
          appointment.id,
          commonData,
        )
      }

      return createAppointment({
        ...commonData,
        patientId: Number(patientId),
      })
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['appointments'],
      })

      onClose()
    },

    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(
          cause.response?.data?.message ??
            'No se pudo guardar la cita.',
        )
        return
      }

      setError('No se pudo guardar la cita.')
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
      setError('Selecciona un médico.')
      return
    }

    if (!dateTime) {
      setError('Selecciona la fecha y la hora.')
      return
    }

    if (new Date(dateTime).getTime() <= Date.now()) {
      setError('La cita debe programarse en el futuro.')
      return
    }

    if (!reason.trim()) {
      setError('Escribe el motivo de la cita.')
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
      aria-labelledby="appointment-form-title"
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
              id="appointment-form-title"
              className="text-xl font-semibold text-slate-950"
            >
              {editing ? 'Reprogramar cita' : 'Nueva cita'}
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              {editing
                ? 'Actualiza el médico, fecha y detalles.'
                : 'Programa una consulta para un paciente.'}
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

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Médico
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

            <label>
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Habitación opcional
              </span>

              <select
                value={roomId}
                onChange={(event) =>
                  setRoomId(event.target.value)
                }
                className={inputClass}
              >
                <option value="">Sin habitación</option>

                {roomsQuery.data?.map((room) => (
                  <option key={room.id} value={room.id}>
                    Habitación {room.number} ·{' '}
                    {room.wardName}
                  </option>
                ))}
              </select>
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Fecha y hora
              </span>

              <input
                type="datetime-local"
                value={dateTime}
                onChange={(event) =>
                  setDateTime(event.target.value)
                }
                min={new Date().toISOString().slice(0, 16)}
                className={inputClass}
              />
            </label>

            <label className="md:col-span-2">
              <span className="mb-2 block text-sm font-medium text-slate-700">
                Motivo
              </span>

              <input
                value={reason}
                onChange={(event) =>
                  setReason(event.target.value)
                }
                maxLength={200}
                placeholder="Motivo principal de la consulta"
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
                maxLength={500}
                rows={5}
                placeholder="Observaciones adicionales"
                className={`${inputClass} resize-none`}
              />
            </label>

            {(patientsQuery.isError ||
              doctorsQuery.isError ||
              roomsQuery.isError) && (
              <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800 md:col-span-2">
                No se pudieron cargar todas las opciones del
                formulario.
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
              className="rounded-xl border border-slate-200 px-4 py-2.5 text-slate-600"
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
                  : 'Crear cita'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}