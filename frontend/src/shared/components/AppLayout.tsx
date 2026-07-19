import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../features/auth/useAuth'
import './AppLayout.css'

export default function AppLayout() {
  const { auth, logout } = useAuth()
  const isAdmin = auth.status === 'authenticated' && auth.user.role === 'ADMIN'

  return (
    <div className="app-layout">
      <header className="app-nav">
        <span className="app-nav-brand">Inky Gallery</span>
        <nav>
          <NavLink to="/" end>
            Gallery
          </NavLink>
          {isAdmin && <NavLink to="/admin">Admin</NavLink>}
        </nav>
        <button type="button" className="app-nav-logout" onClick={() => void logout()}>
          Log out
        </button>
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  )
}
