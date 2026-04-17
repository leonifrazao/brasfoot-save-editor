'use client'

import { useRef, useState } from "react"
import { AlertCircle, FileUp, LoaderCircle, ShieldCheck, TimerReset } from "lucide-react"

import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { workspaceHomePath } from "@/lib/routes"

const SUPPORTED_SAVE_EXTENSIONS = [".s22"] as const
const MAX_UPLOAD_SIZE_MB = 500
const MAX_UPLOAD_BYTES = MAX_UPLOAD_SIZE_MB * 1024 * 1024

export function SessionUploadForm() {
  const inputRef = useRef<HTMLInputElement>(null)
  const [fileName, setFileName] = useState<string | null>(null)
  const [isDragging, setIsDragging] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [clientError, setClientError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  function validateFile(file: File | null) {
    if (!file) {
      return null
    }

    if (file.size > MAX_UPLOAD_BYTES) {
      return `This save file is too large to upload here. Choose a file up to ${MAX_UPLOAD_SIZE_MB} MB.`
    }

    return null
  }

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const nextFile = event.target.files?.[0] ?? null
    const validationError = validateFile(nextFile)

    if (validationError) {
      event.currentTarget.value = ""
      setFileName(null)
      setClientError(validationError)
      setError(null)
      return
    }

    setFileName(nextFile?.name ?? null)
    setClientError(null)
    setError(null)
  }

  function handleDrop(event: React.DragEvent<HTMLLabelElement>) {
    event.preventDefault()
    setIsDragging(false)

    const droppedFile = event.dataTransfer.files?.[0]
    if (!droppedFile || !inputRef.current) {
      return
    }

    const validationError = validateFile(droppedFile)
    if (validationError) {
      inputRef.current.value = ""
      setFileName(null)
      setClientError(validationError)
      setError(null)
      return
    }

    const fileList = new DataTransfer()
    fileList.items.add(droppedFile)
    inputRef.current.files = fileList.files
    setFileName(droppedFile.name)
    setClientError(null)
    setError(null)
  }

  const showPending = isSubmitting
  const visibleError = clientError ?? error

  return (
    <Card variant="elevated" className="overflow-hidden rounded-[1.75rem]">
      <CardHeader className="gap-4 border-b border-border/70 bg-[linear-gradient(180deg,rgba(255,255,255,0.05),rgba(255,255,255,0.01))] px-6 py-6">
        <div className="flex items-center justify-between gap-3">
          <div className="inline-flex items-center gap-2 rounded-full border border-primary/15 bg-primary/8 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-primary">
            Session bootstrap
          </div>
          <div className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground">
            <ShieldCheck className="size-4 text-primary" aria-hidden="true" />
            Secure local upload flow
          </div>
        </div>
        <CardTitle className="text-2xl font-semibold text-foreground">
          Open your save file
        </CardTitle>
        <CardDescription className="max-w-xl text-sm leading-7 text-muted-foreground">
          Your save file opens into an editing session so you can review teams,
          players, and managers before downloading it again later.
        </CardDescription>
      </CardHeader>
      <CardContent className="px-6 py-6">
        <form
          className="space-y-5"
          onSubmit={async (event) => {
            event.preventDefault()

            const selectedFile = inputRef.current?.files?.[0] ?? null
            const validationError = validateFile(selectedFile)

            if (validationError) {
              setClientError(validationError)
              setError(null)
              return
            }

            setClientError(null)
            setError(null)
            setIsSubmitting(true)

            try {
              const formData = new FormData()
              formData.append("saveFile", selectedFile!)

              const response = await fetch("/api/session-upload", {
                method: "POST",
                body: formData,
              })

              const payload = (await response.json()) as {
                error?: string
                sessionId?: string
              }

              if (!response.ok || !payload.sessionId) {
                setError(
                  payload.error ??
                    "We couldn’t open this session. Try again from the upload screen."
                )
                return
              }

              window.location.assign(workspaceHomePath(payload.sessionId))
            } catch {
              setError(
                "We couldn’t open this session. Check whether the frontend and Brasfoot API are running, then try again."
              )
            } finally {
              setIsSubmitting(false)
            }
          }}
        >
            <label
            className={[
              "flex min-h-72 cursor-pointer flex-col items-center justify-center gap-5 rounded-[1.6rem] border border-dashed px-6 py-8 text-center transition-all duration-200 ease-[cubic-bezier(0.16,1,0.3,1)]",
              isDragging
                ? "border-primary bg-primary/8 shadow-[inset_0_0_0_1px_rgba(23,104,255,0.16)]"
                : "border-border/80 bg-background/85 hover:border-primary/60 hover:bg-white/[0.03]",
            ].join(" ")}
            onDragOver={(event) => {
              event.preventDefault()
              setIsDragging(true)
            }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={handleDrop}
          >
            <div className="flex size-16 items-center justify-center rounded-[1.35rem] bg-primary text-primary-foreground shadow-[0_18px_36px_-20px_rgba(31,107,92,0.7)]">
              <FileUp className="size-7" aria-hidden="true" />
            </div>
            <div className="space-y-2">
              <p className="text-lg font-semibold text-foreground">
                Drag a Brasfoot save here or browse from your device.
              </p>
              <p className="mx-auto max-w-md text-sm leading-6 text-muted-foreground">
                Open the current save into a controlled editor session with a
                visible route into overview, teams, players, and managers.
              </p>
            </div>
            <div className="flex flex-wrap items-center justify-center gap-2">
              <span className="rounded-full border border-border/70 bg-card px-3 py-1 text-xs font-medium text-muted-foreground">
                Save format: {SUPPORTED_SAVE_EXTENSIONS.map((extension) => `\`${extension}\``).join(", ")}
              </span>
              <span className="rounded-full border border-border/70 bg-card px-3 py-1 text-xs font-medium text-muted-foreground">
                Desktop and tablet ready
              </span>
            </div>
            <input
              ref={inputRef}
              type="file"
              name="saveFile"
              accept={`${SUPPORTED_SAVE_EXTENSIONS.join(",")},application/octet-stream`}
              className="h-11 w-full max-w-md cursor-pointer rounded-xl border border-border/70 bg-card px-3 py-2 text-sm text-foreground shadow-xs transition-colors file:mr-3 file:rounded-md file:border-0 file:bg-secondary file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-foreground focus-visible:border-ring focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
              onChange={handleFileChange}
              disabled={showPending}
            />
            <p className="text-sm text-muted-foreground">
              {fileName ? `Ready to upload: ${fileName}` : "No file selected yet."}
            </p>
          </label>

          {showPending ? (
            <div className="flex items-center gap-2 rounded-2xl border border-primary/15 bg-primary/8 px-4 py-3 text-sm text-foreground">
              <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
              <span>Opening your save file into an editing session...</span>
            </div>
          ) : null}

          {visibleError ? (
            <Alert variant="destructive" className="border-destructive/30">
              <AlertCircle aria-hidden="true" />
              <AlertTitle>We couldn’t open this session</AlertTitle>
              <AlertDescription>{visibleError}</AlertDescription>
            </Alert>
          ) : null}

          <div className="grid gap-3 rounded-[1.35rem] border border-border/70 bg-background/75 p-4">
            <div className="flex items-start gap-3">
              <ShieldCheck className="mt-0.5 size-4 text-primary" aria-hidden="true" />
              <p className="text-sm leading-6 text-muted-foreground">
                The upload starts a fresh editing session linked to this file.
                Keep your original save until you are ready to export again.
              </p>
            </div>
            <div className="flex items-start gap-3">
              <TimerReset className="mt-0.5 size-4 text-primary" aria-hidden="true" />
              <p className="text-sm leading-6 text-muted-foreground">
                If the file is invalid or the API is unavailable, the UI keeps
                the recovery path visible instead of failing silently.
              </p>
            </div>
          </div>

          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="max-w-xl text-xs font-medium uppercase tracking-[0.16em] text-muted-foreground">
              Single primary action. Clear next step.
            </p>
            <Button type="submit" size="lg" disabled={showPending} className="min-h-11 rounded-2xl px-5 shadow-[0_16px_34px_-20px_rgba(31,107,92,0.65)]">
              {showPending ? "Opening session..." : "Upload Save File"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}
