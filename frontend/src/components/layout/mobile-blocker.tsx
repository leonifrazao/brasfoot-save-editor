"use client"

import { MonitorSmartphone, PanelTopClose, TabletSmartphone } from "lucide-react"

import { Button } from "@/components/ui/button"

type MobileBlockerProps = {
  title: string
  description: string
}

export function MobileBlocker({ title, description }: MobileBlockerProps) {
  return (
    <div className="md:hidden">
      <main className="grain-overlay flex min-h-screen items-center justify-center px-5 py-8">
        <section className="panel-surface panel-elevated w-full max-w-md rounded-[2rem] border border-border/70 p-6">
          <div className="mb-6 flex items-center gap-3">
            <div className="flex size-12 items-center justify-center rounded-2xl bg-primary/14 text-primary">
              <PanelTopClose />
            </div>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-primary">
                Desktop and tablet
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                This release intentionally skips phone layouts.
              </p>
            </div>
          </div>

          <h1 className="text-2xl font-semibold tracking-[-0.03em] text-foreground">
            {title}
          </h1>
          <p className="mt-3 text-sm leading-7 text-muted-foreground">
            {description}
          </p>

          <div className="mt-6 grid gap-3">
            <div className="rounded-[1.25rem] border border-border/70 bg-white/[0.03] px-4 py-3">
              <div className="flex items-center gap-3">
                <TabletSmartphone className="text-primary" />
                <p className="text-sm font-medium text-foreground">
                  Recommended width: 768px and up
                </p>
              </div>
            </div>
            <div className="rounded-[1.25rem] border border-border/70 bg-white/[0.03] px-4 py-3">
              <div className="flex items-center gap-3">
                <MonitorSmartphone className="text-primary" />
                <p className="text-sm font-medium text-foreground">
                  Optimized for tablet landscape and desktop workflows
                </p>
              </div>
            </div>
          </div>

          <Button
            size="lg"
            className="mt-6 w-full"
            onClick={() => window.location.reload()}
          >
            Retry on a larger viewport
          </Button>
        </section>
      </main>
    </div>
  )
}
