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
          Inky Gallery is a place to share drawings with friends who have a digital picture frame, like the{' '}
          <a href="https://shop.pimoroni.com/collections/pimoroni?tags=e-ink%20Displays" target="_blank" rel="noreferrer">
            InkyFrame
          </a>
          .
        </p>
        <p>Made by Robert Derbyshire</p>
      </div>
      <button type="button" className="flip-link" onClick={onBack}>
        ← Back to sign in
      </button>
    </div>
  )
}
