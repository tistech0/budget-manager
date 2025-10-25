<template>
    <div class="step-content">
      <div class="step-header">
        <h1 class="step-title">Informations personnelles</h1>
        <p class="step-subtitle">
          Quelques informations de base pour commencer
        </p>
      </div>
  
      <form @submit.prevent="handleSubmit" class="form">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Prénom *</label>
            <input 
              v-model="userData.prenom"
              type="text" 
              class="form-input"
              required
              maxlength="50"
            >
          </div>
          
          <div class="form-group">
            <label class="form-label">Nom *</label>
            <input 
              v-model="userData.nom"
              type="text" 
              class="form-input"
              required
              maxlength="50"
            >
          </div>
        </div>
  
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Jour de paie *</label>
            <select 
              v-model="userData.jourPaie" 
              class="form-select" 
              required
            >
              <option :value="null">Choisissez...</option>
              <option v-for="day in 31" :key="day" :value="day">
                {{ day }}{{ day === 1 ? 'er' : '' }} du mois
              </option>
            </select>
          </div>
          
          <div class="form-group">
            <label class="form-label">Salaire mensuel net *</label>
            <div class="currency-input">
              <input 
                v-model.number="userData.salaireMensuelNet"
                type="number" 
                class="form-input currency-field"
                placeholder="2500"
                step="0.01"
                min="0"
                max="999999.99"
                required
              >
              <span class="currency-symbol">€</span>
            </div>
          </div>
        </div>
  
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Découvert autorisé (optionnel)</label>
            <div class="currency-input">
              <input
                v-model.number="userData.decouvertAutorise"
                type="number"
                class="form-input currency-field"
                placeholder="1000"
                step="0.01"
                min="0"
                max="999999.99"
              >
              <span class="currency-symbol">€</span>
            </div>
            <small class="form-help">
              Utile pour suivre votre situation sur le compte courant
            </small>
          </div>

          <div class="form-group">
            <label class="form-label">Objectif compte courant (optionnel)</label>
            <div class="currency-input">
              <input
                v-model.number="userData.objectifCompteCourant"
                type="number"
                class="form-input currency-field"
                placeholder="3000"
                step="0.01"
                min="0"
                max="999999.99"
              >
              <span class="currency-symbol">€</span>
            </div>
            <small class="form-help">
              Montant pour 100% sur la jauge du compte courant
            </small>
          </div>
        </div>
      </form>
    </div>
  </template>
  
  <script setup lang="ts">
  import { storeToRefs } from 'pinia'
  import { useOnboardingStore } from '@/stores/onboarding'
  
  const onboardingStore = useOnboardingStore()
  const { userData } = storeToRefs(onboardingStore)
  
  const handleSubmit = (): void => {
    if (userData.value.nom && userData.value.prenom && userData.value.jourPaie && userData.value.salaireMensuelNet) {
      onboardingStore.nextStep()
    }
  }
  </script>
  
  <style scoped>
  /* Supprime les flèches des inputs de type number */
  .currency-field::-webkit-outer-spin-button,
  .currency-field::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  
  .currency-field {
    -moz-appearance: textfield;
  }
  
  /* Améliorations pour les inputs avec devise */
  .currency-input {
    position: relative;
    display: flex;
    align-items: center;
  }
  
  .currency-input .form-input {
    padding-right: 35px;
    flex: 1;
    min-width: 0;
  }
  
  .currency-symbol {
    position: absolute;
    right: 12px;
    color: var(--text-secondary);
    font-weight: 600;
    pointer-events: none;
    font-size: 16px;
  }
  
  /* Améliorer l'espacement des form-groups */
  .form-group {
    min-width: 0;
    flex: 1;
  }
  
  /* S'assurer que les inputs prennent toute la largeur disponible */
  .form-input,
  .form-select {
    width: 100%;
    min-height: 48px;
  }
  </style>