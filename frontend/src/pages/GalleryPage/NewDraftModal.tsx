import { useEffect, useState, type SubmitEvent } from 'react'
import { useAuth } from '../../features/auth/useAuth'
import { DeviceModelsApi } from '../../features/devices/api/deviceModelsApi'
import OrientationPicker from '../../features/devices/OrientationPicker'
import type { DeviceModel, Orientation } from '../../features/devices/types'
import { CanvasesApi } from '../../features/drawings/api/canvasesApi'
import { markDraftCreatedOnThisDevice } from '../../features/drawings/draftStorage'
import { RecipientsApi } from '../../features/recipients/api/recipientsApi'
import { saveTargetedRecipient } from '../../features/recipients/targetedRecipientStorage'
import type { Recipient, RecipientDevice } from '../../features/recipients/types'
import { formatApiError } from '../../shared/api/ApiError'
import RecipientPicker from '../../shared/components/RecipientPicker'
import { useEscapeKey } from '../../shared/hooks/useEscapeKey'

type NewDraftModalProps = {
  onClose: () => void
  onCreated: (canvasId: string) => void
}

type Step = 'recipient' | 'device'

export default function NewDraftModal({ onClose, onCreated }: NewDraftModalProps) {
  const { auth } = useAuth()
  // NewDraftModal only ever renders inside RequireAuth, so this is always
  // populated in practice; the fallback just satisfies the type checker.
  const userId = auth.status === 'authenticated' ? auth.user.userId : ''

  const [step, setStep] = useState<Step>('recipient')
  const [recipients, setRecipients] = useState<Recipient[]>([])
  const [recipientsLoading, setRecipientsLoading] = useState(true)
  const [recipientsLoadError, setRecipientsLoadError] = useState<string | null>(null)

  const [selectedRecipient, setSelectedRecipient] = useState<Recipient | null>(null)
  const [recipientDevices, setRecipientDevices] = useState<RecipientDevice[] | null>(null)
  const [devicesLoading, setDevicesLoading] = useState(false)
  const [devicesLoadError, setDevicesLoadError] = useState<string | null>(null)

  // Only used once we know the recipient has no devices of their own.
  const [deviceModels, setDeviceModels] = useState<DeviceModel[]>([])
  const [modelsLoading, setModelsLoading] = useState(false)
  const [modelsLoadError, setModelsLoadError] = useState<string | null>(null)
  const [deviceModelId, setDeviceModelId] = useState('')
  const [orientation, setOrientation] = useState<Orientation>('LANDSCAPE')

  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEscapeKey(onClose)

  useEffect(() => {
    let cancelled = false

    RecipientsApi.searchRecipients()
      .then(result => {
        if (!cancelled) setRecipients(result)
      })
      .catch(cause => {
        if (!cancelled) setRecipientsLoadError(formatApiError(cause))
      })
      .finally(() => {
        if (!cancelled) setRecipientsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (!selectedRecipient) return

    let cancelled = false
    setDevicesLoading(true)
    setDevicesLoadError(null)

    RecipientsApi.getRecipientDevices(selectedRecipient.userId)
      .then(result => {
        if (!cancelled) setRecipientDevices(result)
      })
      .catch(cause => {
        if (!cancelled) setDevicesLoadError(formatApiError(cause))
      })
      .finally(() => {
        if (!cancelled) setDevicesLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [selectedRecipient])

  const needsFreePick = recipientDevices !== null && recipientDevices.length === 0

  useEffect(() => {
    if (!needsFreePick) return

    let cancelled = false
    setModelsLoading(true)
    setModelsLoadError(null)

    DeviceModelsApi.getDeviceModels()
      .then(models => {
        if (cancelled) return
        setDeviceModels(models)
        setDeviceModelId(models[0]?.deviceModelId ?? '')
      })
      .catch(cause => {
        if (!cancelled) setModelsLoadError(formatApiError(cause))
      })
      .finally(() => {
        if (!cancelled) setModelsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [needsFreePick])

  const selectedModel = deviceModels.find(model => model.deviceModelId === deviceModelId)

  function handlePickRecipient(recipient: Recipient) {
    setSelectedRecipient(recipient)
    setStep('device')
  }

  function handleBack() {
    setStep('recipient')
    setSelectedRecipient(null)
    setRecipientDevices(null)
    setDevicesLoadError(null)
    setSubmitError(null)
  }

  async function createCanvas(targetDeviceModelId: string, canvasOrientation: Orientation) {
    setSubmitting(true)
    setSubmitError(null)

    try {
      const { canvasId } = await CanvasesApi.createCanvas({
        targetDeviceModelId,
        orientation: canvasOrientation,
        canvasType: 'SINGLE',
      })
      markDraftCreatedOnThisDevice(userId, canvasId)
      if (selectedRecipient) saveTargetedRecipient(userId, canvasId, selectedRecipient)
      onCreated(canvasId)
    } catch (cause) {
      setSubmitError(formatApiError(cause))
    } finally {
      setSubmitting(false)
    }
  }

  function handlePickDevice(device: RecipientDevice) {
    void createCanvas(device.deviceModelId, device.orientation)
  }

  function handleFreePickSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!deviceModelId) return
    void createCanvas(deviceModelId, orientation)
  }

  return (
    <div className="admin-modal-backdrop" onClick={onClose}>
      <div
        className="modal-card"
        onClick={e => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label="Start new drawing"
      >
        {step === 'recipient' && (
          <>
            <h2>Who are you drawing for?</h2>

            <RecipientPicker
              recipients={recipients}
              loading={recipientsLoading}
              loadError={recipientsLoadError}
              emptyMessage="No connections yet - ask an admin to add some."
              onSelect={handlePickRecipient}
            />

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={onClose}>
                Cancel
              </button>
            </div>
          </>
        )}

        {step === 'device' && selectedRecipient && (
          <>
            <h2>Pick a device for {selectedRecipient.displayName}</h2>

            {devicesLoading && <p>Checking their devices…</p>}
            {devicesLoadError && (
              <p className="admin-error" role="alert">
                {devicesLoadError}
              </p>
            )}

            {recipientDevices && recipientDevices.length > 0 && (
              <ul className="recipient-list">
                {recipientDevices.map((device, index) => (
                  <li key={index}>
                    <button
                      type="button"
                      className="recipient-option"
                      onClick={() => handlePickDevice(device)}
                      disabled={submitting}
                    >
                      <span>
                        {device.deviceModelName} - {device.landscapeWidthPx}×{device.landscapeHeightPx} (
                        {device.orientation.toLowerCase()})
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            )}

            {needsFreePick && (
              <form onSubmit={handleFreePickSubmit}>
                <p className="recipient-empty">
                  {selectedRecipient.displayName} doesn't have any devices yet - you can still pick one
                  manually.
                </p>

                {modelsLoading && <p>Loading device models…</p>}
                {modelsLoadError && (
                  <p className="admin-error" role="alert">
                    {modelsLoadError}
                  </p>
                )}

                {!modelsLoading && !modelsLoadError && (
                  <div className="modal-fields">
                    <label>
                      Device
                      <select
                        value={deviceModelId}
                        onChange={e => setDeviceModelId(e.target.value)}
                        required
                      >
                        {deviceModels.map(model => (
                          <option key={model.deviceModelId} value={model.deviceModelId}>
                            {model.deviceName}
                          </option>
                        ))}
                      </select>
                    </label>
                    {selectedModel && (
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
                    disabled={submitting || modelsLoading || !!modelsLoadError || deviceModels.length === 0}
                  >
                    {submitting ? 'Starting…' : 'Start drawing'}
                  </button>
                  <button type="button" className="btn-secondary" onClick={handleBack} disabled={submitting}>
                    Back
                  </button>
                </div>
              </form>
            )}

            {!needsFreePick && (
              <>
                {submitError && (
                  <p className="admin-error" role="alert">
                    {submitError}
                  </p>
                )}
                <div className="modal-actions">
                  <button type="button" className="btn-secondary" onClick={handleBack} disabled={submitting}>
                    Back
                  </button>
                </div>
              </>
            )}
          </>
        )}
      </div>
    </div>
  )
}
