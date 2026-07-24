import { NavLink, Outlet } from 'react-router-dom'
import './AdminLayout.css'

export default function AdminLayout() {
  return (
    <div className="admin-layout">
      <nav className="admin-tabs">
        <NavLink to="/admin" end>
          Users
        </NavLink>
        <NavLink to="/admin/device-models">Device models</NavLink>
      </nav>
      <Outlet />
    </div>
  )
}
