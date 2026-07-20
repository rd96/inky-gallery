import { ReactSketchCanvas } from 'react-sketch-canvas'
import './DrawPage.css'

export default function DrawPage() {
  return (
    <div className="draw-page">
      <ReactSketchCanvas
        className="draw-canvas"
        width="800px"
        height="480px"
        strokeWidth={4}
        strokeColor="black"
        canvasColor="white"
      />
    </div>
  )
}
