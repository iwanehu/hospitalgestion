import {
  BedDouble,
  Building2,
  CalendarDays,
  ClipboardPlus,
  Hospital,
  LayoutDashboard,
  LogOut,
  Menu,
  Stethoscope,
  Users,
  X,
} from 'lucide-react'
import { useState } from 'react'
import {
  NavLink,
  Outlet,
  useLocation,
} from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

const navigation = [
  {
    label: 'Panel general',
    path: '/',
    icon: LayoutDashboard,
  },
  {
    label: 'Departamentos',
    path: '/departments',
    icon: Building2,
  },
  {
    label: 'Pacientes',
    path: '/patients',
    icon: Users,
  },
  {
    label: 'Personal sanitario',
    path: '/staff',
    icon: Stethoscope,
  },
  {
    label: 'Habitaciones y camas',
    path: '/facilities',
    icon: BedDouble,
  },
  {
    label: 'Admisiones',
    path: '/admissions',
    icon: ClipboardPlus,
  },
  {
    label: 'Citas',
    path: '/appointments',
    icon: CalendarDays,
  },
]

const pageTitles: Record<string, string> = {
  '/': 'Panel general',
  '/departments': 'Departamentos',
  '/patients': 'Pacientes',
  '/staff': 'Personal sanitario',
  '/facilities': 'Habitaciones y camas',
  '/admissions': 'Admisiones',
  '/appointments': 'Citas',
}

export function AppLayout() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const title =
    pageTitles[location.pathname] ?? 'Hospital Management'

  return (
    <div className="min-h-screen bg-slate-100">
      {sidebarOpen && (
        <button
          type="button"
          aria-label="Cerrar menú"
          className="fixed inset-0 z-30 bg-slate-950/50 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-72 flex-col bg-slate-950 text-white transition-transform duration-300 lg:translate-x-0 ${
          sidebarOpen
            ? 'translate-x-0'
            : '-translate-x-full'
        }`}
      >
        <div className="flex h-20 items-center justify-between border-b border-white/10 px-6">
          <div className="flex items-center gap-3">
            <div className="rounded-xl bg-cyan-500 p-2.5">
              <Hospital className="size-6 text-white" />
            </div>

            <div>
              <p className="font-semibold">Hospital</p>
              <p className="text-xs text-slate-400">
                Management System
              </p>
            </div>
          </div>

          <button
            type="button"
            className="text-slate-400 hover:text-white lg:hidden"
            onClick={() => setSidebarOpen(false)}
          >
            <X className="size-6" />
          </button>
        </div>

        <nav className="flex-1 space-y-1 overflow-y-auto px-4 py-6">
          <p className="mb-3 px-3 text-xs font-semibold tracking-wider text-slate-500">
            GESTIÓN
          </p>

          {navigation.map((item) => {
            const Icon = item.icon

            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === '/'}
                onClick={() => setSidebarOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium transition ${
                    isActive
                      ? 'bg-cyan-500 text-white shadow-lg shadow-cyan-950/30'
                      : 'text-slate-400 hover:bg-white/5 hover:text-white'
                  }`
                }
              >
                <Icon className="size-5" />
                {item.label}
              </NavLink>
            )
          })}
        </nav>

        <div className="border-t border-white/10 p-4">
          <div className="mb-3 rounded-xl bg-white/5 p-3">
            <p className="truncate text-sm font-medium">
              {user?.email}
            </p>
            <p className="mt-1 text-xs text-cyan-300">
              {user?.role}
            </p>
          </div>

          <button
            type="button"
            onClick={logout}
            className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-slate-400 transition hover:bg-red-500/10 hover:text-red-300"
          >
            <LogOut className="size-5" />
            Cerrar sesión
          </button>
        </div>
      </aside>

      <div className="lg:pl-72">
        <header className="sticky top-0 z-20 flex h-20 items-center border-b border-slate-200 bg-white/95 px-5 backdrop-blur lg:px-8">
          <button
            type="button"
            className="mr-4 rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu className="size-6" />
          </button>

          <div>
            <p className="text-xs font-semibold tracking-wider text-cyan-700">
              HOSPITAL MANAGEMENT
            </p>
            <h1 className="text-xl font-semibold text-slate-950">
              {title}
            </h1>
          </div>
        </header>

        <main className="p-5 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
