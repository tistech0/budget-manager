<template>
  <nav
    class="month-selector"
    role="navigation"
    aria-label="Navigation mensuelle"
  >
    <button
      @click="$emit('change-month', 'prev')"
      @keydown.left="$emit('change-month', 'prev')"
      class="month-btn"
      :disabled="disabled || !canGoToPrevious"
      :aria-label="`Aller au mois précédent`"
      aria-keyshortcuts="ArrowLeft"
    >
      <span aria-hidden="true">←</span>
      <span class="sr-only">Mois précédent</span>
    </button>

    <div class="month-info" role="status" aria-live="polite">
      <h2 class="current-month" id="current-month-label">{{ monthLabel }}</h2>
      <span v-if="periodInfo" class="period-info" aria-label="Période du cycle de paie">
        {{ periodInfo }}
      </span>
    </div>

    <button
      @click="$emit('change-month', 'next')"
      @keydown.right="$emit('change-month', 'next')"
      class="month-btn"
      :disabled="disabled || !canGoToNext"
      :aria-label="`Aller au mois suivant`"
      aria-keyshortcuts="ArrowRight"
    >
      <span aria-hidden="true">→</span>
      <span class="sr-only">Mois suivant</span>
    </button>

    <button
      v-if="showDelete"
      @click="$emit('delete-month')"
      class="month-delete-btn"
      aria-label="Supprimer le mois validé"
    >
      <span aria-hidden="true">🗑️</span>
      <span class="sr-only">Supprimer</span>
    </button>
  </nav>
</template>

<script setup lang="ts">
interface Props {
  monthLabel: string
  periodInfo?: string
  canGoToPrevious?: boolean
  canGoToNext?: boolean
  showDelete?: boolean
  disabled?: boolean
}

withDefaults(defineProps<Props>(), {
  canGoToPrevious: true,
  canGoToNext: true,
  showDelete: false,
  disabled: false
})

defineEmits<{
  'change-month': [direction: 'prev' | 'next']
  'delete-month': []
}>()
</script>

<style scoped>
.month-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 2rem;
}

.month-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.month-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.month-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.month-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  min-width: 300px;
}

.current-month {
  font-size: 1.75rem;
  font-weight: 700;
  color: #E8EAF6;
  margin: 0;
  text-transform: capitalize;
}

.period-info {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.month-delete-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(244, 67, 54, 0.1);
  border: 1px solid rgba(244, 67, 54, 0.3);
  border-radius: 10px;
  font-size: 1.25rem;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: 0.5rem;
}

.month-delete-btn:hover {
  background: rgba(244, 67, 54, 0.2);
  border-color: rgba(244, 67, 54, 0.5);
  transform: scale(1.05);
}

/* Screen reader only - accessible but visually hidden */
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

/* Focus styles for keyboard navigation */
.month-btn:focus-visible,
.month-delete-btn:focus-visible {
  outline: 3px solid #667EEA;
  outline-offset: 2px;
}

@media (max-width: 768px) {
  .month-selector {
    gap: 1rem;
    padding: 1rem;
  }

  .month-btn {
    width: 40px;
    height: 40px;
    font-size: 1.25rem;
  }

  .month-info {
    min-width: 200px;
  }

  .current-month {
    font-size: 1.25rem;
  }

  .period-info {
    font-size: 0.75rem;
  }
}
</style>
