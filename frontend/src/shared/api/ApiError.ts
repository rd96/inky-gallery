export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export function formatApiError(cause: unknown): string {
  return cause instanceof ApiError
    ? cause.message
    : 'Something went wrong. Please try again.'
}