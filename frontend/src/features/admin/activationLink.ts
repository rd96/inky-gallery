const REDACTED_TOKEN = '•'.repeat(24)

export function buildActivationLink(activationToken: string): string {
  return `${window.location.origin}/activate?token=${encodeURIComponent(activationToken)}`
}

export function redactedActivationLink(): string {
  return `${window.location.origin}/activate?token=${REDACTED_TOKEN}`
}
