import { NextRequest, NextResponse } from "next/server"

import { getBrasfootApiBaseUrl } from "@/lib/env"

export const dynamic = "force-dynamic"

const FORWARDED_HEADERS = [
  "cache-control",
  "content-disposition",
  "content-length",
  "content-type",
  "expires",
  "pragma",
] as const

async function proxyRequest(
  request: NextRequest,
  { params }: { params: Promise<{ path: string[] }> }
) {
  const { path } = await params
  const requestUrl = new URL(request.url)
  const targetUrl = new URL(
    `${getBrasfootApiBaseUrl()}/${path.map(encodeURIComponent).join("/")}`
  )

  targetUrl.search = requestUrl.search

  const headers = new Headers()
  const contentType = request.headers.get("content-type")
  const accept = request.headers.get("accept")

  if (contentType) {
    headers.set("content-type", contentType)
  }

  if (accept) {
    headers.set("accept", accept)
  }

  let body: BodyInit | undefined

  if (request.method !== "GET" && request.method !== "HEAD") {
    body = await request.arrayBuffer()
  }

  try {
    const response = await fetch(targetUrl, {
      method: request.method,
      headers,
      body,
      cache: "no-store",
      redirect: "manual",
    })

    const responseHeaders = new Headers()

    for (const headerName of FORWARDED_HEADERS) {
      const headerValue = response.headers.get(headerName)
      if (headerValue) {
        responseHeaders.set(headerName, headerValue)
      }
    }

    return new NextResponse(response.body, {
      status: response.status,
      headers: responseHeaders,
    })
  } catch (error) {
    return NextResponse.json(
      {
        error:
          error instanceof Error && error.message
            ? error.message
            : "The frontend could not reach the Brasfoot API.",
      },
      { status: 502 }
    )
  }
}

export const GET = proxyRequest
export const PATCH = proxyRequest
export const POST = proxyRequest
