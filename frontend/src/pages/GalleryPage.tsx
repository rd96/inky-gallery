import { Link } from 'react-router-dom'
import './GalleryPage.css'

export default function GalleryPage() {
  return (
    <div className="gallery-empty">
      <p className="gallery-empty-message">You don't have any drawings yet.</p>
      <Link to="/draw" className="gallery-empty-cta">
        Start new drawing
      </Link>
    </div>
  )
}
