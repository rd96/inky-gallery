import type { AdminUser } from '../../features/admin/types'
import UserListItem from './UserListItem'

type UserListProps = {
  users: AdminUser[]
  currentUsername: string | null
  onChanged: () => void
  onError: (message: string) => void
}

export default function UserList({
  users,
  currentUsername,
  onChanged,
  onError,
}: UserListProps) {
  if (users.length === 0) {
    return <p>No users found.</p>
  }

  return (
    <ul className="user-list">
      {users.map(user => (
        <UserListItem
          key={user.id}
          user={user}
          isSelf={user.username === currentUsername}
          onChanged={onChanged}
          onError={onError}
        />
      ))}
    </ul>
  )
}
