import {
  type PropsWithChildren,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react'
import { UserApi } from './api/userApi'
import { AuthContext, type AuthContextValue } from './AuthContext'
import type { AuthState, User } from './types'

export function AuthProvider({ children }: PropsWithChildren) {
  const [auth, setAuth] = useState<AuthState>({
    status: 'loading',
  })

  const refresh = useCallback(async () => {
    setAuth({ status: 'loading' })

    try {
      const user = await UserApi.getCurrentUser()

      setAuth(
        user === null
          ? { status: 'unauthenticated' }
          : { status: 'authenticated', user },
      )
    } catch (cause) {
      const error =
        cause instanceof Error
          ? cause
          : new Error('An unexpected authentication error occurred')

      setAuth({ status: 'error', error })
    }
  }, [])

  const completeLogin = useCallback((user: User) => {
    setAuth({ status: 'authenticated', user })
  }, [])

  const logout = useCallback(async () => {
    await UserApi.logout()
    setAuth({ status: 'unauthenticated' })
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const value = useMemo<AuthContextValue>(
    () => ({
      auth,
      completeLogin,
      refresh,
      logout,
    }),
    [auth, completeLogin, refresh, logout],
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}