/**
 * Logger utility - Development-only logging
 * Logs are stripped from production builds
 */

export const logger = {
  log: (...args: unknown[]) => {
    if (import.meta.env.DEV) {
      console.log(...args)
    }
  },

  error: (...args: unknown[]) => {
    if (import.meta.env.DEV) {
      console.error(...args)
    } else {
      // In production, could send to error tracking service
      // Example: Sentry.captureMessage(args.join(' '))
    }
  },

  warn: (...args: unknown[]) => {
    if (import.meta.env.DEV) {
      console.warn(...args)
    }
  },

  info: (...args: unknown[]) => {
    if (import.meta.env.DEV) {
      console.info(...args)
    }
  },

  debug: (...args: unknown[]) => {
    if (import.meta.env.DEV) {
      console.debug(...args)
    }
  }
}

export default logger
