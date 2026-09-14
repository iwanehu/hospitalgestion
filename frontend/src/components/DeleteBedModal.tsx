import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import {
  AlertTriangle,
  LoaderCircle,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { deleteBed } from '../api/beds-api'
import type { Bed } from '../types/bed'

interface DeleteBedModalProps {
  bed: Bed
  onClose: () => void
}

interface ApiError {
  message?: string
}

export function DeleteBedModal({
  bed,
  onClose,
}: DeleteBedModalProps) {
  const queryClient = useQueryClient()
  const [error, setError] = useState('')

  const isOccupied = bed.status === 'OCCUPIED'

  const deleteMutation = useMutation({
    mutationFn: () => deleteBed(bed.id),

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ['beds'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['rooms'],
        }),
      ])

      onClose()
    },

    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(
          cause.response?.data?.message ??
            'No se pudo eliminar la cama.',
        )
        return
      }

      setError('No se pudo eliminar la cama.')
    },
  })

  const handleClose = () => {
    if (!deleteMutation.isPending) {
      onClose()
    }
  }

  const handleDelete = () => {
    if (isOccupied || deleteMutation.isPending) {
      return
    }

    setError('')
    deleteMutation.mutate()
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="delete-bed-title"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          handleClose()
        }
      }}
    >
      <div className="w-full max-w-md overflow-hidden rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div>
            <h2
              id="delete-bed-title"
              className="text-xl font-semibold text-slate-950"
            >
              Eliminar cama
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Esta operación no se puede deshacer.
            </p>
          </div>

          <button
            type="button"
            onClick={handleClose}
            disabled={deleteMutation.isPending}
            aria-label="Cerrar"
            className="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <X className="size-5" />
          </button>
        </header>

        <div className="space-y-5 px-6 py-6">
          <div className="flex size-12 items-center justify-center rounded-2xl bg-red-50 text-red-600">
            <AlertTriangle className="size-6" />
          </div>

          <p className="text-slate-700">
            ¿Seguro que quieres eliminar la cama{' '}
            <strong className="font-semibold text-slate-950">
              {bed.bedNumber}
            </strong>
            ?
          </p>

          {isOccupied && (
            <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
              No puedes eliminar una cama ocupada. Primero debes
              liberarla.
            </div>
          )}

          {error && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
            >
              {error}
            </div>
          )}
        </div>

        <footer className="flex justify-end gap-3 border-t border-slate-200 px-6 py-5">
          <button
            type="button"
            onClick={handleClose}
            disabled={deleteMutation.isPending}
            className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Cancelar
          </button>

          <button
            type="button"
            onClick={handleDelete}
            disabled={isOccupied || deleteMutation.isPending}
            className="inline-flex items-center gap-2 rounded-xl bg-red-600 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {deleteMutation.isPending && (
              <LoaderCircle className="size-4 animate-spin" />
            )}

            {deleteMutation.isPending
              ? 'Eliminando...'
              : 'Eliminar cama'}
          </button>
        </footer>
      </div>
    </div>
  )
}