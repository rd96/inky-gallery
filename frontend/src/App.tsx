import { Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import ActivatePage from './pages/ActivatePage'
import GalleryPage from './pages/GalleryPage'
import DrawPage from './pages/DrawPage'
import AdminPage from './pages/AdminPage'
import DeviceModelsPage from './pages/DeviceModelsPage'
import SettingsPage from './pages/SettingsPage'
import UserDetailPage from './pages/UserDetailPage'
import AdminLayout from './shared/components/AdminLayout'
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
          <Route path="/settings" element={<SettingsPage />} />
          <Route element={<RequireAdmin />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin" element={<AdminPage />} />
              <Route path="/admin/device-models" element={<DeviceModelsPage />} />
            </Route>
            <Route path="/admin/users/:userId" element={<UserDetailPage />} />
          </Route>
        </Route>
      </Route>
    </Routes>
  )
}
