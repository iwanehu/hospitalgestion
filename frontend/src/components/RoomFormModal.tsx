import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import { useState, type FormEvent, type ReactNode } from 'react'
import { createRoom, updateRoom } from '../api/rooms-api'
import { getWards } from '../api/wards-api'
import type { Room, RoomType } from '../types/room'
import { roomTypeLabels } from '../types/room'

interface Props {
  room?: Room
  onClose: () => void
}

interface ApiError {
  message?: string
}

const roomTypes = Object.keys(roomTypeLabels) as RoomType[]
const fieldClass =
  'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10 disabled:opacity-60'

export function RoomFormModal({ room, onClose }: Props) {
  const queryClient = useQueryClient()
  const editing = Boolean(room)
  const [number, setNumber] = useState(room?.number ?? '')
  const [floor, setFloor] = useState(String(room?.floor ?? 0))
  const [roomType, setRoomType] = useState<RoomType>(room?.roomType ?? 'INDIVIDUAL')
  const [capacity, setCapacity] = useState(String(room?.capacity ?? 1))
  const [wardId, setWardId] = useState(String(room?.wardId ?? ''))
  const [notes, setNotes] = useState(room?.notes ?? '')
  const [error, setError] = useState('')

  const wardsQuery = useQuery({
    queryKey: ['wards', 'active-for-room-form'],
    queryFn: () =>
      getWards({ page: 0, size: 100, isActive: true }),
  })

  const mutation = useMutation({
    mutationFn: async () => {
      const common = {
        number: number.trim(),
        floor: Number(floor),
        roomType,
        capacity: Number(capacity),
        notes: notes.trim(),
      }

      if (room) {
        return updateRoom(room.id, common)
      }

      return createRoom({ ...common, wardId: Number(wardId) })
    },
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['rooms'] }),
        queryClient.invalidateQueries({ queryKey: ['wards'] }),
      ])
      onClose()
    },
    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(cause.response?.data?.message ?? 'No se pudo guardar la habitación.')
        return
      }
      setError('No se pudo guardar la habitación.')
    },
  })

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    const parsedFloor = Number(floor)
    const parsedCapacity = Number(capacity)
    if (!number.trim()) return setError('El número es obligatorio.')
    if (!Number.isInteger(parsedFloor) || parsedFloor < -1 || parsedFloor > 50) {
      return setError('La planta debe ser un número entero entre -1 y 50.')
    }
    if (!Number.isInteger(parsedCapacity) || parsedCapacity < 1 || parsedCapacity > 10) {
      return setError('La capacidad debe estar entre 1 y 10.')
    }
    if (!editing && !wardId) return setError('Selecciona una sala.')
    mutation.mutate()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm" role="dialog" aria-modal="true" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div>
            <h2 className="text-xl font-semibold text-slate-950">{editing ? 'Editar habitación' : 'Nueva habitación'}</h2>
            <p className="mt-1 text-sm text-slate-500">{editing ? 'Actualiza los datos de la habitación.' : 'Añade una habitación a una sala hospitalaria.'}</p>
          </div>
          <button type="button" onClick={onClose} aria-label="Cerrar" className="rounded-lg p-2 text-slate-400 hover:bg-slate-100"><X className="size-5" /></button>
        </header>

        <form onSubmit={submit} className="p-6">
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Número">
              <input value={number} onChange={(e) => setNumber(e.target.value)} placeholder="Ejemplo: 101" className={fieldClass} />
            </Field>
            <Field label="Planta">
              <input type="number" min={-1} max={50} value={floor} onChange={(e) => setFloor(e.target.value)} className={fieldClass} />
            </Field>
            <Field label="Tipo">
              <select value={roomType} onChange={(e) => setRoomType(e.target.value as RoomType)} className={fieldClass}>
                {roomTypes.map((type) => <option key={type} value={type}>{roomTypeLabels[type]}</option>)}
              </select>
            </Field>
            <Field label="Capacidad">
              <input type="number" min={1} max={10} value={capacity} onChange={(e) => setCapacity(e.target.value)} className={fieldClass} />
            </Field>
            {!editing && (
              <div className="sm:col-span-2">
                <Field label="Sala">
                  <select value={wardId} onChange={(e) => setWardId(e.target.value)} disabled={wardsQuery.isPending} className={fieldClass}>
                    <option value="">Selecciona una sala</option>
                    {wardsQuery.data?.content.map((ward) => <option key={ward.id} value={ward.id}>{ward.name}</option>)}
                  </select>
                </Field>
              </div>
            )}
            <div className="sm:col-span-2">
              <Field label="Notas">
                <textarea value={notes} onChange={(e) => setNotes(e.target.value)} rows={3} placeholder="Información adicional" className={`${fieldClass} resize-none`} />
              </Field>
            </div>
          </div>

          {error && <div role="alert" className="mt-5 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}

          <footer className="mt-6 flex justify-end gap-3 border-t border-slate-100 pt-5">
            <button type="button" onClick={onClose} className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600">Cancelar</button>
            <button type="submit" disabled={mutation.isPending} className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-4 py-2.5 text-sm font-medium text-white hover:bg-cyan-800 disabled:opacity-50">
              {mutation.isPending && <LoaderCircle className="size-4 animate-spin" />}
              {mutation.isPending ? 'Guardando...' : editing ? 'Guardar cambios' : 'Crear habitación'}
            </button>
          </footer>
        </form>
      </div>
    </div>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">{label}</span>{children}</label>
}
