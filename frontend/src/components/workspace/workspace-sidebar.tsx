"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  ActivityIcon,
  ChevronRightIcon,
  DownloadIcon,
  FolderKanbanIcon,
  KeyboardIcon,
  ShieldCheckIcon,
  ShieldEllipsisIcon,
  UsersIcon,
  UserSquare2Icon,
} from "lucide-react";

import {
  getWorkspaceSectionFromPathname,
  workspaceDownloadPath,
  workspaceHomePath,
  workspaceManagersPath,
  workspacePlayersPath,
  workspaceTeamsPath,
} from "@/lib/routes";
import { Badge } from "@/components/ui/badge";
import {
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupAction,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenuBadge,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarSeparator,
} from "@/components/ui/sidebar";

type WorkspaceSidebarProps = {
  sessionId: string;
};

export function WorkspaceSidebar({ sessionId }: WorkspaceSidebarProps) {
  const pathname = usePathname();
  const currentSection = getWorkspaceSectionFromPathname(pathname);
  const workspaceDestinations = [
    {
      title: "Overview",
      section: "overview",
      icon: FolderKanbanIcon,
      href: workspaceHomePath(sessionId),
    },
    {
      title: "Teams",
      section: "teams",
      icon: UsersIcon,
      href: workspaceTeamsPath(sessionId),
    },
    {
      title: "Players",
      section: "players",
      icon: UserSquare2Icon,
      href: workspacePlayersPath(sessionId),
    },
    {
      title: "Managers",
      section: "managers",
      icon: ShieldEllipsisIcon,
      href: workspaceManagersPath(sessionId),
    },
  ] as const;

  return (
    <>
      <SidebarHeader className="gap-4 px-4 py-5">
        <div className="panel-surface panel-elevated rounded-[1.6rem] border border-sidebar-border/70 p-4">
          <div className="flex items-start gap-3">
            <div className="flex size-11 items-center justify-center rounded-2xl bg-primary/12 text-primary">
              <ShieldCheckIcon className="size-5" />
            </div>
            <div className="space-y-1">
              <p className="text-sm font-semibold tracking-tight">
                Session workspace
              </p>
              <p className="text-xs leading-5 text-sidebar-foreground/70">
                Structural navigation on the left, analytical content on the right.
              </p>
            </div>
          </div>
          <div className="mt-4 flex flex-wrap gap-2">
            <Badge variant="success">Session live</Badge>
            <Badge variant="outline">Desktop workflow</Badge>
          </div>
          <div className="mt-4 grid grid-cols-2 gap-2">
            <div className="rounded-xl border border-sidebar-border/70 bg-card/70 px-3 py-2">
              <p className="text-[0.65rem] font-semibold uppercase tracking-[0.14em] text-sidebar-foreground/55">
                Session
              </p>
              <p className="mt-1 truncate text-sm font-semibold text-sidebar-foreground">
                {sessionId}
              </p>
            </div>
            <div className="rounded-xl border border-sidebar-border/70 bg-card/70 px-3 py-2">
              <p className="text-[0.65rem] font-semibold uppercase tracking-[0.14em] text-sidebar-foreground/55">
                Status
              </p>
              <p className="mt-1 text-sm font-semibold text-primary">Live shell</p>
            </div>
          </div>
        </div>
      </SidebarHeader>

      <SidebarSeparator />

      <SidebarContent>
        <SidebarGroup className="px-3 py-4">
          <SidebarGroupLabel className="px-2 text-[0.7rem] font-semibold uppercase tracking-[0.16em] text-sidebar-foreground/55">
            Navigation
          </SidebarGroupLabel>
          <SidebarGroupAction aria-label="Keyboard shortcut">
            <KeyboardIcon />
          </SidebarGroupAction>
          <SidebarGroupContent>
            <SidebarMenu className="gap-1">
              {workspaceDestinations.map((destination) => {
                const Icon = destination.icon;
                const isActive = destination.section === currentSection;

                return (
                  <SidebarMenuItem key={destination.title}>
                    <SidebarMenuButton
                      isActive={isActive}
                      variant={isActive ? "outline" : "default"}
                      render={
                        <Link href={destination.href} />
                      }
                      tooltip={destination.title}
                      className="h-12 rounded-2xl px-3 text-sm font-medium"
                    >
                      {isActive ? (
                        <>
                          <Icon className="size-[18px]" />
                          <span>{destination.title}</span>
                          <SidebarMenuBadge className="right-8 rounded-full border border-primary/20 bg-primary/10 px-2 text-[0.62rem] uppercase tracking-[0.12em] text-primary">
                            Live
                          </SidebarMenuBadge>
                          <ChevronRightIcon className="ml-auto size-4 opacity-60" />
                        </>
                      ) : (
                        <span className="flex w-full items-center gap-2 text-sidebar-foreground/72">
                          <Icon className="size-[18px]" />
                          <span>{destination.title}</span>
                          <span className="ml-auto rounded-full border border-sidebar-border/80 bg-background/75 px-2 py-0.5 text-[0.65rem] font-semibold uppercase tracking-[0.14em] text-sidebar-foreground/55">
                            Open
                          </span>
                        </span>
                      )}
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                );
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup className="px-3 pb-4">
          <SidebarGroupLabel className="px-2 text-[0.7rem] font-semibold uppercase tracking-[0.16em] text-sidebar-foreground/55">
            Session posture
          </SidebarGroupLabel>
          <SidebarGroupContent className="space-y-2 px-2">
            <div className="rounded-[1.15rem] border border-sidebar-border/70 bg-white/[0.02] px-3 py-3">
              <div className="flex items-center gap-2 text-sm font-semibold text-sidebar-foreground">
                <ActivityIcon className="size-4 text-primary" />
                Stable shell
              </div>
              <p className="mt-2 text-xs leading-5 text-sidebar-foreground/70">
                Breadcrumbs, status chips, and command surfaces stay exposed.
              </p>
            </div>
            <div className="rounded-[1.15rem] border border-sidebar-border/70 bg-white/[0.02] px-3 py-3">
              <div className="flex items-center gap-2 text-sm font-semibold text-sidebar-foreground">
                <DownloadIcon className="size-4 text-primary" />
                Save export ready
              </div>
              <p className="mt-2 text-xs leading-5 text-sidebar-foreground/70">
                Download the edited `.s22` file at any time from the top bar or
                directly via <a href={workspaceDownloadPath(sessionId)} className="text-primary underline underline-offset-4">export</a>.
              </p>
            </div>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter className="px-4 pb-5 pt-2">
        <div className="rounded-[1.25rem] border border-sidebar-border/70 bg-background/60 px-3 py-3 text-xs leading-5 text-sidebar-foreground/72">
          Session <span className="font-semibold text-sidebar-foreground">{sessionId}</span> is active.
          The left rail acts like the concrete circulation core of the app: fixed,
          readable, and always trustworthy while the feature wings expand.
        </div>
      </SidebarFooter>
    </>
  );
}
