import type { ReceivedCanvasMessage } from '../../features/messages/types'
import MessageCard from './MessageCard'

type MessageGridProps = {
  messages: ReceivedCanvasMessage[]
}

export default function MessageGrid({ messages }: MessageGridProps) {
  if (messages.length === 0) return null

  return (
    <ul className="drawing-grid">
      {messages.map(message => (
        <MessageCard key={message.messageId} message={message} />
      ))}
    </ul>
  )
}
