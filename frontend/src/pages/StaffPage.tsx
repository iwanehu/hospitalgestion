import {
  keepPreviousData,
  useQuery,
} from '@tanstack/react-query'
import {
  ChevronLeft,
  ChevronRight,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Stethoscope,
  Trash2,
  UserRound,
} from 'lucide-react'
import { useState } from 'react'
import {
  getActiveDepartments,
  getDoctors,
  getNurses,
} from '../api/staff-api'
import { useAuth } from '../auth/useAuth'
import { DeleteDoctorModal } from '../components/DeleteDoctorModal'
import { DeleteNurseModal } from '../components/DeleteNurseModal'
import { DoctorFormModal } from '../components/DoctorFormModal'
import { NurseFormModal } from '../components/NurseFormModal'
import {
  nurseSpecialtyLabels,
  shiftTypeLabels,
  specialtyLabels,
  type Doctor,
  type DoctorFilters,
  type Nurse,
  type NurseFilters,
  type NurseSpecialty,
  type ShiftType,
  type Specialty,
} from '../types/staff'



import { getDepartmentLabel } from '../utils/department-label'

type StaffTab = 'doctors' | 'nurses'

const doctorSpecialties = Object.keys(
  specialtyLabels,
) as Specialty[]

const nurseSpecialties = Object.keys(
  nurseSpecialtyLabels,
) as NurseSpecialty[]

const shiftTypes = Object.keys(
  shiftTypeLabels,
) as ShiftType[]

export function StaffPage() {
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'

  const [activeTab, setActiveTab] =
    useState<StaffTab>('doctors')

  const [doctorPage, setDoctorPage] = useState(0)
  const [doctorText, setDoctorText] = useState('')
  const [doctorSearch, setDoctorSearch] = useState('')
  const [doctorDepartment, setDoctorDepartment] =
    useState('')
  const [doctorSpecialty, setDoctorSpecialty] =
    useState<Specialty | undefined>()

  const [nursePage, setNursePage] = useState(0)
  const [nurseText, setNurseText] = useState('')
  const [nurseSearch, setNurseSearch] = useState('')
  const [nurseDepartment, setNurseDepartment] =
    useState('')
  const [nurseSpecialty, setNurseSpecialty] =
    useState<NurseSpecialty | undefined>()
  const [shiftType, setShiftType] =
    useState<ShiftType | undefined>()
  const [chargeNurse, setChargeNurse] =
    useState<'all' | 'yes' | 'no'>('all')

  const [doctorFormOpen, setDoctorFormOpen] =
    useState(false)
  const [nurseFormOpen, setNurseFormOpen] =
    useState(false)

  const [selectedDoctor, setSelectedDoctor] =
    useState<Doctor | undefined>()
  const [selectedNurse, setSelectedNurse] =
    useState<Nurse | undefined>()

  const [doctorToDelete, setDoctorToDelete] =
    useState<Doctor | null>(null)
  const [nurseToDelete, setNurseToDelete] =
    useState<Nurse | null>(null)

  const departmentsQuery = useQuery({
    queryKey: ['departments', 'active', 'staff'],
    queryFn: getActiveDepartments,
  })

  const doctorFilters: DoctorFilters = {
    page: doctorPage,
    size: 10,
    text: doctorSearch || undefined,
    departmentId: doctorDepartment
      ? Number(doctorDepartment)
      : undefined,
    specialty: doctorSpecialty,
  }

  const nurseFilters: NurseFilters = {
    page: nursePage,
    size: 10,
    text: nurseSearch || undefined,
    departmentId: nurseDepartment
      ? Number(nurseDepartment)
      : undefined,
    specialty: nurseSpecialty,
    shiftType,
    isChargeNurse:
      chargeNurse === 'all'
        ? undefined
        : chargeNurse === 'yes',
  }

  const doctorsQuery = useQuery({
    queryKey: ['doctors', doctorFilters],
    queryFn: () => getDoctors(doctorFilters),
    placeholderData: keepPreviousData,
    enabled: activeTab === 'doctors',
  })

  const nursesQuery = useQuery({
    queryKey: ['nurses', nurseFilters],
    queryFn: () => getNurses(nurseFilters),
    placeholderData: keepPreviousData,
    enabled: activeTab === 'nurses',
  })

  function closeDoctorForm() {
    setDoctorFormOpen(false)
    setSelectedDoctor(undefined)
  }

  function closeNurseForm() {
    setNurseFormOpen(false)
    setSelectedNurse(undefined)
  }

  function resetDoctorFilters() {
    setDoctorText('')
    setDoctorSearch('')
    setDoctorDepartment('')
    setDoctorSpecialty(undefined)
    setDoctorPage(0)
  }

  function resetNurseFilters() {
    setNurseText('')
    setNurseSearch('')
    setNurseDepartment('')
    setNurseSpecialty(undefined)
    setShiftType(undefined)
    setChargeNurse('all')
    setNursePage(0)
  }

  return (
    <div className="mx-auto max-w-7xl">
      <header className="mb-7">
        <p className="text-sm font-medium text-cyan-700">
          EQUIPO HOSPITALARIO
        </p>

        <h2 className="mt-1 text-2xl font-semibold text-slate-950">
          Personal sanitario
        </h2>

        <p className="mt-1 text-sm text-slate-500">
          Gestiona médicos, enfermería y sus perfiles
          profesionales.
        </p>
      </header>

      <section className="mb-7 grid grid-cols-2 gap-2 rounded-2xl border border-slate-200 bg-white p-2 shadow-sm">
        <button
          type="button"
          onClick={() => setActiveTab('doctors')}
          className={`flex items-center justify-center gap-2 rounded-xl px-4 py-3 font-medium transition ${
            activeTab === 'doctors'
              ? 'bg-cyan-700 text-white shadow'
              : 'text-slate-500 hover:bg-slate-50'
          }`}
        >
          <Stethoscope className="size-5" />
          Médicos
        </button>

        <button
          type="button"
          onClick={() => setActiveTab('nurses')}
          className={`flex items-center justify-center gap-2 rounded-xl px-4 py-3 font-medium transition ${
            activeTab === 'nurses'
              ? 'bg-cyan-700 text-white shadow'
              : 'text-slate-500 hover:bg-slate-50'
          }`}
        >
          <UserRound className="size-5" />
          Enfermería
        </button>
      </section>

      {activeTab === 'doctors' ? (
        <>
          <div className="mb-5 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <h3 className="text-xl font-semibold text-slate-950">
                Médicos
              </h3>

              <p className="mt-1 text-sm text-slate-500">
                Administra especialidades, licencias y
                departamentos.
              </p>
            </div>

            {isAdmin && (
              <button
                type="button"
                onClick={() => {
                  setSelectedDoctor(undefined)
                  setDoctorFormOpen(true)
                }}
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white hover:bg-cyan-800"
              >
                <Plus className="size-5" />
                Nuevo médico
              </button>
            )}
          </div>

          <section className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="grid gap-4 lg:grid-cols-3">
              <label>
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Buscar
                </span>

                <div className="flex gap-2">
                  <input
                    value={doctorText}
                    onChange={(event) =>
                      setDoctorText(event.target.value)
                    }
                    onKeyDown={(event) => {
                      if (event.key === 'Enter') {
                        setDoctorPage(0)
                        setDoctorSearch(doctorText.trim())
                      }
                    }}
                    placeholder="Nombre, email o licencia"
                    className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none focus:border-cyan-600"
                  />

                  <button
                    type="button"
                    aria-label="Buscar médicos"
                    onClick={() => {
                      setDoctorPage(0)
                      setDoctorSearch(doctorText.trim())
                    }}
                    className="rounded-xl bg-slate-900 px-4 text-white"
                  >
                    <Search className="size-5" />
                  </button>
                </div>
              </label>

              <label>
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Departamento
                </span>

                <select
                  value={doctorDepartment}
                  onChange={(event) => {
                    setDoctorDepartment(event.target.value)
                    setDoctorPage(0)
                  }}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
                >
                  <option value="">Todos</option>

                  {departmentsQuery.data?.map(
                    (department) => (
                      <option
                        key={department.id}
                        value={department.id}
                      >
                        {getDepartmentLabel(department.departmentType)} ·{' '}
                        {department.location}
                      </option>
                    ),
                  )}
                </select>
              </label>

              <label>
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Especialidad
                </span>

                <select
                  value={doctorSpecialty ?? ''}
                  onChange={(event) => {
                    setDoctorSpecialty(
                      (event.target.value ||
                        undefined) as
                        | Specialty
                        | undefined,
                    )
                    setDoctorPage(0)
                  }}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
                >
                  <option value="">Todas</option>

                  {doctorSpecialties.map((specialty) => (
                    <option
                      key={specialty}
                      value={specialty}
                    >
                      {specialtyLabels[specialty]}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <button
              type="button"
              onClick={resetDoctorFilters}
              className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-cyan-700"
            >
              <RefreshCw className="size-4" />
              Limpiar filtros
            </button>
          </section>

          <StaffTableHeader
            title="Listado de médicos"
            total={doctorsQuery.data?.totalElements}
            icon={<Stethoscope className="size-6" />}
          />

          <section className="-mt-px overflow-hidden rounded-b-2xl border border-slate-200 bg-white shadow-sm">
            {doctorsQuery.isPending && <LoadingRows />}

            {doctorsQuery.isError && (
              <QueryError
                message="No se pudieron cargar los médicos."
                retry={() => void doctorsQuery.refetch()}
              />
            )}

            {doctorsQuery.data &&
              !doctorsQuery.data.empty && (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full min-w-[950px] text-left">
                      <thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                        <tr>
                          <th className="px-5 py-4">
                            Médico
                          </th>
                          <th className="px-5 py-4">
                            Departamento
                          </th>
                          <th className="px-5 py-4">
                            Especialidad
                          </th>
                          <th className="px-5 py-4">
                            Licencia
                          </th>
                          <th className="px-5 py-4">
                            Experiencia
                          </th>
                          <th className="px-5 py-4 text-right">
                            Acciones
                          </th>
                        </tr>
                      </thead>

                      <tbody className="divide-y divide-slate-100">
                        {doctorsQuery.data.content.map(
                          (doctor) => (
                            <tr
                              key={doctor.id}
                              className="hover:bg-slate-50"
                            >
                              <td className="px-5 py-4">
                                <p className="font-medium text-slate-900">
                                  {doctor.fullName}
                                </p>
                                <p className="mt-1 text-sm text-slate-500">
                                  {doctor.email}
                                </p>
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                              {getDepartmentLabel(doctor.departmentType)}
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                                {specialtyLabels[
                                  doctor.specialty
                                ]}
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                                {
                                  doctor.medicalLicenseNumber
                                }
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                                {doctor.yearsOfExperience}{' '}
                                años
                              </td>

                              <td className="px-5 py-4">
                                <div className="flex justify-end gap-2">
                                  <button
                                    type="button"
                                    onClick={() => {
                                      setSelectedDoctor(
                                        doctor,
                                      )
                                      setDoctorFormOpen(true)
                                    }}
                                    aria-label={`Editar ${doctor.fullName}`}
                                    className="rounded-lg border border-slate-200 p-2 text-slate-500 hover:text-cyan-700"
                                  >
                                    <Pencil className="size-4" />
                                  </button>

                                  {isAdmin && (
                                    <button
                                      type="button"
                                      onClick={() =>
                                        setDoctorToDelete(
                                          doctor,
                                        )
                                      }
                                      aria-label={`Eliminar ${doctor.fullName}`}
                                      className="rounded-lg border border-red-200 p-2 text-red-600 hover:bg-red-50"
                                    >
                                      <Trash2 className="size-4" />
                                    </button>
                                  )}
                                </div>
                              </td>
                            </tr>
                          ),
                        )}
                      </tbody>
                    </table>
                  </div>

                  <Pagination
                    page={doctorsQuery.data.page}
                    totalPages={
                      doctorsQuery.data.totalPages
                    }
                    hasPrevious={
                      doctorsQuery.data.hasPrevious
                    }
                    hasNext={doctorsQuery.data.hasNext}
                    previous={() =>
                      setDoctorPage(
                        (current) => current - 1,
                      )
                    }
                    next={() =>
                      setDoctorPage(
                        (current) => current + 1,
                      )
                    }
                  />
                </>
              )}

            {doctorsQuery.data?.empty && (
              <EmptyState
                icon={
                  <Stethoscope className="size-10" />
                }
                title="No se encontraron médicos"
                description="Crea un médico o modifica los filtros."
              />
            )}
          </section>
        </>
      ) : (
        <>
          <div className="mb-5 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <h3 className="text-xl font-semibold text-slate-950">
                Enfermería
              </h3>

              <p className="mt-1 text-sm text-slate-500">
                Administra especialidades, turnos y
                responsabilidades.
              </p>
            </div>

            {isAdmin && (
              <button
                type="button"
                onClick={() => {
                  setSelectedNurse(undefined)
                  setNurseFormOpen(true)
                }}
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white hover:bg-cyan-800"
              >
                <Plus className="size-5" />
                Nuevo enfermero
              </button>
            )}
          </div>

          <section className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
              <label>
                <span className="mb-2 block text-sm font-medium text-slate-700">
                  Buscar
                </span>

                <div className="flex gap-2">
                  <input
                    value={nurseText}
                    onChange={(event) =>
                      setNurseText(event.target.value)
                    }
                    onKeyDown={(event) => {
                      if (event.key === 'Enter') {
                        setNursePage(0)
                        setNurseSearch(nurseText.trim())
                      }
                    }}
                    placeholder="Nombre, email o licencia"
                    className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
                  />

                  <button
                    type="button"
                    aria-label="Buscar enfermería"
                    onClick={() => {
                      setNursePage(0)
                      setNurseSearch(nurseText.trim())
                    }}
                    className="rounded-xl bg-slate-900 px-4 text-white"
                  >
                    <Search className="size-5" />
                  </button>
                </div>
              </label>

              <FilterSelect
                label="Departamento"
                value={nurseDepartment}
                onChange={(value) => {
                  setNurseDepartment(value)
                  setNursePage(0)
                }}
              >
                <option value="">Todos</option>

                {departmentsQuery.data?.map(
                  (department) => (
                    <option
                      key={department.id}
                      value={department.id}
                    >
                      {getDepartmentLabel(department.departmentType)} ·{' '}
                      {department.location}
                    </option>
                  ),
                )}
              </FilterSelect>

              <FilterSelect
                label="Especialidad"
                value={nurseSpecialty ?? ''}
                onChange={(value) => {
                  setNurseSpecialty(
                    (value || undefined) as
                      | NurseSpecialty
                      | undefined,
                  )
                  setNursePage(0)
                }}
              >
                <option value="">Todas</option>

                {nurseSpecialties.map((specialty) => (
                  <option
                    key={specialty}
                    value={specialty}
                  >
                    {nurseSpecialtyLabels[specialty]}
                  </option>
                ))}
              </FilterSelect>

              <FilterSelect
                label="Turno"
                value={shiftType ?? ''}
                onChange={(value) => {
                  setShiftType(
                    (value || undefined) as
                      | ShiftType
                      | undefined,
                  )
                  setNursePage(0)
                }}
              >
                <option value="">Todos</option>

                {shiftTypes.map((shift) => (
                  <option key={shift} value={shift}>
                    {shiftTypeLabels[shift]}
                  </option>
                ))}
              </FilterSelect>

              <FilterSelect
                label="Responsable"
                value={chargeNurse}
                onChange={(value) => {
                  setChargeNurse(
                    value as 'all' | 'yes' | 'no',
                  )
                  setNursePage(0)
                }}
              >
                <option value="all">Todos</option>
                <option value="yes">Responsables</option>
                <option value="no">No responsables</option>
              </FilterSelect>
            </div>

            <button
              type="button"
              onClick={resetNurseFilters}
              className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-cyan-700"
            >
              <RefreshCw className="size-4" />
              Limpiar filtros
            </button>
          </section>

          <StaffTableHeader
            title="Listado de enfermería"
            total={nursesQuery.data?.totalElements}
            icon={<UserRound className="size-6" />}
          />

          <section className="-mt-px overflow-hidden rounded-b-2xl border border-slate-200 bg-white shadow-sm">
            {nursesQuery.isPending && <LoadingRows />}

            {nursesQuery.isError && (
              <QueryError
                message="No se pudo cargar el personal de enfermería."
                retry={() => void nursesQuery.refetch()}
              />
            )}

            {nursesQuery.data &&
              !nursesQuery.data.empty && (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full min-w-[1100px] text-left">
                      <thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                        <tr>
                          <th className="px-5 py-4">
                            Profesional
                          </th>
                          <th className="px-5 py-4">
                            Departamento
                          </th>
                          <th className="px-5 py-4">
                            Especialidad
                          </th>
                          <th className="px-5 py-4">
                            Turno
                          </th>
                          <th className="px-5 py-4">
                            Experiencia
                          </th>
                          <th className="px-5 py-4">
                            Responsable
                          </th>
                          <th className="px-5 py-4 text-right">
                            Acciones
                          </th>
                        </tr>
                      </thead>

                      <tbody className="divide-y divide-slate-100">
                        {nursesQuery.data.content.map(
                          (nurse) => (
                            <tr
                              key={nurse.id}
                              className="hover:bg-slate-50"
                            >
                              <td className="px-5 py-4">
                                <p className="font-medium text-slate-900">
                                  {nurse.fullName}
                                </p>
                                <p className="mt-1 text-sm text-slate-500">
                                  {nurse.email}
                                </p>
                                <p className="mt-1 text-xs text-slate-400">
                                  {nurse.licenseNumber}
                                </p>
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                                {getDepartmentLabel(nurse.departmentType)}
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                                {
                                  nurseSpecialtyLabels[
                                    nurse.specialty
                                  ]
                                }
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                                {
                                  shiftTypeLabels[
                                    nurse.shiftType
                                  ]
                                }
                              </td>

                              <td className="px-5 py-4 text-sm text-slate-600">
                                {nurse.yearsOfExperience}{' '}
                                años
                              </td>

                              <td className="px-5 py-4">
                                <span
                                  className={`rounded-full px-2.5 py-1 text-xs font-medium ${
                                    nurse.isChargeNurse
                                      ? 'bg-cyan-50 text-cyan-700'
                                      : 'bg-slate-100 text-slate-600'
                                  }`}
                                >
                                  {nurse.isChargeNurse
                                    ? 'Sí'
                                    : 'No'}
                                </span>
                              </td>

                              <td className="px-5 py-4">
                                <div className="flex justify-end gap-2">
                                  <button
                                    type="button"
                                    onClick={() => {
                                      setSelectedNurse(nurse)
                                      setNurseFormOpen(true)
                                    }}
                                    aria-label={`Editar ${nurse.fullName}`}
                                    className="rounded-lg border border-slate-200 p-2 text-slate-500 hover:text-cyan-700"
                                  >
                                    <Pencil className="size-4" />
                                  </button>

                                  {isAdmin && (
                                    <button
                                      type="button"
                                      onClick={() =>
                                        setNurseToDelete(nurse)
                                      }
                                      aria-label={`Eliminar ${nurse.fullName}`}
                                      className="rounded-lg border border-red-200 p-2 text-red-600 hover:bg-red-50"
                                    >
                                      <Trash2 className="size-4" />
                                    </button>
                                  )}
                                </div>
                              </td>
                            </tr>
                          ),
                        )}
                      </tbody>
                    </table>
                  </div>

                  <Pagination
                    page={nursesQuery.data.page}
                    totalPages={
                      nursesQuery.data.totalPages
                    }
                    hasPrevious={
                      nursesQuery.data.hasPrevious
                    }
                    hasNext={nursesQuery.data.hasNext}
                    previous={() =>
                      setNursePage(
                        (current) => current - 1,
                      )
                    }
                    next={() =>
                      setNursePage(
                        (current) => current + 1,
                      )
                    }
                  />
                </>
              )}

            {nursesQuery.data?.empty && (
              <EmptyState
                icon={<UserRound className="size-10" />}
                title="No se encontró personal de enfermería"
                description="Crea un perfil o modifica los filtros."
              />
            )}
          </section>
        </>
      )}

      {doctorFormOpen && (
        <DoctorFormModal
          doctor={selectedDoctor}
          onClose={closeDoctorForm}
        />
      )}

      {nurseFormOpen && (
        <NurseFormModal
          nurse={selectedNurse}
          onClose={closeNurseForm}
        />
      )}

      {doctorToDelete && (
        <DeleteDoctorModal
          doctor={doctorToDelete}
          onClose={() => setDoctorToDelete(null)}
        />
      )}

      {nurseToDelete && (
        <DeleteNurseModal
          nurse={nurseToDelete}
          onClose={() => setNurseToDelete(null)}
        />
      )}
    </div>
  )
}

interface FilterSelectProps {
  label: string
  value: string
  onChange: (value: string) => void
  children: React.ReactNode
}

function FilterSelect({
  label,
  value,
  onChange,
  children,
}: FilterSelectProps) {
  return (
    <label>
      <span className="mb-2 block text-sm font-medium text-slate-700">
        {label}
      </span>

      <select
        value={value}
        onChange={(event) =>
          onChange(event.target.value)
        }
        className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"
      >
        {children}
      </select>
    </label>
  )
}

interface StaffTableHeaderProps {
  title: string
  total?: number
  icon: React.ReactNode
}

function StaffTableHeader({
  title,
  total,
  icon,
}: StaffTableHeaderProps) {
  return (
    <header className="flex items-center justify-between rounded-t-2xl border border-slate-200 bg-white px-5 py-4">
      <div>
        <p className="font-medium text-slate-900">
          {title}
        </p>

        <p className="text-sm text-slate-500">
          {total === undefined
            ? 'Cargando resultados'
            : `${total} ${
                total === 1 ? 'resultado' : 'resultados'
              }`}
        </p>
      </div>

      <div className="text-cyan-700">{icon}</div>
    </header>
  )
}

function LoadingRows() {
  return (
    <div className="space-y-3 p-6">
      {[1, 2, 3].map((item) => (
        <div
          key={item}
          className="h-14 animate-pulse rounded-xl bg-slate-100"
        />
      ))}
    </div>
  )
}

interface QueryErrorProps {
  message: string
  retry: () => void
}

function QueryError({
  message,
  retry,
}: QueryErrorProps) {
  return (
    <div className="p-10 text-center">
      <p className="font-medium text-red-700">
        {message}
      </p>

      <button
        type="button"
        onClick={retry}
        className="mt-4 rounded-xl bg-slate-900 px-4 py-2 text-sm text-white"
      >
        Intentar nuevamente
      </button>
    </div>
  )
}

interface EmptyStateProps {
  icon: React.ReactNode
  title: string
  description: string
}

function EmptyState({
  icon,
  title,
  description,
}: EmptyStateProps) {
  return (
    <div className="p-12 text-center">
      <div className="mx-auto flex justify-center text-slate-300">
        {icon}
      </div>

      <p className="mt-4 font-medium text-slate-700">
        {title}
      </p>

      <p className="mt-1 text-sm text-slate-500">
        {description}
      </p>
    </div>
  )
}

interface PaginationProps {
  page: number
  totalPages: number
  hasPrevious: boolean
  hasNext: boolean
  previous: () => void
  next: () => void
}

function Pagination({
  page,
  totalPages,
  hasPrevious,
  hasNext,
  previous,
  next,
}: PaginationProps) {
  return (
    <footer className="flex items-center justify-between border-t border-slate-200 px-5 py-4">
      <p className="text-sm text-slate-500">
        Página {page + 1} de {Math.max(totalPages, 1)}
      </p>

      <div className="flex gap-2">
        <button
          type="button"
          disabled={!hasPrevious}
          onClick={previous}
          aria-label="Página anterior"
          className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"
        >
          <ChevronLeft className="size-5" />
        </button>

        <button
          type="button"
          disabled={!hasNext}
          onClick={next}
          aria-label="Página siguiente"
          className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"
        >
          <ChevronRight className="size-5" />
        </button>
      </div>
    </footer>
  )
}