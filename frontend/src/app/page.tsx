import { ArrowRight, BadgeCheck, Command, ShieldCheck, Sparkles, TimerReset, Users2 } from "lucide-react"

import { MobileBlocker } from "@/components/layout/mobile-blocker"
import { SessionUploadForm } from "@/components/upload/session-upload-form"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader } from "@/components/ui/card"

const trustPoints = [
  {
    title: "Safe first step",
    description:
      "Start with your own save file and move into the editor without touching the original until you are ready to export again.",
    icon: ShieldCheck,
  },
  {
    title: "Built for real editing",
    description:
      "The workspace is designed for teams, players, and managers so the structure feels clear before you make deeper changes.",
    icon: Users2,
  },
  {
    title: "Fast recovery path",
    description:
      "Clear upload feedback and recovery guidance keep the first session understandable for non-technical players.",
    icon: TimerReset,
  },
]

const workflowSteps = [
  "Upload your current Brasfoot save file.",
  "Land inside a stable session workspace.",
  "Move into teams, players, and managers with confidence.",
]

export default function Home() {
  return (
    <>
      <MobileBlocker
        title="The intake screen is designed for larger screens"
        description="Upload, validation, and session orchestration are intentionally optimized for tablet and desktop use in this release."
      />

      <main className="grain-overlay hidden min-h-screen px-6 py-8 text-foreground md:block md:px-10 md:py-10">
        <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-8">
          <section className="panel-surface panel-elevated rounded-[2.2rem] border border-border/70 p-4 md:p-6">
            <div className="grid gap-8 lg:grid-cols-[minmax(0,1.18fr)_minmax(25rem,31rem)] lg:items-stretch">
              <div className="flex flex-col justify-between gap-8 rounded-[1.8rem] border border-white/6 bg-[linear-gradient(145deg,rgba(255,255,255,0.04),rgba(255,255,255,0.01))] p-6 md:p-8">
              <div className="space-y-6">
                <div className="flex flex-wrap items-center gap-3">
                  <Badge variant="outline" className="px-4 py-2 text-sm normal-case tracking-normal">
                    Brasfoot Save Editor
                  </Badge>
                  <Badge variant="default" className="px-4 py-2 text-sm normal-case tracking-normal">
                    <Sparkles className="size-4" aria-hidden="true" />
                    Dark workspace system
                  </Badge>
                  <Badge variant="secondary" className="px-4 py-2 text-sm normal-case tracking-normal">
                    <BadgeCheck className="size-4" aria-hidden="true" />
                    Desktop and tablet first
                  </Badge>
                </div>

                <div className="max-w-3xl space-y-5">
                  <p className="text-sm font-semibold uppercase tracking-[0.24em] text-primary">
                    Upload. Orient. Edit. Export.
                  </p>
                  <h1 className="max-w-3xl text-[clamp(2.8rem,6vw,5.8rem)] font-semibold leading-[0.9] tracking-[-0.05em] text-foreground">
                    A dark control room for Brasfoot save editing.
                  </h1>
                  <p className="max-w-2xl text-base leading-8 text-muted-foreground md:text-lg">
                    Built with Next.js App Router and a deeply customized shadcn/ui
                    foundation, this frontend treats upload, route context, and
                    future data editing as one continuous desktop-grade experience.
                  </p>
                </div>
              </div>

              <div className="grid gap-4 md:grid-cols-3">
                {trustPoints.map(({ title, description, icon: Icon }) => (
                  <Card
                    key={title}
                    variant="ghost"
                    className="border-border/70 bg-card/60"
                  >
                    <CardContent className="p-5">
                    <div className="mb-4 flex size-11 items-center justify-center rounded-2xl bg-primary/12 text-primary">
                      <Icon className="size-5" aria-hidden="true" />
                    </div>
                    <h2 className="text-base font-semibold text-foreground">{title}</h2>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                      {description}
                    </p>
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>

            <div className="flex flex-col gap-4">
              <SessionUploadForm />
              <Card variant="ghost" className="panel-surface">
                <CardContent className="p-5">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">
                      Editing flow
                    </p>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                      The first session should explain itself at a glance.
                    </p>
                  </div>
                  <ArrowRight className="size-5 text-primary" aria-hidden="true" />
                </div>
                <ol className="mt-5 space-y-3">
                  {workflowSteps.map((step, index) => (
                    <li
                      key={step}
                      className="flex items-start gap-3 rounded-2xl border border-border/60 bg-background/72 px-4 py-3"
                    >
                      <span className="mt-0.5 flex size-7 shrink-0 items-center justify-center rounded-full bg-primary text-sm font-semibold text-primary-foreground">
                        {index + 1}
                      </span>
                      <span className="text-sm leading-6 text-foreground">{step}</span>
                    </li>
                  ))}
                </ol>
                </CardContent>
              </Card>
            </div>
          </div>
          </section>

          <section className="grid gap-4 lg:grid-cols-[minmax(0,1.5fr)_minmax(0,1fr)]">
            <Card variant="ghost" className="panel-surface p-1">
              <CardHeader>
                <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">
              What the shell prioritizes
            </p>
              </CardHeader>
              <CardContent>
            <div className="mt-5 grid gap-4 md:grid-cols-3">
              <div className="rounded-[1.25rem] border border-border/70 bg-background/74 p-4">
                <p className="text-base font-semibold">Clear current context</p>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Session identity, route cues, and destination hierarchy stay
                  visible before entity editing becomes dense.
                </p>
              </div>
              <div className="rounded-[1.25rem] border border-border/70 bg-background/74 p-4">
                <p className="text-base font-semibold">Calm surfaces</p>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Warm neutrals, restrained accents, and strong spacing keep the
                  app readable for longer editing sessions.
                </p>
              </div>
              <div className="rounded-[1.25rem] border border-border/70 bg-background/74 p-4">
                <p className="text-base font-semibold">Visible recovery</p>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Upload, loading, and failure states all explain what is
                  happening and what to do next.
                </p>
              </div>
            </div>
              </CardContent>
            </Card>

            <Card variant="elevated" className="border-primary/15 p-1">
              <CardHeader>
                <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">
              Product tone
            </p>
              </CardHeader>
              <CardContent>
            <p className="mt-4 text-2xl font-semibold leading-tight text-foreground">
              More mission control, less copy-paste admin.
            </p>
            <p className="mt-3 text-sm leading-7 text-muted-foreground">
              Your tech lead is only right if you stop at the raw kit. shadcn/ui
              gives us the concrete frame; the distinctiveness comes from token
              orchestration, composition, motion discipline, and surface hierarchy.
            </p>
            <div className="mt-5 flex flex-wrap gap-2 text-sm text-muted-foreground">
              <span className="inline-flex items-center gap-2 rounded-full border border-border/70 px-3 py-1.5">
                <Command className="size-4 text-primary" />
                Keyboard-led interactions
              </span>
              <span className="inline-flex items-center gap-2 rounded-full border border-border/70 px-3 py-1.5">
                <ArrowRight className="size-4 text-primary" />
                Single primary action
              </span>
            </div>
              </CardContent>
            </Card>
          </section>
        </div>
      </main>
    </>
  )
}
