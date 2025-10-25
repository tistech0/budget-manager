<template>
  <div class="chart-container">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  type ChartOptions
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend)

interface Objectif {
  nom: string
  progression: number
  couleur?: string
}

interface Props {
  objectifs: Objectif[]
}

const props = defineProps<Props>()

const chartData = computed(() => ({
  labels: props.objectifs.map(obj => obj.nom),
  datasets: [
    {
      label: 'Progression (%)',
      data: props.objectifs.map(obj => obj.progression),
      backgroundColor: props.objectifs.map(obj => obj.couleur || 'rgba(102, 126, 234, 0.8)'),
      borderColor: props.objectifs.map(obj => {
        const color = obj.couleur || 'rgba(102, 126, 234, 1)'
        return color.replace('0.8', '1')
      }),
      borderWidth: 2,
      borderRadius: 8,
      borderSkipped: false
    }
  ]
}))

const chartOptions: ChartOptions<'bar'> = {
  responsive: true,
  maintainAspectRatio: false,
  indexAxis: 'y',
  scales: {
    x: {
      beginAtZero: true,
      max: 100,
      grid: {
        color: 'rgba(255, 255, 255, 0.1)',
        lineWidth: 1
      },
      ticks: {
        color: 'rgba(255, 255, 255, 0.7)',
        font: {
          size: 12
        },
        callback: function(value) {
          return value + '%'
        }
      }
    },
    y: {
      grid: {
        display: false
      },
      ticks: {
        color: 'rgba(255, 255, 255, 0.9)',
        font: {
          size: 13,
          weight: 500
        }
      }
    }
  },
  plugins: {
    legend: {
      display: false
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
          return `Progression: ${context.parsed.x.toFixed(1)}%`
        }
      }
    }
  }
}
</script>

<style scoped>
.chart-container {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>
