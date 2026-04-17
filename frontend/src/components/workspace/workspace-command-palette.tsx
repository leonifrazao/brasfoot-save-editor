"use client"

import { useEffect, useMemo, useState } from "react"
import { ArrowRight, Command, Download, Search, Upload, Users2 } from "lucide-react"
import { usePathname } from "next/navigation"

import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import {
  workspaceDownloadPath,
  workspaceHomePath,
  workspaceManagersPath,
  workspacePlayersPath,
  workspaceTeamsPath,
} from "@/lib/routes"

type WorkspaceCommandPaletteProps = {
  sessionId: string
}

const commands = [
  {
    id: "overview",
    label: "Go to overview",
    hint: "Return to the current session dashboard",
    icon: Search,
  },
  {
    id: "teams",
    label: "Teams workspace",
    hint: "Preview the next editing surface",
    icon: Users2,
  },
  {
    id: "players",
    label: "Players workspace",
    hint: "Open the squad editor for a selected team",
    icon: Search,
  },
  {
    id: "managers",
    label: "Managers workspace",
    hint: "Review and edit board and fan confidence",
    icon: Users2,
  },
  {
    id: "download",
    label: "Download save",
    hint: "Export the edited `.s22` file for this session",
    icon: Download,
  },
  {
    id: "new-upload",
    label: "Start another upload",
    hint: "Return to the intake screen and open a new save",
    icon: Upload,
  },
] as const

export function WorkspaceCommandPalette({
  sessionId,
}: WorkspaceCommandPaletteProps) {
  const pathname = usePathname()
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState("")

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault()
        setOpen((value) => !value)
      }
    }

    window.addEventListener("keydown", onKeyDown)
    return () => window.removeEventListener("keydown", onKeyDown)
  }, [])

  const filteredCommands = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (!normalized) {
      return commands
    }

    return commands.filter((command) =>
      `${command.label} ${command.hint}`.toLowerCase().includes(normalized)
    )
  }, [query])

  function runCommand(id: (typeof commands)[number]["id"]) {
    setOpen(false)

    if (id === "overview") {
      if (pathname !== workspaceHomePath(sessionId)) {
        window.location.assign(workspaceHomePath(sessionId))
      }
      return
    }

    if (id === "teams") {
      window.location.assign(workspaceTeamsPath(sessionId))
      return
    }

    if (id === "players") {
      window.location.assign(workspacePlayersPath(sessionId))
      return
    }

    if (id === "managers") {
      window.location.assign(workspaceManagersPath(sessionId))
      return
    }

    if (id === "download") {
      window.location.assign(workspaceDownloadPath(sessionId))
      return
    }

    if (id === "new-upload") {
      window.location.assign("/")
      return
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        render={
          <Button variant="outline" size="sm" className="hidden lg:inline-flex" />
        }
      >
        <Command data-icon="inline-start" />
        Command palette
        <span className="ml-2 rounded-md border border-border/70 bg-background/70 px-2 py-0.5 text-[0.65rem] font-semibold uppercase tracking-[0.12em] text-muted-foreground">
          Cmd K
        </span>
      </DialogTrigger>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Command palette</DialogTitle>
          <DialogDescription>
            Fast route changes and high-signal actions for the current session.
          </DialogDescription>
        </DialogHeader>

        <div className="rounded-[1.25rem] border border-border/70 bg-background/70 p-3">
          <div className="flex items-center gap-3 rounded-xl border border-border/70 bg-input/70 px-3">
            <Search className="text-muted-foreground" />
            <Input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search commands or routes"
              aria-label="Search commands"
              className="border-0 bg-transparent px-0 shadow-none focus-visible:ring-0"
            />
          </div>

          <div className="mt-3 flex flex-col gap-2">
            {filteredCommands.length > 0 ? (
              filteredCommands.map((command) => {
                const Icon = command.icon

                return (
                  <button
                    key={command.id}
                    type="button"
                    onClick={() => runCommand(command.id)}
                    className="flex items-center gap-3 rounded-[1.1rem] border border-border/70 bg-white/[0.02] px-4 py-3 text-left transition-colors hover:bg-white/[0.05] focus-visible:ring-3 focus-visible:ring-ring/30"
                  >
                    <div className="flex size-10 items-center justify-center rounded-xl bg-primary/12 text-primary">
                      <Icon />
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-semibold text-foreground">
                        {command.label}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {command.hint}
                      </p>
                    </div>
                    <ArrowRight className="text-muted-foreground" />
                  </button>
                )
              })
            ) : (
              <div className="rounded-[1.1rem] border border-border/70 bg-white/[0.02] px-4 py-6 text-center text-sm text-muted-foreground">
                No matching command. Keep the query short and action-oriented.
              </div>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}
