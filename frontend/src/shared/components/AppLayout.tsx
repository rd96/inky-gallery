import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../features/auth/useAuth'
import './AppLayout.css'

export default function AppLayout() {
  const { auth } = useAuth()
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
      </header>
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  )
}
