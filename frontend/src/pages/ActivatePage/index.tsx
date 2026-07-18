import { useEffect, useState, type SubmitEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { UserApi } from '../../features/auth/api/userApi'
import { useAuth } from '../../features/auth/useAuth'
import { ApiError } from '../../shared/api/ApiError'
import '../LoginPage/LoginPage.css'
import '../LoginPage/LoginCard.css'
import './ActivatePage.css'

type ActivationState =
  | { status: 'loading' }
  | { status: 'invalid' }
  | { status: 'ready'; username: string; displayName: string }

export default function ActivatePage() {
  const { completeLogin } = useAuth()
  const navigate = useNavigate()
  const [token] = useState(
    () => new URLSearchParams(window.location.search).get('token'),
  )
  const [state, setState] = useState<ActivationState>({ status: 'loading' })
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    window.history.replaceState(null, '', window.location.pathname)
  }, [])

  useEffect(() => {
    if (token === null) {
      setState({ status: 'invalid' })
      return
    }

    UserApi.getActivationDetails(token)
      .then(details =>
        setState({
          status: 'ready',
          username: details.username,
          displayName: details.displayName,
        }),
      )
      .catch(() => setState({ status: 'invalid' }))
  }, [token])

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()

    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    if (token === null) return

    setError(null)
    setSubmitting(true)

    try {
      const user = await UserApi.activate({ activationToken: token, password })
      completeLogin(user)
      navigate('/', { replace: true })
    } catch (cause) {
      setError(
        cause instanceof ApiError
          ? cause.message
          : 'Something went wrong. Please try again.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (state.status === 'loading') {
    return (
      <main className="login-page">
        <div className="card activate-card">
          <p>Loading…</p>
        </div>
      </main>
    )
  }

  if (state.status === 'invalid') {
    return (
      <main className="login-page">
        <div className="card activate-card">
          <header>
            <h1>Invalid link</h1>
          </header>
          <p className="tagline">
            This activation link is invalid or has expired.
          </p>
        </div>
      </main>
    )
  }

  return (
    <main className="login-page">
      <div className="card activate-card">
        <header>
          <h1>Activate your account</h1>
        </header>
        <p className="tagline">
          Welcome, {state.displayName}. Choose a password to get started.
        </p>
        <form onSubmit={handleSubmit} noValidate>
          <div className="field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              value={state.username}
              disabled
            />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              autoComplete="new-password"
              autoFocus
              required
            />
          </div>
          <div className="field">
            <label htmlFor="confirmPassword">Confirm password</label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={e => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
              required
            />
          </div>
          {error && (
            <p className="error" role="alert">
              {error}
            </p>
          )}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Activating…' : 'Activate account'}
          </button>
        </form>
      </div>
    </main>
  )
}
