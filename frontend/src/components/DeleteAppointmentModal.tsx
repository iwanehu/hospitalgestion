import { useMutation, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import {
  AlertTriangle,
  LoaderCircle,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { deleteAppointment } from '../api/appointments-api'
import type { Appointment } from '../types/appointment'

interface Props {
  appointment: Appointment
  onClose: () => void
}

interface ApiError {
  message?: string
}

export function DeleteAppointmentModal({
  appointment,
  onClose,
}: Props) {
  const queryClient = useQueryClient()
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: () =>
      deleteAppointment(appointment.id),

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
            'No se pudo eliminar la cita.',
        )
        return
      }

      setError('No se pudo eliminar la cita.')
    },
  })

  const canDelete =
    appointment.status === 'CANCELLED' ||
    appointment.status === 'COMPLETED' ||
    appointment.status === 'NO_SHOW'

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="delete-appointment-title"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
    >
      <div className="w-full max-w-md overflow-hidden rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div>
            <h2
              id="delete-appointment-title"
              className="text-xl font-semibold text-slate-950"
            >
              Eliminar cita
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Esta operación no se puede deshacer.
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

        <div className="space-y-5 px-6 py-6">
          <div className="flex size-12 items-center justify-center rounded-2xl bg-red-50 text-red-600">
            <AlertTriangle className="size-6" />
          </div>

          <p className="text-slate-700">
            ¿Seguro que quieres eliminar la cita de{' '}
            <strong>{appointment.patientName}</strong>?
          </p>

          {!canDelete && (
            <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
              Primero debes completar, cancelar o marcar como no
              presentada la cita.
            </div>
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
            className="rounded-xl border border-slate-200 px-4 py-2.5 text-slate-600"
          >
            Cancelar
          </button>

          <button
            type="button"
            disabled={!canDelete || mutation.isPending}
            onClick={() => {
              setError('')
              mutation.mutate()
            }}
            className="inline-flex items-center gap-2 rounded-xl bg-red-600 px-4 py-2.5 text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {mutation.isPending && (
              <LoaderCircle className="size-4 animate-spin" />
            )}

            {mutation.isPending
              ? 'Eliminando...'
              : 'Eliminar cita'}
          </button>
        </footer>
      </div>
    </div>
  )
}