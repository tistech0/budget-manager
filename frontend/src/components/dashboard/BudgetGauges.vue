<template>
  <div class="budget-gauges" role="region" aria-label="Jauges budgétaires">
    <article
      class="gauge-card glass-card"
      v-for="(gauge, index) in gauges"
      :key="gauge.label"
      role="article"
      :aria-labelledby="`gauge-label-${index}`"
    >
      <div class="gauge-header">
        <span class="gauge-label" :id="`gauge-label-${index}`">{{ gauge.label }}</span>
        <span
          class="gauge-percent"
          :class="gauge.statusClass"
          :aria-label="`${Math.round(gauge.percentage)} pourcent utilisé`"
        >
          {{ Math.round(gauge.percentage) }}%
        </span>
      </div>

      <div class="gauge-bar-container" role="group" :aria-label="`Progression ${gauge.label}`">
        <div
          class="gauge-bar"
          role="progressbar"
          :aria-valuenow="Math.round(gauge.percentage)"
          aria-valuemin="0"
          aria-valuemax="100"
          :aria-label="`${gauge.label}: ${Math.round(gauge.percentage)} pourcent`"
        >
          <div
            class="gauge-fill"
            :style="{
              width: `${Math.min(gauge.percentage, 100)}%`,
              background: gauge.color
            }"
          ></div>
        </div>
      </div>

      <div class="gauge-amounts" aria-label="Montants">
        <span class="gauge-current" aria-label="Montant actuel">
          {{ formatCurrency(gauge.current) }}
        </span>
        <span class="gauge-separator" aria-hidden="true">/</span>
        <span class="gauge-target" aria-label="Montant cible">
          {{ formatCurrency(gauge.target) }}
        </span>
      </div>

      <div
        v-if="gauge.remaining !== undefined"
        class="gauge-remaining"
        :aria-label="`Reste à dépenser ou épargner: ${formatCurrency(Math.abs(gauge.remaining))}`"
      >
        <span class="remaining-label">Reste :</span>
        <span class="remaining-value" :class="{ 'negative': gauge.remaining < 0 }">
          {{ formatCurrency(Math.abs(gauge.remaining)) }}
        </span>
      </div>
    </article>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatCurrency } from '@/utils/formatters'

export interface BudgetGauge {
  label: string
  current: number
  target: number
  color: string
  remaining?: number
}

interface Props {
  gauges: BudgetGauge[]
}

const props = defineProps<Props>()

const gaugesWithComputed = computed(() => {
  return props.gauges.map(gauge => ({
    ...gauge,
    percentage: gauge.target > 0 ? (gauge.current / gauge.target) * 100 : 0,
    statusClass: getStatusClass(gauge)
  }))
})

function getStatusClass(gauge: BudgetGauge): string {
  if (gauge.target === 0) return 'neutral'
  const percentage = (gauge.current / gauge.target) * 100

  if (percentage >= 100) return 'over'
  if (percentage >= 80) return 'good'
  if (percentage >= 50) return 'medium'
  return 'low'
}

// Use computed gauges in template
const gauges = gaugesWithComputed
</script>

<style scoped>
.budget-gauges {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.gauge-card {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: all 0.3s ease;
}

.gauge-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.2);
}

.gauge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gauge-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.gauge-percent {
  font-size: 1.125rem;
  font-weight: 700;
  padding: 0.25rem 0.75rem;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
}

.gauge-percent.low {
  color: #FFB74D;
  background: rgba(255, 183, 77, 0.1);
}

.gauge-percent.medium {
  color: #FDD835;
  background: rgba(253, 216, 53, 0.1);
}

.gauge-percent.good {
  color: #4CAF50;
  background: rgba(76, 175, 80, 0.1);
}

.gauge-percent.over {
  color: #F44336;
  background: rgba(244, 67, 54, 0.1);
}

.gauge-bar-container {
  width: 100%;
}

.gauge-bar {
  width: 100%;
  height: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.gauge-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.5s ease;
  position: relative;
}

.gauge-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2));
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.gauge-amounts {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  font-size: 1rem;
}

.gauge-current {
  font-size: 1.5rem;
  font-weight: 700;
  color: #E8EAF6;
}

.gauge-separator {
  color: rgba(255, 255, 255, 0.4);
  font-weight: 600;
}

.gauge-target {
  color: rgba(255, 255, 255, 0.6);
  font-weight: 600;
}

.gauge-remaining {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 0.875rem;
}

.remaining-label {
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.remaining-value {
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}

.remaining-value.negative {
  color: #F44336;
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

@media (max-width: 768px) {
  .budget-gauges {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .gauge-card {
    padding: 1.25rem;
  }

  .gauge-current {
    font-size: 1.25rem;
  }
}
</style>
