import { useRef, useState } from 'react'
import { ReactSketchCanvas, type ReactSketchCanvasRef } from 'react-sketch-canvas'
import { INKY_PALETTE } from './inkyPalette'
import './DrawPage.css'

type PaletteMode = 'inky' | 'full'

const MIN_STROKE_WIDTH = 1
const MAX_STROKE_WIDTH = 50

export default function DrawPage() {
  const canvasRef = useRef<ReactSketchCanvasRef>(null)
  const [mode, setMode] = useState<PaletteMode>('inky')
  const [strokeColor, setStrokeColor] = useState(INKY_PALETTE[0].hex)
  const [strokeWidth, setStrokeWidth] = useState(4)
  const [isErasing, setIsErasing] = useState(false)

  function toggleEraser() {
    const next = !isErasing
    setIsErasing(next)
    canvasRef.current?.eraseMode(next)
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
      />
    </div>
  )
}
