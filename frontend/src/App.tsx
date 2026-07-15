import './App.css'
import LoginPage from './pages/LoginPage'
import { useAuth } from './features/auth/useAuth'
import GalleryPage from './pages/GalleryPage'

export default function App() {
  const { auth, refresh } = useAuth()

  switch (auth.status) {
    case 'loading':
      return null

    case 'unauthenticated':
      return <LoginPage />

    case 'error':
      return (
        <main>
          <p>Unable to connect to the server.</p>
          <button type="button" onClick={() => void refresh()}>
            Try again
          </button>
        </main>
      )

    case 'authenticated':
      return <GalleryPage user={auth.user} />
  }
}