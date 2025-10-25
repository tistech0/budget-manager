<template>
  <div class="chart-container">
    <Doughnut :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  type ChartOptions
} from 'chart.js'

ChartJS.register(ArcElement, Tooltip, Legend)

interface Props {
  chargesFixes: number
  depensesVariables: number
  epargne: number
}

const props = defineProps<Props>()

const chartData = computed(() => ({
  labels: ['Charges Fixes', 'Dépenses Variables', 'Épargne'],
  datasets: [
    {
      data: [props.chargesFixes, props.depensesVariables, props.epargne],
      backgroundColor: [
        'rgba(239, 68, 68, 0.8)',   // Red for fixed charges
        'rgba(249, 115, 22, 0.8)',  // Orange for variable expenses
        'rgba(34, 197, 94, 0.8)'    // Green for savings
      ],
      borderColor: [
        'rgba(239, 68, 68, 1)',
        'rgba(249, 115, 22, 1)',
        'rgba(34, 197, 94, 1)'
      ],
      borderWidth: 2,
      hoverOffset: 10
    }
  ]
}))

const chartOptions: ChartOptions<'doughnut'> = {
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        color: 'rgba(255, 255, 255, 0.9)',
        padding: 15,
        font: {
          size: 13,
          weight: 500
        },
        usePointStyle: true,
        pointStyle: 'circle'
      }
    },
    tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      padding: 12,
      titleFont: {
        size: 14,
        weight: 'bold'
      },
      bodyFont: {
        size: 13
      },
      callbacks: {
        label: function(context) {
          const label = context.label || ''
          const value = context.parsed || 0
          return `${label}: ${value}%`
        }
      }
    }
  },
  cutout: '60%'
}
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
