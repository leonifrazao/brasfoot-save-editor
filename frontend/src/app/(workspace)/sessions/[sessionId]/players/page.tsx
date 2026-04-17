import { PlayersWorkspace } from "@/components/workspace/players-workspace"
import { getTeamsServer } from "@/lib/api/brasfoot-server"

type PlayersPageProps = {
  params: Promise<{
    sessionId: string
  }>
}

export default async function PlayersPage({ params }: PlayersPageProps) {
  const { sessionId } = await params
  const teams = await getTeamsServer(sessionId)

  return <PlayersWorkspace sessionId={sessionId} initialTeams={teams} />
}
