import {
  BedDouble,
  CalendarDays,
  Stethoscope,
  Users,
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

const cards = [
  {
    label: 'Pacientes',
    description: 'Historias y datos clínicos',
    icon: Users,
    color: 'bg-blue-50 text-blue-700',
    path: '/patients',
  },
  {
    label: 'Personal sanitario',
    description: 'Médicos y enfermería',
    icon: Stethoscope,
    color: 'bg-emerald-50 text-emerald-700',
    path: '/staff',
  },
  {
    label: 'Camas',
    description: 'Ocupación y disponibilidad',
    icon: BedDouble,
    color: 'bg-violet-50 text-violet-700',
    path: '/facilities',
  },
  {
    label: 'Citas',
    description: 'Agenda hospitalaria',
    icon: CalendarDays,
    color: 'bg-amber-50 text-amber-700',
    path: '/appointments',
  },
]

export function DashboardPage() {
  const { user } = useAuth()

  return (
    <div className="mx-auto max-w-7xl">
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
            <Link
              key={card.label}
              to={card.path}
              className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:border-cyan-200 hover:shadow-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-cyan-600 focus-visible:ring-offset-2"
            >
              <div
                className={`mb-5 inline-flex rounded-xl p-3 transition group-hover:scale-105 ${card.color}`}
              >
                <Icon className="size-6" />
              </div>

              <h3 className="font-semibold text-slate-950 group-hover:text-cyan-700">
                {card.label}
              </h3>

              <p className="mt-1 text-sm text-slate-500">
                {card.description}
              </p>
            </Link>
          )
        })}
      </section>
    </div>
  )
}