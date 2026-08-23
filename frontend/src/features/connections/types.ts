export type Connection = {
  userId: string
  username: string
  displayName: string
}

export type Connections = {
  senders: Connection[]
  recipients: Connection[]
}
