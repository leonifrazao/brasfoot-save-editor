"use client"

import { useEffect, useMemo, useState, useTransition } from "react"
import { Layers3, Save, Search, Shield, Users2 } from "lucide-react"
import { toast } from "sonner"

import type { Player, Team } from "@/lib/api/brasfoot-types"
import {
  batchUpdatePlayers,
  getPlayers,
  updatePlayer,
} from "@/lib/api/brasfoot-client"
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

type PlayerDraft = {
  age: string
  overall: string
  position: string
  energy: string
  morale: string
  starLocal: boolean
  starGlobal: boolean
}

type PlayerBulkDraft = {
  age: string
  overall: string
  position: string
  energy: string
  morale: string
  starLocal: string
  starGlobal: string
}

type StoredPlayersState = {
  selectedTeamId: number | null
  selectedPlayerIdByTeamId: Record<string, number | null>
  draftsByPlayerKey: Record<string, PlayerDraft>
}

type PlayersWorkspaceProps = {
  sessionId: string
  initialTeams: Team[]
}

function readStoredState(sessionId: string): StoredPlayersState | null {
  if (typeof window === "undefined") {
    return null
  }

  const storedValue = window.localStorage.getItem(getStorageKey(sessionId))
  if (!storedValue) {
    return null
  }

  try {
    return JSON.parse(storedValue) as StoredPlayersState
  } catch {
    window.localStorage.removeItem(getStorageKey(sessionId))
    return null
  }
}

function getStorageKey(sessionId: string) {
  return `brasfoot-save-editor:${sessionId}:players`
}

function makeDraft(player: Player): PlayerDraft {
  return {
    age: String(player.age),
    overall: String(player.overall),
    position: String(player.position),
    energy: String(player.energy),
    morale: String(player.morale),
    starLocal: player.starLocal,
    starGlobal: player.starGlobal,
  }
}

function getPlayerKey(teamId: number, playerId: number) {
  return `${teamId}:${playerId}`
}

export function PlayersWorkspace({
  sessionId,
  initialTeams,
}: PlayersWorkspaceProps) {
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(
    initialTeams[0]?.id ?? null
  )
  const [playersByTeamId, setPlayersByTeamId] = useState<Record<number, Player[]>>(
    {}
  )
  const [loadedTeamIds, setLoadedTeamIds] = useState<Record<number, true>>({})
  const [selectedPlayerIdByTeamId, setSelectedPlayerIdByTeamId] = useState<
    Record<string, number | null>
  >({})
  const [draftsByPlayerKey, setDraftsByPlayerKey] = useState<
    Record<string, PlayerDraft>
  >({})
  const [teamFilter, setTeamFilter] = useState("")
  const [playerFilter, setPlayerFilter] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [bulkError, setBulkError] = useState<string | null>(null)
  const [bulkDraft, setBulkDraft] = useState<PlayerBulkDraft>({
    age: "",
    overall: "",
    position: "",
    energy: "",
    morale: "",
    starLocal: "",
    starGlobal: "",
  })
  const [isPending, startTransition] = useTransition()

  const filteredTeams = useMemo(() => {
    const normalized = teamFilter.trim().toLowerCase()

    if (!normalized) {
      return initialTeams
    }

    return initialTeams.filter((team) =>
      `${team.name} ${team.id}`.toLowerCase().includes(normalized)
    )
  }, [initialTeams, teamFilter])

  const activeTeam =
    initialTeams.find((team) => team.id === selectedTeamId) ??
    filteredTeams[0] ??
    null

  const teamPlayers = useMemo(
    () => (activeTeam ? playersByTeamId[activeTeam.id] ?? [] : []),
    [activeTeam, playersByTeamId]
  )

  const filteredPlayers = useMemo(() => {
    const normalized = playerFilter.trim().toLowerCase()

    if (!normalized) {
      return teamPlayers
    }

    return teamPlayers.filter((player) =>
      `${player.name} ${player.id}`.toLowerCase().includes(normalized)
    )
  }, [playerFilter, teamPlayers])

  const activePlayerId = activeTeam
    ? selectedPlayerIdByTeamId[String(activeTeam.id)] ?? null
    : null

  const activePlayer =
    filteredPlayers.find((player) => player.id === activePlayerId) ??
    teamPlayers.find((player) => player.id === activePlayerId) ??
    filteredPlayers[0] ??
    teamPlayers[0] ??
    null

  const currentDraft =
    activeTeam && activePlayer
      ? draftsByPlayerKey[getPlayerKey(activeTeam.id, activePlayer.id)] ??
        makeDraft(activePlayer)
      : null

  const hasUnsavedChanges =
    activePlayer && currentDraft
      ? currentDraft.age !== String(activePlayer.age) ||
        currentDraft.overall !== String(activePlayer.overall) ||
        currentDraft.position !== String(activePlayer.position) ||
        currentDraft.energy !== String(activePlayer.energy) ||
        currentDraft.morale !== String(activePlayer.morale) ||
        currentDraft.starLocal !== activePlayer.starLocal ||
        currentDraft.starGlobal !== activePlayer.starGlobal
      : false

  useEffect(() => {
    const storedState = readStoredState(sessionId)

    if (!storedState) {
      return
    }

    Promise.resolve().then(() => {
      setDraftsByPlayerKey(storedState.draftsByPlayerKey ?? {})
      setSelectedPlayerIdByTeamId(storedState.selectedPlayerIdByTeamId ?? {})

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

    const payload: StoredPlayersState = {
      selectedTeamId,
      selectedPlayerIdByTeamId,
      draftsByPlayerKey,
    }

    window.localStorage.setItem(getStorageKey(sessionId), JSON.stringify(payload))
  }, [draftsByPlayerKey, selectedPlayerIdByTeamId, selectedTeamId, sessionId])

  useEffect(() => {
    if (!activeTeam) {
      return
    }

    if (playersByTeamId[activeTeam.id]) {
      return
    }

    let cancelled = false

    getPlayers(sessionId, activeTeam.id)
      .then((players) => {
        if (cancelled) {
          return
        }

        setPlayersByTeamId((current) => ({
          ...current,
          [activeTeam.id]: players,
        }))
        setLoadedTeamIds((current) => ({
          ...current,
          [activeTeam.id]: true,
        }))
        setSelectedPlayerIdByTeamId((current) => ({
          ...current,
          [String(activeTeam.id)]:
            current[String(activeTeam.id)] ??
            players[0]?.id ??
            null,
        }))
      })
      .catch((loadError) => {
        if (cancelled) {
          return
        }

        setError(
          loadError instanceof Error && loadError.message
            ? loadError.message
            : "The players could not be loaded."
        )
      })

    return () => {
      cancelled = true
    }
  }, [activeTeam, playersByTeamId, sessionId])

  const isLoadingPlayers = activeTeam
    ? !loadedTeamIds[activeTeam.id] && !playersByTeamId[activeTeam.id]
    : false

  const hasBulkChanges = Object.values(bulkDraft).some((value) => value.trim() !== "")

  function updateDraft(patch: Partial<PlayerDraft>) {
    if (!activeTeam || !activePlayer) {
      return
    }

    const key = getPlayerKey(activeTeam.id, activePlayer.id)

    setDraftsByPlayerKey((current) => ({
      ...current,
      [key]: {
        ...(current[key] ?? makeDraft(activePlayer)),
        ...patch,
      },
    }))
  }

  function handleSave() {
    if (!activeTeam || !activePlayer || !currentDraft) {
      return
    }

    const age = Number(currentDraft.age)
    const overall = Number(currentDraft.overall)
    const position = Number(currentDraft.position)
    const energy = Number(currentDraft.energy)
    const morale = Number(currentDraft.morale)

    const numericValues = [age, overall, position, energy, morale]

    if (numericValues.some((value) => !Number.isFinite(value))) {
      setError("All player fields must contain valid numeric values.")
      return
    }

    setError(null)

    startTransition(async () => {
      try {
        const updatedPlayer = await updatePlayer(
          sessionId,
          activeTeam.id,
          activePlayer.id,
          {
            age,
            overall,
            position,
            energy,
            morale,
            starLocal: currentDraft.starLocal,
            starGlobal: currentDraft.starGlobal,
          }
        )

        setPlayersByTeamId((current) => ({
          ...current,
          [activeTeam.id]: (current[activeTeam.id] ?? []).map((player) =>
            player.id === updatedPlayer.id ? updatedPlayer : player
          ),
        }))
        setDraftsByPlayerKey((current) => ({
          ...current,
          [getPlayerKey(activeTeam.id, updatedPlayer.id)]: makeDraft(updatedPlayer),
        }))
        toast.success(`Player ${updatedPlayer.name} updated.`)
      } catch (saveError) {
        setError(
          saveError instanceof Error && saveError.message
            ? saveError.message
            : "The player update failed."
        )
      }
    })
  }

  function updateBulkDraft(patch: Partial<PlayerBulkDraft>) {
    setBulkDraft((current) => ({
      ...current,
      ...patch,
    }))
  }

  function parseOptionalNumber(value: string) {
    if (value.trim() === "") {
      return undefined
    }

    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : Number.NaN
  }

  function handleBulkSave() {
    if (!activeTeam || filteredPlayers.length === 0) {
      return
    }

    const payload = {
      age: parseOptionalNumber(bulkDraft.age),
      overall: parseOptionalNumber(bulkDraft.overall),
      position: parseOptionalNumber(bulkDraft.position),
      energy: parseOptionalNumber(bulkDraft.energy),
      morale: parseOptionalNumber(bulkDraft.morale),
      starLocal:
        bulkDraft.starLocal === ""
          ? undefined
          : bulkDraft.starLocal === "true",
      starGlobal:
        bulkDraft.starGlobal === ""
          ? undefined
          : bulkDraft.starGlobal === "true",
    }

    if (Object.values(payload).every((value) => value === undefined)) {
      setBulkError("Fill at least one field before running a bulk update.")
      return
    }

    if (Object.values(payload).some((value) => Number.isNaN(value))) {
      setBulkError("Bulk player fields must be valid numbers or left blank.")
      return
    }

    setBulkError(null)

    startTransition(async () => {
      try {
        const response = await batchUpdatePlayers(
          sessionId,
          activeTeam.id,
          filteredPlayers.map((player) => ({
            playerId: player.id,
            ...payload,
          }))
        )

        const updatedPlayers = response.results
          .filter((result) => result.success && result.data)
          .map((result) => result.data as Player)

        setPlayersByTeamId((current) => {
          const nextPlayers = [...(current[activeTeam.id] ?? [])]

          for (const updatedPlayer of updatedPlayers) {
            const index = nextPlayers.findIndex((player) => player.id === updatedPlayer.id)
            if (index >= 0) {
              nextPlayers[index] = updatedPlayer
            }
          }

          return {
            ...current,
            [activeTeam.id]: nextPlayers,
          }
        })

        setDraftsByPlayerKey((current) => {
          const nextDrafts = { ...current }

          for (const updatedPlayer of updatedPlayers) {
            nextDrafts[getPlayerKey(activeTeam.id, updatedPlayer.id)] =
              makeDraft(updatedPlayer)
          }

          return nextDrafts
        })

        const failed = response.results.filter((result) => !result.success).length
        setBulkDraft({
          age: "",
          overall: "",
          position: "",
          energy: "",
          morale: "",
          starLocal: "",
          starGlobal: "",
        })
        toast.success(
          failed === 0
            ? `${updatedPlayers.length} players updated.`
            : `${updatedPlayers.length} players updated, ${failed} failed.`
        )
      } catch (saveError) {
        setBulkError(
          saveError instanceof Error && saveError.message
            ? saveError.message
            : "The bulk player update failed."
        )
      }
    })
  }

  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,0.86fr)_minmax(0,1.05fr)_minmax(24rem,0.9fr)]">
      <Card variant="ghost" className="panel-surface">
        <CardHeader className="gap-4 border-b border-border/70 pb-5">
          <div className="flex items-center gap-2">
            <Users2 className="size-4 text-primary" />
            <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
              Teams
            </p>
          </div>
          <CardTitle className="text-xl">Choose a club</CardTitle>
          <Input
            value={teamFilter}
            onChange={(event) => setTeamFilter(event.target.value)}
            placeholder="Filter teams"
          />
        </CardHeader>
        <CardContent className="space-y-2 py-5">
          {filteredTeams.map((team) => (
            <button
              key={team.id}
              type="button"
              onClick={() => {
                setSelectedTeamId(team.id)
                setError(null)
              }}
              className={[
                "w-full rounded-[1.2rem] border px-4 py-3 text-left transition-colors",
                activeTeam?.id === team.id
                  ? "border-primary/30 bg-primary/10"
                  : "border-border/70 bg-background/70 hover:bg-white/[0.04]",
              ].join(" ")}
            >
              <div className="font-semibold text-foreground">{team.name}</div>
              <div className="mt-1 text-xs text-muted-foreground">
                Team #{team.id}
              </div>
            </button>
          ))}
        </CardContent>
      </Card>

      <Card variant="elevated">
        <CardHeader className="gap-4 border-b border-border/70 pb-5">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="success">
              {activeTeam ? activeTeam.name : "No team"}
            </Badge>
            <Badge variant="outline">{teamPlayers.length} players</Badge>
          </div>
          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <CardTitle className="text-2xl">Players workspace</CardTitle>
              <CardDescription className="mt-2 max-w-2xl text-sm leading-6">
                Load one squad at a time to keep the most data-dense editor
                readable.
              </CardDescription>
            </div>
            <div className="relative w-full max-w-sm">
              <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={playerFilter}
                onChange={(event) => setPlayerFilter(event.target.value)}
                placeholder="Search player"
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent className="py-5">
          {isLoadingPlayers ? (
            <div className="rounded-[1.2rem] border border-dashed border-border/70 px-4 py-8 text-sm text-muted-foreground">
              Loading players for the selected team...
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Player</TableHead>
                  <TableHead>Overall</TableHead>
                  <TableHead>Energy</TableHead>
                  <TableHead>Morale</TableHead>
                  <TableHead>Stars</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredPlayers.map((player) => (
                  <TableRow
                    key={player.id}
                    className={
                      activePlayer?.id === player.id ? "bg-white/[0.05]" : undefined
                    }
                  >
                    <TableCell>
                      <button
                        type="button"
                        onClick={() =>
                          activeTeam &&
                          setSelectedPlayerIdByTeamId((current) => ({
                            ...current,
                            [String(activeTeam.id)]: player.id,
                          }))
                        }
                        className="w-full text-left"
                      >
                        <div className="font-semibold text-foreground">
                          {player.name}
                        </div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          Player #{player.id}
                        </div>
                      </button>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {player.overall}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {player.energy}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {player.morale}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {[player.starLocal ? "L" : null, player.starGlobal ? "G" : null]
                        .filter(Boolean)
                        .join(" / ") || "-"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Card variant="ghost" className="panel-surface panel-elevated">
        <CardHeader className="gap-4 border-b border-border/70 pb-5">
          <div className="flex items-center gap-2">
            <Shield className="size-4 text-primary" />
            <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
              Player editor
            </p>
          </div>
          <CardTitle className="text-xl">
            {activePlayer ? activePlayer.name : "Select a player"}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-5 py-5">
          {activePlayer && currentDraft ? (
            <>
              <div className="rounded-[1.2rem] border border-primary/20 bg-primary/8 p-4">
                <div className="flex items-center gap-2">
                  <Layers3 className="size-4 text-primary" />
                  <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
                    Bulk changes
                  </p>
                </div>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Apply the fields below to all {filteredPlayers.length} players
                  currently visible for {activeTeam?.name}.
                </p>
                <div className="mt-4 grid gap-3 sm:grid-cols-2">
                  {[
                    ["Age", bulkDraft.age, "age"],
                    ["Overall", bulkDraft.overall, "overall"],
                    ["Position", bulkDraft.position, "position"],
                    ["Energy", bulkDraft.energy, "energy"],
                    ["Morale", bulkDraft.morale, "morale"],
                  ].map(([label, value, key]) => (
                    <div key={String(key)} className="space-y-2">
                      <label className="text-sm font-medium text-foreground">
                        {label}
                      </label>
                      <Input
                        type="number"
                        placeholder="Leave blank to keep current values"
                        value={String(value)}
                        onChange={(event) =>
                          updateBulkDraft({
                            [String(key)]: event.target.value,
                          } as Partial<PlayerBulkDraft>)
                        }
                      />
                    </div>
                  ))}
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      Local star
                    </label>
                    <select
                      value={bulkDraft.starLocal}
                      onChange={(event) =>
                        updateBulkDraft({ starLocal: event.target.value })
                      }
                      className="h-10 w-full rounded-xl border border-input bg-input/60 px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/30"
                    >
                      <option value="">Keep current values</option>
                      <option value="true">Set enabled</option>
                      <option value="false">Set disabled</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      Global star
                    </label>
                    <select
                      value={bulkDraft.starGlobal}
                      onChange={(event) =>
                        updateBulkDraft({ starGlobal: event.target.value })
                      }
                      className="h-10 w-full rounded-xl border border-input bg-input/60 px-3 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/30"
                    >
                      <option value="">Keep current values</option>
                      <option value="true">Set enabled</option>
                      <option value="false">Set disabled</option>
                    </select>
                  </div>
                </div>
                {bulkError ? (
                  <Alert variant="destructive" className="mt-4">
                    <AlertTitle>Bulk player update failed</AlertTitle>
                    <AlertDescription>{bulkError}</AlertDescription>
                  </Alert>
                ) : null}
                <div className="mt-4">
                  <Button
                    variant="secondary"
                    onClick={handleBulkSave}
                    disabled={isPending || !hasBulkChanges || filteredPlayers.length === 0}
                  >
                    <Layers3 data-icon="inline-start" />
                    {isPending ? "Applying..." : `Apply to ${filteredPlayers.length} players`}
                  </Button>
                </div>
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                {[
                  ["Age", currentDraft.age],
                  ["Overall", currentDraft.overall],
                  ["Position", currentDraft.position],
                  ["Energy", currentDraft.energy],
                  ["Morale", currentDraft.morale],
                ].map(([label, value]) => (
                  <div key={label} className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      {label}
                    </label>
                    <Input
                      type="number"
                      value={value}
                      onChange={(event) =>
                        updateDraft({
                          [label.toLowerCase()]: event.target.value,
                        } as Partial<PlayerDraft>)
                      }
                    />
                  </div>
                ))}
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <label className="flex items-center justify-between rounded-[1.1rem] border border-border/70 bg-background/72 px-4 py-3 text-sm text-foreground">
                  <span>Local star</span>
                  <input
                    type="checkbox"
                    checked={currentDraft.starLocal}
                    onChange={(event) =>
                      updateDraft({ starLocal: event.target.checked })
                    }
                    className="size-4 accent-[var(--primary)]"
                  />
                </label>
                <label className="flex items-center justify-between rounded-[1.1rem] border border-border/70 bg-background/72 px-4 py-3 text-sm text-foreground">
                  <span>Global star</span>
                  <input
                    type="checkbox"
                    checked={currentDraft.starGlobal}
                    onChange={(event) =>
                      updateDraft({ starGlobal: event.target.checked })
                    }
                    className="size-4 accent-[var(--primary)]"
                  />
                </label>
              </div>

              {error ? (
                <Alert variant="destructive">
                  <AlertTitle>Player update failed</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              ) : null}

              <div className="rounded-[1.2rem] border border-border/70 bg-background/72 p-4 text-sm text-muted-foreground">
                {hasUnsavedChanges
                  ? "This player has local draft edits waiting to be saved."
                  : "This player is in sync with the API session."}
              </div>

              <Button
                onClick={handleSave}
                disabled={isPending || !hasUnsavedChanges}
              >
                <Save data-icon="inline-start" />
                {isPending ? "Saving..." : "Save player changes"}
              </Button>
            </>
          ) : (
            <div className="rounded-[1.2rem] border border-dashed border-border/70 px-4 py-8 text-sm text-muted-foreground">
              Select a team and player to begin editing.
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
