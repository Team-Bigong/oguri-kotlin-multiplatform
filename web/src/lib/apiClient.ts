import { webEnvironment } from "../config/env"

const ADMIN_ACCESS_TOKEN_STORAGE_KEY = "oguri_admin_access_token"
const CONTENT_TYPE_HEADER_NAME = "Content-Type"
const AUTHORIZATION_HEADER_NAME = "Authorization"

const buildRequestUrl = (path: string): string => {
  const normalizedBase = webEnvironment.apiBaseUrl.replace(/\/$/, "")
  const normalizedPath = path.startsWith("/") ? path : `/${path}`
  return `${normalizedBase}${normalizedPath}`
}

export const getAdminAccessToken = (): string => {
  return sessionStorage.getItem(ADMIN_ACCESS_TOKEN_STORAGE_KEY) ?? ""
}

export const setAdminAccessToken = (accessToken: string): void => {
  sessionStorage.setItem(ADMIN_ACCESS_TOKEN_STORAGE_KEY, accessToken)
}

export const clearAdminAccessToken = (): void => {
  sessionStorage.removeItem(ADMIN_ACCESS_TOKEN_STORAGE_KEY)
}

const request = async <TResponse>(
  path: string,
  options: RequestInit = {},
  includeAccessToken: boolean = true
): Promise<TResponse> => {
  const adminAccessToken = getAdminAccessToken()
  const requestHeaders: Record<string, string> = {
    [CONTENT_TYPE_HEADER_NAME]: "application/json",
    ...(options.headers as Record<string, string> | undefined)
  }
  if (includeAccessToken && adminAccessToken.length > 0) {
    requestHeaders[AUTHORIZATION_HEADER_NAME] = `Bearer ${adminAccessToken}`
  }

  const response = await fetch(buildRequestUrl(path), {
    ...options,
    headers: requestHeaders
  })

  if (!response.ok) {
    const errorBody = await response.text()
    throw new Error(`API 요청 실패: ${response.status} ${errorBody}`)
  }

  if (response.status === 204) {
    return undefined as TResponse
  }

  return response.json() as Promise<TResponse>
}

export const adminApiClient = {
  get: <TResponse>(path: string): Promise<TResponse> => request<TResponse>(path),
  postPublic: <TResponse>(path: string, body: unknown): Promise<TResponse> =>
    request<TResponse>(path, { method: "POST", body: JSON.stringify(body) }, false),
  post: <TResponse>(path: string, body: unknown): Promise<TResponse> =>
    request<TResponse>(path, { method: "POST", body: JSON.stringify(body) }),
  put: <TResponse>(path: string, body: unknown): Promise<TResponse> =>
    request<TResponse>(path, { method: "PUT", body: JSON.stringify(body) }),
  delete: <TResponse>(path: string): Promise<TResponse> =>
    request<TResponse>(path, { method: "DELETE" })
}
