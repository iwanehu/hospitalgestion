import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { LoaderCircle, X } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { createBed, updateBed } from '../api/beds-api'
import { getRooms } from '../api/rooms-api'
import type { Bed } from '../types/bed'

interface Props { bed?: Bed; onClose: () => void }
interface ApiError { message?: string }

const fieldClass = 'w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-cyan-600 focus:bg-white focus:ring-4 focus:ring-cyan-600/10 disabled:opacity-60'

export function BedFormModal({ bed, onClose }: Props) {
  const queryClient = useQueryClient()
  const editing = Boolean(bed)
  const [bedNumber, setBedNumber] = useState(bed?.bedNumber ?? '')
  const [roomId, setRoomId] = useState(String(bed?.roomId ?? ''))
  const [notes, setNotes] = useState(bed?.notes ?? '')
  const [error, setError] = useState('')

  const roomsQuery = useQuery({
    queryKey: ['rooms', 'bed-form'],
    queryFn: () => getRooms({ page: 0, size: 100 }),
  })

  const mutation = useMutation({
    mutationFn: () => bed
      ? updateBed(bed.id, { bedNumber: bedNumber.trim(), notes: notes.trim() })
      : createBed({ bedNumber: bedNumber.trim(), roomId: Number(roomId), notes: notes.trim() }),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['beds'] }),
        queryClient.invalidateQueries({ queryKey: ['rooms'] }),
      ])
      onClose()
    },
    onError: (cause: unknown) => {
      if (axios.isAxiosError<ApiError>(cause)) {
        setError(cause.response?.data?.message ?? 'No se pudo guardar la cama.')
        return
      }
      setError('No se pudo guardar la cama.')
    },
  })

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    if (!/^[A-Za-z]{1,4}-?\d{1,3}$/.test(bedNumber.trim())) {
      return setError('Usa un número válido, por ejemplo A-1 o UCI12.')
    }
    if (!editing && !roomId) return setError('Selecciona una habitación.')
    if (notes.length > 500) return setError('Las notas no pueden superar 500 caracteres.')
    mutation.mutate()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/55 p-4 backdrop-blur-sm" role="dialog" aria-modal="true" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="w-full max-w-xl overflow-hidden rounded-3xl bg-white shadow-2xl">
        <header className="flex items-start justify-between border-b border-slate-200 px-6 py-5">
          <div><h2 className="text-xl font-semibold text-slate-950">{editing ? 'Editar cama' : 'Nueva cama'}</h2><p className="mt-1 text-sm text-slate-500">{editing ? 'Actualiza los datos de la cama.' : 'Añade una cama a una habitación.'}</p></div>
          <button type="button" onClick={onClose} aria-label="Cerrar" className="rounded-lg p-2 text-slate-400 hover:bg-slate-100"><X className="size-5" /></button>
        </header>
        <form onSubmit={submit} className="space-y-5 p-6">
          <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Número de cama</span><input value={bedNumber} onChange={(e) => setBedNumber(e.target.value)} placeholder="Ejemplo: A-1" className={fieldClass} /></label>
          {!editing && <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Habitación</span><select value={roomId} onChange={(e) => setRoomId(e.target.value)} disabled={roomsQuery.isPending} className={fieldClass}><option value="">Selecciona una habitación</option>{roomsQuery.data?.content.map((room) => <option key={room.id} value={room.id}>{room.number} · {room.wardName}</option>)}</select></label>}
          <label className="block"><span className="mb-2 block text-sm font-medium text-slate-700">Notas</span><textarea value={notes} onChange={(e) => setNotes(e.target.value)} maxLength={500} rows={4} placeholder="Información adicional" className={`${fieldClass} resize-none`} /><span className="mt-1 block text-right text-xs text-slate-400">{notes.length}/500</span></label>
          {error && <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}
          <footer className="flex justify-end gap-3 border-t border-slate-100 pt-5"><button type="button" onClick={onClose} className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600">Cancelar</button><button type="submit" disabled={mutation.isPending} className="inline-flex items-center gap-2 rounded-xl bg-cyan-700 px-4 py-2.5 text-sm font-medium text-white hover:bg-cyan-800 disabled:opacity-50">{mutation.isPending && <LoaderCircle className="size-4 animate-spin" />}{mutation.isPending ? 'Guardando...' : editing ? 'Guardar cambios' : 'Crear cama'}</button></footer>
        </form>
      </div>
    </div>
  )
}
