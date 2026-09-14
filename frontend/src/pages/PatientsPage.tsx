import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight, Pencil, Plus, RefreshCw, Search, Trash2, Users } from 'lucide-react'
import { useState } from 'react'
import { getPatients } from '../api/patients-api'
import { useAuth } from '../auth/useAuth'
import { DeletePatientModal } from '../components/DeletePatientModal'
import { PatientFormModal } from '../components/PatientFormModal'
import { bloodTypeLabels, type BloodType, type Patient, type PatientFilters } from '../types/patient'

const bloodTypes = Object.keys(bloodTypeLabels) as BloodType[]

export function PatientsPage() {
  const { user } = useAuth()
  const [page, setPage] = useState(0)
  const [text, setText] = useState('')
  const [searchText, setSearchText] = useState('')
  const [bloodType, setBloodType] = useState<BloodType | undefined>()
  const [insurance, setInsurance] = useState<'all' | 'yes' | 'no'>('all')
  const [birthDateFrom, setBirthDateFrom] = useState('')
  const [birthDateTo, setBirthDateTo] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [selectedPatient, setSelectedPatient] = useState<Patient | undefined>()
  const [patientToDelete, setPatientToDelete] = useState<Patient | null>(null)

  const canCreate = user?.role === 'ADMIN' || user?.role === 'RECEPTIONIST'
  const isAdmin = user?.role === 'ADMIN'
  const filters: PatientFilters = {
    page,
    size: 10,
    text: searchText || undefined,
    bloodType,
    hasHealthInsurance: insurance === 'all' ? undefined : insurance === 'yes',
    birthDateFrom: birthDateFrom || undefined,
    birthDateTo: birthDateTo || undefined,
  }

  const patientsQuery = useQuery({
    queryKey: ['patients', filters],
    queryFn: () => getPatients(filters),
    placeholderData: keepPreviousData,
  })

  const data = patientsQuery.data
  const closeForm = () => { setFormOpen(false); setSelectedPatient(undefined) }
  const reset = () => {
    setText('')
    setSearchText('')
    setBloodType(undefined)
    setInsurance('all')
    setBirthDateFrom('')
    setBirthDateTo('')
    setPage(0)
  }

  return (
    <div className="mx-auto max-w-7xl">
      <div className="mb-7 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div><p className="text-sm font-medium text-cyan-700">GESTIÓN CLÍNICA</p><h2 className="mt-1 text-2xl font-semibold text-slate-950">Pacientes</h2><p className="mt-1 text-sm text-slate-500">Gestiona perfiles, contactos e información clínica.</p></div>
        {canCreate && <button type="button" onClick={() => { setSelectedPatient(undefined); setFormOpen(true) }} className="inline-flex items-center justify-center gap-2 rounded-xl bg-cyan-700 px-4 py-3 text-sm font-medium text-white hover:bg-cyan-800"><Plus className="size-5" /> Nuevo paciente</button>}
      </div>

      <section className="mb-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <label className="block xl:col-span-2"><span className="mb-2 block text-sm font-medium text-slate-700">Buscar</span><div className="flex gap-2"><input value={text} onChange={(e) => setText(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') { setPage(0); setSearchText(text.trim()) } }} placeholder="Nombre, email o documento" className="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none focus:border-cyan-600" /><button type="button" onClick={() => { setPage(0); setSearchText(text.trim()) }} aria-label="Buscar paciente" className="rounded-xl bg-slate-900 px-4 text-white"><Search className="size-5" /></button></div></label>
          <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Grupo sanguíneo</span><select value={bloodType ?? ''} onChange={(e) => { setBloodType((e.target.value || undefined) as BloodType | undefined); setPage(0) }} className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"><option value="">Todos</option>{bloodTypes.map((type) => <option key={type} value={type}>{bloodTypeLabels[type]}</option>)}</select></label>
          <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Seguro médico</span><select value={insurance} onChange={(e) => { setInsurance(e.target.value as 'all' | 'yes' | 'no'); setPage(0) }} className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5"><option value="all">Todos</option><option value="yes">Con seguro</option><option value="no">Sin seguro</option></select></label>
          <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Nacimiento desde</span><input type="date" value={birthDateFrom} onChange={(e) => { setBirthDateFrom(e.target.value); setPage(0) }} className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5" /></label>
          <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Nacimiento hasta</span><input type="date" value={birthDateTo} onChange={(e) => { setBirthDateTo(e.target.value); setPage(0) }} className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5" /></label>
        </div>
        <button type="button" onClick={reset} className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-cyan-700"><RefreshCw className="size-4" /> Limpiar filtros</button>
      </section>

      <section className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <header className="flex items-center justify-between border-b border-slate-200 px-5 py-4"><div><p className="font-medium text-slate-900">Listado de pacientes</p><p className="text-sm text-slate-500">{data ? `${data.totalElements} ${data.totalElements === 1 ? 'resultado' : 'resultados'}` : 'Cargando resultados'}</p></div><Users className="size-6 text-cyan-700" /></header>
        {patientsQuery.isPending && <div className="space-y-3 p-6">{[1, 2, 3].map((item) => <div key={item} className="h-14 animate-pulse rounded-xl bg-slate-100" />)}</div>}
        {patientsQuery.isError && <div className="p-10 text-center"><p className="font-medium text-red-700">No se pudieron cargar los pacientes.</p><button type="button" onClick={() => void patientsQuery.refetch()} className="mt-4 rounded-xl bg-slate-900 px-4 py-2 text-sm text-white">Intentar nuevamente</button></div>}
        {data && !data.empty && <><div className="overflow-x-auto"><table className="w-full min-w-[1050px] text-left"><thead className="bg-slate-50 text-xs uppercase tracking-wider text-slate-500"><tr><th className="px-5 py-4">Paciente</th><th className="px-5 py-4">Documento</th><th className="px-5 py-4">Nacimiento</th><th className="px-5 py-4">Sangre</th><th className="px-5 py-4">Seguro</th><th className="px-5 py-4">Contacto</th><th className="px-5 py-4 text-right">Acciones</th></tr></thead><tbody className="divide-y divide-slate-100">{data.content.map((patient) => <tr key={patient.id} className="hover:bg-slate-50"><td className="px-5 py-4"><p className="font-medium text-slate-900">{patient.fullName}</p><p className="mt-1 text-sm text-slate-500">{patient.email}</p></td><td className="px-5 py-4 text-sm text-slate-600">{patient.documentId}</td><td className="px-5 py-4 text-sm text-slate-600">{patient.birthDate}</td><td className="px-5 py-4"><span className="rounded-full bg-red-50 px-2.5 py-1 text-xs font-medium text-red-700">{patient.bloodType ? bloodTypeLabels[patient.bloodType] : '—'}</span></td><td className="px-5 py-4 text-sm text-slate-600">{patient.hasHealthInsurance ? patient.healthInsuranceProvider || 'Sí' : 'No'}</td><td className="px-5 py-4"><p className="text-sm text-slate-700">{patient.emergencyContactName || '—'}</p><p className="text-xs text-slate-500">{patient.emergencyContactPhone}</p></td><td className="px-5 py-4"><div className="flex justify-end gap-2"><button type="button" onClick={() => { setSelectedPatient(patient); setFormOpen(true) }} aria-label={`Editar ${patient.fullName}`} className="rounded-lg border border-slate-200 p-2 text-slate-500 hover:text-cyan-700"><Pencil className="size-4" /></button>{isAdmin && <button type="button" onClick={() => setPatientToDelete(patient)} aria-label={`Eliminar ${patient.fullName}`} className="rounded-lg border border-red-200 p-2 text-red-600 hover:bg-red-50"><Trash2 className="size-4" /></button>}</div></td></tr>)}</tbody></table></div><footer className="flex items-center justify-between border-t border-slate-200 px-5 py-4"><p className="text-sm text-slate-500">Página {data.page + 1} de {Math.max(data.totalPages, 1)}</p><div className="flex gap-2"><button type="button" disabled={!data.hasPrevious} onClick={() => setPage((current) => current - 1)} aria-label="Página anterior" className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"><ChevronLeft className="size-5" /></button><button type="button" disabled={!data.hasNext} onClick={() => setPage((current) => current + 1)} aria-label="Página siguiente" className="rounded-lg border border-slate-200 p-2 disabled:opacity-40"><ChevronRight className="size-5" /></button></div></footer></>}
        {data?.empty && <div className="p-12 text-center"><Users className="mx-auto size-10 text-slate-300" /><p className="mt-4 font-medium text-slate-700">No se encontraron pacientes</p><p className="mt-1 text-sm text-slate-500">Crea un paciente o modifica los filtros.</p></div>}
      </section>
      {formOpen && <PatientFormModal patient={selectedPatient} onClose={closeForm} />}
      {patientToDelete && <DeletePatientModal patient={patientToDelete} onClose={() => setPatientToDelete(null)} />}
    </div>
  )
}
