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

type ActivationDetails = {
  username: string
  displayName: string
  expiresAt: string
}

function getActivationDetails(activationToken: string): Promise<ActivationDetails> {
  return ApiClient.query<ActivationDetails, { activationToken: string }>(
    '/api/auth/activate',
    { activationToken },
  )
}

type ActivateRequest = {
  activationToken: string
  password: string
}

async function activate(request: ActivateRequest): Promise<User> {
  await ApiClient.post<void, ActivateRequest>('/api/auth/activate', request)

  const user = await getCurrentUser()

  if (user === null) {
    throw new Error(
      'Activation succeeded but no authenticated user was returned',
    )
  }

  return user
}

function logout(): Promise<void> {
  return ApiClient.post<void>('/api/auth/logout')
}

type PasswordResetDetails = {
  username: string
  displayName: string
  expiresAt: string
}

function getPasswordResetDetails(passwordResetToken: string): Promise<PasswordResetDetails> {
  return ApiClient.query<PasswordResetDetails, { passwordResetToken: string }>(
    '/api/auth/reset',
    { passwordResetToken },
  )
}

type ResetPasswordRequest = {
  passwordResetToken: string
  password: string
}

async function resetPassword(request: ResetPasswordRequest): Promise<User> {
  await ApiClient.post<void, ResetPasswordRequest>('/api/auth/reset', request)

  const user = await getCurrentUser()

  if (user === null) {
    throw new Error(
      'Password reset succeeded but no authenticated user was returned',
    )
  }

  return user
}

export const UserApi = {
  getCurrentUser,
  login,
  logout,
  getActivationDetails,
  activate,
  getPasswordResetDetails,
  resetPassword,
}