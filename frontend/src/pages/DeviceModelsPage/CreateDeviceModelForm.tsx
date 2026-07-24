import { useState, type SubmitEvent } from 'react'
import { AdminApi } from '../../features/admin/api/adminApi'
import { formatApiError } from '../../shared/api/ApiError'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'

type CreateDeviceModelFormProps = {
  onClose: () => void
  onCreated: () => void
}

export default function CreateDeviceModelForm({
  onClose,
  onCreated,
}: CreateDeviceModelFormProps) {
  const [deviceName, setDeviceName] = useState('')
  const [landscapeWidthPx, setLandscapeWidthPx] = useState('')
  const [landscapeHeightPx, setLandscapeHeightPx] = useState('')
  const [colourSwatch, setColourSwatch] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isDirty =
    deviceName.trim() !== '' ||
    landscapeWidthPx !== '' ||
    landscapeHeightPx !== '' ||
    colourSwatch.length > 0

  function handleBackdropClick() {
    if (isDirty) return
    onClose()
  }

  useEscapeKey(handleBackdropClick)

  function addColour() {
    setColourSwatch(swatch => [...swatch, '#000000'])
  }

  function updateColour(index: number, value: string) {
    setColourSwatch(swatch => swatch.map((colour, i) => (i === index ? value : colour)))
  }

  function removeColour(index: number) {
    setColourSwatch(swatch => swatch.filter((_, i) => i !== index))
  }

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    try {
      await AdminApi.createDeviceModel({
        deviceName,
        landscapeWidthPx: Number(landscapeWidthPx),
        landscapeHeightPx: Number(landscapeHeightPx),
        colourSwatch: colourSwatch.length > 0 ? colourSwatch : undefined,
      })

      onCreated()
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
        aria-label="New device model"
      >
        <h2>New device model</h2>
        <div className="modal-fields">
          <label>
            Name
            <input
              value={deviceName}
              onChange={e => setDeviceName(e.target.value)}
              autoFocus
              required
            />
          </label>
          <div className="device-model-dimensions-field">
            <span>Landscape dimensions (px)</span>
            <div className="device-model-dimensions-inputs">
              <input
                type="number"
                min={100}
                max={10000}
                value={landscapeWidthPx}
                onChange={e => setLandscapeWidthPx(e.target.value)}
                aria-label="Width"
                placeholder="Width"
                required
              />
              <span aria-hidden="true">×</span>
              <input
                type="number"
                min={100}
                max={10000}
                value={landscapeHeightPx}
                onChange={e => setLandscapeHeightPx(e.target.value)}
                aria-label="Height"
                placeholder="Height"
                required
              />
            </div>
          </div>
          <div className="device-model-swatch-field">
            <span>Colour swatch (optional)</span>
            <div className="device-model-swatch-editor">
              {colourSwatch.map((colour, index) => (
                <div key={index} className="device-model-swatch-input">
                  <input
                    type="color"
                    value={colour}
                    onChange={e => updateColour(index, e.target.value)}
                  />
                  <button
                    type="button"
                    className="device-model-swatch-remove"
                    onClick={() => removeColour(index)}
                    aria-label="Remove colour"
                  >
                    ×
                  </button>
                </div>
              ))}
              <button type="button" className="btn-secondary" onClick={addColour}>
                Add colour
              </button>
            </div>
          </div>
        </div>
        {error && (
          <p className="admin-error" role="alert">
            {error}
          </p>
        )}
        <div className="modal-actions">
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create'}
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
