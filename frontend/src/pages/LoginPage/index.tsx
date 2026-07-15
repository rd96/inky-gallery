import { useState } from 'react'
import FlipCard from '../../shared/components/FlipCard'
import LoginCard from './LoginCard'
import AboutCard from './AboutCard'
import './LoginPage.css'

export default function LoginPage() {
  const [flipped, setFlipped] = useState(false)

  return (
    <main className="login-page">
      <FlipCard
        className="login-flip-card"
        flipped={flipped}
        front={<LoginCard onShowAbout={() => setFlipped(true)} />}
        back={<AboutCard onBack={() => setFlipped(false)} />}
      />
    </main>
  )
}
