import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import {
  AlertTriangle,
  LoaderCircle,
  Trash2,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { deleteDepartment } from '../api/departments-api'
import {
  departmentTypeLabels,
  type Department,
} from '../types/department'

interface ApiErrorResponse {
  message?: string
}

interface DeleteDepartmentModalProps {
  department: Department
  onClose: () => void
}

export function DeleteDepartmentModal({
  department,
  onClose,
}: DeleteDepartmentModalProps) {
  const queryClient = useQueryClient()
  const [serverError, setServerError] = useState('')

  const deleteMutation = useMutation({
    mutationFn: () => deleteDepartment(department.id),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['departments'],
      })

      onClose()
    },

    onError: (error: unknown) => {
      if (axios.isAxiosError<ApiErrorResponse>(error)) {
        setServerError(
          error.response?.data?.message ??
            'No se pudo eliminar el departamento',
        )
        return
      }

      setServerError('Se produjo un error inesperado')
    },
  })

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        aria-label="Cerrar confirmación"
        className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm"
        onClick={() => {
          if (!deleteMutation.isPending) {
            onClose()
          }
        }}
      />

      <section className="relative z-10 w-full max-w-md rounded-3xl bg-white shadow-2xl">
        <header className="flex justify-end px-5 pt-5">
          <button
            type="button"
            disabled={deleteMutation.isPending}
            onClick={onClose}
            className="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 disabled:opacity-50"
            aria-label="Cerrar"
          >
            <X className="size-5" />
          </button>
        </header>

        <div className="px-7 pb-7 text-center">
          <div className="mx-auto mb-5 flex size-16 items-center justify-center rounded-2xl bg-red-50 text-red-600">
            <AlertTriangle className="size-8" />
          </div>

          <h2 className="text-xl font-semibold text-slate-950">
            Eliminar departamento
          </h2>

          <p className="mt-3 text-sm leading-6 text-slate-500">
            Vas a eliminar permanentemente el departamento de{' '}
            <strong className="text-slate-800">
              {
                departmentTypeLabels[
                  department.departmentType
                ]
              }
            </strong>
            . Esta acción no se puede deshacer.
          </p>

          {department.totalWards > 0 && (
            <div className="mt-5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-left text-sm text-amber-800">
              Este departamento contiene{' '}
              <strong>
                {department.totalWards}{' '}
                {department.totalWards === 1
                  ? 'sala'
                  : 'salas'}
              </strong>
              . El backend podría impedir su eliminación.
            </div>
          )}

          {serverError && (
            <div
              role="alert"
              className="mt-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
            >
              {serverError}
            </div>
          )}

          <footer className="mt-7 flex justify-center gap-3">
            <button
              type="button"
              disabled={deleteMutation.isPending}
              onClick={onClose}
              className="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:opacity-50"
            >
              Cancelar
            </button>

            <button
              type="button"
              disabled={deleteMutation.isPending}
              onClick={() => {
                setServerError('')
                deleteMutation.mutate()
              }}
              className="inline-flex items-center gap-2 rounded-xl bg-red-600 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {deleteMutation.isPending ? (
                <LoaderCircle className="size-4 animate-spin" />
              ) : (
                <Trash2 className="size-4" />
              )}

              {deleteMutation.isPending
                ? 'Eliminando...'
                : 'Eliminar'}
            </button>
          </footer>
        </div>
      </section>
    </div>
  )
}