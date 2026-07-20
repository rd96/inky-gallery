import { useRef, useState } from 'react'
import { ReactSketchCanvas, type CanvasPath, type ReactSketchCanvasRef } from 'react-sketch-canvas'
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

export default function DrawPage() {
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
  const [history, setHistory] = useState<History>(BLANK_HISTORY)

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
      </div>

      <ReactSketchCanvas
        ref={canvasRef}
        className="draw-canvas"
        width="800px"
        height="480px"
        strokeWidth={strokeWidth}
        eraserWidth={strokeWidth}
        strokeColor={strokeColor}
        canvasColor="white"
        onStroke={commitSnapshot}
      />
    </div>
  )
}
