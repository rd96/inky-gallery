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

  post<TResponse, TBody>(
    path: string,
    body: TBody,
  ): Promise<TResponse> {
    return request<TResponse>(path, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    })
  },
}