<template>
  <div class="step-content">
    <div class="step-header">
      <h1 class="step-title">Répartition de votre budget</h1>
      <p class="step-subtitle">
        Définissez comment répartir votre salaire entre charges fixes, dépenses variables et épargne
      </p>
    </div>

    <div class="budget-config-section">
      <!-- Méthode recommandée -->
      <div class="method-selector">
        <h3>Choisissez votre méthode</h3>
        <div class="methods-grid">
          <div 
            class="method-card"
            :class="{ selected: selectedMethod === '50-30-20' }"
            @click="selectMethod('50-30-20')"
          >
            <h4>📊 Règle 50/30/20</h4>
            <p>Méthode recommandée pour débuter</p>
            <div class="method-breakdown">
              <span>50% Charges fixes</span>
              <span>30% Dépenses variables</span>
              <span>20% Épargne</span>
            </div>
          </div>
          
          <div 
            class="method-card"
            :class="{ selected: selectedMethod === 'custom' }"
            @click="selectMethod('custom')"
          >
            <h4>🎯 Personnalisé</h4>
            <p>Adaptez selon votre situation</p>
            <div class="method-breakdown">
              <span>Charges fixes variables</span>
              <span>Dépenses ajustables</span>
              <span>Épargne optimisée</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Configuration des pourcentages -->
      <div class="budget-configuration">
        <h3>Configuration de votre répartition</h3>
        
        <div class="config-form">
          <div class="budget-sliders">
            <div class="slider-group">
              <label class="slider-label">
                <span class="category-icon">🏠</span>
                <span>Charges fixes</span>
                <span class="percentage-display">{{ budgetConfig.pourcentageChargesFixes }}%</span>
              </label>
              <input 
                v-model.number="budgetConfig.pourcentageChargesFixes"
                type="range"
                min="0"
                max="100"
                step="1"
                class="budget-slider charges-fixes"
                @input="updateBudget"
              >
              <div class="slider-amount">{{ formatCurrency(montantChargesFixes) }}</div>
            </div>

            <div class="slider-group">
              <label class="slider-label">
                <span class="category-icon">🛍️</span>
                <span>Dépenses variables</span>
                <span class="percentage-display">{{ budgetConfig.pourcentageDepensesVariables }}%</span>
              </label>
              <input 
                v-model.number="budgetConfig.pourcentageDepensesVariables"
                type="range"
                min="0"
                max="100"
                step="1"
                class="budget-slider depenses-variables"
                @input="updateBudget"
              >
              <div class="slider-amount">{{ formatCurrency(montantDepensesVariables) }}</div>
            </div>

            <div class="slider-group">
              <label class="slider-label">
                <span class="category-icon">💰</span>
                <span>Épargne</span>
                <span class="percentage-display">{{ budgetConfig.pourcentageEpargne }}%</span>
              </label>
              <input 
                v-model.number="budgetConfig.pourcentageEpargne"
                type="range"
                min="0"
                max="100"
                step="1"
                class="budget-slider epargne"
                @input="updateBudget"
              >
              <div class="slider-amount">{{ formatCurrency(montantEpargne) }}</div>
            </div>
          </div>

          <!-- Validation du total -->
          <div class="budget-validation" :class="{ valid: isValidTotal, invalid: !isValidTotal }">
            <div class="validation-content">
              <span class="validation-icon">{{ isValidTotal ? '✅' : '⚠️' }}</span>
              <span class="validation-text">
                Total : {{ totalPercentage }}% 
                {{ isValidTotal ? '(Parfait !)' : '(Doit égaler 100%)' }}
              </span>
            </div>
            <div v-if="!isValidTotal" class="validation-suggestion">
              {{ totalPercentage > 100 ? 'Réduisez' : 'Augmentez' }} de {{ Math.abs(100 - totalPercentage) }}%
            </div>
          </div>

          <!-- Aperçu du budget -->
          <div class="budget-preview">
            <h4>Aperçu mensuel</h4>
            <div class="preview-grid">
              <div class="preview-item charges-fixes">
                <span class="preview-label">🏠 Charges fixes</span>
                <span class="preview-amount">{{ formatCurrency(montantChargesFixes) }}</span>
              </div>
              <div class="preview-item depenses-variables">
                <span class="preview-label">🛍️ Dépenses variables</span>
                <span class="preview-amount">{{ formatCurrency(montantDepensesVariables) }}</span>
              </div>
              <div class="preview-item epargne">
                <span class="preview-label">💰 Épargne</span>
                <span class="preview-amount">{{ formatCurrency(montantEpargne) }}</span>
              </div>
              <div class="preview-total">
                <span class="preview-label">💵 Total</span>
                <span class="preview-amount">{{ formatCurrency(userData.salaireMensuelNet) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useOnboardingStore } from '@/stores/onboarding'
import { formatCurrency } from '@/utils/formatters'

const onboardingStore = useOnboardingStore()
const { userData, budgetConfig: storeBudgetConfig } = storeToRefs(onboardingStore)

const selectedMethod = ref('50-30-20')
const budgetConfig = ref({
  pourcentageChargesFixes: 50,
  pourcentageDepensesVariables: 30,
  pourcentageEpargne: 20
})

// Load saved values from store when component mounts
onMounted(() => {
  if (storeBudgetConfig.value) {
    budgetConfig.value = { ...storeBudgetConfig.value }

    // Detect which method is being used
    if (budgetConfig.value.pourcentageChargesFixes === 50 &&
        budgetConfig.value.pourcentageDepensesVariables === 30 &&
        budgetConfig.value.pourcentageEpargne === 20) {
      selectedMethod.value = '50-30-20'
    } else {
      selectedMethod.value = 'custom'
    }
  }
})

const totalPercentage = computed(() => {
  return budgetConfig.value.pourcentageChargesFixes + 
         budgetConfig.value.pourcentageDepensesVariables + 
         budgetConfig.value.pourcentageEpargne
})

const isValidTotal = computed(() => totalPercentage.value === 100)

const montantChargesFixes = computed(() => {
  return (userData.value.salaireMensuelNet || 0) * (budgetConfig.value.pourcentageChargesFixes / 100)
})

const montantDepensesVariables = computed(() => {
  return (userData.value.salaireMensuelNet || 0) * (budgetConfig.value.pourcentageDepensesVariables / 100)
})

const montantEpargne = computed(() => {
  return (userData.value.salaireMensuelNet || 0) * (budgetConfig.value.pourcentageEpargne / 100)
})

const selectMethod = (method: string) => {
  selectedMethod.value = method
  
  if (method === '50-30-20') {
    budgetConfig.value = {
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }
  }
}

const updateBudget = () => {
  // Auto-ajustement pour garder 100% quand possible
  if (selectedMethod.value === '50-30-20') {
    selectedMethod.value = 'custom'
  }
}

// Sauvegarder la configuration dans le store
watch(budgetConfig, (newConfig) => {
  if (isValidTotal.value) {
    onboardingStore.setBudgetConfig(newConfig)
  }
}, { deep: true })
</script>

<style scoped>
.budget-config-section {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.method-selector h3 {
  color: var(--text-primary);
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.methods-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.method-card {
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: center;
}

.method-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.method-card.selected {
  background: rgba(102, 126, 234, 0.1);
  border-color: var(--primary-color);
}

.method-card h4 {
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.method-card p {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.method-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.method-breakdown span {
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.budget-configuration h3 {
  color: var(--text-primary);
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.budget-sliders {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  margin-bottom: 2rem;
}

.slider-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.slider-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--text-primary);
  font-weight: 500;
}

.category-icon {
  font-size: 1.2rem;
  margin-right: 0.5rem;
}

.percentage-display {
  font-weight: 600;
  color: var(--primary-color);
  font-size: 1.1rem;
}

.budget-slider {
  width: 100%;
  height: 8px;
  border-radius: 4px;
  outline: none;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.1);
}

.budget-slider::-webkit-slider-thumb {
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--primary-color);
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.budget-slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--primary-color);
  cursor: pointer;
  border: none;
}

.slider-amount {
  text-align: right;
  color: var(--success-color);
  font-weight: 600;
  font-size: 1rem;
}

.budget-validation {
  padding: 1rem;
  border-radius: var(--radius-md);
  margin-bottom: 1.5rem;
  transition: all var(--transition-fast);
}

.budget-validation.valid {
  background: rgba(72, 187, 120, 0.1);
  border: 1px solid rgba(72, 187, 120, 0.3);
}

.budget-validation.invalid {
  background: rgba(245, 101, 101, 0.1);
  border: 1px solid rgba(245, 101, 101, 0.3);
}

.validation-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.validation-icon {
  font-size: 1.2rem;
}

.validation-text {
  color: var(--text-primary);
  font-weight: 600;
}

.validation-suggestion {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-style: italic;
}

.budget-preview {
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
}

.budget-preview h4 {
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.preview-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-md);
}

.preview-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: var(--radius-md);
  font-weight: 600;
}

.preview-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.preview-amount {
  color: var(--text-primary);
  font-weight: 600;
}

.preview-total .preview-label,
.preview-total .preview-amount {
  color: var(--text-primary);
}

@media (max-width: 768px) {
  .methods-grid {
    grid-template-columns: 1fr;
  }
  
  .slider-label {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .percentage-display {
    align-self: flex-end;
  }
}
</style>