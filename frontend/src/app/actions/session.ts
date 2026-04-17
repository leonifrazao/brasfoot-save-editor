'use server'

import { redirect } from "next/navigation"

import { uploadSessionSave } from "@/lib/api/session"
import { workspaceHomePath } from "@/lib/routes"

export type SessionUploadActionResult = {
  error: string | null
}

export const initialSessionUploadActionResult: SessionUploadActionResult = {
  error: null,
}

export async function createSessionAction(
  _previousState: SessionUploadActionResult,
  formData: FormData
): Promise<SessionUploadActionResult> {
  const file = formData.get("saveFile")

  if (!(file instanceof File) || file.size === 0) {
    return {
      error: "Select a Brasfoot save file to open an editing session.",
    }
  }

  let sessionId: string

  try {
    sessionId = await uploadSessionSave(file)
  } catch (error) {
    return {
      error:
        error instanceof Error && error.message
          ? error.message
          : "We couldn’t open this session. Try again from the upload screen. If the file is valid and the problem continues, upload it again to start a fresh session.",
    }
  }

  redirect(workspaceHomePath(sessionId))
}
