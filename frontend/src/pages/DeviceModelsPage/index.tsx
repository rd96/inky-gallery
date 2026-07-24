import { useCallback, useEffect, useState } from 'react'
import { DevicesApi } from '../../features/devices/api/devicesApi'
import type { DeviceModel } from '../../features/devices/types'
import CreateDeviceModelForm from './CreateDeviceModelForm'
import DeviceModelList from './DeviceModelList'
import '../AdminPage/AdminPage.css'
import './DeviceModelsPage.css'

export default function DeviceModelsPage() {
  const [deviceModels, setDeviceModels] = useState<DeviceModel[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)

  const loadDeviceModels = useCallback(async (options?: { silent?: boolean }) => {
    if (!options?.silent) setLoading(true)
    setError(null)

    try {
      const result = await DevicesApi.getDeviceModels()
      setDeviceModels(result)
    } catch {
      setError('Unable to load device models. Please try again.')
    } finally {
      if (!options?.silent) setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadDeviceModels()
  }, [loadDeviceModels])

  return (
    <div className="admin-page">
      <div className="device-models-toolbar">
        <h1>Device models</h1>
        <button
          type="button"
          className="admin-new-user-toggle"
          onClick={() => setCreating(true)}
        >
          New device model
        </button>
      </div>

      {error && (
        <p className="admin-error" role="alert">
          {error}
        </p>
      )}

      {loading ? <p>Loading…</p> : <DeviceModelList deviceModels={deviceModels} />}

      {creating && (
        <CreateDeviceModelForm
          onClose={() => setCreating(false)}
          onCreated={() => {
            setCreating(false)
            void loadDeviceModels({ silent: true })
          }}
        />
      )}
    </div>
  )
}
