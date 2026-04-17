import { ReactNode } from "react";

import { MobileBlocker } from "@/components/layout/mobile-blocker";
import {
  Sidebar,
  SidebarInset,
  SidebarProvider,
  SidebarRail,
} from "@/components/ui/sidebar";
import { WorkspaceSidebar } from "@/components/workspace/workspace-sidebar";
import { WorkspaceTopbar } from "@/components/workspace/workspace-topbar";

type WorkspaceShellProps = {
  sessionId: string;
  children: ReactNode;
};

export function WorkspaceShell({ sessionId, children }: WorkspaceShellProps) {
  return (
    <>
      <MobileBlocker
        title="The editor shell is not available on phones"
        description="This workspace is intentionally tuned for larger canvases so session navigation, data density, and keyboard-driven actions stay reliable."
      />

      <div className="hidden md:block">
        <SidebarProvider defaultOpen>
          <Sidebar
            collapsible="icon"
            variant="inset"
            className="border-r border-sidebar-border/70 bg-[linear-gradient(180deg,rgba(15,15,15,0.98),rgba(8,8,8,0.96))]"
          >
            <WorkspaceSidebar sessionId={sessionId} />
          </Sidebar>
          <SidebarRail />
          <SidebarInset className="min-h-svh bg-transparent">
            <div className="flex min-h-svh flex-col">
              <WorkspaceTopbar sessionId={sessionId} />
              <div className="flex-1 px-5 py-6 lg:px-7">
                <div className="mx-auto w-full max-w-[1280px]">{children}</div>
              </div>
            </div>
          </SidebarInset>
        </SidebarProvider>
      </div>
    </>
  );
}
