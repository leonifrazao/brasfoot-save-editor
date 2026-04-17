import type { Metadata } from "next"

import { Toaster } from "@/components/ui/sonner"
import "./globals.css"

export const metadata: Metadata = {
  title: {
    default: "Brasfoot Save Editor",
    template: "%s | Brasfoot Save Editor",
  },
  description:
    "Dark desktop-first Brasfoot save editing workspace built with Next.js and shadcn/ui.",
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html
      lang="en"
      className="h-full dark antialiased"
      suppressHydrationWarning
    >
      <body className="min-h-full bg-background font-sans text-foreground">
        {children}
        <Toaster position="top-right" richColors />
      </body>
    </html>
  )
}
