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

import { PatientsPage } from './pages/PatientsPage'


import { FacilitiesPage } from './pages/FacilitiesPage'

import { StaffPage } from './pages/StaffPage'

import { AdmissionsPage } from './pages/AdmissionsPage'

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
              element={<ComingSoonPage title="Citas" />}
            />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
        
      </Routes>
    </BrowserRouter>
  )
}