import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { ReactSketchCanvas, type CanvasPath, type ReactSketchCanvasRef } from 'react-sketch-canvas'
import { DRAWING_CANVAS_HEIGHT_PX, DRAWING_CANVAS_WIDTH_PX } from '../../features/drawings/canvasSize'

type DraftCardProps = {
  paths: CanvasPath[]
}

export default function DraftCard({ paths }: DraftCardProps) {
  const canvasRef = useRef<ReactSketchCanvasRef>(null)
  const [imageUrl, setImageUrl] = useState<string | null>(null)

  // ReactSketchCanvas has no prop for initial paths - they must be loaded
  // imperatively via the ref once it's mounted.
  useEffect(() => {
    canvasRef.current?.loadPaths(paths)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Rendering the live canvas straight into the grid clips it - the SVG has
  // no viewBox, so shrinking it to card size cuts off paths instead of
  // scaling them. Exporting a flat PNG from the full-size offscreen canvas
  // and displaying that with object-fit lets it scale down cleanly, the
  // same way saved drawings are shown.
  function handleChange(updatedPaths: CanvasPath[]) {
    if (updatedPaths.length === 0) return
    // Without explicit dimensions, exportImage multiplies by
    // window.devicePixelRatio, doubling output size on Retina displays.
    canvasRef.current
      ?.exportImage('png', { width: DRAWING_CANVAS_WIDTH_PX, height: DRAWING_CANVAS_HEIGHT_PX })
      .then(setImageUrl)
      .catch(() => {})
  }

  return (
    <Link
      to="/draw"
      className="draft-card"
      style={{ aspectRatio: `${DRAWING_CANVAS_WIDTH_PX} / ${DRAWING_CANVAS_HEIGHT_PX}` }}
    >
      <div className="draft-card-offscreen" aria-hidden="true">
        <ReactSketchCanvas
          ref={canvasRef}
          width={`${DRAWING_CANVAS_WIDTH_PX}px`}
          height={`${DRAWING_CANVAS_HEIGHT_PX}px`}
          canvasColor="white"
          onChange={handleChange}
          readOnly
        />
      </div>
      {imageUrl && <img className="draft-card-image" src={imageUrl} alt="" />}
      <span className="draft-card-label">Continue draft</span>
    </Link>
  )
}
