export type Role = 'ADMIN' | 'USER'

export type User = {
  userId: string
  username: string
  displayName: string
  role: Role
}

export type AuthState =
  | { status: 'loading' }
  | { status: 'authenticated'; user: User }
  | { status: 'unauthenticated' }
  | { status: 'error'; error: Error }