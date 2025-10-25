<template>
  <div class="dashboard-stats" role="region" aria-label="Statistiques financières">
    <article
      class="stat-card glass-card"
      role="article"
      aria-labelledby="stat-accounts-label"
    >
      <div class="stat-header">
        <span class="stat-icon" aria-hidden="true">💳</span>
        <span class="stat-label" id="stat-accounts-label">Comptes</span>
      </div>
      <div class="stat-value" aria-label="Total des comptes">
        {{ formatCurrency(totalAccounts) }}
      </div>
      <div class="stat-footer">
        <span class="stat-count" aria-label="Nombre de comptes">
          {{ accountsCount }} compte{{ accountsCount > 1 ? 's' : '' }}
        </span>
      </div>
    </article>

    <article
      class="stat-card glass-card"
      role="article"
      aria-labelledby="stat-goals-label"
    >
      <div class="stat-header">
        <span class="stat-icon" aria-hidden="true">🎯</span>
        <span class="stat-label" id="stat-goals-label">Objectifs</span>
      </div>
      <div class="stat-value" aria-label="Total des objectifs">
        {{ formatCurrency(totalGoals) }}
      </div>
      <div class="stat-footer">
        <span class="stat-count" aria-label="Nombre d'objectifs">
          {{ goalsCount }} objectif{{ goalsCount > 1 ? 's' : '' }}
        </span>
        <span
          class="stat-progress"
          :aria-label="`${goalProgress} pourcent de progression atteint`"
        >
          {{ goalProgress }}% atteint
        </span>
      </div>
    </article>

    <article
      class="stat-card glass-card"
      role="article"
      aria-labelledby="stat-free-label"
    >
      <div class="stat-header">
        <span class="stat-icon" aria-hidden="true">💰</span>
        <span class="stat-label" id="stat-free-label">Épargne libre</span>
      </div>
      <div
        class="stat-value"
        :class="{ 'positive': freeMoneyClass === 'positive', 'negative': freeMoneyClass === 'negative' }"
        :aria-label="`Épargne libre: ${formatCurrency(freeMoney)}, ${freeMoneyInfo}`"
      >
        {{ formatCurrency(freeMoney) }}
      </div>
      <div class="stat-footer">
        <span class="stat-info">{{ freeMoneyInfo }}</span>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatCurrency } from '@/utils/formatters'

interface Props {
  totalAccounts: number
  accountsCount: number
  totalGoals: number
  totalGoalsTarget: number
  goalsCount: number
  freeMoney: number
}

const props = defineProps<Props>()

const goalProgress = computed(() => {
  if (props.totalGoalsTarget === 0) return 0
  return Math.round((props.totalGoals / props.totalGoalsTarget) * 100)
})

const freeMoneyClass = computed(() => {
  if (props.freeMoney > 0) return 'positive'
  if (props.freeMoney < 0) return 'negative'
  return 'neutral'
})

const freeMoneyInfo = computed(() => {
  if (props.freeMoney > 0) return 'Disponible'
  if (props.freeMoney < 0) return 'Attention'
  return 'Équilibré'
})
</script>

<style scoped>
.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.2);
}

.stat-card:focus-within {
  outline: 3px solid #667EEA;
  outline-offset: 2px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stat-icon {
  font-size: 1.75rem;
}

.stat-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #E8EAF6;
  line-height: 1.2;
}

.stat-value.positive {
  color: #4CAF50;
}

.stat-value.negative {
  color: #F44336;
}

.stat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  padding-top: 0.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-count {
  font-weight: 500;
}

.stat-progress {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.stat-info {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

@media (max-width: 768px) {
  .dashboard-stats {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .stat-card {
    padding: 1.25rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .stat-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>
