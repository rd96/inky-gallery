import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../features/auth/useAuth'
import './AppLayout.css'

export default function AppLayout() {
  const { auth, logout } = useAuth()
  const isAdmin = auth.status === 'authenticated' && auth.user.role === 'ADMIN'
  const displayName = auth.status === 'authenticated' ? auth.user.displayName : ''

  return (
    <div className="app-layout">
      <header className="app-nav">
        <NavLink to="/" end className="app-nav-brand">
          Inky Gallery
        </NavLink>
        <nav>{isAdmin && <NavLink to="/admin">Admin</NavLink>}</nav>
        <div className="app-nav-actions">
          <span className="app-nav-user">{displayName}</span>
          <span className="app-nav-divider" aria-hidden="true" />
          <NavLink to="/settings">Settings</NavLink>
          <span className="app-nav-divider" aria-hidden="true" />
          <button type="button" className="app-nav-logout" onClick={() => void logout()}>
            Log out
          </button>
        </div>
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  )
}
