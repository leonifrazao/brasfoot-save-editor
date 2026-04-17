"use client";

import { useEffect } from "react";

import { SessionShellError } from "@/components/workspace/session-shell-error";

type SessionErrorProps = {
  error: Error & { digest?: string };
  reset: () => void;
};

export default function Error({ error, reset }: SessionErrorProps) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return <SessionShellError onRetry={reset} />;
}
