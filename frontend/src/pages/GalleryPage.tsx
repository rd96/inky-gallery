import { useAuth } from '../features/auth/useAuth'

export default function GalleryPage() {
  const { auth } = useAuth()

  if (auth.status !== 'authenticated') return null

  return (
    <>
      <h1>Welcome {auth.user.username}</h1>
    </>
  )
}
