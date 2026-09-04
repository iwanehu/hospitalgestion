import {
  BedDouble,
  CalendarDays,
  Hospital,
  LogOut,
  Stethoscope,
  Users,
} from 'lucide-react'
import { useAuth } from '../auth/useAuth'

const cards = [
  {
    label: 'Pacientes',
    description: 'Historias y datos clínicos',
    icon: Users,
    color: 'bg-blue-50 text-blue-700',
  },
  {
    label: 'Personal sanitario',
    description: 'Médicos y enfermería',
    icon: Stethoscope,
    color: 'bg-emerald-50 text-emerald-700',
  },
  {
    label: 'Camas',
    description: 'Ocupación y disponibilidad',
    icon: BedDouble,
    color: 'bg-violet-50 text-violet-700',
  },
  {
    label: 'Citas',
    description: 'Agenda hospitalaria',
    icon: CalendarDays,
    color: 'bg-amber-50 text-amber-700',
  },
]

export function DashboardPage() {
  const { user, logout } = useAuth()

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-cyan-700 p-2.5">
              <Hospital className="size-6 text-white" />
            </div>

            <div>
              <h1 className="font-semibold text-slate-950">
                Hospital Management
              </h1>
              <p className="text-xs text-slate-500">
                Panel de administración
              </p>
            </div>
          </div>

          <button
            onClick={logout}
            className="flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2 text-sm font-medium text-slate-600 transition hover:border-red-200 hover:bg-red-50 hover:text-red-700"
          >
            <LogOut className="size-4" />
            Cerrar sesión
          </button>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-6 py-10">
        <section className="mb-10">
          <p className="text-sm font-medium text-cyan-700">
            PANEL GENERAL
          </p>

          <h2 className="mt-2 text-3xl font-semibold tracking-tight text-slate-950">
            Bienvenido al hospital
          </h2>

          <p className="mt-2 text-slate-500">
            Sesión iniciada como {user?.email}
            {' · '}
            {user?.role}
          </p>
        </section>

        <section className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
          {cards.map((card) => {
            const Icon = card.icon

            return (
              <article
                key={card.label}
                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg"
              >
                <div
                  className={`mb-5 inline-flex rounded-xl p-3 ${card.color}`}
                >
                  <Icon className="size-6" />
                </div>

                <h3 className="font-semibold text-slate-950">
                  {card.label}
                </h3>

                <p className="mt-1 text-sm text-slate-500">
                  {card.description}
                </p>
              </article>
            )
          })}
        </section>
      </main>
    </div>
  )
}
