import { NextResponse } from "next/server"

import { uploadSessionSave } from "@/lib/api/session"

export async function POST(request: Request) {
  const formData = await request.formData()
  const file = formData.get("saveFile")

  if (!(file instanceof File) || file.size === 0) {
    return NextResponse.json(
      {
        error: "Select a Brasfoot save file to open an editing session.",
      },
      { status: 400 }
    )
  }

  try {
    const sessionId = await uploadSessionSave(file)

    return NextResponse.json({
      sessionId,
    })
  } catch (error) {
    return NextResponse.json(
      {
        error:
          error instanceof Error && error.message
            ? error.message
            : "We couldn’t open this session. Try again from the upload screen. If the file is valid and the problem continues, upload it again to start a fresh session.",
      },
      { status: 502 }
    )
  }
}
