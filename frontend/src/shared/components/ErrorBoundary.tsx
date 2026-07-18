import { Component, type ErrorInfo, type PropsWithChildren } from 'react'
import FullPageMessage from './FullPageMessage'

type ErrorBoundaryState = {
  error: Error | null
}

export class ErrorBoundary extends Component<
  PropsWithChildren,
  ErrorBoundaryState
> {
  state: ErrorBoundaryState = { error: null }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { error }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Unhandled error in component tree', error, errorInfo)
  }

  render() {
    if (this.state.error) {
      return (
        <FullPageMessage
          heading="Something went wrong"
          message="An unexpected error occurred. Try reloading the page."
          actionLabel="Reload"
          onAction={() => window.location.reload()}
        />
      )
    }

    return this.props.children
  }
}
