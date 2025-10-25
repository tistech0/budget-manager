<template>
    <div class="step-content">
      <div class="step-header">
        <h1 class="step-title">Vos banques</h1>
        <p class="step-subtitle">
          Sélectionnez les banques avec lesquelles vous avez des comptes
        </p>
      </div>
  
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Chargement des banques...</p>
      </div>
  
      <div v-else-if="availableBanks.length === 0" class="empty-state">
        <p>Aucune banque disponible pour le moment.</p>
        <button @click="loadBanks" class="btn btn-secondary">
          Recharger
        </button>
      </div>
  
      <div v-else class="banks-grid">
        <div 
          v-for="bank in availableBanks"
          :key="bank.id"
          class="bank-card"
          :class="{ selected: isBankSelected(bank) }"
          @click="toggleBank(bank)"
          @keydown.enter="toggleBank(bank)"
          @keydown.space.prevent="toggleBank(bank)"
          tabindex="0"
          :aria-pressed="isBankSelected(bank)"
          role="button"
        >
          <div class="bank-header">
            <div 
              class="bank-color"
              :style="{ backgroundColor: bank.couleurTheme }"
              :aria-hidden="true"
            ></div>
            <div class="bank-info">
              <h3 class="bank-name">{{ bank.nom }}</h3>
            </div>
            <div class="bank-check" :aria-hidden="true">
              <span v-if="isBankSelected(bank)" class="check-icon">✓</span>
            </div>
          </div>
        </div>
      </div>
  
      <div v-if="selectedBanks.length > 0" class="selected-summary">
        <h3 class="selected-title">Banques sélectionnées ({{ selectedBanks.length }})</h3>
        <div class="selected-banks">
          <span 
            v-for="bank in selectedBanks"
            :key="bank.id"
            class="selected-bank-tag"
            :style="{ borderColor: bank.couleurTheme }"
          >
            <div 
              class="tag-color"
              :style="{ backgroundColor: bank.couleurTheme }"
            ></div>
            {{ bank.nom }}
          </span>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { storeToRefs } from 'pinia'
  import { useOnboardingStore } from '@/stores/onboarding'
  
  const onboardingStore = useOnboardingStore()
  const { loading, availableBanks, selectedBanks } = storeToRefs(onboardingStore)
  const { toggleBank, isBankSelected, loadBanks } = onboardingStore
  </script>
  
  <style scoped>
  .selected-summary {
    margin-top: 2rem;
    padding: 1.5rem;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-lg);
  }
  
  .selected-title {
    color: var(--text-primary);
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .selected-title::before {
    content: '✓';
    color: var(--success-color);
    font-size: 1.2rem;
  }
  
  .selected-banks {
    display: flex;
    flex-wrap: wrap;
    gap: 0.75rem;
  }
  
  .selected-bank-tag {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-md);
    color: var(--text-primary);
    font-size: 0.9rem;
    font-weight: 500;
    transition: all var(--transition-fast);
  }
  
  .selected-bank-tag:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateY(-1px);
  }
  
  .tag-color {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }
  
  .loading-container {
    text-align: center;
    padding: 3rem 1rem;
    color: var(--text-secondary);
  }
  
  .loading-container .loading-spinner {
    margin: 0 auto 1rem auto;
  }
  </style>