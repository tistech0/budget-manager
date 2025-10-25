<template>
    <div class="jauge-card glass-card">
      <div class="jauge-header">
        <h4 class="jauge-titre">{{ titre }}</h4>
        <span v-if="badge" class="jauge-badge" :class="`badge-${badge.class}`">
          {{ badge.text }}
        </span>
      </div>
      
      <div class="jauge-body">
        <div class="jauge-progress">
          <div 
            class="progress-track"
            :class="{ 'inversed': inversed }"
          >
            <div 
              class="progress-bar"
              :style="{ 
                width: `${Math.min(pourcentage, 100)}%`,
                backgroundColor: getProgressColor
              }"
            ></div>
          </div>
          <span class="progress-percentage">{{ Math.round(pourcentage) }}%</span>
        </div>
        
        <div class="jauge-description">
          {{ description }}
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { computed } from 'vue'
  
  interface Badge {
    text: string
    class: 'success' | 'warning' | 'danger' | 'info'
  }
  
  interface Props {
    titre: string
    valeur: number
    maximum: number
    pourcentage: number
    couleur: string
    description?: string
    inversed?: boolean  // Pour le compte courant (découvert)
    badge?: Badge
  }
  
  const props = withDefaults(defineProps<Props>(), {
    description: '',
    inversed: false
  })
  
  const getProgressColor = computed(() => {
    if (props.inversed) {
      // Pour le découvert : rouge si élevé, vert si bas
      if (props.pourcentage > 75) return '#F44336'
      if (props.pourcentage > 50) return '#FF9800'
      return '#4CAF50'
    }
    
    // Normal : vert si élevé, rouge si bas
    if (props.pourcentage >= 100) return '#4CAF50'
    if (props.pourcentage >= 75) return '#8BC34A'
    if (props.pourcentage >= 50) return '#FFC107'
    if (props.pourcentage >= 25) return '#FF9800'
    return '#F44336'
  })
  </script>
  
  <style scoped>
  .jauge-card {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border: 2px solid rgba(255, 255, 255, 0.25);
    border-radius: 20px;
    padding: 1.5rem;
    transition: all 0.3s;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  }
  
  .jauge-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
    border-color: rgba(255, 255, 255, 0.35);
  }
  
  .jauge-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1.5rem;
  }
  
  .jauge-titre {
    color: white;
    font-size: 1.1rem;
    margin: 0;
    font-weight: 600;
  }
  
  .jauge-badge {
    font-size: 0.75rem;
    padding: 0.25rem 0.75rem;
    border-radius: 12px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .badge-success {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.3) 0%, rgba(5, 150, 105, 0.3) 100%);
    color: #10b981;
    border: 2px solid rgba(16, 185, 129, 0.5);
    box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
  }
  
  .badge-warning {
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.3) 0%, rgba(245, 158, 11, 0.3) 100%);
    color: #fbbf24;
    border: 2px solid rgba(251, 191, 36, 0.5);
    box-shadow: 0 2px 8px rgba(251, 191, 36, 0.3);
  }
  
  .badge-danger {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.3) 0%, rgba(220, 38, 38, 0.3) 100%);
    color: #ef4444;
    border: 2px solid rgba(239, 68, 68, 0.5);
    box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
  }
  
  .badge-info {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.3) 0%, rgba(37, 99, 235, 0.3) 100%);
    color: #3b82f6;
    border: 2px solid rgba(59, 130, 246, 0.5);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  }
  
  .jauge-body {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .jauge-progress {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .progress-track {
    flex: 1;
    height: 16px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 10px;
    overflow: hidden;
    position: relative;
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .progress-track.inversed {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.2) 0%, rgba(5, 150, 105, 0.2) 100%);
  }
  
  .progress-bar {
    height: 100%;
    border-radius: 10px;
    transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
  
  .progress-bar::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.4),
      transparent
    );
    animation: shimmer 2s infinite;
  }
  
  @keyframes shimmer {
    0% {
      transform: translateX(-100%);
    }
    100% {
      transform: translateX(100%);
    }
  }
  
  .progress-percentage {
    color: white;
    font-weight: bold;
    font-size: 1.2rem;
    min-width: 60px;
    text-align: right;
  }
  
  .jauge-description {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.95rem;
    line-height: 1.5;
  }
  
  @media (max-width: 768px) {
    .jauge-card {
      padding: 1.25rem;
    }
    
    .jauge-titre {
      font-size: 1rem;
    }
    
    .progress-percentage {
      font-size: 1rem;
    }
  }
  </style>