import { WorkspaceShell } from "@/components/workspace/workspace-shell";

type SessionLayoutProps = {
  children: React.ReactNode;
  params: Promise<{
    sessionId: string;
  }>;
};

export default async function SessionLayout({
  children,
  params,
}: SessionLayoutProps) {
  const { sessionId } = await params;

  return <WorkspaceShell sessionId={sessionId}>{children}</WorkspaceShell>;
}
