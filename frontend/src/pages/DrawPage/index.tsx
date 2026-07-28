import { useEffect, useRef, useState } from 'react'
import { ReactSketchCanvas, type CanvasPath, type ReactSketchCanvasRef } from 'react-sketch-canvas'
import { DrawingsApi } from '../../features/drawings/api/drawingsApi'
import { DRAWING_CANVAS_HEIGHT_PX, DRAWING_CANVAS_WIDTH_PX } from '../../features/drawings/canvasSize'
import { getDraftStorageKey, loadDraftPaths } from '../../features/drawings/draftStorage'
import { useAuth } from '../../features/auth/useAuth'
import { formatApiError } from '../../shared/api/ApiError'
import { INKY_PALETTE } from './inkyPalette'
import './DrawPage.css'

type PaletteMode = 'inky' | 'full'

type History = {
  snapshots: CanvasPath[][]
  index: number
}

const MIN_STROKE_WIDTH = 1
const MAX_STROKE_WIDTH = 50
const BLANK_HISTORY: History = { snapshots: [[]], index: 0 }

function loadStoredHistory(userId: string): History {
  const paths = loadDraftPaths(userId)
  return paths.length > 0 ? { snapshots: [paths], index: 0 } : BLANK_HISTORY
}

export default function DrawPage() {
  const { auth } = useAuth()
  // DrawPage only ever renders inside RequireAuth, so this is always
  // populated in practice; the fallback just satisfies the type checker.
  const userId = auth.status === 'authenticated' ? auth.user.userId : ''
  const storageKey = getDraftStorageKey(userId)

  const canvasRef = useRef<ReactSketchCanvasRef>(null)
  const [mode, setMode] = useState<PaletteMode>('inky')
  const [strokeColor, setStrokeColor] = useState(INKY_PALETTE[0].hex)
  const [strokeWidth, setStrokeWidth] = useState(4)
  const [isErasing, setIsErasing] = useState(false)

  // react-sketch-canvas's own undo()/redo()/clearCanvas() didn't reliably
  // restore state when tested (clearing then undoing stayed blank), so we
  // own the history ourselves: every completed stroke snapshots the full
  // path list via exportPaths(), and undo/redo replay a snapshot via
  // resetCanvas() + loadPaths().
  const [history, setHistory] = useState<History>(() => loadStoredHistory(userId))
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

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
    setSaved(false)
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

  function handleClear() {
    canvasRef.current?.resetCanvas()
    setHistory(({ snapshots, index }) => ({
      snapshots: [...snapshots.slice(0, index + 1), []],
      index: index + 1,
    }))
  }

  async function handleSave() {
    const canvas = canvasRef.current
    if (!canvas) return

    setSaving(true)
    setSaved(false)
    setSaveError(null)

    try {
      // Without explicit dimensions, exportImage multiplies by
      // window.devicePixelRatio, so a Retina display would save an
      // 1600x960 PNG for an 800x480 canvas. Pin it to the canvas's
      // actual size.
      const dataUrl = await canvas.exportImage('png', {
        width: DRAWING_CANVAS_WIDTH_PX,
        height: DRAWING_CANVAS_HEIGHT_PX,
      })
      const png = await (await fetch(dataUrl)).blob()
      await DrawingsApi.saveDrawing(png)
      setSaved(true)
    } catch (cause) {
      setSaveError(formatApiError(cause))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="draw-page">
      <div className="draw-toolbar">
        <div className="toolbar-row">
          <div className="toolbar-button-group">
            <button
              type="button"
              className={`toolbar-button ${mode === 'inky' ? 'active' : ''}`}
              onClick={() => setMode('inky')}
            >
              Inky palette
            </button>
            <button
              type="button"
              className={`toolbar-button ${mode === 'full' ? 'active' : ''}`}
              onClick={() => setMode('full')}
            >
              Full color
            </button>
          </div>

          {mode === 'inky' ? (
            <div className="palette-swatches">
              {INKY_PALETTE.map(({ name, hex }) => (
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
            {saving ? 'Saving…' : 'Save drawing'}
          </button>
          {saveError && (
            <p className="draw-save-error" role="alert">
              {saveError}
            </p>
          )}
          {saved && <p className="draw-save-success">Saved!</p>}
        </div>
      </div>

      <ReactSketchCanvas
        ref={canvasRef}
        className="draw-canvas"
        width={`${DRAWING_CANVAS_WIDTH_PX}px`}
        height={`${DRAWING_CANVAS_HEIGHT_PX}px`}
        strokeWidth={strokeWidth}
        eraserWidth={strokeWidth}
        strokeColor={strokeColor}
        canvasColor="white"
        onStroke={commitSnapshot}
      />
    </div>
  )
}
