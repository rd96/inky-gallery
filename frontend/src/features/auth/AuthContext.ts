import { createContext } from 'react'
import type { AuthState, User } from './types'

export type AuthContextValue = {
  auth: AuthState
  completeLogin: (user: User) => void
  refresh: () => Promise<void>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined,
)