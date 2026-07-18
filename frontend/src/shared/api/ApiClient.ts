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
    throw new ApiError(
      response.status,
      `Request failed with status ${response.status}`,
    )
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
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
}