import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import { useState, type FormEvent, type ReactNode } from 'react'
import {
  createPatient,
  getAvailablePatientUsers,
  updatePatient,
} from '../api/patients-api'
import {
  bloodTypeLabels,
  type BloodType,
  type Patient,
} from '../types/patient'

interface Props {
  patient?: Patient
  onClose: () => void
}

interface ApiError {
  message?: string
}

const bloodTypes = Object.keys(bloodTypeLabels) as BloodType[]
const fieldClass =
  'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10 disabled:opacity-60'

export function PatientFormModal({ patient, onClose }: Props) {
  const queryClient = useQueryClient()
  const editing = Boolean(patient)
  const [userId, setUserId] = useState('')
  const [birthDate, setBirthDate] = useState(patient?.birthDate ?? '')
  const [bloodType, setBloodType] = useState<BloodType | ''>(patient?.bloodType ?? '')
  const [contactName, setContactName] = useState(patient?.emergencyContactName ?? '')
  const [contactPhone, setContactPhone] = useState(patient?.emergencyContactPhone ?? '')
  const [contactRelationship, setContactRelationship] = useState(patient?.emergencyContactRelationship ?? '')
  const [allergies, setAllergies] = useState(patient?.allergies ?? '')
  const [insured, setInsured] = useState(patient?.hasHealthInsurance ?? false)
  const [provider, setProvider] = useState(patient?.healthInsuranceProvider ?? '')
  const [insuranceNumber, setInsuranceNumber] = useState(patient?.healthInsuranceNumber ?? '')
  const [medicalHistory, setMedicalHistory] = useState(patient?.medicalHistory ?? '')
  const [error, setError] = useState('')

  const usersQuery = useQuery({
    queryKey: ['available-patient-users'],
    queryFn: getAvailablePatientUsers,
    enabled: !editing,
  })

  const mutation = useMutation({
    mutationFn: () => {
      const common = {
        bloodType: bloodType || null,
        emergencyContactName: contactName.trim(),
        emergencyContactPhone: contactPhone.trim(),
        emergencyContactRelationship: contactRelationship.trim(),
        allergies: allergies.trim(),
        hasHealthInsurance: insured,
        healthInsuranceProvider: insured ? provider.trim() : '',
        healthInsuranceNumber: insured ? insuranceNumber.trim() : '',
        medicalHistory: medicalHistory.trim(),
      }

      return patient
        ? updatePatient(patient.id, common)
        : createPatient({ ...common, userId: Number(userId), birthDate })
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['patients'] }),
        queryClient.invalidateQueries({ queryKey: ['available-patient-users'] }),
      ])
      onClose()
    },
    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(cause.response?.data?.message ?? 'No se pudo guardar el paciente.')
        return
      }
      setError('No se pudo guardar el paciente.')
    },
  })

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    if (!editing && !userId) return setError('Selecciona un usuario paciente.')
    if (!editing && !birthDate) return setError('La fecha de nacimiento es obligatoria.')
    if (contactName.length > 150 || contactPhone.length > 20 || contactRelationship.length > 50) return setError('Revisa la longitud de los datos del contacto de emergencia.')
    if (allergies.length > 500 || medicalHistory.length > 1000) return setError('Las notas clínicas superan la longitud permitida.')
    if (insured && (!provider.trim() || !insuranceNumber.trim())) return setError('Completa los datos del seguro médico.')
    mutation.mutate()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm" role="dialog" aria-modal="true" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="max-h-[94vh] w-full max-w-3xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div><h2 className="text-xl font-semibold text-slate-950">{editing ? 'Editar paciente' : 'Nuevo paciente'}</h2><p className="mt-1 text-sm text-slate-500">{editing ? 'Actualiza la información clínica y de contacto.' : 'Asocia un usuario PATIENT con su perfil clínico.'}</p></div>
          <button type="button" onClick={onClose} aria-label="Cerrar" className="rounded-lg p-2 text-slate-400 hover:bg-slate-100"><X className="size-5" /></button>
        </header>

        <form onSubmit={submit} className="space-y-6 p-6">
          <div className="grid gap-5 sm:grid-cols-2">
            {!editing && <><label className="block sm:col-span-2"><span className="mb-2 block text-sm font-medium text-slate-700">Usuario paciente</span><select value={userId} onChange={(e) => setUserId(e.target.value)} disabled={usersQuery.isPending} className={fieldClass}><option value="">Selecciona un usuario</option>{usersQuery.data?.map((user) => <option key={user.id} value={user.id}>{user.firstName} {user.lastName} · {user.documentId}</option>)}</select>{usersQuery.data?.length === 0 && <span className="mt-2 block text-sm text-amber-700">No existen usuarios PATIENT activos sin perfil.</span>}</label><Field label="Fecha de nacimiento"><input type="date" max={new Date().toISOString().slice(0, 10)} value={birthDate} onChange={(e) => setBirthDate(e.target.value)} className={fieldClass} /></Field></>}
            <Field label="Grupo sanguíneo"><select value={bloodType} onChange={(e) => setBloodType(e.target.value as BloodType | '')} className={fieldClass}><option value="">Sin especificar</option>{bloodTypes.map((type) => <option key={type} value={type}>{bloodTypeLabels[type]}</option>)}</select></Field>
            <Field label="Contacto de emergencia"><input value={contactName} onChange={(e) => setContactName(e.target.value)} maxLength={150} className={fieldClass} /></Field>
            <Field label="Teléfono de emergencia"><input value={contactPhone} onChange={(e) => setContactPhone(e.target.value)} maxLength={20} className={fieldClass} /></Field>
            <Field label="Relación"><input value={contactRelationship} onChange={(e) => setContactRelationship(e.target.value)} maxLength={50} placeholder="Ejemplo: Madre" className={fieldClass} /></Field>
            <label className="flex items-center gap-3 rounded-xl border border-slate-200 p-4 sm:col-span-2"><input type="checkbox" checked={insured} onChange={(e) => setInsured(e.target.checked)} className="size-4 accent-cyan-700" /><span className="text-sm font-medium text-slate-700">Tiene seguro médico</span></label>
            {insured && <><Field label="Aseguradora"><input value={provider} onChange={(e) => setProvider(e.target.value)} maxLength={100} className={fieldClass} /></Field><Field label="Número de póliza"><input value={insuranceNumber} onChange={(e) => setInsuranceNumber(e.target.value)} maxLength={50} className={fieldClass} /></Field></>}
            <div className="sm:col-span-2"><Field label="Alergias"><textarea value={allergies} onChange={(e) => setAllergies(e.target.value)} maxLength={500} rows={3} className={`${fieldClass} resize-none`} /></Field></div>
            <div className="sm:col-span-2"><Field label="Historial médico"><textarea value={medicalHistory} onChange={(e) => setMedicalHistory(e.target.value)} maxLength={1000} rows={4} className={`${fieldClass} resize-none`} /></Field></div>
          </div>
          {error && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}
          <footer className="flex justify-end gap-3 border-t border-slate-100 pt-5"><button type="button" onClick={onClose} className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600">Cancelar</button><button type="submit" disabled={mutation.isPending} className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-4 py-2.5 text-sm font-medium text-white disabled:opacity-50">{mutation.isPending && <LoaderCircle className="size-4 animate-spin" />}{mutation.isPending ? 'Guardando...' : editing ? 'Guardar cambios' : 'Crear paciente'}</button></footer>
        </form>
      </div>
    </div>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">{label}</span>{children}</label>
}
