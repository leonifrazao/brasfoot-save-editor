"use client"

import { useEffect, useMemo, useState, useTransition } from "react"
import { Building2, Layers3, Save, Search, Wallet } from "lucide-react"
import { toast } from "sonner"

import type { Team } from "@/lib/api/brasfoot-types"
import {
  formatCurrency,
  getTeamReputationLabel,
  TEAM_REPUTATION_OPTIONS,
} from "@/lib/api/brasfoot-types"
import { batchUpdateTeams, updateTeam } from "@/lib/api/brasfoot-client"
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

type TeamDraft = {
  money: string
  reputation: string
}

type TeamBulkDraft = {
  money: string
  reputation: string
}

type StoredTeamState = {
  selectedTeamId: number | null
  draftsByTeamId: Record<string, TeamDraft>
}

type TeamsWorkspaceProps = {
  sessionId: string
  initialTeams: Team[]
}

function readStoredState(sessionId: string): StoredTeamState | null {
  if (typeof window === "undefined") {
    return null
  }

  const storedValue = window.localStorage.getItem(getStorageKey(sessionId))
  if (!storedValue) {
    return null
  }

  try {
    return JSON.parse(storedValue) as StoredTeamState
  } catch {
    window.localStorage.removeItem(getStorageKey(sessionId))
    return null
  }
}

function getStorageKey(sessionId: string) {
  return `brasfoot-save-editor:${sessionId}:teams`
}

function makeDraft(team: Team): TeamDraft {
  return {
    money: String(team.money),
    reputation: String(team.reputation),
  }
}

export function TeamsWorkspace({
  sessionId,
  initialTeams,
}: TeamsWorkspaceProps) {
  const [teams, setTeams] = useState(initialTeams)
  const [filter, setFilter] = useState("")
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(
    initialTeams[0]?.id ?? null
  )
  const [draftsByTeamId, setDraftsByTeamId] = useState<
    Record<string, TeamDraft>
  >({})
  const [error, setError] = useState<string | null>(null)
  const [bulkError, setBulkError] = useState<string | null>(null)
  const [bulkDraft, setBulkDraft] = useState<TeamBulkDraft>({
    money: "",
    reputation: "",
  })
  const [isPending, startTransition] = useTransition()

  const filteredTeams = useMemo(() => {
    const normalized = filter.trim().toLowerCase()

    if (!normalized) {
      return teams
    }

    return teams.filter((team) =>
      `${team.name} ${team.id}`.toLowerCase().includes(normalized)
    )
  }, [filter, teams])

  const selectedTeam =
    teams.find((team) => team.id === selectedTeamId) ?? filteredTeams[0] ?? null

  const currentDraft = selectedTeam
    ? draftsByTeamId[String(selectedTeam.id)] ?? makeDraft(selectedTeam)
    : null

  const hasUnsavedChanges =
    selectedTeam && currentDraft
      ? currentDraft.money !== String(selectedTeam.money) ||
        currentDraft.reputation !== String(selectedTeam.reputation)
      : false

  const hasBulkChanges =
    bulkDraft.money.trim() !== "" || bulkDraft.reputation.trim() !== ""

  useEffect(() => {
    const storedState = readStoredState(sessionId)

    if (!storedState) {
      return
    }

    Promise.resolve().then(() => {
      setDraftsByTeamId(storedState.draftsByTeamId ?? {})

      if (
        storedState.selectedTeamId &&
        initialTeams.some((team) => team.id === storedState.selectedTeamId)
      ) {
        setSelectedTeamId(storedState.selectedTeamId)
      }
    })
  }, [initialTeams, sessionId])

  useEffect(() => {
    if (typeof window === "undefined") {
      return
    }

    const payload: StoredTeamState = {
      selectedTeamId,
      draftsByTeamId,
    }

    window.localStorage.setItem(getStorageKey(sessionId), JSON.stringify(payload))
  }, [draftsByTeamId, selectedTeamId, sessionId])

  function updateDraft(patch: Partial<TeamDraft>) {
    if (!selectedTeam) {
      return
    }

    setDraftsByTeamId((current) => ({
      ...current,
      [String(selectedTeam.id)]: {
        ...(current[String(selectedTeam.id)] ?? makeDraft(selectedTeam)),
        ...patch,
      },
    }))
  }

  function handleSave() {
    if (!selectedTeam || !currentDraft) {
      return
    }

    setError(null)

    const money = Number(currentDraft.money)
    const reputation = Number(currentDraft.reputation)

    if (!Number.isFinite(money) || money < 0) {
      setError("Money must be zero or a positive number.")
      return
    }

    if (!Number.isInteger(reputation) || reputation < 0 || reputation > 5) {
      setError("Reputation must stay between Municipal and Mundial.")
      return
    }

    startTransition(async () => {
      try {
        const updatedTeam = await updateTeam(sessionId, selectedTeam.id, {
          money,
          reputation,
        })

        setTeams((current) =>
          current.map((team) => (team.id === updatedTeam.id ? updatedTeam : team))
        )
        setDraftsByTeamId((current) => ({
          ...current,
          [String(updatedTeam.id)]: makeDraft(updatedTeam),
        }))
        toast.success(`Team ${updatedTeam.name} updated.`)
      } catch (saveError) {
        setError(
          saveError instanceof Error && saveError.message
            ? saveError.message
            : "The team update failed."
        )
      }
    })
  }

  function handleBulkSave() {
    const money =
      bulkDraft.money.trim() === "" ? undefined : Number(bulkDraft.money)
    const reputation =
      bulkDraft.reputation.trim() === "" ? undefined : Number(bulkDraft.reputation)

    if (money === undefined && reputation === undefined) {
      setBulkError("Fill at least one field before running a bulk update.")
      return
    }

    if (money !== undefined && (!Number.isFinite(money) || money < 0)) {
      setBulkError("Bulk money must be zero or a positive number.")
      return
    }

    if (
      reputation !== undefined &&
      (!Number.isInteger(reputation) || reputation < 0 || reputation > 5)
    ) {
      setBulkError("Bulk reputation must stay between Municipal and Mundial.")
      return
    }

    setBulkError(null)

    startTransition(async () => {
      try {
        const response = await batchUpdateTeams(
          sessionId,
          filteredTeams.map((team) => ({
            teamId: team.id,
            money,
            reputation,
          }))
        )

        const updatedTeams = response.results
          .filter((result) => result.success && result.data)
          .map((result) => result.data as Team)

        setTeams((current) => {
          const nextTeams = [...current]

          for (const updatedTeam of updatedTeams) {
            const index = nextTeams.findIndex((team) => team.id === updatedTeam.id)
            if (index >= 0) {
              nextTeams[index] = updatedTeam
            }
          }

          return nextTeams
        })

        setDraftsByTeamId((current) => {
          const nextDrafts = { ...current }

          for (const updatedTeam of updatedTeams) {
            nextDrafts[String(updatedTeam.id)] = makeDraft(updatedTeam)
          }

          return nextDrafts
        })

        const failed = response.results.filter((result) => !result.success).length
        setBulkDraft({ money: "", reputation: "" })
        toast.success(
          failed === 0
            ? `${updatedTeams.length} teams updated.`
            : `${updatedTeams.length} teams updated, ${failed} failed.`
        )
      } catch (saveError) {
        setBulkError(
          saveError instanceof Error && saveError.message
            ? saveError.message
            : "The bulk team update failed."
        )
      }
    })
  }

  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1.2fr)_minmax(24rem,0.9fr)]">
      <Card variant="elevated">
        <CardHeader className="gap-4 border-b border-border/70 pb-5">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="success">Teams loaded</Badge>
            <Badge variant="outline">{teams.length} total</Badge>
          </div>
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <CardTitle className="text-2xl">Teams workspace</CardTitle>
              <CardDescription className="mt-2 max-w-2xl text-sm leading-6">
                Review club finances and reputation in one place. Draft changes
                stay in local browser storage until you save or replace them.
              </CardDescription>
            </div>
            <div className="relative w-full max-w-sm">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={filter}
                onChange={(event) => setFilter(event.target.value)}
                placeholder="Search team by name or id"
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent className="py-5">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Team</TableHead>
                <TableHead>Money</TableHead>
                <TableHead>Reputation</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredTeams.map((team) => {
                const isActive = selectedTeam?.id === team.id

                return (
                  <TableRow
                    key={team.id}
                    className={isActive ? "bg-white/[0.05]" : undefined}
                  >
                    <TableCell>
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedTeamId(team.id)
                          setError(null)
                        }}
                        className="w-full text-left"
                      >
                        <div className="font-semibold text-foreground">
                          {team.name}
                        </div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          Team #{team.id}
                        </div>
                      </button>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formatCurrency(team.money)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {getTeamReputationLabel(team.reputation)}
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card variant="ghost" className="panel-surface panel-elevated">
        <CardHeader className="gap-4 border-b border-border/70 pb-5">
          <div className="flex items-center gap-2">
            <Building2 className="size-4 text-primary" />
            <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
              Team editor
            </p>
          </div>
          <CardTitle className="text-xl">
            {selectedTeam ? selectedTeam.name : "Select a team"}
          </CardTitle>
          <CardDescription className="text-sm leading-6">
            {selectedTeam
              ? "Edit the selected club and push the change back into the active API session."
              : "Choose a team from the list to start editing."}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-5 py-5">
          {selectedTeam && currentDraft ? (
            <>
              <div className="rounded-[1.2rem] border border-primary/20 bg-primary/8 p-4">
                <div className="flex items-center gap-2">
                  <Layers3 className="size-4 text-primary" />
                  <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
                    Bulk changes
                  </p>
                </div>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Apply money and/or reputation to all {filteredTeams.length} teams
                  currently visible in the table.
                </p>
                <div className="mt-4 grid gap-3 sm:grid-cols-2">
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      Money
                    </label>
                    <Input
                      type="number"
                      value={bulkDraft.money}
                      placeholder="Leave blank to skip"
                      onChange={(event) =>
                        setBulkDraft((current) => ({
                          ...current,
                          money: event.target.value,
                        }))
                      }
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      Reputation
                    </label>
                    <select
                      value={bulkDraft.reputation}
                      onChange={(event) =>
                        setBulkDraft((current) => ({
                          ...current,
                          reputation: event.target.value,
                        }))
                      }
                      className="h-10 w-full rounded-xl border border-input bg-input/60 px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/30"
                    >
                      <option value="">Keep current reputation</option>
                      {TEAM_REPUTATION_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
                {bulkError ? (
                  <Alert variant="destructive" className="mt-4">
                    <AlertTitle>Bulk team update failed</AlertTitle>
                    <AlertDescription>{bulkError}</AlertDescription>
                  </Alert>
                ) : null}
                <div className="mt-4">
                  <Button
                    variant="secondary"
                    onClick={handleBulkSave}
                    disabled={isPending || !hasBulkChanges || filteredTeams.length === 0}
                  >
                    <Layers3 data-icon="inline-start" />
                    {isPending ? "Applying..." : `Apply to ${filteredTeams.length} teams`}
                  </Button>
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div className="rounded-[1.2rem] border border-border/70 bg-background/72 p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                    Current budget
                  </p>
                  <p className="mt-3 text-2xl font-semibold">
                    {formatCurrency(selectedTeam.money)}
                  </p>
                </div>
                <div className="rounded-[1.2rem] border border-border/70 bg-background/72 p-4">
                  <p className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                    Current reputation
                  </p>
                  <p className="mt-3 text-2xl font-semibold">
                    {getTeamReputationLabel(selectedTeam.reputation)}
                  </p>
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground">
                  Money
                </label>
                <div className="relative">
                  <Wallet className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    type="number"
                    min="0"
                    value={currentDraft.money}
                    onChange={(event) => updateDraft({ money: event.target.value })}
                    className="pl-9"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground">
                  Reputation
                </label>
                <select
                  value={currentDraft.reputation}
                  onChange={(event) =>
                    updateDraft({ reputation: event.target.value })
                  }
                  className="h-10 w-full rounded-xl border border-input bg-input/60 px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/30"
                >
                  {TEAM_REPUTATION_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              {error ? (
                <Alert variant="destructive">
                  <AlertTitle>Team update failed</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              ) : null}

              <div className="rounded-[1.2rem] border border-border/70 bg-background/72 p-4 text-sm text-muted-foreground">
                {hasUnsavedChanges
                  ? "This team has local draft changes waiting to be saved."
                  : "This team is in sync with the session stored on the API."}
              </div>

              <Button onClick={handleSave} disabled={isPending || !hasUnsavedChanges}>
                <Save data-icon="inline-start" />
                {isPending ? "Saving..." : "Save team changes"}
              </Button>
            </>
          ) : (
            <div className="rounded-[1.2rem] border border-dashed border-border/70 px-4 py-8 text-sm text-muted-foreground">
              No team is selected yet.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
