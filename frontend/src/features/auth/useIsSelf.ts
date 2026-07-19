import { useAuth } from './useAuth'

// Usernames are the only stable-ish identifier /api/auth/me returns today
// (no user id) — this comparison breaks if that ever changes to compare
// against a real id instead.
export function useIsSelf(username: string): boolean {
  const { auth } = useAuth()
  return auth.status === 'authenticated' && auth.user.username === username
}
