import { zodResolver } from '@hookform/resolvers/zod'
import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { createDepartment } from '../api/departments-api'
import {
  departmentTypeLabels,
  departmentTypes,
} from '../types/department'

const departmentSchema = z.object({
  departmentType: z.enum(departmentTypes),

  location: z
    .string()
    .trim()
    .min(1, 'La ubicación es obligatoria')
    .max(100, 'Máximo 100 caracteres'),

  phoneExtension: z
    .string()
    .trim()
    .max(10, 'Máximo 10 caracteres'),

  description: z
    .string()
    .trim()
    .max(500, 'Máximo 500 caracteres'),
})

type DepartmentForm = z.infer<typeof departmentSchema>

interface ApiErrorResponse {
  message?: string
}

interface DepartmentFormModalProps {
  open: boolean
  onClose: () => void
}

export function DepartmentFormModal({
  open,
  onClose,
}: DepartmentFormModalProps) {
  const queryClient = useQueryClient()
  const [serverError, setServerError] = useState('')

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DepartmentForm>({
    resolver: zodResolver(departmentSchema),
    defaultValues: {
      departmentType: 'EMERGENCY',
      location: '',
      phoneExtension: '',
      description: '',
    },
  })

  const closeModal = () => {
    setServerError('')
    reset()
    onClose()
  }

  const createMutation = useMutation({
    mutationFn: createDepartment,

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['departments'],
      })

      closeModal()
    },

    onError: (error: unknown) => {
      if (axios.isAxiosError<ApiErrorResponse>(error)) {
        setServerError(
          error.response?.data?.message ??
            'No se pudo crear el departamento',
        )
        return
      }

      setServerError('Se produjo un error inesperado')
    },
  })

  if (!open) {
    return null
  }

  const onSubmit = (values: DepartmentForm) => {
    setServerError('')
    createMutation.mutate(values)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        aria-label="Cerrar formulario"
        className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm"
        onClick={() => {
          if (!createMutation.isPending) {
            closeModal()
          }
        }}
      />

      <section className="relative z-10 max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div>
            <h2 className="text-xl font-semibold text-slate-950">
              Nuevo departamento
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Añade una nueva área al hospital.
            </p>
          </div>

          <button
            type="button"
            disabled={createMutation.isPending}
            onClick={closeModal}
            className="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 disabled:cursor-not-allowed disabled:opacity-50"
            aria-label="Cerrar"
          >
            <X className="size-5" />
          </button>
        </header>

        <form
          onSubmit={handleSubmit(onSubmit)}
          className="space-y-5 p-6"
          noValidate
        >
          <div>
            <label
              htmlFor="new-department-type"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Tipo de departamento
            </label>

            <select
              id="new-department-type"
              {...register('departmentType')}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
            >
              {departmentTypes.map((type) => (
                <option key={type} value={type}>
                  {departmentTypeLabels[type]}
                </option>
              ))}
            </select>

            {errors.departmentType && (
              <p className="mt-2 text-sm text-red-600">
                {errors.departmentType.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="new-department-location"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Ubicación
            </label>

            <input
              id="new-department-location"
              placeholder="Ejemplo: Planta 1"
              {...register('location')}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition placeholder:text-slate-400 focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
            />

            {errors.location && (
              <p className="mt-2 text-sm text-red-600">
                {errors.location.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="new-department-extension"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Extensión telefónica
            </label>

            <input
              id="new-department-extension"
              placeholder="Ejemplo: 101"
              {...register('phoneExtension')}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition placeholder:text-slate-400 focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
            />

            {errors.phoneExtension && (
              <p className="mt-2 text-sm text-red-600">
                {errors.phoneExtension.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="new-department-description"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Descripción
            </label>

            <textarea
              id="new-department-description"
              rows={4}
              placeholder="Describe las funciones del departamento"
              {...register('description')}
              className="w-full resize-none rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition placeholder:text-slate-400 focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
            />

            {errors.description && (
              <p className="mt-2 text-sm text-red-600">
                {errors.description.message}
              </p>
            )}
          </div>

          {serverError && (
            <div
              role="alert"
              className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
            >
              {serverError}
            </div>
          )}

          <footer className="flex justify-end gap-3 border-t border-slate-100 pt-5">
            <button
              type="button"
              disabled={createMutation.isPending}
              onClick={closeModal}
              className="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Cancelar
            </button>

            <button
              type="submit"
              disabled={createMutation.isPending}
              className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-5 py-2.5 text-sm font-medium text-white transition hover:bg-cyan-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {createMutation.isPending && (
                <LoaderCircle className="size-4 animate-spin" />
              )}

              {createMutation.isPending
                ? 'Guardando...'
                : 'Crear departamento'}
            </button>
          </footer>
        </form>
      </section>
    </div>
  )
}