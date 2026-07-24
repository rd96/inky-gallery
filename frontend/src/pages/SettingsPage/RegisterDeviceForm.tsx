import { useState, type SubmitEvent } from 'react'
import { MyDevicesApi } from '../../features/devices/api/myDevicesApi'
import type { DeviceModel, Orientation } from '../../features/devices/types'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'
import OrientationPicker from './OrientationPicker'

type RegisterDeviceFormProps = {
  deviceModels: DeviceModel[]
  onClose: () => void
  onRegistered: () => void
}

export default function RegisterDeviceForm({
  deviceModels,
  onClose,
  onRegistered,
}: RegisterDeviceFormProps) {
  const [deviceNickname, setDeviceNickname] = useState('')
  const [deviceModelId, setDeviceModelId] = useState(deviceModels[0]?.deviceModelId ?? '')
  const [orientation, setOrientation] = useState<Orientation>('LANDSCAPE')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isDirty = deviceNickname.trim() !== ''
  const selectedModel = deviceModels.find(model => model.deviceModelId === deviceModelId)

  function handleBackdropClick() {
    if (isDirty) return
    onClose()
  }

  useEscapeKey(handleBackdropClick)

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    try {
      await MyDevicesApi.registerDevice({
        deviceNickname,
        deviceModelId,
        orientation,
      })

      onRegistered()
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="admin-modal-backdrop" onClick={handleBackdropClick}>
      <form
        className="modal-card"
        onSubmit={handleSubmit}
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Register device"
      >
        <h2>Register device</h2>
        <div className="modal-fields">
          <label>
            Device model
            <select
              value={deviceModelId}
              onChange={e => setDeviceModelId(e.target.value)}
              autoFocus
              required
            >
              {deviceModels.map(model => (
                <option key={model.deviceModelId} value={model.deviceModelId}>
                  {model.deviceName}
                </option>
              ))}
            </select>
          </label>
          <label>
            Nickname
            <input
              value={deviceNickname}
              onChange={e => setDeviceNickname(e.target.value)}
              maxLength={50}
              required
            />
          </label>
          {selectedModel && selectedModel.landscapeWidthPx !== selectedModel.landscapeHeightPx && (
            <div className="orientation-field">
              <OrientationPicker
                value={orientation}
                onChange={setOrientation}
                landscapeWidthPx={selectedModel.landscapeWidthPx}
                landscapeHeightPx={selectedModel.landscapeHeightPx}
              />
            </div>
          )}
        </div>
        {error && (
          <p className="admin-error" role="alert">
            {error}
          </p>
        )}
        <div className="modal-actions">
          <button
            type="submit"
            className="btn-primary"
            disabled={submitting || deviceModels.length === 0}
          >
            {submitting ? 'Registering…' : 'Register'}
          </button>
          <button
            type="button"
            className="btn-secondary"
            onClick={onClose}
            disabled={submitting}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
