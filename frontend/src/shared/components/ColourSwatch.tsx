import './ColourSwatch.css'

type ColourSwatchProps = {
  colours: string[]
  max?: number
}

const DEFAULT_MAX = 7

export default function ColourSwatch({ colours, max = DEFAULT_MAX }: ColourSwatchProps) {
  if (colours.length === 0) return null

  const visible = colours.slice(0, max)
  const remaining = colours.length - visible.length

  return (
    <div className="device-model-swatch">
      {visible.map((colour, index) => (
        <span
          key={`${colour}-${index}`}
          className="device-model-swatch-dot"
          style={{ backgroundColor: colour }}
          title={colour}
        />
      ))}
      {remaining > 0 && (
        <span
          className="device-model-swatch-more"
          title={`${remaining} more colour${remaining === 1 ? '' : 's'}`}
        >
          +{remaining}
        </span>
      )}
    </div>
  )
}
