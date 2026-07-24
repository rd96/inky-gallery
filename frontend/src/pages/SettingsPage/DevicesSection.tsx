import { useEffect, useState } from 'react'
import { DeviceModelsApi } from '../../features/devices/api/deviceModelsApi'
import { MyDevicesApi } from '../../features/devices/api/myDevicesApi'
import type { DeviceModel, UserDevice } from '../../features/devices/types'
import { formatApiError } from '../../shared/api/ApiError'
import ColourSwatch from '../../shared/components/ColourSwatch'
import EditDeviceModal from './EditDeviceModal'
import RegisterDeviceForm from './RegisterDeviceForm'

export default function DevicesSection() {
  const [devices, setDevices] = useState<UserDevice[]>([])
  const [deviceModels, setDeviceModels] = useState<DeviceModel[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [registering, setRegistering] = useState(false)
  const [editingDevice, setEditingDevice] = useState<UserDevice | null>(null)
  const [togglingDeviceId, setTogglingDeviceId] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([MyDevicesApi.getMyDevices(), DeviceModelsApi.getDeviceModels()])
      .then(([devicesResult, deviceModelsResult]) => {
        setDevices(devicesResult)
        setDeviceModels(deviceModelsResult)
      })
      .catch(cause => setError(formatApiError(cause)))
      .finally(() => setLoading(false))
  }, [])

  async function refreshDevices() {
    try {
      setDevices(await MyDevicesApi.getMyDevices())
    } catch (cause) {
      setError(formatApiError(cause))
    }
  }

  async function toggleEnabled(device: UserDevice) {
    setTogglingDeviceId(device.deviceId)
    try {
      await MyDevicesApi.updateDevice(device.deviceId, { enabled: !device.enabled })
      await refreshDevices()
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setTogglingDeviceId(null)
    }
  }

  return (
    <section className="settings-section">
      <div className="settings-section-header">
        <h2>Devices</h2>
        <button
          type="button"
          className="admin-new-user-toggle"
          onClick={() => setRegistering(true)}
        >
          Register device
        </button>
      </div>

      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p>Loading…</p>
      ) : devices.length === 0 ? (
        <p className="device-model-empty">No devices registered yet.</p>
      ) : (
        <ul className="device-model-list">
          {devices.map(device => (
            <li key={device.deviceId} className="device-model-list-item">
              <div className="device-model-cell">
                <span className="device-model-name">{device.deviceNickname}</span>
                <span className="device-model-dimensions">
                  {device.modelName} · {device.widthPx} × {device.heightPx}px ·{' '}
                  {device.orientation === 'LANDSCAPE' ? 'Landscape' : 'Portrait'}
                </span>
              </div>
              <div className="user-list-item-actions">
                <ColourSwatch colours={device.colourSwatch ?? []} />
                <button
                  type="button"
                  className={`status-badge status-badge--pill ${device.enabled ? 'status-badge--enabled' : 'status-badge--disabled'}`}
                  onClick={() => void toggleEnabled(device)}
                  disabled={togglingDeviceId === device.deviceId}
                >
                  {togglingDeviceId === device.deviceId
                    ? 'Saving…'
                    : device.enabled
                      ? 'Enabled'
                      : 'Disabled'}
                </button>
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setEditingDevice(device)}
                >
                  Edit
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {registering && (
        <RegisterDeviceForm
          deviceModels={deviceModels}
          onClose={() => setRegistering(false)}
          onRegistered={() => {
            setRegistering(false)
            void refreshDevices()
          }}
        />
      )}

      {editingDevice && (
        <EditDeviceModal
          device={editingDevice}
          onClose={() => setEditingDevice(null)}
          onSaved={() => {
            setEditingDevice(null)
            void refreshDevices()
          }}
        />
      )}
    </section>
  )
}
