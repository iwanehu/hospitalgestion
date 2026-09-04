import { Construction } from 'lucide-react'

interface ComingSoonPageProps {
  title: string
}

export function ComingSoonPage({
  title,
}: ComingSoonPageProps) {
  return (
    <section className="flex min-h-[60vh] items-center justify-center">
      <div className="max-w-md rounded-3xl border border-slate-200 bg-white p-10 text-center shadow-sm">
        <div className="mx-auto mb-5 flex size-16 items-center justify-center rounded-2xl bg-amber-50 text-amber-600">
          <Construction className="size-8" />
        </div>

        <h2 className="text-2xl font-semibold text-slate-950">
          {title}
        </h2>

        <p className="mt-3 text-slate-500">
          Este módulo será incorporado durante las próximas
          etapas del frontend.
        </p>
      </div>
    </section>
  )
}
