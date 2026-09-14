import { useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import {
  AlertTriangle,
  CalendarCheck,
  LoaderCircle,
  X,
} from 'lucide-react'
import { type FormEvent, useState } from 'react'
import {
  cancelAppointment,
  updateAppointmentStatus,
} from '../api/appointments-api'
import {
  appointmentStatusLabels,
  type Appointment,
  type AppointmentStatus,
} from '../types/appointment'

interface Props {
  appointment: Appointment
  status: AppointmentStatus
  onClose: () => void
}

interface ApiError {
  message?: string
}

export function AppointmentStatusModal({
  appointment,
  status,
  onClose,
}: Props) {
  const queryClient = useQueryClient()
  const cancelling = status === 'CANCELLED'
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: () => {
      if (cancelling) {
        return cancelAppointment(appointment.id, {
          reason: reason.trim(),
        })
      }

      return updateAppointmentStatus(
        appointment.id,
        status,
      )
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
            'No se pudo actualizar la cita.',
        )
        return
      }

      setError('No se pudo actualizar la cita.')
    },
  })

  function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()
    setError('')

    if (cancelling && !reason.trim()) {
      setError('Escribe el motivo de la cancelación.')
      return
    }

    mutation.mutate()
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="appointment-status-title"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <div className="w-full max-w-lg overflow-hidden rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div>
            <h2
              id="appointment-status-title"
              className="text-xl font-semibold text-slate-950"
            >
              {cancelling
                ? 'Cancelar cita'
                : `Marcar como ${appointmentStatusLabels[
                    status
                  ].toLowerCase()}`}
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Actualiza el estado de la cita seleccionada.
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
          <div className="space-y-5 px-6 py-6">
            <div
              className={`flex size-12 items-center justify-center rounded-2xl ${
                cancelling
                  ? 'bg-red-50 text-red-600'
                  : 'bg-cyan-50 text-cyan-700'
              }`}
            >
              {cancelling ? (
                <AlertTriangle className="size-6" />
              ) : (
                <CalendarCheck className="size-6" />
              )}
            </div>

            <div>
              <p className="font-medium text-slate-900">
                {appointment.patientName}
              </p>

              <p className="mt-1 text-sm text-slate-500">
                Con {appointment.doctorName} ·{' '}
                {formatDateTime(appointment.dateTime)}
              </p>
            </div>

            <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-700">
              Estado actual:{' '}
              <strong>
                {
                  appointmentStatusLabels[
                    appointment.status
                  ]
                }
              </strong>
              <br />
              Nuevo estado:{' '}
              <strong>
                {appointmentStatusLabels[status]}
              </strong>
            </div>

            {cancelling && (
              <label className="block">
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Motivo de cancelación
                </span>

                <textarea
                  value={reason}
                  onChange={(event) =>
                    setReason(event.target.value)
                  }
                  maxLength={200}
                  rows={4}
                  placeholder="Indica por qué se cancela la cita"
                  className="w-full resize-none rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-red-500"
                />
              </label>
            )}

            {error && (
              <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                {error}
              </div>
            )}
          </div>

          <footer className="flex justify-end gap-3 border-t border-slate-200 px-6 py-5">
            <button
              type="button"
              onClick={onClose}
              disabled={mutation.isPending}
              className="rounded-xl border border-slate-200 px-4 py-2.5 text-slate-600"
            >
              Volver
            </button>

            <button
              type="submit"
              disabled={mutation.isPending}
              className={`inline-flex items-center gap-2 rounded-xl px-4 py-2.5 text-white disabled:opacity-50 ${
                cancelling
                  ? 'bg-red-600 hover:bg-red-700'
                  : 'bg-cyan-700 hover:bg-cyan-800'
              }`}
            >
              {mutation.isPending && (
                <LoaderCircle className="size-4 animate-spin" />
              )}

              {mutation.isPending
                ? 'Procesando...'
                : cancelling
                  ? 'Cancelar cita'
                  : 'Confirmar cambio'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}

function formatDateTime(value: string): string {
  return new Intl.DateTimeFormat('es-ES', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}