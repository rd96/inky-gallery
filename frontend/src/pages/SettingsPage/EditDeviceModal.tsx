import { useState, type SubmitEvent } from 'react'
import { MyDevicesApi } from '../../features/devices/api/myDevicesApi'
import type { Orientation, UserDevice } from '../../features/devices/types'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'
import OrientationPicker from '../../features/devices/OrientationPicker'

type EditDeviceModalProps = {
  device: UserDevice
  onClose: () => void
  onSaved: () => void
}

export default function EditDeviceModal({ device, onClose, onSaved }: EditDeviceModalProps) {
  const [deviceNickname, setDeviceNickname] = useState(device.deviceNickname)
  const [orientation, setOrientation] = useState<Orientation>(device.orientation)
  const [enabled, setEnabled] = useState(device.enabled)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isDirty =
    deviceNickname !== device.deviceNickname ||
    orientation !== device.orientation ||
    enabled !== device.enabled

  // device.widthPx/heightPx are already swapped to match the current orientation,
  // so unswap using the current orientation to recover the model's landscape shape.
  const landscapeWidthPx = device.orientation === 'LANDSCAPE' ? device.widthPx : device.heightPx
  const landscapeHeightPx = device.orientation === 'LANDSCAPE' ? device.heightPx : device.widthPx

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
      await MyDevicesApi.updateDevice(device.deviceId, {
        deviceNickname,
        orientation,
        enabled,
      })

      onSaved()
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
        aria-label="Edit device"
      >
        <h2>Edit device</h2>
        <div className="modal-fields">
          <label>
            Nickname
            <input
              value={deviceNickname}
              onChange={e => setDeviceNickname(e.target.value)}
              maxLength={50}
              autoFocus
              required
            />
          </label>
          {landscapeWidthPx !== landscapeHeightPx && (
            <div className="orientation-field">
              <OrientationPicker
                value={orientation}
                onChange={setOrientation}
                landscapeWidthPx={landscapeWidthPx}
                landscapeHeightPx={landscapeHeightPx}
              />
            </div>
          )}
          <label className="settings-checkbox-label">
            <input
              type="checkbox"
              checked={enabled}
              onChange={e => setEnabled(e.target.checked)}
            />
            Enabled
          </label>
        </div>
        {error && (
          <p className="admin-error" role="alert">
            {error}
          </p>
        )}
        <div className="modal-actions">
          <button type="submit" className="btn-primary" disabled={submitting || !isDirty}>
            {submitting ? 'Saving…' : 'Save'}
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
