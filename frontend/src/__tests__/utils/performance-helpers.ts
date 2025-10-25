/**
 * Performance Testing Utilities
 * Helper functions for testing component performance
 */

import { nextTick } from 'vue'
import type { VueWrapper } from '@vue/test-utils'

/**
 * Measure component mount time
 */
export async function measureMountTime<T>(
  mountFn: () => VueWrapper<T>
): Promise<{ wrapper: VueWrapper<T>; duration: number }> {
  const start = performance.now()
  const wrapper = mountFn()
  await nextTick()
  const end = performance.now()

  return {
    wrapper,
    duration: end - start
  }
}

/**
 * Measure component update time
 */
export async function measureUpdateTime(
  updateFn: () => void | Promise<void>
): Promise<number> {
  const start = performance.now()
  await updateFn()
  await nextTick()
  const end = performance.now()

  return end - start
}

/**
 * Benchmark a function with multiple iterations
 */
export async function benchmark(
  name: string,
  fn: () => void | Promise<void>,
  iterations: number = 100
): Promise<{
  name: string
  iterations: number
  total: number
  average: number
  min: number
  max: number
}> {
  const durations: number[] = []

  for (let i = 0; i < iterations; i++) {
    const start = performance.now()
    await fn()
    const end = performance.now()
    durations.push(end - start)
  }

  const total = durations.reduce((sum, d) => sum + d, 0)
  const average = total / iterations
  const min = Math.min(...durations)
  const max = Math.max(...durations)

  return {
    name,
    iterations,
    total,
    average,
    min,
    max
  }
}

/**
 * Assert performance threshold
 */
export function assertPerformance(
  duration: number,
  threshold: number,
  message?: string
) {
  if (duration > threshold) {
    throw new Error(
      message ||
        `Performance threshold exceeded: ${duration.toFixed(2)}ms > ${threshold}ms`
    )
  }
}

/**
 * Measure memory usage before and after a function
 */
export async function measureMemory(
  fn: () => void | Promise<void>
): Promise<{
  before: number
  after: number
  delta: number
} | null> {
  if (!('memory' in performance)) {
    return null
  }

  const memory = (performance as any).memory

  // Force garbage collection if available
  if ('gc' in global) {
    (global as any).gc()
  }

  const before = memory.usedJSHeapSize

  await fn()
  await nextTick()

  const after = memory.usedJSHeapSize
  const delta = after - before

  return {
    before,
    after,
    delta
  }
}

/**
 * Wait for component to be fully rendered
 */
export async function waitForRender(wrapper: VueWrapper<any>, timeout = 1000): Promise<void> {
  const start = Date.now()

  while (Date.now() - start < timeout) {
    await nextTick()
    // Check if component has rendered content
    if (wrapper.html().length > 0) {
      return
    }
    await new Promise(resolve => setTimeout(resolve, 10))
  }

  throw new Error('Component render timeout')
}

/**
 * Measure re-renders count
 */
export class RenderCounter {
  private count = 0
  private updateCallback?: () => void

  constructor(wrapper: VueWrapper<any>) {
    // Watch for component updates
    const originalUpdate = wrapper.vm.$forceUpdate
    wrapper.vm.$forceUpdate = () => {
      this.count++
      if (this.updateCallback) {
        this.updateCallback()
      }
      originalUpdate.call(wrapper.vm)
    }
  }

  getCount(): number {
    return this.count
  }

  reset(): void {
    this.count = 0
  }

  onUpdate(callback: () => void): void {
    this.updateCallback = callback
  }
}

/**
 * Performance test suite helper
 */
export class PerformanceTestSuite {
  private results: Map<string, any> = new Map()

  async test(name: string, fn: () => void | Promise<void>): Promise<void> {
    const start = performance.now()

    try {
      await fn()
      const end = performance.now()
      const duration = end - start

      this.results.set(name, {
        name,
        duration,
        status: 'passed'
      })
    } catch (error) {
      const end = performance.now()
      const duration = end - start

      this.results.set(name, {
        name,
        duration,
        status: 'failed',
        error
      })
    }
  }

  getResults() {
    return Array.from(this.results.values())
  }

  getSummary() {
    const results = this.getResults()
    const passed = results.filter(r => r.status === 'passed').length
    const failed = results.filter(r => r.status === 'failed').length
    const totalDuration = results.reduce((sum, r) => sum + r.duration, 0)

    return {
      total: results.length,
      passed,
      failed,
      totalDuration,
      averageDuration: totalDuration / results.length
    }
  }

  printSummary(): void {
    const summary = this.getSummary()
    console.log('\n=== Performance Test Summary ===')
    console.log(`Total Tests: ${summary.total}`)
    console.log(`Passed: ${summary.passed}`)
    console.log(`Failed: ${summary.failed}`)
    console.log(`Total Duration: ${summary.totalDuration.toFixed(2)}ms`)
    console.log(`Average Duration: ${summary.averageDuration.toFixed(2)}ms`)

    this.getResults().forEach(result => {
      const status = result.status === 'passed' ? '✓' : '✗'
      console.log(`  ${status} ${result.name} - ${result.duration.toFixed(2)}ms`)
    })
  }
}

/**
 * Simulate heavy computation for testing
 */
export function simulateHeavyComputation(duration: number = 100): void {
  const start = Date.now()
  while (Date.now() - start < duration) {
    // Busy wait
    Math.random()
  }
}

/**
 * Create large data set for performance testing
 */
export function createLargeDataset<T>(
  itemFactory: (index: number) => T,
  size: number = 1000
): T[] {
  return Array.from({ length: size }, (_, i) => itemFactory(i))
}

/**
 * Measure DOM operations
 */
export async function measureDOMOperations(
  operations: () => void | Promise<void>
): Promise<{
  duration: number
  nodeCount: number
  layoutCount: number
}> {
  // Get initial DOM state
  const initialNodeCount = document.querySelectorAll('*').length

  // Track layout recalculations (if Performance API supports it)
  let layoutCount = 0
  const observer = new PerformanceObserver((list) => {
    for (const entry of list.getEntries()) {
      if (entry.entryType === 'measure' && entry.name.includes('layout')) {
        layoutCount++
      }
    }
  })

  try {
    observer.observe({ entryTypes: ['measure'] })
  } catch {
    // Observer not supported
  }

  const start = performance.now()
  await operations()
  await nextTick()
  const end = performance.now()

  const finalNodeCount = document.querySelectorAll('*').length
  observer.disconnect()

  return {
    duration: end - start,
    nodeCount: finalNodeCount - initialNodeCount,
    layoutCount
  }
}

/**
 * Throttle performance thresholds for CI/local environments
 */
export const PERFORMANCE_THRESHOLDS = {
  MOUNT_TIME: process.env.CI ? 500 : 200,
  UPDATE_TIME: process.env.CI ? 200 : 100,
  API_CALL: process.env.CI ? 2000 : 1000,
  RENDER_TIME: process.env.CI ? 300 : 150
} as const

/**
 * Check if performance API is available
 */
export function hasPerformanceAPI(): boolean {
  return typeof performance !== 'undefined' && 'now' in performance
}

/**
 * Check if memory API is available
 */
export function hasMemoryAPI(): boolean {
  return typeof performance !== 'undefined' && 'memory' in performance
}
