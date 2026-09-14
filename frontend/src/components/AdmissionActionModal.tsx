import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import {
  ArrowRightLeft,
  Ban,
  CheckCircle2,
  LoaderCircle,
  X,
} from 'lucide-react'
import { type FormEvent, useState } from 'react'
import {
  cancelAdmission,
  dischargeAdmission,
  getAvailableAdmissionBeds,
  transferAdmission,
} from '../api/admissions-api'
import type { Admission } from '../types/admission'

export type AdmissionAction =
  | 'discharge'
  | 'transfer'
  | 'cancel'

interface Props {
  admission: Admission
  action: AdmissionAction
  onClose: () => void
}

interface ApiError {
  message?: string
}

const actionContent = {
  discharge: {
    title: 'Dar de alta',
    description:
      'Finaliza el ingreso y envía la cama a limpieza.',
    button: 'Confirmar alta',
    color: 'bg-emerald-600 hover:bg-emerald-700',
    icon: CheckCircle2,
  },
  transfer: {
    title: 'Trasladar paciente',
    description:
      'Asigna al paciente una nueva cama disponible.',
    button: 'Confirmar traslado',
    color: 'bg-cyan-700 hover:bg-cyan-800',
    icon: ArrowRightLeft,
  },
  cancel: {
    title: 'Cancelar admisión',
    description:
      'Cancela el ingreso y libera la cama asignada.',
    button: 'Cancelar admisión',
    color: 'bg-red-600 hover:bg-red-700',
    icon: Ban,
  },
} satisfies Record<
  AdmissionAction,
  {
    title: string
    description: string
    button: string
    color: string
    icon: typeof CheckCircle2
  }
>

export function AdmissionActionModal({
  admission,
  action,
  onClose,
}: Props) {
  const queryClient = useQueryClient()
  const [bedId, setBedId] = useState('')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState('')

  const content = actionContent[action]
  const Icon = content.icon

  const bedsQuery = useQuery({
    queryKey: ['admission-options', 'beds'],
    queryFn: getAvailableAdmissionBeds,
    enabled: action === 'transfer',
  })

  const mutation = useMutation({
    mutationFn: () => {
      if (action === 'discharge') {
        return dischargeAdmission(admission.id, {
          notes: notes.trim() || undefined,
        })
      }

      if (action === 'transfer') {
        return transferAdmission(admission.id, {
          newBedId: Number(bedId),
          reason: notes.trim() || undefined,
        })
      }

      return cancelAdmission(admission.id)
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
            'No se pudo completar la operación.',
        )
        return
      }

      setError('No se pudo completar la operación.')
    },
  })

  function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()
    setError('')

    if (action === 'transfer' && !bedId) {
      setError('Selecciona una nueva cama.')
      return
    }

    mutation.mutate()
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="admission-action-title"
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
              id="admission-action-title"
              className="text-xl font-semibold text-slate-950"
            >
              {content.title}
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              {content.description}
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
                action === 'cancel'
                  ? 'bg-red-50 text-red-600'
                  : action === 'discharge'
                    ? 'bg-emerald-50 text-emerald-600'
                    : 'bg-cyan-50 text-cyan-700'
              }`}
            >
              <Icon className="size-6" />
            </div>

            <p className="text-slate-700">
              Paciente:{' '}
              <strong>{admission.patientName}</strong>
            </p>

            {action === 'transfer' && (
              <label className="block">
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Nueva cama
                </span>

                <select
                  value={bedId}
                  onChange={(event) =>
                    setBedId(event.target.value)
                  }
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3"
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
                    No existen otras camas disponibles.
                  </p>
                )}
              </label>
            )}

            {action !== 'cancel' && (
              <label className="block">
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  {action === 'discharge'
                    ? 'Notas del alta'
                    : 'Motivo del traslado'}
                </span>

                <textarea
                  value={notes}
                  onChange={(event) =>
                    setNotes(event.target.value)
                  }
                  maxLength={
                    action === 'discharge' ? 1000 : 500
                  }
                  rows={5}
                  className="w-full resize-none rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-cyan-600"
                />
              </label>
            )}

            {action === 'cancel' && (
              <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                Esta acción cambiará el estado de la admisión a
                cancelada y enviará la cama a limpieza.
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
              disabled={mutation.isPending}
              className="rounded-xl border border-slate-200 px-4 py-2.5 text-slate-600 hover:bg-slate-50"
            >
              Volver
            </button>

            <button
              type="submit"
              disabled={
                mutation.isPending ||
                (action === 'transfer' &&
                  bedsQuery.data?.length === 0)
              }
              className={`inline-flex items-center gap-2 rounded-xl px-4 py-2.5 text-white disabled:cursor-not-allowed disabled:opacity-50 ${content.color}`}
            >
              {mutation.isPending && (
                <LoaderCircle className="size-4 animate-spin" />
              )}

              {mutation.isPending
                ? 'Procesando...'
                : content.button}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}