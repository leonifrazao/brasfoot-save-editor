export type Team = {
  id: number
  name: string
  money: number
  reputation: number
}

export type Player = {
  id: number
  name: string
  age: number
  overall: number
  position: number
  energy: number
  morale: number
  starLocal: boolean
  starGlobal: boolean
}

export type Manager = {
  id: number
  name: string
  isHuman: boolean
  teamId: number
  confidenceBoard: number
  confidenceFans: number
}

export type TeamUpdatePayload = {
  money?: number
  reputation?: number
}

export type PlayerUpdatePayload = {
  age?: number
  overall?: number
  position?: number
  energy?: number
  morale?: number
  starLocal?: boolean
  starGlobal?: boolean
}

export type ManagerUpdatePayload = {
  name?: string
  confidenceBoard?: number
  confidenceFans?: number
}

export const TEAM_REPUTATION_OPTIONS = [
  { value: 0, label: "Municipal" },
  { value: 1, label: "Estadual" },
  { value: 2, label: "Regional" },
  { value: 3, label: "Nacional" },
  { value: 4, label: "Continental" },
  { value: 5, label: "Mundial" },
] as const

export function formatCurrency(value: number) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
    maximumFractionDigits: 0,
  }).format(value)
}

export function getTeamReputationLabel(value: number) {
  return (
    TEAM_REPUTATION_OPTIONS.find((option) => option.value === value)?.label ??
    `Nivel ${value}`
  )
}
