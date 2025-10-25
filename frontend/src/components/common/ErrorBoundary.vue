<template>
  <div class="error-boundary">
    <div
      v-if="hasError"
      class="error-container glass-card"
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
      ref="errorContainer"
    >
      <div class="error-icon" aria-hidden="true">⚠️</div>
      <h2 class="error-title" id="error-title">{{ errorTitle }}</h2>
      <p class="error-message" id="error-message">{{ errorMessage }}</p>

      <div v-if="errorDetails" class="error-details">
        <button
          @click="toggleDetails"
          @keydown.enter="toggleDetails"
          @keydown.space.prevent="toggleDetails"
          class="details-toggle"
          :aria-expanded="showDetails"
          aria-controls="error-stack"
        >
          {{ showDetails ? 'Masquer les détails' : 'Afficher les détails' }}
        </button>
        <pre
          v-if="showDetails"
          class="error-stack"
          id="error-stack"
          role="region"
          aria-label="Détails de l'erreur"
          tabindex="0"
        >{{ errorDetails }}</pre>
      </div>

      <div class="error-actions" role="group" aria-label="Actions">
        <button
          @click="handleRetry"
          @keydown.enter="handleRetry"
          class="btn btn-primary"
          ref="retryButton"
        >
          <span aria-hidden="true">🔄</span> Réessayer
        </button>
        <button
          v-if="showHome"
          @click="goHome"
          @keydown.enter="goHome"
          class="btn btn-secondary"
        >
          <span aria-hidden="true">🏠</span> Retour au tableau de bord
        </button>
      </div>
    </div>

    <slot v-else></slot>
  </div>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { logger } from '@/utils/logger'

interface Props {
  title?: string
  message?: string
  showHome?: boolean
  onRetry?: () => void | Promise<void>
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Une erreur est survenue',
  message: 'Nous sommes désolés, une erreur inattendue s\'est produite.',
  showHome: true
})

const emit = defineEmits<{
  error: [error: Error]
  retry: []
}>()

const router = useRouter()
const hasError = ref(false)
const error = ref<Error | null>(null)
const showDetails = ref(false)
const errorContainer = ref<HTMLElement | null>(null)
const retryButton = ref<HTMLButtonElement | null>(null)

const errorTitle = computed(() => props.title)
const errorMessage = computed(() => {
  if (error.value) {
    return error.value.message || props.message
  }
  return props.message
})

const errorDetails = computed(() => {
  if (error.value) {
    return error.value.stack || error.value.toString()
  }
  return null
})

// Capture errors from child components
onErrorCaptured((err: Error) => {
  hasError.value = true
  error.value = err

  logger.error('ErrorBoundary caught error:', {
    message: err.message,
    stack: err.stack
  })

  emit('error', err)

  // Prevent error from propagating
  return false
})

// Focus management - focus the retry button when error appears
watch(hasError, async (newVal) => {
  if (newVal) {
    await nextTick()
    retryButton.value?.focus()
  }
})

const toggleDetails = () => {
  showDetails.value = !showDetails.value
}

const handleRetry = async () => {
  hasError.value = false
  error.value = null
  showDetails.value = false

  if (props.onRetry) {
    try {
      await props.onRetry()
    } catch (err) {
      logger.error('Retry failed:', err)
      hasError.value = true
      error.value = err as Error
    }
  }

  emit('retry')
}

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.error-boundary {
  width: 100%;
  min-height: 200px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
  max-width: 600px;
  margin: 2rem auto;
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}

.error-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #E8EAF6;
  margin: 0 0 1rem 0;
}

.error-message {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.6;
  margin: 0 0 2rem 0;
}

.error-details {
  width: 100%;
  margin-bottom: 2rem;
}

.details-toggle {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.details-toggle:hover {
  background: rgba(255, 255, 255, 0.15);
}

.details-toggle:focus-visible {
  outline: 3px solid #667EEA;
  outline-offset: 2px;
}

.error-stack {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: #FF6B6B;
  font-size: 0.75rem;
  text-align: left;
  overflow-x: auto;
  max-height: 200px;
  overflow-y: auto;
}

.error-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
}

.btn:focus-visible {
  outline: 3px solid #667EEA;
  outline-offset: 2px;
}

.error-stack:focus-visible {
  outline: 2px solid rgba(255, 255, 255, 0.5);
  outline-offset: 2px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

@media (max-width: 768px) {
  .error-container {
    padding: 2rem 1.5rem;
  }

  .error-icon {
    font-size: 3rem;
  }

  .error-title {
    font-size: 1.5rem;
  }

  .error-actions {
    flex-direction: column;
    width: 100%;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
