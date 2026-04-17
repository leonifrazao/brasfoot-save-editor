const DEFAULT_BRASFOOT_API_BASE_URL = "http://localhost:8080"

export function getBrasfootApiBaseUrl() {
  return process.env.BRASFOOT_API_BASE_URL ?? DEFAULT_BRASFOOT_API_BASE_URL
}
