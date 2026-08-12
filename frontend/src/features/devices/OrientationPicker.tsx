import type { Orientation } from './types'

type OrientationPickerProps = {
  value: Orientation
  onChange: (orientation: Orientation) => void
  landscapeWidthPx: number
  landscapeHeightPx: number
}

const LONG_SIDE_REM = 3
const MIN_SIDE_REM = 1

export default function OrientationPicker({
  value,
  onChange,
  landscapeWidthPx,
  landscapeHeightPx,
}: OrientationPickerProps) {
  const isSquare = landscapeWidthPx === landscapeHeightPx

  // A square device has no meaningful orientation to pick - show it as a
  // single static card (with its dimensions) rather than a two-way toggle.
  if (isSquare) {
    return (
      <div className="orientation-picker">
        <div className="orientation-option orientation-option--selected">
          <span className="orientation-box-frame" aria-hidden="true">
            <span
              className="orientation-box"
              style={{ width: `${LONG_SIDE_REM}rem`, height: `${LONG_SIDE_REM}rem` }}
            />
          </span>
          Square
          <span className="orientation-dimensions">
            {landscapeWidthPx}×{landscapeHeightPx}
          </span>
        </div>
      </div>
    )
  }

  const ratio = landscapeWidthPx / landscapeHeightPx
  const shortSideRem = Math.max(MIN_SIDE_REM, LONG_SIDE_REM / ratio)

  return (
    <div className="orientation-picker" role="radiogroup" aria-label="Orientation">
      <button
        type="button"
        role="radio"
        aria-checked={value === 'LANDSCAPE'}
        className={
          value === 'LANDSCAPE'
            ? 'orientation-option orientation-option--selected'
            : 'orientation-option'
        }
        onClick={() => onChange('LANDSCAPE')}
      >
        <span className="orientation-box-frame" aria-hidden="true">
          <span
            className="orientation-box"
            style={{ width: `${LONG_SIDE_REM}rem`, height: `${shortSideRem}rem` }}
          />
        </span>
        Landscape
        <span className="orientation-dimensions">
          {landscapeWidthPx}×{landscapeHeightPx}
        </span>
      </button>
      <button
        type="button"
        role="radio"
        aria-checked={value === 'PORTRAIT'}
        className={
          value === 'PORTRAIT'
            ? 'orientation-option orientation-option--selected'
            : 'orientation-option'
        }
        onClick={() => onChange('PORTRAIT')}
      >
        <span className="orientation-box-frame" aria-hidden="true">
          <span
            className="orientation-box"
            style={{ width: `${shortSideRem}rem`, height: `${LONG_SIDE_REM}rem` }}
          />
        </span>
        Portrait
        <span className="orientation-dimensions">
          {landscapeHeightPx}×{landscapeWidthPx}
        </span>
      </button>
    </div>
  )
}
