import { Link } from 'react-router-dom'
import ConnectionsSection from './ConnectionsSection'
import DeleteAccountSection from './DeleteAccountSection'
import DevicesSection from './DevicesSection'
import ProfileSection from './ProfileSection'
import '../AdminPage/AdminPage.css'
import './SettingsPage.css'

export default function SettingsPage() {
  return (
    <div className="settings-page">
      <Link to="/" className="settings-back">
        ← Back to gallery
      </Link>
      <h1>Settings</h1>
      <ProfileSection />
      <DevicesSection />
      <ConnectionsSection />
      <DeleteAccountSection />
    </div>
  )
}
