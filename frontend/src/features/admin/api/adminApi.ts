import { ApiClient } from '../../../shared/api/ApiClient'
import type { ActivationStatus, AdminUser, UserConnections } from '../types'
import type { Role } from '../../auth/types'

export const ADMIN_USER_PAGE_SIZE = 50

type UserResponseDTO = {
  userId: string
  username: string
  displayName: string
  role: AdminUser['role']
  activationStatus: AdminUser['activationStatus']
  enabled: boolean
  createdAt: string
}

type QueryUsersRequest = {
  nameSearch?: string
  role?: Role
  activationStatus?: ActivationStatus
  enabled?: boolean
  page: number
}

type QueryUsersResponse = {
  users: UserResponseDTO[]
  totalCount: number
}

export type UserSearchResult = {
  users: AdminUser[]
  totalCount: number
}

export type SearchUsersParams = {
  nameSearch: string
  role?: Role
  activationStatus?: ActivationStatus
  enabled?: boolean
  page: number
}

function toAdminUser(user: UserResponseDTO): AdminUser {
  return {
    id: user.userId,
    username: user.username,
    displayName: user.displayName,
    role: user.role,
    activationStatus: user.activationStatus,
    enabled: user.enabled,
    createdAt: user.createdAt,
  }
}

async function searchUsers(params: SearchUsersParams): Promise<UserSearchResult> {
  const response = await ApiClient.query<QueryUsersResponse, QueryUsersRequest>(
    '/api/admin/users',
    {
      nameSearch:
        params.nameSearch.trim() === '' ? undefined : params.nameSearch.trim(),
      role: params.role,
      activationStatus: params.activationStatus,
      enabled: params.enabled,
      page: params.page,
    },
  )

  return {
    users: response.users.map(toAdminUser),
    totalCount: response.totalCount,
  }
}

async function getUser(id: string): Promise<AdminUser> {
  const response = await ApiClient.get<UserResponseDTO>(`/api/admin/users/${id}`)
  return toAdminUser(response)
}

type CreateUserRequest = {
  username: string
  displayName: string
  role: Role
}

export type CreateUserResult = {
  userId: string
  activationToken: string
  expiresAt: string
}

function createUser(request: CreateUserRequest): Promise<CreateUserResult> {
  return ApiClient.post<CreateUserResult, CreateUserRequest>(
    '/api/admin/users',
    request,
  )
}

export type UpdateUserRequest = {
  username?: string
  displayName?: string
  enabled?: boolean
  role?: Role
}

function updateUser(id: string, patch: UpdateUserRequest): Promise<void> {
  return ApiClient.put<void, UpdateUserRequest>(`/api/admin/users/${id}`, patch)
}

export type CreateActivationTokenResult = {
  activationToken: string
}

function createActivationToken(id: string): Promise<CreateActivationTokenResult> {
  return ApiClient.post<CreateActivationTokenResult>(
    `/api/admin/users/${id}/activation-tokens`,
  )
}

function getUserConnections(id: string): Promise<UserConnections> {
  return ApiClient.get<UserConnections>(`/api/admin/users/${id}/connections`)
}

type CreateConnectionRequest = {
  senderUserId: string
  recipientUserId: string
}

function createConnection(request: CreateConnectionRequest): Promise<void> {
  return ApiClient.post<void, CreateConnectionRequest>('/api/admin/connections', request)
}

function deleteConnection(connectionId: string): Promise<void> {
  return ApiClient.delete<void>(`/api/admin/connections/${connectionId}`)
}

export const AdminApi = {
  searchUsers,
  getUser,
  createUser,
  updateUser,
  createActivationToken,
  getUserConnections,
  createConnection,
  deleteConnection,
}
