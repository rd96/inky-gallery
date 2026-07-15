import { useLayoutEffect, useRef, useState, type ReactNode } from 'react'
import './FlipCard.css'

type FlipCardProps = {
  front: ReactNode
  back: ReactNode
  flipped: boolean
  className?: string
}

export default function FlipCard({ front, back, flipped, className }: FlipCardProps) {
  const frontRef = useRef<HTMLDivElement>(null)
  const backRef = useRef<HTMLDivElement>(null)
  const [height, setHeight] = useState<number>()

  useLayoutEffect(() => {
    const activeFace = flipped ? backRef.current : frontRef.current
    setHeight(activeFace?.scrollHeight)
  })

  return (
    <div className={`flip-card ${className ?? ''}`} style={{ height }}>
      <div className={`flip-card-inner ${flipped ? 'is-flipped' : ''}`}>
        <div ref={frontRef} className="flip-card-face flip-card-front">
          {front}
        </div>
        <div ref={backRef} className="flip-card-face flip-card-back">
          {back}
        </div>
      </div>
    </div>
  )
}
