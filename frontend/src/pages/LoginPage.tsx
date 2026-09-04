import { zodResolver } from '@hookform/resolvers/zod'
import axios from 'axios'
import {
  Activity,
  Eye,
  EyeOff,
  Hospital,
  LoaderCircle,
  ShieldCheck,
} from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../auth/useAuth'

const loginSchema = z.object({
  email: z
    .email('Introduce un correo electrónico válido')
    .trim()
    .toLowerCase(),

  password: z
    .string()
    .min(1, 'Introduce tu contraseña'),
})

type LoginForm = z.infer<typeof loginSchema>

interface ApiErrorResponse {
  message?: string
}

export function LoginPage() {
  const navigate = useNavigate()
  const { isAuthenticated, login } = useAuth()
  const [showPassword, setShowPassword] = useState(false)
  const [serverError, setServerError] = useState('')

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  const onSubmit = async (values: LoginForm) => {
    setServerError('')

    try {
      await login(values)
      navigate('/', { replace: true })
    } catch (error: unknown) {
      if (axios.isAxiosError<ApiErrorResponse>(error)) {
        setServerError(
          error.response?.data?.message ??
            'No se pudo conectar con el servidor',
        )
        return
      }

      setServerError('Se produjo un error inesperado')
    }
  }

  return (
    <main className="relative flex min-h-screen overflow-hidden bg-slate-950">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,#164e63_0%,transparent_38%),radial-gradient(circle_at_bottom_right,#0f766e_0%,transparent_35%)] opacity-80" />

      <section className="relative hidden w-1/2 flex-col justify-between p-14 text-white lg:flex">
        <div className="flex items-center gap-3">
          <div className="rounded-2xl bg-cyan-400/15 p-3 ring-1 ring-cyan-300/30">
            <Hospital className="size-8 text-cyan-300" />
          </div>

          <div>
            <p className="text-xl font-semibold">
              Hospital Management
            </p>
            <p className="text-sm text-slate-400">
              Plataforma de gestión clínica
            </p>
          </div>
        </div>

        <div className="max-w-xl">
          <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-cyan-300/20 bg-cyan-400/10 px-4 py-2 text-sm text-cyan-200">
            <Activity className="size-4" />
            Gestión hospitalaria centralizada
          </div>

          <h1 className="text-5xl font-semibold leading-tight">
            Atención sanitaria,
            <span className="block text-cyan-300">
              organizada y conectada.
            </span>
          </h1>

          <p className="mt-6 max-w-lg text-lg leading-8 text-slate-300">
            Administra pacientes, personal, departamentos,
            habitaciones, camas, citas e ingresos desde un
            único espacio seguro.
          </p>
        </div>

        <div className="flex items-center gap-3 text-sm text-slate-400">
          <ShieldCheck className="size-5 text-emerald-400" />
          Acceso protegido mediante autenticación JWT
        </div>
      </section>

      <section className="relative flex w-full items-center justify-center p-6 lg:w-1/2">
        <div className="w-full max-w-md rounded-3xl border border-white/10 bg-white p-8 shadow-2xl shadow-black/30 sm:p-10">
          <div className="mb-8 lg:hidden">
            <div className="mb-4 inline-flex rounded-2xl bg-cyan-50 p-3">
              <Hospital className="size-7 text-cyan-700" />
            </div>

            <p className="font-semibold text-slate-900">
              Hospital Management
            </p>
          </div>

          <div className="mb-8">
            <h2 className="text-3xl font-semibold tracking-tight text-slate-950">
              Bienvenido
            </h2>
            <p className="mt-2 text-slate-500">
              Introduce tus credenciales para continuar.
            </p>
          </div>

          <form
            className="space-y-5"
            onSubmit={handleSubmit(onSubmit)}
            noValidate
          >
            <div>
              <label
                htmlFor="email"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Correo electrónico
              </label>

              <input
                id="email"
                type="email"
                autoComplete="email"
                placeholder="admin@hospital.com"
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
                {...register('email')}
              />

              {errors.email && (
                <p className="mt-2 text-sm text-red-600">
                  {errors.email.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="password"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Contraseña
              </label>

              <div className="relative">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  placeholder="Tu contraseña"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 pr-12 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10"
                  {...register('password')}
                />

                <button
                  type="button"
                  onClick={() =>
                    setShowPassword((current) => !current)
                  }
                  className="absolute inset-y-0 right-0 flex w-12 items-center justify-center text-slate-400 transition hover:text-slate-700"
                  aria-label={
                    showPassword
                      ? 'Ocultar contraseña'
                      : 'Mostrar contraseña'
                  }
                >
                  {showPassword ? (
                    <EyeOff className="size-5" />
                  ) : (
                    <Eye className="size-5" />
                  )}
                </button>
              </div>

              {errors.password && (
                <p className="mt-2 text-sm text-red-600">
                  {errors.password.message}
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

            <button
              type="submit"
              disabled={isSubmitting}
              className="flex w-full items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 font-medium text-white transition hover:bg-cyan-800 focus:outline-none focus:ring-4 focus:ring-cyan-700/20 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting && (
                <LoaderCircle className="size-5 animate-spin" />
              )}

              {isSubmitting
                ? 'Iniciando sesión...'
                : 'Iniciar sesión'}
            </button>
          </form>

          <p className="mt-8 text-center text-xs text-slate-400">
            Hospital Management API · Portfolio project
          </p>
        </div>
      </section>
    </main>
  )
}
