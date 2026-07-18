import type { ReactNode } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import FullPageMessage from '../../shared/components/FullPageMessage'
import { useAuth } from './useAuth'

export function RequireAuth() {
  const { auth, refresh } = useAuth()

  switch (auth.status) {
    case 'loading':
      return null

    case 'error':
      return (
        <FullPageMessage
          heading="Unable to connect"
          message="Unable to connect to the server."
          actionLabel="Try again"
          onAction={() => void refresh()}
        />
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
