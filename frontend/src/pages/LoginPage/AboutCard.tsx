import './AboutCard.css'

type AboutCardProps = {
  onBack: () => void
}

export default function AboutCard({ onBack }: AboutCardProps) {
  return (
    <div className="card card--about">
      <header>
        <h2>About Inky Gallery</h2>
      </header>
      <div className="about-body">
        <p>
          TODO
        </p>
      </div>
      <button type="button" className="flip-link" onClick={onBack}>
        ← Back to sign in
      </button>
    </div>
  )
}
