import { ApiError } from './ApiError.ts'

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const response = await fetch(path, {
    ...options,
    credentials: 'include',
    headers: {
      ...options.headers,
    },
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const message =
      typeof body?.error === 'string'
        ? body.error
        : `Request failed with status ${response.status}`

    throw new ApiError(response.status, message)
  }

  const text = await response.text()
  return (text === '' ? undefined : JSON.parse(text)) as T
}

export const ApiClient = {
  get<T>(path: string): Promise<T> {
    return request<T>(path)
  },

  post<TResponse, TBody = undefined>(
    path: string,
    body?: TBody,
  ): Promise<TResponse> {
    return request<TResponse>(path, {
      method: 'POST',
      headers:
        body === undefined ? {} : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  },

  delete<TResponse>(path: string): Promise<TResponse> {
    return request<TResponse>(path, { method: 'DELETE' })
  },

  put<TResponse, TBody>(path: string, body: TBody): Promise<TResponse> {
    return request<TResponse>(path, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },

  patch<TResponse, TBody>(path: string, body: TBody): Promise<TResponse> {
    return request<TResponse>(path, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },

  query<TResponse, TBody>(path: string, body: TBody): Promise<TResponse> {
    return request<TResponse>(path, {
      method: 'QUERY',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
  },
}