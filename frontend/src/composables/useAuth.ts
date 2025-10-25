import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useOnboardingStore } from '@/stores/onboarding'
import { logger } from '@/utils/logger'

/**
 * Authentication composable for mono-user app
 *
 * In this app, "authentication" means the user has completed onboarding.
 * The onboarding state is persisted in localStorage.
 */
export function useAuth() {
  const router = useRouter()
  const onboardingStore = useOnboardingStore()

  /**
   * Check if user has completed onboarding (is "authenticated")
   */
  const isAuthenticated = computed(() => onboardingStore.isCompleted)

  /**
   * Check if user data is available
   */
  const hasUser = computed(() => onboardingStore.currentUser !== null)

  /**
   * Get current user
   */
  const currentUser = computed(() => onboardingStore.currentUser)

  /**
   * Check localStorage for persisted onboarding state
   * Returns true if onboarding was completed previously
   */
  const checkPersistedAuth = (): boolean => {
    const completed = localStorage.getItem('onboarding-completed')
    const userId = localStorage.getItem('user-id')
    return completed === 'true' && userId !== null
  }

  /**
   * Load authentication state from localStorage
   * This should be called on app initialization
   */
  const loadAuthState = async (): Promise<boolean> => {
    try {
      await onboardingStore.loadState()
      logger.info('Auth state loaded:', {
        isCompleted: onboardingStore.isCompleted,
        hasUser: hasUser.value
      })
      return onboardingStore.isCompleted
    } catch (error) {
      logger.error('Failed to load auth state:', error)
      return false
    }
  }

  /**
   * Logout - clear onboarding state
   */
  const logout = () => {
    onboardingStore.clearState()
    router.push('/onboarding')
    logger.info('User logged out')
  }

  /**
   * Navigate to dashboard if authenticated, otherwise to onboarding
   */
  const navigateToDefault = () => {
    if (isAuthenticated.value) {
      router.push('/dashboard')
    } else {
      router.push('/onboarding')
    }
  }

  /**
   * Require authentication for current route
   * Redirects to onboarding if not authenticated
   */
  const requireAuth = (): boolean => {
    if (!isAuthenticated.value) {
      logger.warn('Authentication required, redirecting to onboarding')
      router.push('/onboarding')
      return false
    }
    return true
  }

  return {
    // State
    isAuthenticated,
    hasUser,
    currentUser,

    // Methods
    checkPersistedAuth,
    loadAuthState,
    logout,
    navigateToDefault,
    requireAuth
  }
}
