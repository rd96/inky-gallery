import { useEffect, useRef, useState } from 'react'
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom'
import { ReactSketchCanvas, type CanvasPath, type ReactSketchCanvasRef } from 'react-sketch-canvas'
import { useAuth } from '../../features/auth/useAuth'
import { CanvasesApi } from '../../features/drawings/api/canvasesApi'
import { DrawingsApi } from '../../features/drawings/api/drawingsApi'
import {
  getDraftStorageKey,
  isDraftUnknownToThisDevice,
  loadDraftPaths,
  UNKNOWN_DRAFT_DEVICE_MESSAGE,
} from '../../features/drawings/draftStorage'
import type { CanvasDetail } from '../../features/drawings/types'
import { formatApiError } from '../../shared/api/ApiError'
import SendCanvasModal from '../../shared/components/SendCanvasModal'
import './DrawPage.css'

type PaletteMode = 'palette' | 'full'

const DEFAULT_STROKE_COLOR = '#000000'

type History = {
  snapshots: CanvasPath[][]
  index: number
}

const MIN_STROKE_WIDTH = 1
const MAX_STROKE_WIDTH = 50
const BLANK_HISTORY: History = { snapshots: [[]], index: 0 }

function loadStoredHistory(userId: string, canvasId: string): History {
  const paths = loadDraftPaths(userId, canvasId)
  return paths.length > 0 ? { snapshots: [paths], index: 0 } : BLANK_HISTORY
}

// Keyed by canvasId below so navigating between two /draw/:canvasId URLs
// always remounts fresh instead of reusing state loaded for a different canvas.
export default function DrawPage() {
  const { canvasId } = useParams()

  if (!canvasId) {
    return <Navigate to="/" replace />
  }

  return <DrawCanvasLoader key={canvasId} canvasId={canvasId} />
}

type DrawCanvasLoaderProps = {
  canvasId: string
}

// Canvas dimensions and palette depend on the target device model, so they
// have to be fetched before the sketch canvas can be rendered.
function DrawCanvasLoader({ canvasId }: DrawCanvasLoaderProps) {
  const [canvas, setCanvas] = useState<CanvasDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    CanvasesApi.getMyCanvas(canvasId)
      .then(result => {
        if (!cancelled) setCanvas(result)
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
  }, [canvasId])

  if (loading) return <p className="draw-page-status">Loading…</p>

  if (loadError || !canvas) {
    return (
      <div className="draw-page-status">
        <p role="alert">{loadError ?? 'Drawing not found.'}</p>
        <Link to="/">Back to gallery</Link>
      </div>
    )
  }

  return <DrawCanvas canvasId={canvasId} canvas={canvas} />
}

type DrawCanvasProps = {
  canvasId: string
  canvas: CanvasDetail
}

function DrawCanvas({ canvasId, canvas }: DrawCanvasProps) {
  const navigate = useNavigate()
  const { auth } = useAuth()
  // DrawPage only ever renders inside RequireAuth, so this is always
  // populated in practice; the fallback just satisfies the type checker.
  const userId = auth.status === 'authenticated' ? auth.user.userId : ''
  const storageKey = getDraftStorageKey(userId, canvasId)

  const [isUnknownToThisDevice] = useState(() => isDraftUnknownToThisDevice(userId, canvasId))

  // The device model may only support a fixed set of ink colours - when it
  // does, offer those as swatches; otherwise there's no palette to restrict
  // to, so just go straight to the unrestricted colour picker.
  const paletteSwatches = canvas.palette?.map(hex => ({ name: hex, hex })) ?? []
  const hasPalette = paletteSwatches.length > 0

  const canvasRef = useRef<ReactSketchCanvasRef>(null)
  const [mode, setMode] = useState<PaletteMode>(hasPalette ? 'palette' : 'full')
  const [strokeColor, setStrokeColor] = useState(
    hasPalette ? paletteSwatches[0].hex : DEFAULT_STROKE_COLOR,
  )
  const [strokeWidth, setStrokeWidth] = useState(4)
  const [isErasing, setIsErasing] = useState(false)

  // react-sketch-canvas's own undo()/redo()/clearCanvas() didn't reliably
  // restore state when tested (clearing then undoing stayed blank), so we
  // own the history ourselves: every completed stroke snapshots the full
  // path list via exportPaths(), and undo/redo replay a snapshot via
  // resetCanvas() + loadPaths().
  const [history, setHistory] = useState<History>(() => loadStoredHistory(userId, canvasId))
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [showSendModal, setShowSendModal] = useState(false)

  // Restore into the canvas itself once on mount - the initial history
  // state above already has the saved paths, but the canvas needs its ref
  // to be ready before loadPaths() can be called.
  useEffect(() => {
    const paths = history.snapshots[history.index]
    if (paths.length > 0) canvasRef.current?.loadPaths(paths)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    const paths = history.snapshots[history.index]
    if (paths.length === 0) {
      localStorage.removeItem(storageKey)
    } else {
      localStorage.setItem(storageKey, JSON.stringify(paths))
    }
  }, [history, storageKey])

  function toggleEraser() {
    const next = !isErasing
    setIsErasing(next)
    canvasRef.current?.eraseMode(next)
  }

  async function commitSnapshot() {
    const paths = (await canvasRef.current?.exportPaths()) ?? []
    setHistory(({ snapshots, index }) => ({
      snapshots: [...snapshots.slice(0, index + 1), paths],
      index: index + 1,
    }))
    setSaveError(null)
  }

  function restoreSnapshot(targetIndex: number) {
    canvasRef.current?.resetCanvas()
    const paths = history.snapshots[targetIndex]
    if (paths.length > 0) canvasRef.current?.loadPaths(paths)
    setHistory(current => ({ ...current, index: targetIndex }))
  }

  function handleUndo() {
    if (history.index === 0) return
    restoreSnapshot(history.index - 1)
  }

  function handleRedo() {
    if (history.index === history.snapshots.length - 1) return
    restoreSnapshot(history.index + 1)
  }

  function resetToBlank() {
    canvasRef.current?.resetCanvas()
    setHistory(({ snapshots, index }) => ({
      snapshots: [...snapshots.slice(0, index + 1), []],
      index: index + 1,
    }))
  }

  function handleClear() {
    resetToBlank()
  }

  async function handleSave() {
    const canvasRefCurrent = canvasRef.current
    if (!canvasRefCurrent) return

    setSaving(true)
    setSaveError(null)

    try {
      // Without explicit dimensions, exportImage multiplies by
      // window.devicePixelRatio, so a Retina display would save an
      // oversized PNG relative to the canvas's actual size.
      const dataUrl = await canvasRefCurrent.exportImage('png', {
        width: canvas.widthPx,
        height: canvas.heightPx,
      })
      const png = await (await fetch(dataUrl)).blob()
      await DrawingsApi.saveDrawing(canvasId, png)
      // The canvas is now a saved drawing, not a draft - free up this slot.
      resetToBlank()
      setShowSendModal(true)
    } catch (cause) {
      setSaveError(formatApiError(cause))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="draw-page">
      {isUnknownToThisDevice && (
        <p className="draw-page-notice" style={{ width: `${canvas.widthPx}px` }}>
          {UNKNOWN_DRAFT_DEVICE_MESSAGE}
        </p>
      )}

      <div className="draw-toolbar" style={{ width: `${canvas.widthPx}px` }}>
        <div className="toolbar-row">
          {hasPalette && (
            <div className="toolbar-button-group">
              <button
                type="button"
                className={`toolbar-button ${mode === 'palette' ? 'active' : ''}`}
                onClick={() => setMode('palette')}
              >
                Palette
              </button>
              <button
                type="button"
                className={`toolbar-button ${mode === 'full' ? 'active' : ''}`}
                onClick={() => setMode('full')}
              >
                Full color
              </button>
            </div>
          )}

          {hasPalette && mode === 'palette' ? (
            <div className="palette-swatches">
              {paletteSwatches.map(({ name, hex }) => (
                <button
                  key={hex}
                  type="button"
                  className={`palette-swatch ${strokeColor === hex ? 'active' : ''}`}
                  style={{ backgroundColor: hex }}
                  aria-label={name}
                  title={name}
                  onClick={() => setStrokeColor(hex)}
                />
              ))}
            </div>
          ) : (
            <input
              type="color"
              className="palette-color-input"
              value={strokeColor}
              onChange={e => setStrokeColor(e.target.value)}
              aria-label="Pick a color"
            />
          )}
        </div>

        <div className="toolbar-row">
          <label className="stroke-width-control">
            Size
            <input
              type="range"
              min={MIN_STROKE_WIDTH}
              max={MAX_STROKE_WIDTH}
              value={strokeWidth}
              onChange={e => setStrokeWidth(Number(e.target.value))}
            />
            <span>{strokeWidth}px</span>
          </label>

          <button
            type="button"
            className={`toolbar-button ${isErasing ? 'active' : ''}`}
            onClick={toggleEraser}
          >
            Eraser
          </button>

          <div className="toolbar-button-group">
            <button
              type="button"
              className="toolbar-button"
              onClick={handleUndo}
              disabled={history.index === 0}
            >
              Undo
            </button>
            <button
              type="button"
              className="toolbar-button"
              onClick={handleRedo}
              disabled={history.index === history.snapshots.length - 1}
            >
              Redo
            </button>
          </div>

          <button
            type="button"
            className="toolbar-button danger clear-button"
            onClick={handleClear}
          >
            Clear
          </button>
        </div>

        <div className="toolbar-row">
          <button
            type="button"
            className="save-button"
            onClick={() => void handleSave()}
            disabled={saving}
          >
            {saving ? 'Saving…' : 'Save and send'}
          </button>
          {saveError && (
            <p className="draw-save-error" role="alert">
              {saveError}
            </p>
          )}
        </div>
      </div>

      <ReactSketchCanvas
        ref={canvasRef}
        className="draw-canvas"
        width={`${canvas.widthPx}px`}
        height={`${canvas.heightPx}px`}
        strokeWidth={strokeWidth}
        eraserWidth={strokeWidth}
        strokeColor={strokeColor}
        canvasColor="white"
        onStroke={commitSnapshot}
      />

      {showSendModal && (
        <SendCanvasModal
          canvasId={canvasId}
          deviceModelId={canvas.deviceModelId}
          orientation={canvas.orientation}
          onClose={() => navigate('/')}
          onSent={() => navigate('/')}
        />
      )}
    </div>
  )
}
