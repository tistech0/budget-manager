# Frontend Performance Optimization Guidelines

**Last Updated:** 2025-10-24
**Status:** Active

This document provides comprehensive guidelines for optimizing performance in the Budget Manager frontend application.

---

## Table of Contents

1. [Performance Principles](#performance-principles)
2. [Build Optimizations](#build-optimizations)
3. [Component Optimization](#component-optimization)
4. [Data Management](#data-management)
5. [Code Splitting & Lazy Loading](#code-splitting--lazy-loading)
6. [Performance Monitoring](#performance-monitoring)
7. [Testing Performance](#testing-performance)
8. [Best Practices](#best-practices)
9. [Common Pitfalls](#common-pitfalls)
10. [Performance Checklist](#performance-checklist)

---

## Performance Principles

### Core Principles

1. **Measure First, Optimize Second**
   - Always profile before optimizing
   - Use browser DevTools Performance tab
   - Use the `usePerformance` composable for tracking

2. **Optimize for Perceived Performance**
   - First Contentful Paint (FCP) < 1.8s
   - Time to Interactive (TTI) < 3.9s
   - Cumulative Layout Shift (CLS) < 0.1

3. **Progressive Enhancement**
   - Start with minimal bundle
   - Load additional features on demand
   - Prioritize critical rendering path

---

## Build Optimizations

### Vite Configuration

Our optimized `vite.config.ts` includes:

```typescript
// ✅ Manual chunk splitting
manualChunks: {
  'vue-vendor': ['vue', 'vue-router', 'pinia'],
  'validation': ['zod']
}

// ✅ Directory-based code splitting
chunkFileNames: (chunkInfo) => {
  if (facadeModuleId.includes('/views/')) {
    return 'assets/views/[name]-[hash].js'
  }
  // ... more patterns
}

// ✅ Tree-shaking optimization
pure: ['console.log', 'console.info', 'console.debug']
```

### Bundle Size Targets

| Asset Type | Target Size | Warning Threshold |
|------------|-------------|-------------------|
| Initial JS Bundle | < 200 KB | 300 KB |
| Vendor Chunk | < 150 KB | 200 KB |
| Route Chunk | < 100 KB | 150 KB |
| CSS Bundle | < 50 KB | 75 KB |

### Build Commands

```bash
# Production build with analysis
npm run build

# Preview production build
npm run preview

# Check bundle size
du -sh dist/assets/*
```

---

## Component Optimization

### 1. Use Computed Properties

**❌ Bad - Re-calculates on every render:**
```vue
<template>
  <div>{{ expenses.filter(e => e.amount > 100).length }}</div>
</template>
```

**✅ Good - Cached until dependencies change:**
```vue
<script setup lang="ts">
const expensiveExpenses = computed(() =>
  expenses.value.filter(e => e.amount > 100)
)
</script>

<template>
  <div>{{ expensiveExpenses.length }}</div>
</template>
```

### 2. Avoid Reactive Data Overhead

**❌ Bad - Everything is reactive:**
```typescript
const state = reactive({
  data: [], // changes frequently
  config: {}, // never changes
  constants: {} // never changes
})
```

**✅ Good - Only reactive when needed:**
```typescript
const data = ref([]) // reactive
const config = Object.freeze({ ... }) // immutable
const CONSTANTS = { ... } // static
```

### 3. Use v-show vs v-if Wisely

**Use `v-if` when:**
- Condition rarely changes
- Component is heavy to render
- Conditional rendering is based on route/auth

**Use `v-show` when:**
- Toggled frequently
- Component is lightweight
- Need instant visibility toggle

### 4. Virtual Scrolling for Large Lists

For lists > 100 items, use virtual scrolling:

```vue
<!-- Using vue-virtual-scroller -->
<RecycleScroller
  :items="transactions"
  :item-size="60"
  key-field="id"
>
  <template #default="{ item }">
    <TransactionItem :transaction="item" />
  </template>
</RecycleScroller>
```

### 5. Debounce User Input

```vue
<script setup lang="ts">
import { debounce } from 'lodash-es'

const searchTransactions = debounce((query: string) => {
  // Expensive search operation
  store.searchTransactions(query)
}, 300)
</script>

<template>
  <input @input="searchTransactions($event.target.value)" />
</template>
```

---

## Data Management

### Store Optimization

**✅ Use Computed Getters:**
```typescript
// In Pinia store
const totalExpenses = computed(() => {
  return transactions.value.reduce((sum, t) => sum + t.amount, 0)
})
```

**✅ Selective Updates:**
```typescript
// ❌ Bad - Updates entire array
transactions.value = [...updatedTransactions]

// ✅ Good - Updates single item
const index = transactions.value.findIndex(t => t.id === id)
transactions.value[index] = updatedTransaction
```

**✅ Lazy Data Loading:**
```typescript
// Load data only when needed
const loadCharges = async () => {
  if (!chargesFixes.value.length) {
    await fetchCharges()
  }
}
```

### API Call Optimization

**Use the Performance Composable:**
```typescript
import { useApiPerformance } from '@/composables/usePerformance'

const { trackApiCall } = useApiPerformance()

const fetchDashboard = async () => {
  return trackApiCall('GET', '/api/dashboard', async () => {
    const response = await apiService.getDashboard(month)
    return response.data
  })
}
```

**Request Deduplication:**
```typescript
// Prevent duplicate simultaneous requests
let pendingRequest: Promise<T> | null = null

const fetchData = async () => {
  if (pendingRequest) return pendingRequest

  pendingRequest = apiService.getData()
  const result = await pendingRequest
  pendingRequest = null

  return result
}
```

---

## Code Splitting & Lazy Loading

### Route-Level Splitting (✅ Already Implemented)

```typescript
// router/index.ts
const routes = [
  {
    path: '/dashboard',
    component: () => import('@/views/DashboardView.vue')
  }
]
```

### Component-Level Lazy Loading

**For Heavy Components:**
```vue
<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

const BudgetChart = defineAsyncComponent(() =>
  import('@/components/BudgetPieChart.vue')
)
</script>
```

**With Loading State:**
```typescript
const BudgetChart = defineAsyncComponent({
  loader: () => import('@/components/BudgetPieChart.vue'),
  loadingComponent: LoadingSpinner,
  delay: 200,
  timeout: 3000
})
```

### Modal Lazy Loading

```vue
<script setup lang="ts">
const ValidateSalaryModal = defineAsyncComponent(() =>
  import('@/components/modals/ValidateSalaryModal.vue')
)

const showModal = ref(false)
</script>

<template>
  <!-- Modal only loads when needed -->
  <ValidateSalaryModal v-if="showModal" />
</template>
```

---

## Performance Monitoring

### Using the Performance Composable

**Component Performance:**
```vue
<script setup lang="ts">
import { usePerformance } from '@/composables/usePerformance'

const { measureAsync, mark } = usePerformance('DashboardView')

onMounted(async () => {
  await measureAsync('loadData', async () => {
    await loadDashboardData()
  })

  mark('initialRenderComplete')
})
</script>
```

**Render Performance:**
```typescript
import { useRenderPerformance } from '@/composables/usePerformance'

const { renderCount } = useRenderPerformance('ExpensiveComponent', () => [
  transactions.value,
  filters.value
])

// Warns if component re-renders > 10 times
```

**Memory Monitoring:**
```typescript
import { useMemoryMonitoring } from '@/composables/usePerformance'

const { logMemory } = useMemoryMonitoring()

// Check memory usage
onMounted(() => {
  logMemory()
})
```

### Browser DevTools

**Performance Tab:**
1. Open DevTools → Performance
2. Click Record
3. Perform user interaction
4. Stop recording
5. Analyze flame graph

**Key Metrics to Watch:**
- Scripting time
- Rendering time
- Painting time
- Long tasks (> 50ms)

---

## Testing Performance

### Use Performance Test Helpers

```typescript
import {
  measureMountTime,
  assertPerformance,
  PERFORMANCE_THRESHOLDS
} from '@/__tests__/utils/performance-helpers'

it('should mount quickly', async () => {
  const { wrapper, duration } = await measureMountTime(() =>
    mount(DashboardView)
  )

  assertPerformance(
    duration,
    PERFORMANCE_THRESHOLDS.MOUNT_TIME,
    'Dashboard mount time exceeds threshold'
  )
})
```

### Benchmark Critical Paths

```typescript
import { benchmark } from '@/__tests__/utils/performance-helpers'

it('should filter transactions efficiently', async () => {
  const result = await benchmark(
    'Filter 1000 transactions',
    () => filterTransactions(largeDataset, criteria),
    100
  )

  expect(result.average).toBeLessThan(10) // < 10ms average
})
```

---

## Best Practices

### ✅ DO

1. **Use Composables for Shared Logic**
   - Reduces code duplication
   - Easier to test and optimize

2. **Implement Proper Loading States**
   - Better perceived performance
   - Prevents layout shift

3. **Use Proper Key Attributes**
   ```vue
   <div v-for="item in items" :key="item.id">
     <!-- Helps Vue optimize re-renders -->
   </div>
   ```

4. **Freeze Large Static Data**
   ```typescript
   const STATIC_CONFIG = Object.freeze({
     // Large configuration object
   })
   ```

5. **Use `shallowRef` for Large Objects**
   ```typescript
   // Only tracks reference, not deep changes
   const largeObject = shallowRef({ /* ... */ })
   ```

### ❌ DON'T

1. **Don't Use Watchers for Computed Values**
   ```typescript
   // ❌ Bad
   watch([a, b], () => {
     c.value = a.value + b.value
   })

   // ✅ Good
   const c = computed(() => a.value + b.value)
   ```

2. **Don't Create Functions in Templates**
   ```vue
   <!-- ❌ Bad - Creates new function on every render -->
   <button @click="() => doSomething(item)">

   <!-- ✅ Good - Stable reference -->
   <button @click="handleClick(item)">
   ```

3. **Don't Deep Watch Unless Necessary**
   ```typescript
   // ❌ Bad - Expensive
   watch(state, () => { }, { deep: true })

   // ✅ Good - Watch specific properties
   watch(() => state.property, () => { })
   ```

4. **Don't Forget to Cleanup**
   ```typescript
   onBeforeUnmount(() => {
     clearInterval(timer)
     eventBus.off('event', handler)
     abortController.abort()
   })
   ```

---

## Common Pitfalls

### 1. Memory Leaks

**Problem:** Event listeners not removed
```typescript
// ❌ Bad
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

// ✅ Good
onMounted(() => {
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
```

### 2. Unnecessary Re-renders

**Problem:** Unstable object references
```vue
<!-- ❌ Bad - New object every render -->
<Component :options="{ mode: 'dark' }" />

<!-- ✅ Good - Stable reference -->
<script setup>
const options = { mode: 'dark' }
</script>
<Component :options="options" />
```

### 3. Blocking Main Thread

**Problem:** Heavy computation in component
```typescript
// ❌ Bad - Blocks UI
const processData = () => {
  return heavyDataProcessing(largeDataset)
}

// ✅ Good - Use Web Worker or async chunking
const processData = async () => {
  const chunks = chunkArray(largeDataset, 100)
  const results = []

  for (const chunk of chunks) {
    results.push(...processChunk(chunk))
    await nextTick() // Let browser breathe
  }

  return results
}
```

---

## Performance Checklist

### Before Committing Code

- [ ] No unnecessary reactive data
- [ ] Computed properties used instead of watchers where possible
- [ ] Proper keys on v-for loops
- [ ] Event listeners cleaned up
- [ ] No inline functions in templates
- [ ] Large lists use virtual scrolling
- [ ] Heavy components are lazy loaded
- [ ] API calls are tracked and optimized

### Before Releasing

- [ ] Bundle size within targets
- [ ] No console logs in production
- [ ] Source maps disabled in production
- [ ] All routes lazy loaded
- [ ] Critical CSS inlined
- [ ] Images optimized
- [ ] Lighthouse score > 90
- [ ] No memory leaks (DevTools Memory profiler)

### Performance Testing

- [ ] Component mount times tested
- [ ] Critical paths benchmarked
- [ ] Memory usage monitored
- [ ] Re-render count tracked
- [ ] API response times measured

---

## Tools & Resources

### Development Tools

- **Vue DevTools:** Component performance profiling
- **Chrome DevTools:** Performance tab, Memory profiler
- **Lighthouse:** Overall performance audit
- **Bundle Analyzer:** Visualize bundle composition

### Monitoring Tools

- `usePerformance` composable (internal)
- Performance test helpers
- Browser Performance API
- Memory API (Chrome)

### Useful Commands

```bash
# Analyze bundle
npm run build -- --mode production

# Preview production
npm run preview

# Run performance tests
npm run test:unit -- --run performance

# Type check (should be fast)
npm run type-check
```

---

## Performance Targets

### Load Performance

| Metric | Target | Good | Poor |
|--------|--------|------|------|
| FCP | < 1.0s | < 1.8s | > 3.0s |
| LCP | < 1.5s | < 2.5s | > 4.0s |
| TTI | < 2.5s | < 3.9s | > 7.3s |
| CLS | < 0.05 | < 0.1 | > 0.25 |
| TBT | < 150ms | < 300ms | > 600ms |

### Runtime Performance

| Operation | Target | Max |
|-----------|--------|-----|
| Component Mount | < 100ms | 200ms |
| Re-render | < 50ms | 100ms |
| API Call | < 500ms | 1000ms |
| User Interaction | < 16ms | 50ms |
| Search/Filter | < 100ms | 300ms |

---

## Updates & Improvements

### Recent Optimizations (2025-10-24)

- ✅ Vite config optimized with manual chunk splitting
- ✅ Performance monitoring composable created
- ✅ Performance test utilities added
- ✅ Routes already using lazy loading
- ✅ Console logs dropped in production
- ✅ ES2020 target for production builds

### Future Improvements

- [ ] Implement virtual scrolling for transaction lists
- [ ] Add service worker for offline support
- [ ] Optimize images with WebP format
- [ ] Implement HTTP/2 push for critical assets
- [ ] Add performance budgets to CI/CD
- [ ] Implement request deduplication globally

---

## Getting Help

**Questions?**
- Review this guide
- Check browser DevTools
- Use performance composables
- Run performance tests

**Performance Issues?**
1. Profile with DevTools
2. Check bundle size
3. Review component render counts
4. Monitor memory usage
5. Test with production build

---

**Document Version:** 1.0
**Last Reviewed:** 2025-10-24
**Next Review:** 2025-11-24
