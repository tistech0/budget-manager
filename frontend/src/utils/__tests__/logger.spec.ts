import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { logger } from '../logger'

describe('logger', () => {
  let consoleLogSpy: any
  let consoleErrorSpy: any
  let consoleWarnSpy: any
  let consoleInfoSpy: any
  let consoleDebugSpy: any
  let originalEnv: any

  beforeEach(() => {
    // Spy on console methods
    consoleLogSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
    consoleInfoSpy = vi.spyOn(console, 'info').mockImplementation(() => {})
    consoleDebugSpy = vi.spyOn(console, 'debug').mockImplementation(() => {})

    // Store original env
    originalEnv = import.meta.env.DEV
  })

  afterEach(() => {
    // Restore all mocks
    vi.restoreAllMocks()
    // Restore env
    import.meta.env.DEV = originalEnv
  })

  describe('in development mode', () => {
    beforeEach(() => {
      import.meta.env.DEV = true
    })

    it('should call console.log when logger.log is called', () => {
      logger.log('test message')
      expect(consoleLogSpy).toHaveBeenCalledWith('test message')
    })

    it('should call console.error when logger.error is called', () => {
      logger.error('error message')
      expect(consoleErrorSpy).toHaveBeenCalledWith('error message')
    })

    it('should call console.warn when logger.warn is called', () => {
      logger.warn('warning message')
      expect(consoleWarnSpy).toHaveBeenCalledWith('warning message')
    })

    it('should call console.info when logger.info is called', () => {
      logger.info('info message')
      expect(consoleInfoSpy).toHaveBeenCalledWith('info message')
    })

    it('should call console.debug when logger.debug is called', () => {
      logger.debug('debug message')
      expect(consoleDebugSpy).toHaveBeenCalledWith('debug message')
    })

    it('should handle multiple arguments', () => {
      logger.log('message', 'arg2', 'arg3')
      expect(consoleLogSpy).toHaveBeenCalledWith('message', 'arg2', 'arg3')
    })

    it('should handle objects', () => {
      const obj = { key: 'value' }
      logger.log('object:', obj)
      expect(consoleLogSpy).toHaveBeenCalledWith('object:', obj)
    })

    it('should handle arrays', () => {
      const arr = [1, 2, 3]
      logger.warn('array:', arr)
      expect(consoleWarnSpy).toHaveBeenCalledWith('array:', arr)
    })
  })

  describe('in production mode', () => {
    beforeEach(() => {
      import.meta.env.DEV = false
    })

    it('should not call console.log in production', () => {
      logger.log('test message')
      expect(consoleLogSpy).not.toHaveBeenCalled()
    })

    it('should not call console.error in production', () => {
      logger.error('error message')
      expect(consoleErrorSpy).not.toHaveBeenCalled()
    })

    it('should not call console.warn in production', () => {
      logger.warn('warning message')
      expect(consoleWarnSpy).not.toHaveBeenCalled()
    })

    it('should not call console.info in production', () => {
      logger.info('info message')
      expect(consoleInfoSpy).not.toHaveBeenCalled()
    })

    it('should not call console.debug in production', () => {
      logger.debug('debug message')
      expect(consoleDebugSpy).not.toHaveBeenCalled()
    })
  })

  describe('edge cases', () => {
    beforeEach(() => {
      import.meta.env.DEV = true
    })

    it('should handle null values', () => {
      logger.log(null)
      expect(consoleLogSpy).toHaveBeenCalledWith(null)
    })

    it('should handle undefined values', () => {
      logger.log(undefined)
      expect(consoleLogSpy).toHaveBeenCalledWith(undefined)
    })

    it('should handle empty calls', () => {
      logger.log()
      expect(consoleLogSpy).toHaveBeenCalled()
    })

    it('should handle numbers', () => {
      logger.log(123, 456)
      expect(consoleLogSpy).toHaveBeenCalledWith(123, 456)
    })

    it('should handle booleans', () => {
      logger.log(true, false)
      expect(consoleLogSpy).toHaveBeenCalledWith(true, false)
    })
  })
})
