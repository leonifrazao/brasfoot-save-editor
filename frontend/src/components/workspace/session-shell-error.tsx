"use client";

import { AlertCircleIcon } from "lucide-react";

import {
  Alert,
  AlertAction,
  AlertDescription,
  AlertTitle,
} from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

type SessionShellErrorProps = {
  onRetry: () => void;
};

export function SessionShellError({ onRetry }: SessionShellErrorProps) {
  return (
    <Card className="border border-border/70 bg-card/90 shadow-sm">
      <CardHeader className="gap-2 border-b border-border/70 pb-5">
        <p className="text-sm font-semibold uppercase tracking-[0.16em] text-destructive">
          Session support state
        </p>
        <CardTitle className="text-2xl leading-tight">
          We couldn’t open this session
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-5 py-6">
        <Alert variant="destructive" className="border-destructive/30 bg-destructive/6">
          <AlertCircleIcon className="size-4" />
          <AlertTitle>Recoverable session error</AlertTitle>
          <AlertDescription>
            We couldn’t open this session. Try again from the upload screen. If
            the file is valid and the problem continues, upload it again to
            start a fresh session.
          </AlertDescription>
          <AlertAction>
            <Button variant="destructive" onClick={onRetry}>
              Try again
            </Button>
          </AlertAction>
        </Alert>
      </CardContent>
    </Card>
  );
}
