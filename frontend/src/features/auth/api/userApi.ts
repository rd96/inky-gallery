import { ApiError } from '../../../shared/api/ApiError'
import { ApiClient } from '../../../shared/api/ApiClient'
import type { User } from '../types'

type LoginRequest = {
  username: string
  password: string
}

async function getCurrentUser(): Promise<User | null> {
  try {
    return await ApiClient.get<User>('/api/auth/me')
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      return null
    }

    throw error
  }
}

async function login(request: LoginRequest): Promise<User> {
  await ApiClient.post<void, LoginRequest>(
    '/api/auth/login',
    request,
  )

  const user = await getCurrentUser()

  if (user === null) {
    throw new Error(
      'Login succeeded but no authenticated user was returned',
    )
  }

  return user
}

export const UserApi = {
  getCurrentUser,
  login,
}