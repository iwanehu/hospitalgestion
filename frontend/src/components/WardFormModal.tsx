import { zodResolver } from '@hookform/resolvers/zod'
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { getActiveDepartments } from '../api/departments-api'
import {
  createWard,
  updateWard,
} from '../api/wards-api'
import { departmentTypeLabels } from '../types/department'
import type { Ward } from '../types/ward'

const wardSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, 'El nombre es obligatorio')
    .max(100, 'Máximo 100 caracteres'),

  description: z
    .string()
    .trim()
    .max(500, 'Máximo 500 caracteres'),

  departmentId: z
    .number()
    .int()
    .positive('Selecciona un departamento'),

  isActive: z.boolean(),
})

type WardForm = z.infer<typeof wardSchema>

interface ApiErrorResponse {
  message?: string
}

interface WardFormModalProps {
  ward?: Ward
  onClose: () => void
}

export function WardFormModal({
  ward,
  onClose,
}: WardFormModalProps) {
  const queryClient = useQueryClient()
  const [serverError, setServerError] = useState('')

  const editing = ward !== undefined

  const departmentsQuery = useQuery({
    queryKey: ['active-departments'],
    queryFn: getActiveDepartments,
  })

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<WardForm>({
    resolver: zodResolver(wardSchema),
    defaultValues: {
      name: ward?.name ?? '',
      description: ward?.description ?? '',
      departmentId: ward?.departmentId ?? 0,
      isActive: ward?.isActive ?? true,
    },
  })

  const saveMutation = useMutation({
    mutationFn: async (values: WardForm) => {
      if (ward) {
        return updateWard(ward.id, {
          name: values.name,
          description: values.description,
          isActive: values.isActive,
        })
      }

      return createWard({
        name: values.name,
        description: values.description,
        departmentId: values.departmentId,
      })
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['wards'],
      })

      onClose()
    },

    onError: (error: unknown) => {
      if (axios.isAxiosError<ApiErrorResponse>(error)) {
        setServerError(
          error.response?.data?.message ??
            `No se pudo ${
              editing ? 'actualizar' : 'crear'
            } la sala`,
        )
        return
      }

      setServerError('Se produjo un error inesperado')
    },
  })

  const onSubmit = (values: WardForm) => {
    setServerError('')
    saveMutation.mutate(values)
  }

  const departments =
    departmentsQuery.data ?? []

  const noDepartments =
    !editing &&
    !departmentsQuery.isPending &&
    departments.length === 0

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        aria-label="Cerrar formulario"
        className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm"
        onClick={() => {
          if (!saveMutation.isPending) {
            onClose()
          }
        }}
      />

      <section className="relative z-10 max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div>
            <h2 className="text-xl font-semibold text-slate-950">
              {editing ? 'Editar sala' : 'Nueva sala'}
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              {editing
                ? 'Actualiza la información de la sala.'
                : 'Asocia una nueva sala a un departamento.'}
            </p>
          </div>

          <button
            type="button"
            disabled={saveMutation.isPending}
            onClick={onClose}
            className="rounded-lg p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 disabled:opacity-50"
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
              htmlFor="ward-form-name"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Nombre
            </label>

            <input
              id="ward-form-name"
              placeholder="Ejemplo: Observación general"
              {...register('name')}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
            />

            {errors.name && (
              <p className="mt-2 text-sm text-red-600">
                {errors.name.message}
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="ward-form-department"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Departamento
            </label>

            <select
              id="ward-form-department"
              disabled={
                editing ||
                departmentsQuery.isPending ||
                noDepartments
              }
              {...register('departmentId', {
                valueAsNumber: true,
              })}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-cyan-600 focus:ring-4 focus:ring-cyan-600/10 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500"
            >
              <option value={0}>
                {departmentsQuery.isPending
                  ? 'Cargando departamentos...'
                  : 'Selecciona un departamento'}
              </option>

              {editing && ward && (
                <option value={ward.departmentId}>
                  {
                    departmentTypeLabels[
                      ward.departmentType
                    ]
                  }
                </option>
              )}

              {!editing &&
                departments.map((department) => (
                  <option
                    key={department.id}
                    value={department.id}
                  >
                    {
                      departmentTypeLabels[
                        department.departmentType
                      ]
                    }
                    {' — '}
                    {department.location}
                  </option>
                ))}
            </select>

            {errors.departmentId && (
              <p className="mt-2 text-sm text-red-600">
                {errors.departmentId.message}
              </p>
            )}

            {editing && (
              <p className="mt-2 text-xs text-slate-500">
                El departamento no puede modificarse durante
                la edición.
              </p>
            )}

            {noDepartments && (
              <p className="mt-2 text-sm text-amber-700">
                Primero debes crear y activar un departamento.
              </p>
            )}
          </div>

          <div>
            <label
              htmlFor="ward-form-description"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Descripción
            </label>

            <textarea
              id="ward-form-description"
              rows={4}
              placeholder="Describe las funciones de la sala"
              {...register('description')}
              className="w-full resize-none rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
            />

            {errors.description && (
              <p className="mt-2 text-sm text-red-600">
                {errors.description.message}
              </p>
            )}
          </div>

          {editing && (
            <div>
              <label
                htmlFor="ward-form-status"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Estado
              </label>

              <select
                id="ward-form-status"
                {...register('isActive', {
                  setValueAs: (value) => value === 'true',
                })}
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none transition focus:border-cyan-600 focus:ring-4 focus:ring-cyan-600/10"
              >
                <option value="true">Activo</option>
                <option value="false">Inactivo</option>
              </select>
            </div>
          )}

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
              disabled={saveMutation.isPending}
              onClick={onClose}
              className="rounded-xl border border-slate-200 px-5 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 disabled:opacity-50"
            >
              Cancelar
            </button>

            <button
              type="submit"
              disabled={
                saveMutation.isPending || noDepartments
              }
              className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-5 py-2.5 text-sm font-medium text-white hover:bg-cyan-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {saveMutation.isPending && (
                <LoaderCircle className="size-4 animate-spin" />
              )}

              {saveMutation.isPending
                ? 'Guardando...'
                : editing
                  ? 'Guardar cambios'
                  : 'Crear sala'}
            </button>
          </footer>
        </form>
      </section>
    </div>
  )
}