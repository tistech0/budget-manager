/**
 * Performance Monitoring Composable
 * Tracks component render times, API calls, and user interactions
 */

import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { logger } from '@/utils/logger'

interface PerformanceMetric {
  name: string
  startTime: number
  endTime?: number
  duration?: number
  metadata?: Record<string, any>
}

const metrics = ref<PerformanceMetric[]>([])
const isMonitoring = ref(false)

/**
 * Main performance composable
 */
export function usePerformance(componentName?: string) {
  const componentMetrics = ref<PerformanceMetric[]>([])
  let mountTime: number

  /**
   * Track component mount time
   */
  onMounted(() => {
    if (componentName) {
      mountTime = performance.now()
      const metric: PerformanceMetric = {
        name: `${componentName}:mount`,
        startTime: mountTime,
        endTime: mountTime,
        duration: 0
      }
      componentMetrics.value.push(metric)

      if (import.meta.env.DEV) {
        logger.info(`[Performance] ${componentName} mounted`, {
          timestamp: mountTime
        })
      }
    }
  })

  /**
   * Track component unmount
   */
  onBeforeUnmount(() => {
    if (componentName && mountTime) {
      const unmountTime = performance.now()
      const lifetime = unmountTime - mountTime

      if (import.meta.env.DEV) {
        logger.info(`[Performance] ${componentName} unmounted`, {
          lifetime: `${lifetime.toFixed(2)}ms`
        })
      }
    }
  })

  /**
   * Measure the execution time of an async function
   */
  const measureAsync = async <T>(
    name: string,
    fn: () => Promise<T>,
    metadata?: Record<string, any>
  ): Promise<T> => {
    const startTime = performance.now()
    const metric: PerformanceMetric = {
      name: componentName ? `${componentName}:${name}` : name,
      startTime,
      ...(metadata && { metadata })
    }

    try {
      const result = await fn()
      const endTime = performance.now()
      const duration = endTime - startTime

      metric.endTime = endTime
      metric.duration = duration
      componentMetrics.value.push(metric)

      if (import.meta.env.DEV) {
        logger.info(`[Performance] ${metric.name}`, {
          duration: `${duration.toFixed(2)}ms`,
          ...metadata
        })
      }

      return result
    } catch (error) {
      const endTime = performance.now()
      metric.endTime = endTime
      metric.duration = endTime - startTime

      if (import.meta.env.DEV) {
        logger.error(`[Performance] ${metric.name} failed`, {
          duration: `${metric.duration.toFixed(2)}ms`,
          error
        })
      }

      throw error
    }
  }

  /**
   * Measure the execution time of a sync function
   */
  const measure = <T>(
    name: string,
    fn: () => T,
    metadata?: Record<string, any>
  ): T => {
    const startTime = performance.now()
    const metric: PerformanceMetric = {
      name: componentName ? `${componentName}:${name}` : name,
      startTime,
      ...(metadata && { metadata })
    }

    try {
      const result = fn()
      const endTime = performance.now()
      const duration = endTime - startTime

      metric.endTime = endTime
      metric.duration = duration
      componentMetrics.value.push(metric)

      if (import.meta.env.DEV) {
        logger.info(`[Performance] ${metric.name}`, {
          duration: `${duration.toFixed(2)}ms`,
          ...metadata
        })
      }

      return result
    } catch (error) {
      const endTime = performance.now()
      metric.endTime = endTime
      metric.duration = endTime - startTime

      if (import.meta.env.DEV) {
        logger.error(`[Performance] ${metric.name} failed`, {
          duration: `${metric.duration.toFixed(2)}ms`,
          error
        })
      }

      throw error
    }
  }

  /**
   * Mark a specific point in time
   */
  const mark = (name: string, metadata?: Record<string, any>) => {
    const timestamp = performance.now()
    const metric: PerformanceMetric = {
      name: componentName ? `${componentName}:${name}` : name,
      startTime: timestamp,
      endTime: timestamp,
      duration: 0,
      ...(metadata && { metadata })
    }

    componentMetrics.value.push(metric)

    if (import.meta.env.DEV) {
      logger.info(`[Performance] Mark: ${metric.name}`, {
        timestamp: `${timestamp.toFixed(2)}ms`,
        ...metadata
      })
    }
  }

  /**
   * Get all metrics for this component
   */
  const getMetrics = () => componentMetrics.value

  /**
   * Clear all metrics for this component
   */
  const clearMetrics = () => {
    componentMetrics.value = []
  }

  /**
   * Get average duration for a specific metric name
   */
  const getAverageDuration = (metricName: string): number => {
    const filtered = componentMetrics.value.filter(m => m.name.includes(metricName))
    if (filtered.length === 0) return 0

    const total = filtered.reduce((sum, m) => sum + (m.duration || 0), 0)
    return total / filtered.length
  }

  return {
    measureAsync,
    measure,
    mark,
    getMetrics,
    clearMetrics,
    getAverageDuration,
    metrics: componentMetrics
  }
}

/**
 * Track API call performance
 */
export function useApiPerformance() {
  const { measureAsync, getMetrics } = usePerformance('API')

  const trackApiCall = async <T>(
    endpoint: string,
    method: string,
    fn: () => Promise<T>
  ): Promise<T> => {
    return measureAsync(`${method} ${endpoint}`, fn, { endpoint, method })
  }

  return {
    trackApiCall,
    getMetrics
  }
}

/**
 * Track render performance with reactive dependencies
 */
export function useRenderPerformance(
  componentName: string,
  dependencies: () => any[]
) {
  const renderCount = ref(0)
  const { mark } = usePerformance(componentName)

  // Track re-renders when dependencies change
  watch(
    dependencies,
    () => {
      renderCount.value++
      mark('render', { renderCount: renderCount.value })

      if (import.meta.env.DEV && renderCount.value > 10) {
        logger.warn(`[Performance] ${componentName} has re-rendered ${renderCount.value} times`, {
          suggestion: 'Consider using computed properties or memoization'
        })
      }
    },
    { deep: true }
  )

  return {
    renderCount
  }
}

/**
 * Monitor memory usage (browser API)
 */
export function useMemoryMonitoring() {
  const checkMemory = () => {
    if ('memory' in performance) {
      const memory = (performance as any).memory
      return {
        usedJSHeapSize: memory.usedJSHeapSize,
        totalJSHeapSize: memory.totalJSHeapSize,
        jsHeapSizeLimit: memory.jsHeapSizeLimit,
        usagePercentage: (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100
      }
    }
    return null
  }

  const logMemory = () => {
    const memory = checkMemory()
    if (memory && import.meta.env.DEV) {
      logger.info('[Performance] Memory Usage', {
        used: `${(memory.usedJSHeapSize / 1048576).toFixed(2)} MB`,
        total: `${(memory.totalJSHeapSize / 1048576).toFixed(2)} MB`,
        limit: `${(memory.jsHeapSizeLimit / 1048576).toFixed(2)} MB`,
        usage: `${memory.usagePercentage.toFixed(2)}%`
      })

      if (memory.usagePercentage > 80) {
        logger.warn('[Performance] High memory usage detected', {
          usage: `${memory.usagePercentage.toFixed(2)}%`
        })
      }
    }
  }

  return {
    checkMemory,
    logMemory
  }
}

/**
 * Global performance monitoring controls
 */
export function startPerformanceMonitoring() {
  isMonitoring.value = true
  metrics.value = []

  if (import.meta.env.DEV) {
    logger.info('[Performance] Monitoring started')
  }
}

export function stopPerformanceMonitoring() {
  isMonitoring.value = false

  if (import.meta.env.DEV) {
    logger.info('[Performance] Monitoring stopped', {
      totalMetrics: metrics.value.length
    })
  }
}

export function getGlobalMetrics() {
  return metrics.value
}

export function clearGlobalMetrics() {
  metrics.value = []
}
