import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useOnboardingStore } from '@/stores/onboarding'
import { logger } from '@/utils/logger'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    redirect: '/onboarding'
  },
  {
    path: '/onboarding',
    name: 'onboarding',
    component: () => import('@/views/OnboardingView.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/patrimoine',
    name: 'patrimoine',
    component: () => import('@/views/PatrimoineView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/comptes',
    name: 'comptes',
    component: () => import('@/views/ComptesView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/objectifs',
    name: 'objectifs',
    component: () => import('@/views/ObjectifsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/transactions',
    name: 'transactions',
    component: () => import('@/views/TransactionsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profil',
    name: 'profil',
    component: () => import('@/views/UserProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * Navigation Guards for mono-user Budget Manager app
 *
 * Authentication Logic:
 * - "Auth" means the user has completed onboarding
 * - State is persisted in localStorage (onboarding-completed, user-id)
 * - State is loaded once on first navigation and cached
 */

// Cache to avoid repeated state loading
let stateLoaded = false
let stateLoadError = false

/**
 * Load onboarding state from localStorage
 * This is called once on first navigation
 */
async function loadOnboardingState(): Promise<boolean> {
  if (stateLoaded) return true
  if (stateLoadError) return false

  const onboardingStore = useOnboardingStore()

  try {
    await onboardingStore.loadState()
    stateLoaded = true

    logger.info('Router: Onboarding state loaded', {
      completed: onboardingStore.isCompleted,
      hasUser: onboardingStore.currentUser !== null
    })

    return true
  } catch (error) {
    stateLoadError = true
    logger.error('Router: Failed to load onboarding state', error)
    return false
  }
}

/**
 * Main navigation guard
 */
router.beforeEach(async (to, from, next) => {
  // Load state on first navigation
  await loadOnboardingState()

  const onboardingStore = useOnboardingStore()
  const isAuthenticated = onboardingStore.isCompleted

  // Protected routes (require completed onboarding)
  if (to.meta.requiresAuth) {
    if (!isAuthenticated) {
      logger.info('Router: Protected route accessed without auth, redirecting to onboarding', {
        from: from.path,
        to: to.path
      })
      next({ name: 'onboarding', query: { redirect: to.fullPath } })
      return
    }
  }

  // Guest routes (onboarding should not be completed)
  if (to.meta.requiresGuest) {
    if (isAuthenticated) {
      logger.info('Router: Guest route accessed with auth, redirecting to dashboard', {
        from: from.path,
        to: to.path
      })
      next({ name: 'dashboard' })
      return
    }
  }

  // Allow navigation
  next()
})

/**
 * Reset the state loading cache
 * Call this when user logs out or app is reset
 */
export const resetAuthState = () => {
  stateLoaded = false
  stateLoadError = false
  logger.info('Router: Auth state reset')
}

export default router
