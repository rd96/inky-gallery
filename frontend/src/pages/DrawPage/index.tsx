import { useState } from 'react'
import { ReactSketchCanvas } from 'react-sketch-canvas'
import { INKY_PALETTE } from './inkyPalette'
import './DrawPage.css'

type PaletteMode = 'inky' | 'full'

export default function DrawPage() {
  const [mode, setMode] = useState<PaletteMode>('inky')
  const [strokeColor, setStrokeColor] = useState(INKY_PALETTE[0].hex)

  return (
    <div className="draw-page">
      <div className="draw-toolbar">
        <div className="palette-mode-toggle">
          <button
            type="button"
            className={mode === 'inky' ? 'active' : undefined}
            onClick={() => setMode('inky')}
          >
            Inky palette
          </button>
          <button
            type="button"
            className={mode === 'full' ? 'active' : undefined}
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

      <ReactSketchCanvas
        className="draw-canvas"
        width="800px"
        height="480px"
        strokeWidth={4}
        strokeColor={strokeColor}
        canvasColor="white"
      />
    </div>
  )
}
