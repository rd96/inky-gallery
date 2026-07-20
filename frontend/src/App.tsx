import { Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import ActivatePage from './pages/ActivatePage'
import GalleryPage from './pages/GalleryPage'
import DrawPage from './pages/DrawPage'
import AdminPage from './pages/AdminPage'
import AppLayout from './shared/components/AppLayout'
import {
  RedirectIfAuthenticated,
  RequireAdmin,
  RequireAuth,
} from './features/auth/RouteGuards'

export default function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <RedirectIfAuthenticated>
            <LoginPage />
          </RedirectIfAuthenticated>
        }
      />
      <Route path="/activate" element={<ActivatePage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<GalleryPage />} />
          <Route path="/draw" element={<DrawPage />} />
          <Route element={<RequireAdmin />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>
        </Route>
      </Route>
    </Routes>
  )
}
