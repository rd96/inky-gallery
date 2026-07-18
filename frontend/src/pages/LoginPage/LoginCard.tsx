import { useState, type SubmitEvent } from 'react'
import { useAuth } from '../../features/auth/useAuth'
import { UserApi } from '../../features/auth/api/userApi'
import { ApiError } from '../../shared/api/ApiError'
import './LoginCard.css'

type LoginCardProps = {
  onShowAbout: () => void
}

export default function LoginCard({ onShowAbout }: LoginCardProps) {
  const { completeLogin } = useAuth()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: SubmitEvent<HTMLFormElement>) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)

    try {
      const user = await UserApi.login({ username, password })
      completeLogin(user)
    } catch (cause) {
      setError(
        cause instanceof ApiError && cause.status === 401
          ? 'Incorrect username or password.'
          : 'Something went wrong. Please try again.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="card">
      <header>
        <h1>Inky Gallery</h1>
      </header>
      <p className="tagline">Tagline TODO.</p>
      <form onSubmit={handleSubmit} noValidate>
        <div className="field">
          <label htmlFor="username">Username</label>
          <input
            id="username"
            type="text"
            value={username}
            onChange={e => setUsername(e.target.value)}
            autoComplete="username"
            autoFocus
            required
          />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </div>
        {error && <p className="error" role="alert">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
      <button type="button" className="flip-link" onClick={onShowAbout}>
        About this project →
      </button>
    </div>
  )
}
