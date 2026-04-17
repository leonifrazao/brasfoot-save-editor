import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

export function SessionShellLoading() {
  return (
    <div className="space-y-6">
      <Card className="border border-border/70 bg-card/75 shadow-sm">
        <CardHeader className="gap-3 border-b border-border/70 pb-5">
          <Skeleton className="h-4 w-32" />
          <Skeleton className="h-6 w-64" />
          <Skeleton className="h-4 w-80 max-w-full" />
        </CardHeader>
      </Card>

      <Card className="border border-border/70 bg-card/85 shadow-sm">
        <CardHeader className="gap-3 border-b border-border/70 pb-5">
          <Skeleton className="h-4 w-28" />
          <Skeleton className="h-9 w-80 max-w-full" />
          <Skeleton className="h-4 w-full max-w-2xl" />
        </CardHeader>
        <CardContent className="grid gap-4 py-6 md:grid-cols-2">
          <Skeleton className="h-36 w-full rounded-2xl" />
          <Skeleton className="h-36 w-full rounded-2xl" />
        </CardContent>
      </Card>
    </div>
  );
}
