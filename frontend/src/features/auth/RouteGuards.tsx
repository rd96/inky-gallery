import type { ReactNode } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './useAuth'

export function RequireAuth() {
  const { auth, refresh } = useAuth()

  switch (auth.status) {
    case 'loading':
      return null

    case 'error':
      return (
        <main>
          <p>Unable to connect to the server.</p>
          <button type="button" onClick={() => void refresh()}>
            Try again
          </button>
        </main>
      )

    case 'unauthenticated':
      return <Navigate to="/login" replace />

    case 'authenticated':
      return <Outlet />
  }
}

export function RequireAdmin() {
  const { auth } = useAuth()

  if (auth.status !== 'authenticated' || auth.user.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}

export function RedirectIfAuthenticated({ children }: { children: ReactNode }) {
  const { auth } = useAuth()

  if (auth.status === 'authenticated') {
    return <Navigate to="/" replace />
  }

  return children
}
