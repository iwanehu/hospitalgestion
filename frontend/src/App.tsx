import {
  lazy,
  Suspense,
} from 'react'
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from 'react-router-dom'
import { AppLayout } from './layouts/AppLayout'
import { ProtectedRoute } from './routes/ProtectedRoute'

const LoginPage = lazy(() =>
  import('./pages/LoginPage').then((module) => ({
    default: module.LoginPage,
  })),
)

const DashboardPage = lazy(() =>
  import('./pages/DashboardPage').then((module) => ({
    default: module.DashboardPage,
  })),
)

const DepartmentsPage = lazy(() =>
  import('./pages/DepartmentsPage').then((module) => ({
    default: module.DepartmentsPage,
  })),
)

const PatientsPage = lazy(() =>
  import('./pages/PatientsPage').then((module) => ({
    default: module.PatientsPage,
  })),
)

const StaffPage = lazy(() =>
  import('./pages/StaffPage').then((module) => ({
    default: module.StaffPage,
  })),
)

const FacilitiesPage = lazy(() =>
  import('./pages/FacilitiesPage').then((module) => ({
    default: module.FacilitiesPage,
  })),
)

const AdmissionsPage = lazy(() =>
  import('./pages/AdmissionsPage').then((module) => ({
    default: module.AdmissionsPage,
  })),
)

const AppointmentsPage = lazy(() =>
  import('./pages/AppointmentsPage').then((module) => ({
    default: module.AppointmentsPage,
  })),
)

function PageLoader() {
  return (
    <div className="flex min-h-64 items-center justify-center">
      <div className="flex flex-col items-center gap-3">
        <div className="size-9 animate-spin rounded-full border-4 border-slate-200 border-t-cyan-700" />

        <p className="text-sm text-slate-500">
          Cargando módulo...
        </p>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageLoader />}>
        <Routes>
          <Route
            path="/login"
            element={<LoginPage />}
          />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route
                index
                element={<DashboardPage />}
              />

              <Route
                path="departments"
                element={<DepartmentsPage />}
              />

              <Route
                path="patients"
                element={<PatientsPage />}
              />

              <Route
                path="staff"
                element={<StaffPage />}
              />

              <Route
                path="facilities"
                element={<FacilitiesPage />}
              />

              <Route
                path="admissions"
                element={<AdmissionsPage />}
              />

              <Route
                path="appointments"
                element={<AppointmentsPage />}
              />
            </Route>
          </Route>

          <Route
            path="*"
            element={<Navigate to="/" replace />}
          />
        </Routes>
      </Suspense>
    </BrowserRouter>
  )
}