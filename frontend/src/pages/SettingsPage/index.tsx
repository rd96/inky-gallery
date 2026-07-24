import DeleteAccountSection from './DeleteAccountSection'
import DevicesSection from './DevicesSection'
import ProfileSection from './ProfileSection'
import '../AdminPage/AdminPage.css'
import './SettingsPage.css'

export default function SettingsPage() {
  return (
    <div className="settings-page">
      <h1>Settings</h1>
      <ProfileSection />
      <DevicesSection />
      <DeleteAccountSection />
    </div>
  )
}
