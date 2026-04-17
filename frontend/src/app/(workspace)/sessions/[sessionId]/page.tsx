import { WorkspaceEmptyState } from "@/components/workspace/workspace-empty-state";
import {
  getManagersServer,
  getTeamsServer,
} from "@/lib/api/brasfoot-server";

type SessionPageProps = {
  params: Promise<{
    sessionId: string;
  }>;
};

export default async function SessionPage({ params }: SessionPageProps) {
  const { sessionId } = await params;
  const [teams, managers] = await Promise.all([
    getTeamsServer(sessionId),
    getManagersServer(sessionId),
  ]);

  return (
    <WorkspaceEmptyState
      sessionId={sessionId}
      teams={teams}
      managers={managers}
    />
  );
}
