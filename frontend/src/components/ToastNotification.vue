<template>
  <div class="toast-container">
    <div
      v-for="toast in toasts"
      :key="toast.id"
      :class="['toast', `toast-${toast.type}`]"
      @click="remove(toast.id)"
    >
      <div class="toast-icon">
        {{ getIcon(toast.type) }}
      </div>
      <div class="toast-message">
        {{ toast.message }}
      </div>
      <button class="toast-close" @click.stop="remove(toast.id)">
        ✕
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useToast, type ToastType } from '@/composables/useToast'

const { toasts, remove } = useToast()

const getIcon = (type: ToastType): string => {
  const icons: Record<ToastType, string> = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  }
  return icons[type]
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 90px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 300px;
  max-width: 500px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  pointer-events: all;
  border-left: 4px solid;
  transition: all 0.3s ease;
}

.toast:hover {
  transform: translateX(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
}

.toast-success {
  border-left-color: #10b981;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-error {
  border-left-color: #ef4444;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-warning {
  border-left-color: #f59e0b;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-info {
  border-left-color: #3b82f6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.toast-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-weight: bold;
  flex-shrink: 0;
}

.toast-success .toast-icon {
  background: #10b981;
  color: white;
}

.toast-error .toast-icon {
  background: #ef4444;
  color: white;
}

.toast-warning .toast-icon {
  background: #f59e0b;
  color: white;
}

.toast-info .toast-icon {
  background: #3b82f6;
  color: white;
}

.toast-message {
  flex: 1;
  color: #1f2937;
  font-weight: 500;
  font-size: 0.95rem;
  line-height: 1.4;
}

.toast-close {
  background: transparent;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  font-size: 18px;
  line-height: 1;
  transition: color 0.2s;
  flex-shrink: 0;
}

.toast-close:hover {
  color: #1f2937;
}

/* Toast animations */
.toast-enter-active {
  animation: toast-in 0.3s ease-out;
}

.toast-leave-active {
  animation: toast-out 0.3s ease-in;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes toast-out {
  from {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
  to {
    opacity: 0;
    transform: translateX(100%) scale(0.9);
  }
}

@media (max-width: 640px) {
  .toast-container {
    top: 70px;
    right: 10px;
    left: 10px;
  }

  .toast {
    min-width: auto;
    max-width: none;
  }
}
</style>
