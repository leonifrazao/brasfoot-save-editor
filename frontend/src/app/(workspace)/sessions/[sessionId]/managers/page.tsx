import { ManagersWorkspace } from "@/components/workspace/managers-workspace"
import {
  getManagersServer,
  getTeamsServer,
} from "@/lib/api/brasfoot-server"

type ManagersPageProps = {
  params: Promise<{
    sessionId: string
  }>
}

export default async function ManagersPage({ params }: ManagersPageProps) {
  const { sessionId } = await params
  const [teams, managers] = await Promise.all([
    getTeamsServer(sessionId),
    getManagersServer(sessionId),
  ])

  return (
    <ManagersWorkspace
      sessionId={sessionId}
      initialManagers={managers}
      teams={teams}
    />
  )
}
