<template>
  <div class="loading-state" :class="{ 'fullscreen': fullscreen }">
    <div class="loading-content">
      <div class="loading-spinner" :class="sizeClass"></div>
      <p v-if="message" class="loading-message">{{ message }}</p>
      <p v-if="submessage" class="loading-submessage">{{ submessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  message?: string
  submessage?: string
  size?: 'small' | 'medium' | 'large'
  fullscreen?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  fullscreen: false
})

const sizeClass = computed(() => `size-${props.size}`)
</script>

<style scoped>
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  min-height: 200px;
}

.loading-state.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(8px);
  z-index: 9999;
  min-height: 100vh;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.loading-spinner {
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #667EEA;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-spinner.size-small {
  width: 32px;
  height: 32px;
  border-width: 3px;
}

.loading-spinner.size-medium {
  width: 48px;
  height: 48px;
  border-width: 4px;
}

.loading-spinner.size-large {
  width: 64px;
  height: 64px;
  border-width: 5px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-message {
  font-size: 1rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  text-align: center;
}

.loading-submessage {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  text-align: center;
}

@media (max-width: 768px) {
  .loading-state {
    padding: 3rem 1.5rem;
  }

  .loading-spinner.size-medium {
    width: 40px;
    height: 40px;
  }

  .loading-spinner.size-large {
    width: 56px;
    height: 56px;
  }
}
</style>
