export type WorkspaceSection = "overview" | "teams" | "players" | "managers"

export function workspaceHomePath(sessionId: string) {
  return `/sessions/${sessionId}`
}

export function workspaceTeamsPath(sessionId: string) {
  return `${workspaceHomePath(sessionId)}/teams`
}

export function workspacePlayersPath(sessionId: string) {
  return `${workspaceHomePath(sessionId)}/players`
}

export function workspaceManagersPath(sessionId: string) {
  return `${workspaceHomePath(sessionId)}/managers`
}

export function workspaceDownloadPath(sessionId: string) {
  return `/api/brasfoot/api/v1/sessions/${sessionId}/download`
}

export function getWorkspaceSectionFromPathname(
  pathname: string
): WorkspaceSection {
  if (pathname.endsWith("/teams")) {
    return "teams"
  }

  if (pathname.endsWith("/players")) {
    return "players"
  }

  if (pathname.endsWith("/managers")) {
    return "managers"
  }

  return "overview"
}

export function getWorkspaceSectionLabel(section: WorkspaceSection) {
  switch (section) {
    case "teams":
      return "Teams"
    case "players":
      return "Players"
    case "managers":
      return "Managers"
    default:
      return "Overview"
  }
}

export function rootPath() {
  return "/"
}
