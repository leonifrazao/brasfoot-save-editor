import { getBrasfootApiBaseUrl } from "@/lib/env"

const SESSION_UPLOAD_TIMEOUT_MS = 60_000

type UploadSessionResponse = {
  id?: unknown
  sessionId?: unknown
  session?: {
    id?: unknown
  } | null
}

type ApiErrorResponse = {
  detail?: unknown
  message?: unknown
  error?: unknown
  title?: unknown
}

function getSessionId(payload: UploadSessionResponse) {
  const candidate = payload.id ?? payload.sessionId ?? payload.session?.id

  if (typeof candidate === "string" && candidate.length > 0) {
    return candidate
  }

  if (typeof candidate === "number" && Number.isFinite(candidate)) {
    return String(candidate)
  }

  throw new Error("Session upload succeeded but the response did not include a usable session id.")
}

export async function uploadSessionSave(file: File) {
  const formData = new FormData()
  formData.append("file", file, file.name)

  let response: Response

  try {
    response = await fetch(`${getBrasfootApiBaseUrl()}/api/v1/sessions`, {
      method: "POST",
      body: formData,
      cache: "no-store",
      signal: AbortSignal.timeout(SESSION_UPLOAD_TIMEOUT_MS),
    })
  } catch (error) {
    if (error instanceof Error && error.name === "TimeoutError") {
      throw new Error(
        "The upload took too long to reach the Brasfoot API. Check whether the backend is running and try again."
      )
    }

    if (error instanceof Error) {
      throw new Error(
        error.message || "We couldn't reach the Brasfoot API to create an editing session."
      )
    }

    throw new Error("We couldn't reach the Brasfoot API to create an editing session.")
  }

  if (!response.ok) {
    let message = "We couldn't create an editing session for that save file."

    const contentType = response.headers.get("content-type") ?? ""
    if (contentType.includes("application/json")) {
      const payload = (await response.json()) as ApiErrorResponse
      const candidate =
        payload.detail ?? payload.message ?? payload.error ?? payload.title

      if (typeof candidate === "string" && candidate.trim().length > 0) {
        message = candidate
      }
    } else {
      const text = await response.text()

      if (text.trim().length > 0) {
        message = text
      }
    }

    throw new Error(message)
  }

  const payload = (await response.json()) as UploadSessionResponse

  return getSessionId(payload)
}
