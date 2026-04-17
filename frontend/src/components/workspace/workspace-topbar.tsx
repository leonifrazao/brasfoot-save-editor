"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { BadgeCheck, Clock3, Download, HomeIcon, Layers3 } from "lucide-react";

import { WorkspaceCommandPalette } from "@/components/workspace/workspace-command-palette";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Separator } from "@/components/ui/separator";
import { SidebarTrigger } from "@/components/ui/sidebar";
import {
  getWorkspaceSectionFromPathname,
  getWorkspaceSectionLabel,
  workspaceDownloadPath,
  workspaceHomePath,
} from "@/lib/routes";

type WorkspaceTopbarProps = {
  sessionId: string;
};

export function WorkspaceTopbar({ sessionId }: WorkspaceTopbarProps) {
  const pathname = usePathname();
  const currentSection = getWorkspaceSectionFromPathname(pathname);
  const currentSectionLabel = getWorkspaceSectionLabel(currentSection);

  return (
    <header className="sticky top-0 z-10 border-b border-border/70 bg-background/86 backdrop-blur-xl">
      <div className="mx-auto flex w-full max-w-[1280px] flex-col gap-4 px-5 py-4 lg:px-7">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              <SidebarTrigger className="lg:hidden" />
              <Badge variant="default">
                <BadgeCheck className="size-3.5" aria-hidden="true" />
                Active editor shell
              </Badge>
              <Badge variant="outline">
                <Clock3 className="size-3.5" aria-hidden="true" />
                Session loaded for review
              </Badge>
              <Badge variant="secondary">
                <Layers3 className="size-3.5" aria-hidden="true" />
                Desktop and tablet first
              </Badge>
            </div>
            <Breadcrumb>
              <BreadcrumbList>
                <BreadcrumbItem>
                  <BreadcrumbLink render={<Link href="/" />} className="font-medium text-muted-foreground">
                    Home
                  </BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                  <BreadcrumbLink
                    render={<Link href={workspaceHomePath(sessionId)} />}
                    className="font-medium text-primary"
                  >
                    Workspace
                  </BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator />
                <BreadcrumbItem>
                  <BreadcrumbPage>{currentSectionLabel}</BreadcrumbPage>
                </BreadcrumbItem>
              </BreadcrumbList>
            </Breadcrumb>
            <div className="space-y-1">
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">
                Session command deck
              </p>
              <p className="text-[clamp(1.4rem,2.2vw,2rem)] font-semibold tracking-[-0.03em] text-foreground">
                Workspace / {currentSectionLabel}
              </p>
              <p className="max-w-2xl text-sm leading-6 text-muted-foreground">
                Session {sessionId} stays active across overview, teams,
                players, and managers. The primary actions remain visible while
                the editing surface changes.
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <WorkspaceCommandPalette sessionId={sessionId} />
            <Button
              variant="outline"
              size="sm"
              nativeButton={false}
              render={<Link href={workspaceHomePath(sessionId)} />}
            >
              <HomeIcon data-icon="inline-start" />
              Overview
            </Button>
            <Button
              variant="secondary"
              size="sm"
              nativeButton={false}
              render={<a href={workspaceDownloadPath(sessionId)} />}
            >
              <Download data-icon="inline-start" />
              Download save
            </Button>
            <div className="flex items-center gap-3 rounded-full border border-primary/20 bg-primary/8 px-4 py-2 text-sm text-primary shadow-[0_14px_30px_-24px_rgba(23,104,255,0.7)]">
              <span className="size-2 rounded-full bg-primary" />
              <span className="font-semibold">Active session</span>
            </div>
          </div>
        </div>
        <Separator />
      </div>
    </header>
  );
}
