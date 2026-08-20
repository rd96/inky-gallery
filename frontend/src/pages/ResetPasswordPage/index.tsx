import { useEffect, useState, type SubmitEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { UserApi } from '../../features/auth/api/userApi'
import { useAuth } from '../../features/auth/useAuth'
import { formatApiError } from '../../shared/api/ApiError'
import '../LoginPage/LoginPage.css'
import '../LoginPage/LoginCard.css'
import './ResetPasswordPage.css'

type ResetPasswordState =
  | { status: 'loading' }
  | { status: 'invalid' }
  | { status: 'ready'; username: string; displayName: string }

export default function ResetPasswordPage() {
  const { completeLogin } = useAuth()
  const navigate = useNavigate()
  const [token] = useState(
    () => new URLSearchParams(window.location.search).get('token'),
  )
  const [state, setState] = useState<ResetPasswordState>({ status: 'loading' })
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

    UserApi.getPasswordResetDetails(token)
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
      const user = await UserApi.resetPassword({ passwordResetToken: token, password })
      completeLogin(user)
      navigate('/', { replace: true })
    } catch (cause) {
      setError(formatApiError(cause))
    } finally {
      setSubmitting(false)
    }
  }

  if (state.status === 'loading') {
    return (
      <main className="login-page">
        <div className="card reset-password-card">
          <p>Loading…</p>
        </div>
      </main>
    )
  }

  if (state.status === 'invalid') {
    return (
      <main className="login-page">
        <div className="card reset-password-card">
          <header>
            <h1>Invalid link</h1>
          </header>
          <p className="tagline">
            This password reset link is invalid or has expired.
          </p>
        </div>
      </main>
    )
  }

  return (
    <main className="login-page">
      <div className="card reset-password-card">
        <header>
          <h1>Reset your password</h1>
        </header>
        <p className="tagline">
          Welcome back, {state.displayName}. Choose a new password.
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
            <label htmlFor="password">New password</label>
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
            <label htmlFor="confirmPassword">Confirm new password</label>
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
            {submitting ? 'Resetting…' : 'Reset password'}
          </button>
        </form>
      </div>
    </main>
  )
}
