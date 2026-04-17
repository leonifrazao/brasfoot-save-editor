import { TeamsWorkspace } from "@/components/workspace/teams-workspace"
import { getTeamsServer } from "@/lib/api/brasfoot-server"

type TeamsPageProps = {
  params: Promise<{
    sessionId: string
  }>
}

export default async function TeamsPage({ params }: TeamsPageProps) {
  const { sessionId } = await params
  const teams = await getTeamsServer(sessionId)

  return <TeamsWorkspace sessionId={sessionId} initialTeams={teams} />
}
