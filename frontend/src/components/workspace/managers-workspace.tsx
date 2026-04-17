"use client"

import { useEffect, useMemo, useState, useTransition } from "react"
import { Layers3, Save, ShieldEllipsis, UserRoundSearch } from "lucide-react"
import { toast } from "sonner"

import type { Manager, Team } from "@/lib/api/brasfoot-types"
import { batchUpdateManagers, updateManager } from "@/lib/api/brasfoot-client"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"

type ManagerDraft = {
  name: string
  confidenceBoard: string
  confidenceFans: string
}

type ManagerBulkDraft = {
  name: string
  confidenceBoard: string
  confidenceFans: string
}

type StoredManagersState = {
  selectedManagerId: number | null
  draftsByManagerId: Record<string, ManagerDraft>
}

type ManagersWorkspaceProps = {
  sessionId: string
  initialManagers: Manager[]
  teams: Team[]
}

function readStoredState(sessionId: string): StoredManagersState | null {
  if (typeof window === "undefined") {
    return null
  }

  const storedValue = window.localStorage.getItem(getStorageKey(sessionId))
  if (!storedValue) {
    return null
  }

  try {
    return JSON.parse(storedValue) as StoredManagersState
  } catch {
    window.localStorage.removeItem(getStorageKey(sessionId))
    return null
  }
}

function getStorageKey(sessionId: string) {
  return `brasfoot-save-editor:${sessionId}:managers`
}

function makeDraft(manager: Manager): ManagerDraft {
  return {
    name: manager.name,
    confidenceBoard: String(manager.confidenceBoard),
    confidenceFans: String(manager.confidenceFans),
  }
}

export function ManagersWorkspace({
  sessionId,
  initialManagers,
  teams,
}: ManagersWorkspaceProps) {
  const [managers, setManagers] = useState(initialManagers)
  const [selectedManagerId, setSelectedManagerId] = useState<number | null>(
    initialManagers[0]?.id ?? null
  )
  const [draftsByManagerId, setDraftsByManagerId] = useState<
    Record<string, ManagerDraft>
  >({})
  const [filter, setFilter] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [bulkError, setBulkError] = useState<string | null>(null)
  const [bulkDraft, setBulkDraft] = useState<ManagerBulkDraft>({
    name: "",
    confidenceBoard: "",
    confidenceFans: "",
  })
  const [isPending, startTransition] = useTransition()

  const filteredManagers = useMemo(() => {
    const normalized = filter.trim().toLowerCase()

    if (!normalized) {
      return managers
    }

    return managers.filter((manager) =>
      `${manager.name} ${manager.id}`.toLowerCase().includes(normalized)
    )
  }, [filter, managers])

  const selectedManager =
    managers.find((manager) => manager.id === selectedManagerId) ??
    filteredManagers[0] ??
    null

  const currentDraft = selectedManager
    ? draftsByManagerId[String(selectedManager.id)] ?? makeDraft(selectedManager)
    : null

  const hasUnsavedChanges =
    selectedManager && currentDraft
      ? currentDraft.name !== selectedManager.name ||
        currentDraft.confidenceBoard !== String(selectedManager.confidenceBoard) ||
        currentDraft.confidenceFans !== String(selectedManager.confidenceFans)
      : false

  const hasBulkChanges = Object.values(bulkDraft).some((value) => value.trim() !== "")

  useEffect(() => {
    const storedState = readStoredState(sessionId)

    if (!storedState) {
      return
    }

    Promise.resolve().then(() => {
      setDraftsByManagerId(storedState.draftsByManagerId ?? {})

      if (
        storedState.selectedManagerId &&
        initialManagers.some((manager) => manager.id === storedState.selectedManagerId)
      ) {
        setSelectedManagerId(storedState.selectedManagerId)
      }
    })
  }, [initialManagers, sessionId])

  useEffect(() => {
    if (typeof window === "undefined") {
      return
    }

    const payload: StoredManagersState = {
      selectedManagerId,
      draftsByManagerId,
    }

    window.localStorage.setItem(getStorageKey(sessionId), JSON.stringify(payload))
  }, [draftsByManagerId, selectedManagerId, sessionId])

  function getTeamName(teamId: number) {
    return teams.find((team) => team.id === teamId)?.name ?? `Team #${teamId}`
  }

  function updateDraft(patch: Partial<ManagerDraft>) {
    if (!selectedManager) {
      return
    }

    setDraftsByManagerId((current) => ({
      ...current,
      [String(selectedManager.id)]: {
        ...(current[String(selectedManager.id)] ?? makeDraft(selectedManager)),
        ...patch,
      },
    }))
  }

  function handleSave() {
    if (!selectedManager || !currentDraft) {
      return
    }

    const confidenceBoard = Number(currentDraft.confidenceBoard)
    const confidenceFans = Number(currentDraft.confidenceFans)

    if (
      !Number.isFinite(confidenceBoard) ||
      !Number.isFinite(confidenceFans) ||
      confidenceBoard < 0 ||
      confidenceFans < 0
    ) {
      setError("Manager confidence values must be valid positive numbers.")
      return
    }

    setError(null)

    startTransition(async () => {
      try {
        const updatedManager = await updateManager(sessionId, selectedManager.id, {
          name: currentDraft.name,
          confidenceBoard,
          confidenceFans,
        })

        setManagers((current) =>
          current.map((manager) =>
            manager.id === updatedManager.id ? updatedManager : manager
          )
        )
        setDraftsByManagerId((current) => ({
          ...current,
          [String(updatedManager.id)]: makeDraft(updatedManager),
        }))
        toast.success(`Manager ${updatedManager.name} updated.`)
      } catch (saveError) {
        setError(
          saveError instanceof Error && saveError.message
            ? saveError.message
            : "The manager update failed."
        )
      }
    })
  }

  function handleBulkSave() {
    const confidenceBoard =
      bulkDraft.confidenceBoard.trim() === ""
        ? undefined
        : Number(bulkDraft.confidenceBoard)
    const confidenceFans =
      bulkDraft.confidenceFans.trim() === ""
        ? undefined
        : Number(bulkDraft.confidenceFans)
    const name = bulkDraft.name.trim() === "" ? undefined : bulkDraft.name.trim()

    if (name === undefined && confidenceBoard === undefined && confidenceFans === undefined) {
      setBulkError("Fill at least one field before running a bulk update.")
      return
    }

    if (
      (confidenceBoard !== undefined &&
        (!Number.isFinite(confidenceBoard) || confidenceBoard < 0)) ||
      (confidenceFans !== undefined &&
        (!Number.isFinite(confidenceFans) || confidenceFans < 0))
    ) {
      setBulkError("Bulk confidence values must be valid positive numbers.")
      return
    }

    setBulkError(null)

    startTransition(async () => {
      try {
        const response = await batchUpdateManagers(
          sessionId,
          filteredManagers.map((manager) => ({
            managerId: manager.id,
            name,
            confidenceBoard,
            confidenceFans,
          }))
        )

        const updatedManagers = response.results
          .filter((result) => result.success && result.data)
          .map((result) => result.data as Manager)

        setManagers((current) => {
          const nextManagers = [...current]

          for (const updatedManager of updatedManagers) {
            const index = nextManagers.findIndex(
              (manager) => manager.id === updatedManager.id
            )
            if (index >= 0) {
              nextManagers[index] = updatedManager
            }
          }

          return nextManagers
        })

        setDraftsByManagerId((current) => {
          const nextDrafts = { ...current }

          for (const updatedManager of updatedManagers) {
            nextDrafts[String(updatedManager.id)] = makeDraft(updatedManager)
          }

          return nextDrafts
        })

        const failed = response.results.filter((result) => !result.success).length
        setBulkDraft({
          name: "",
          confidenceBoard: "",
          confidenceFans: "",
        })
        toast.success(
          failed === 0
            ? `${updatedManagers.length} managers updated.`
            : `${updatedManagers.length} managers updated, ${failed} failed.`
        )
      } catch (saveError) {
        setBulkError(
          saveError instanceof Error && saveError.message
            ? saveError.message
            : "The bulk manager update failed."
        )
      }
    })
  }

  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1.15fr)_minmax(24rem,0.9fr)]">
      <Card variant="elevated">
        <CardHeader className="gap-4 border-b border-border/70 pb-5">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="success">Managers loaded</Badge>
            <Badge variant="outline">{managers.length} total</Badge>
          </div>
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <CardTitle className="text-2xl">Managers workspace</CardTitle>
              <CardDescription className="mt-2 max-w-2xl text-sm leading-6">
                Update names and confidence indicators for the human profile and
                CPU managers without leaving the current session.
              </CardDescription>
            </div>
            <div className="relative w-full max-w-sm">
              <UserRoundSearch className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
                placeholder="Search manager"
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent className="py-5">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Manager</TableHead>
                <TableHead>Team</TableHead>
                <TableHead>Board</TableHead>
                <TableHead>Fans</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredManagers.map((manager) => (
                <TableRow
                  key={manager.id}
                  className={
                    selectedManager?.id === manager.id ? "bg-white/[0.05]" : undefined
                  }
                >
                  <TableCell>
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedManagerId(manager.id)
                        setError(null)
                      }}
                      className="w-full text-left"
                    >
                      <div className="font-semibold text-foreground">
                        {manager.name}
                      </div>
                      <div className="mt-1 text-xs text-muted-foreground">
                        {manager.isHuman ? "Human manager" : "CPU manager"}
                      </div>
                    </button>
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {getTeamName(manager.teamId)}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {manager.confidenceBoard}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {manager.confidenceFans}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card variant="ghost" className="panel-surface panel-elevated">
        <CardHeader className="gap-4 border-b border-border/70 pb-5">
          <div className="flex items-center gap-2">
            <ShieldEllipsis className="size-4 text-primary" />
            <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
              Manager editor
            </p>
          </div>
          <CardTitle className="text-xl">
            {selectedManager ? selectedManager.name : "Select a manager"}
          </CardTitle>
          <CardDescription className="text-sm leading-6">
            {selectedManager
              ? `Editing ${selectedManager.isHuman ? "the human manager" : "a CPU manager"} for ${getTeamName(selectedManager.teamId)}.`
              : "Choose a manager from the list to start editing."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-5 py-5">
          {selectedManager && currentDraft ? (
            <>
              <div className="rounded-[1.2rem] border border-primary/20 bg-primary/8 p-4">
                <div className="flex items-center gap-2">
                  <Layers3 className="size-4 text-primary" />
                  <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
                    Bulk changes
                  </p>
                </div>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Apply the fields below to all {filteredManagers.length} managers
                  currently visible in the table.
                </p>
                <div className="mt-4 space-y-3">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      Name
                    </label>
                    <Input
                      value={bulkDraft.name}
                      placeholder="Leave blank to keep current names"
                      onChange={(event) =>
                        setBulkDraft((current) => ({
                          ...current,
                          name: event.target.value,
                        }))
                      }
                    />
                  </div>
                  <div className="grid gap-3 sm:grid-cols-2">
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-foreground">
                        Board confidence
                      </label>
                      <Input
                        type="number"
                        placeholder="Leave blank to skip"
                        value={bulkDraft.confidenceBoard}
                        onChange={(event) =>
                          setBulkDraft((current) => ({
                            ...current,
                            confidenceBoard: event.target.value,
                          }))
                        }
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-medium text-foreground">
                        Fan confidence
                      </label>
                      <Input
                        type="number"
                        placeholder="Leave blank to skip"
                        value={bulkDraft.confidenceFans}
                        onChange={(event) =>
                          setBulkDraft((current) => ({
                            ...current,
                            confidenceFans: event.target.value,
                          }))
                        }
                      />
                    </div>
                  </div>
                </div>
                {bulkError ? (
                  <Alert variant="destructive" className="mt-4">
                    <AlertTitle>Bulk manager update failed</AlertTitle>
                    <AlertDescription>{bulkError}</AlertDescription>
                  </Alert>
                ) : null}
                <div className="mt-4">
                  <Button
                    variant="secondary"
                    onClick={handleBulkSave}
                    disabled={isPending || !hasBulkChanges || filteredManagers.length === 0}
                  >
                    <Layers3 data-icon="inline-start" />
                    {isPending ? "Applying..." : `Apply to ${filteredManagers.length} managers`}
                  </Button>
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground">
                  Name
                </label>
                <Input
                  value={currentDraft.name}
                  onChange={(event) => updateDraft({ name: event.target.value })}
                />
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2">
                  <label className="text-sm font-medium text-foreground">
                    Board confidence
                  </label>
                  <Input
                    type="number"
                    value={currentDraft.confidenceBoard}
                    onChange={(event) =>
                      updateDraft({ confidenceBoard: event.target.value })
                    }
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-sm font-medium text-foreground">
                    Fan confidence
                  </label>
                  <Input
                    type="number"
                    value={currentDraft.confidenceFans}
                    onChange={(event) =>
                      updateDraft({ confidenceFans: event.target.value })
                    }
                  />
                </div>
              </div>

              {error ? (
                <Alert variant="destructive">
                  <AlertTitle>Manager update failed</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              ) : null}

              <div className="rounded-[1.2rem] border border-border/70 bg-background/72 p-4 text-sm text-muted-foreground">
                {hasUnsavedChanges
                  ? "This manager has local draft changes waiting to be saved."
                  : "This manager is in sync with the API session."}
              </div>

              <Button onClick={handleSave} disabled={isPending || !hasUnsavedChanges}>
                <Save data-icon="inline-start" />
                {isPending ? "Saving..." : "Save manager changes"}
              </Button>
            </>
          ) : (
            <div className="rounded-[1.2rem] border border-dashed border-border/70 px-4 py-8 text-sm text-muted-foreground">
              No manager is selected yet.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
