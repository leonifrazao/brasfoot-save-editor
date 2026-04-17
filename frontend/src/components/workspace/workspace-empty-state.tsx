import Link from "next/link";
import {
  ArrowRight,
  Download,
  FolderKanban,
  ShieldCheck,
  Sparkles,
  UserSquare2,
  Users2,
} from "lucide-react";

import type { Manager, Team } from "@/lib/api/brasfoot-types";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  formatCurrency,
  getTeamReputationLabel,
} from "@/lib/api/brasfoot-types";
import {
  workspaceDownloadPath,
  workspaceManagersPath,
  workspacePlayersPath,
  workspaceTeamsPath,
} from "@/lib/routes";

type WorkspaceEmptyStateProps = {
  sessionId: string;
  teams: Team[];
  managers: Manager[];
};

export function WorkspaceEmptyState({
  sessionId,
  teams,
  managers,
}: WorkspaceEmptyStateProps) {
  const totalMoney = teams.reduce((total, team) => total + team.money, 0);
  const humanManagers = managers.filter((manager) => manager.isHuman).length;
  const topTeams = [...teams]
    .sort((left, right) => right.money - left.money)
    .slice(0, 5);

  return (
    <div className="space-y-6">
      <div className="grid gap-4 xl:grid-cols-[minmax(0,1.7fr)_minmax(21rem,1fr)]">
        <Card variant="elevated" className="overflow-hidden">
          <CardHeader className="gap-5 border-b border-border/70 bg-[linear-gradient(180deg,rgba(255,255,255,0.05),rgba(255,255,255,0.01))] pb-6">
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">
              Session overview
            </p>
            <div className="space-y-3">
              <CardTitle className="text-[clamp(2rem,3.8vw,3.4rem)] leading-[1.02] tracking-[-0.04em]">
                Session ready. The shell is already doing useful work.
              </CardTitle>
              <CardDescription className="max-w-2xl text-base leading-7 text-muted-foreground">
                This dashboard is not decorative filler. It is the orientation
                layer between upload and editing, designed to hold route context,
                action readiness, and entity priorities on one calm surface.
              </CardDescription>
            </div>
            <div className="flex flex-wrap gap-2">
              <Badge variant="success">
                <ShieldCheck className="size-3.5" aria-hidden="true" />
                API session loaded
              </Badge>
              <Badge variant="outline">
                <FolderKanban className="size-3.5" aria-hidden="true" />
                Overview is live
              </Badge>
              <Badge variant="secondary">
                <Download className="size-3.5" aria-hidden="true" />
                Export is ready
              </Badge>
            </div>
          </CardHeader>
          <CardContent className="grid gap-4 py-6 lg:grid-cols-12">
            <div className="lg:col-span-7">
              <div className="grid gap-4 md:grid-cols-3">
                {[
                  {
                    label: "Session state",
                    value: "Active",
                    body: "The uploaded save is attached to a live editing workspace.",
                  },
                  {
                    label: "Teams loaded",
                    value: String(teams.length),
                    body: "Every team in the current save is available for review and editing.",
                  },
                  {
                    label: "Total market cash",
                    value: formatCurrency(totalMoney),
                    body: "A quick read of the money already exposed by the active save file.",
                  },
                  {
                    label: "Human managers",
                    value: String(humanManagers),
                    body: "Useful when you want to separate the playable profile from CPU staff.",
                  },
                ].map((metric) => (
                  <div
                    key={metric.label}
                    className="rounded-[1.35rem] border border-border/70 bg-background/72 p-4"
                  >
                    <p className="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                      {metric.label}
                    </p>
                    <p className="mt-3 text-2xl font-semibold tracking-[-0.03em] text-foreground">
                      {metric.value}
                    </p>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                      {metric.body}
                    </p>
                  </div>
                ))}
              </div>

              <div className="mt-4 rounded-[1.45rem] border border-border/70 bg-background/72 p-3">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Team</TableHead>
                      <TableHead>Money</TableHead>
                      <TableHead>Reputation</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {topTeams.map((team) => (
                      <TableRow key={team.id}>
                        <TableCell className="font-semibold text-foreground">
                          {team.name}
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {formatCurrency(team.money)}
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {getTeamReputationLabel(team.reputation)}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </div>

            <div className="space-y-4 lg:col-span-5">
              <div className="rounded-[1.45rem] border border-primary/20 bg-primary/8 p-5">
                <p className="text-sm font-semibold uppercase tracking-[0.16em] text-primary">
                  Session {sessionId}
                </p>
                <p className="mt-3 text-xl font-semibold tracking-[-0.03em] text-foreground">
                  Export and recovery
                </p>
                <p className="mt-3 text-sm leading-7 text-muted-foreground">
                  The modified save still lives in memory on the Java API. When
                  you finish the changes, export the current session directly to
                  a new `.s22` file.
                </p>
                <div className="mt-4">
                  <Button
                    nativeButton={false}
                    render={<a href={workspaceDownloadPath(sessionId)} />}
                  >
                    <Download data-icon="inline-start" />
                    Download current save
                  </Button>
                </div>
              </div>

              <div className="rounded-[1.45rem] border border-border/70 bg-background/72 p-5">
                <p className="text-sm font-semibold text-foreground">Next step</p>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  Move into the entity editors. Each surface now reads the live
                  session, keeps local draft state in the browser, and sends
                  updates back to the Java API.
                </p>
                <div className="mt-4 flex flex-wrap gap-2">
                  <Button
                    variant="outline"
                    nativeButton={false}
                    render={<Link href={workspaceTeamsPath(sessionId)} />}
                  >
                    Teams
                  </Button>
                  <Button
                    variant="outline"
                    nativeButton={false}
                    render={<Link href={workspacePlayersPath(sessionId)} />}
                  >
                    Players
                  </Button>
                  <Button
                    variant="outline"
                    nativeButton={false}
                    render={<Link href={workspaceManagersPath(sessionId)} />}
                  >
                    Managers
                  </Button>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card variant="ghost" className="panel-surface panel-elevated">
          <CardHeader className="gap-3 border-b border-border/70 pb-5">
            <CardTitle className="text-xl">What unlocks next</CardTitle>
            <CardDescription className="text-sm leading-6 text-muted-foreground">
              The shell is already framing the next destinations so the product
              feels continuous as editing modules arrive.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3 py-5">
            {[
              `${teams.length} teams are available for money and reputation edits.`,
              "Players can be filtered by team before attribute changes.",
              `${managers.length} managers are ready for confidence and naming updates.`,
            ].map((item) => (
              <div
                key={item}
                className="flex items-start gap-3 rounded-2xl border border-border/70 bg-card/68 px-4 py-3"
              >
                <ArrowRight className="mt-0.5 size-4 text-primary" aria-hidden="true" />
                <p className="text-sm leading-6 text-foreground">{item}</p>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        {[
          {
            title: "Overview first",
            body: "The dashboard now summarizes the active session instead of acting as decorative filler.",
            icon: FolderKanban,
          },
          {
            title: "Teams are editable",
            body: "Money and reputation changes can be applied directly from the workspace.",
            icon: Users2,
          },
          {
            title: "Players stay scoped",
            body: "The player editor keeps a team-first flow so the largest dataset remains legible.",
            icon: UserSquare2,
          },
          {
            title: "Managers are covered",
            body: "Name, board confidence, and fan confidence now flow through the same session contract.",
            icon: ShieldCheck,
          },
          {
            title: "Premium restraint",
            body: "The dark theme stays contrast-driven while the workspace shifts from shell to actual operations.",
            icon: Sparkles,
          },
        ].map(({ title, body, icon: Icon }) => (
          <Card key={title} variant="ghost" className="panel-surface">
            <CardContent className="space-y-3 py-5">
              <div className="flex size-11 items-center justify-center rounded-2xl bg-primary/12 text-primary">
                <Icon className="size-5" aria-hidden="true" />
              </div>
              <div>
                <p className="text-base font-semibold text-foreground">{title}</p>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">{body}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
