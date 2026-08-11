import { useEffect, useState, type SubmitEvent } from 'react'
import { useAuth } from '../../features/auth/useAuth'
import { DeviceModelsApi } from '../../features/devices/api/deviceModelsApi'
import OrientationPicker from '../../features/devices/OrientationPicker'
import type { DeviceModel, Orientation } from '../../features/devices/types'
import { CanvasesApi } from '../../features/drawings/api/canvasesApi'
import { markDraftCreatedOnThisDevice } from '../../features/drawings/draftStorage'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'

type NewDraftModalProps = {
  onClose: () => void
  onCreated: (canvasId: string) => void
}

export default function NewDraftModal({ onClose, onCreated }: NewDraftModalProps) {
  const { auth } = useAuth()
  // NewDraftModal only ever renders inside RequireAuth, so this is always
  // populated in practice; the fallback just satisfies the type checker.
  const userId = auth.status === 'authenticated' ? auth.user.userId : ''

  const [deviceModels, setDeviceModels] = useState<DeviceModel[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [deviceModelId, setDeviceModelId] = useState('')
  const [orientation, setOrientation] = useState<Orientation>('LANDSCAPE')
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    DeviceModelsApi.getDeviceModels()
      .then(models => {
        if (cancelled) return
        setDeviceModels(models)
        setDeviceModelId(models[0]?.deviceModelId ?? '')
      })
      .catch(cause => {
        if (!cancelled) setLoadError(formatApiError(cause))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEscapeKey(onClose)

  const selectedModel = deviceModels.find(model => model.deviceModelId === deviceModelId)

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!deviceModelId) return

    setSubmitting(true)
    setSubmitError(null)

    try {
      const { canvasId } = await CanvasesApi.createCanvas({
        targetDeviceModelId: deviceModelId,
        orientation,
        canvasType: 'SINGLE',
      })
      markDraftCreatedOnThisDevice(userId, canvasId)
      onCreated(canvasId)
    } catch (cause) {
      setSubmitError(formatApiError(cause))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="admin-modal-backdrop" onClick={onClose}>
      <form
        className="modal-card"
        onSubmit={handleSubmit}
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Start new drawing"
      >
        <h2>Start new drawing</h2>

        {loading && <p>Loading device models…</p>}
        {loadError && (
          <p className="admin-error" role="alert">
            {loadError}
          </p>
        )}

        {!loading && !loadError && (
          <div className="modal-fields">
            <label>
              Device
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
        )}

        {submitError && (
          <p className="admin-error" role="alert">
            {submitError}
          </p>
        )}

        <div className="modal-actions">
          <button
            type="submit"
            className="btn-primary"
            disabled={submitting || loading || !!loadError || deviceModels.length === 0}
          >
            {submitting ? 'Starting…' : 'Start drawing'}
          </button>
          <button type="button" className="btn-secondary" onClick={onClose} disabled={submitting}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
