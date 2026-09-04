import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from 'react-router-dom'
import { AppLayout } from './layouts/AppLayout'
import { ComingSoonPage } from './pages/ComingSoonPage'
import { DashboardPage } from './pages/DashboardPage'
import { DepartmentsPage } from './pages/DepartmentsPage'
import { LoginPage } from './pages/LoginPage'
import { ProtectedRoute } from './routes/ProtectedRoute'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route index element={<DashboardPage />} />

            <Route
              path="departments"
              element={<DepartmentsPage />}
            />

            <Route
              path="patients"
              element={<ComingSoonPage title="Pacientes" />}
            />

            <Route
              path="staff"
              element={
                <ComingSoonPage title="Personal sanitario" />
              }
            />

            <Route
              path="facilities"
              element={
                <ComingSoonPage title="Habitaciones y camas" />
              }
            />

            <Route
              path="admissions"
              element={
                <ComingSoonPage title="Admisiones" />
              }
            />

            <Route
              path="appointments"
              element={<ComingSoonPage title="Citas" />}
            />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}