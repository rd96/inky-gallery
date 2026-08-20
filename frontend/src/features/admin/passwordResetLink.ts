const REDACTED_TOKEN = '•'.repeat(24)

export function buildPasswordResetLink(passwordResetToken: string): string {
  return `${window.location.origin}/reset?token=${encodeURIComponent(passwordResetToken)}`
}

export function redactedPasswordResetLink(): string {
  return `${window.location.origin}/reset?token=${REDACTED_TOKEN}`
}
