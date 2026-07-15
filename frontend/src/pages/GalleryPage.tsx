import type { User } from '../features/auth/types.ts'

type GalleryPageProps = {
  user: User
}

export default function GalleryPage({ user }: GalleryPageProps) {
    return <>
        <h1>Welcome ${user.username}</h1>
    </>
}