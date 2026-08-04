import ColourSwatch from '../../shared/components/ColourSwatch'
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
      <ColourSwatch colours={deviceModel.palette ?? []} />
    </li>
  )
}
