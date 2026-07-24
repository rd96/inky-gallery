import type { DeviceModel } from '../../features/devices/types'
import DeviceModelListItem from './DeviceModelListItem'

type DeviceModelListProps = {
  deviceModels: DeviceModel[]
}

export default function DeviceModelList({ deviceModels }: DeviceModelListProps) {
  if (deviceModels.length === 0) {
    return <p className="device-model-empty">No device models yet.</p>
  }

  return (
    <ul className="device-model-list">
      {deviceModels.map(deviceModel => (
        <DeviceModelListItem key={deviceModel.deviceModelId} deviceModel={deviceModel} />
      ))}
    </ul>
  )
}
