import type {
  Manager,
  ManagerUpdatePayload,
  Player,
  PlayerUpdatePayload,
  Team,
  TeamUpdatePayload,
} from "@/lib/api/brasfoot-types"

type ApiErrorResponse = {
  detail?: unknown
  message?: unknown
  error?: unknown
  title?: unknown
}

type BatchResult<T> = {
  index: number
  success: boolean
  data: T | null
  error: string | null
}

type BatchResponse<T> = {
  results: BatchResult<T>[]
}

const API_PREFIX = "/api/brasfoot"

function getErrorMessage(payload: ApiErrorResponse) {
  const candidate =
    payload.detail ?? payload.message ?? payload.error ?? payload.title

  return typeof candidate === "string" && candidate.trim().length > 0
    ? candidate
    : null
}

async function readErrorMessage(response: Response) {
  const contentType = response.headers.get("content-type") ?? ""

  if (contentType.includes("application/json")) {
    const payload = (await response.json()) as ApiErrorResponse
    return (
      getErrorMessage(payload) ??
      "The Brasfoot API rejected this request without a detailed message."
    )
  }

  const text = await response.text()
  return text.trim().length > 0
    ? text
    : "The Brasfoot API rejected this request without a detailed message."
}

async function brasfootFetch<T>(path: string, init?: RequestInit) {
  const response = await fetch(`${API_PREFIX}${path}`, {
    ...init,
    cache: "no-store",
    headers: {
      Accept: "application/json",
      ...(init?.headers ?? {}),
    },
  })

  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }

  return (await response.json()) as T
}

export function getSessionDownloadUrl(sessionId: string) {
  return `${API_PREFIX}/api/v1/sessions/${sessionId}/download`
}

export function getTeams(sessionId: string) {
  return brasfootFetch<Team[]>(`/api/v1/sessions/${sessionId}/teams`)
}

export function updateTeam(
  sessionId: string,
  teamId: number,
  payload: TeamUpdatePayload
) {
  return brasfootFetch<Team>(`/api/v1/sessions/${sessionId}/teams/${teamId}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })
}

export function batchUpdateTeams(
  sessionId: string,
  payload: Array<{ teamId: number } & TeamUpdatePayload>
) {
  return brasfootFetch<BatchResponse<Team>>(`/api/v1/sessions/${sessionId}/teams/batch`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })
}

export function getPlayers(sessionId: string, teamId: number) {
  return brasfootFetch<Player[]>(
    `/api/v1/sessions/${sessionId}/teams/${teamId}/players`
  )
}

export function updatePlayer(
  sessionId: string,
  teamId: number,
  playerId: number,
  payload: PlayerUpdatePayload
) {
  return brasfootFetch<Player>(
    `/api/v1/sessions/${sessionId}/teams/${teamId}/players/${playerId}`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  )
}

export function batchUpdatePlayers(
  sessionId: string,
  teamId: number,
  payload: Array<{ playerId: number } & PlayerUpdatePayload>
) {
  return brasfootFetch<BatchResponse<Player>>(
    `/api/v1/sessions/${sessionId}/teams/${teamId}/players/batch`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  )
}

export function getManagers(sessionId: string) {
  return brasfootFetch<Manager[]>(`/api/v1/sessions/${sessionId}/managers`)
}

export function updateManager(
  sessionId: string,
  managerId: number,
  payload: ManagerUpdatePayload
) {
  return brasfootFetch<Manager>(
    `/api/v1/sessions/${sessionId}/managers/${managerId}`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  )
}

export function batchUpdateManagers(
  sessionId: string,
  payload: Array<{ managerId: number } & ManagerUpdatePayload>
) {
  return brasfootFetch<BatchResponse<Manager>>(
    `/api/v1/sessions/${sessionId}/managers/batch`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    }
  )
}
