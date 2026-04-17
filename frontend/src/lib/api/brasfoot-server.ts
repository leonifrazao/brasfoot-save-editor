import "server-only"

import { getBrasfootApiBaseUrl } from "@/lib/env"
import type { Manager, Player, Team } from "@/lib/api/brasfoot-types"

type ApiErrorResponse = {
  detail?: unknown
  message?: unknown
  error?: unknown
  title?: unknown
}

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

async function brasfootServerFetch<T>(path: string) {
  const response = await fetch(`${getBrasfootApiBaseUrl()}${path}`, {
    cache: "no-store",
    headers: {
      Accept: "application/json",
    },
  })

  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }

  return (await response.json()) as T
}

export function getTeamsServer(sessionId: string) {
  return brasfootServerFetch<Team[]>(`/api/v1/sessions/${sessionId}/teams`)
}

export function getManagersServer(sessionId: string) {
  return brasfootServerFetch<Manager[]>(
    `/api/v1/sessions/${sessionId}/managers`
  )
}

export function getPlayersServer(sessionId: string, teamId: number) {
  return brasfootServerFetch<Player[]>(
    `/api/v1/sessions/${sessionId}/teams/${teamId}/players`
  )
}
