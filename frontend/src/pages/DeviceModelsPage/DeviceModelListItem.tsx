import type { DeviceModel } from '../../features/devices/types'

type DeviceModelListItemProps = {
  deviceModel: DeviceModel
}

export default function DeviceModelListItem({ deviceModel }: DeviceModelListItemProps) {
  return (
    <li className="device-model-list-item">
      <div className="device-model-cell">
        <span className="device-model-name">{deviceModel.deviceName}</span>
        <span className="device-model-dimensions">
          {deviceModel.landscapeWidthPx} × {deviceModel.landscapeHeightPx}px
        </span>
      </div>
      {deviceModel.colourSwatch && deviceModel.colourSwatch.length > 0 && (
        <div className="device-model-swatch">
          {deviceModel.colourSwatch.map((colour, index) => (
            <span
              key={`${colour}-${index}`}
              className="device-model-swatch-dot"
              style={{ backgroundColor: colour }}
              title={colour}
            />
          ))}
        </div>
      )}
    </li>
  )
}
