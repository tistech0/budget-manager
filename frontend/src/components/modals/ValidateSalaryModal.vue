<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import BaseModal from './BaseModal.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { apiService } from '@/services/api'
import type { ValidationSalaireRequest, TypeTransaction } from '@/types'
import { logger } from '@/utils/logger'

interface Props {
  modelValue: boolean
  currentMonth: string // Format: "2025-01"
}

interface PrimeEntry {
  id: string
  amount: number | null
  description: string
  date: string
  accountId: string
}

interface FreelanceEntry {
  id: string
  amount: number | null
  description: string
  date: string
  accountId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const dashboardStore = useDashboardStore()

// Common settings for all entries
const commonSettings = ref<{
  defaultAccountId: string
  descriptionPrefix: string
}>({
  defaultAccountId: '',
  descriptionPrefix: ''
})

// Form state - restructured for multiple entries
const salaire = ref<{
  amount: number | null
  date: string
}>({
  amount: null,
  date: ''
})

const primes = ref<PrimeEntry[]>([])
const freelances = ref<FreelanceEntry[]>([])

// UI state
const isLoading = ref(false)
const error = ref<string>('')
const hasSalaryForMonth = ref(false)

// Computed
const user = computed(() => dashboardStore.user)
const comptes = computed(() => dashboardStore.comptes.filter(c => c.actif))
const principalAccount = computed(() =>
  comptes.value.find(c => c.type === 'COMPTE_COURANT') || comptes.value[0]
)

const formattedMonth = computed(() => {
  const [year, month] = props.currentMonth.split('-')
  const date = new Date(parseInt(year), parseInt(month) - 1)
  return date.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' })
})

const isFormValid = computed(() => {
  // Common settings must be filled
  if (!commonSettings.value.defaultAccountId) return false

  // Salary section: only valid if no salary exists for month
  const hasSalaire = !hasSalaryForMonth.value && salaire.value.date

  const hasPrimes = primes.value.length > 0 && primes.value.every(p =>
    p.amount !== null && p.amount > 0 && p.description && p.date && p.accountId
  )
  const hasFreelances = freelances.value.length > 0 && freelances.value.every(f =>
    f.amount !== null && f.amount > 0 && f.description && f.date && f.accountId
  )

  return hasSalaire || hasPrimes || hasFreelances
})

const isSalaireSectionDisabled = computed(() => hasSalaryForMonth.value)

// Helper function to generate unique IDs
const generateId = () => `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`

// Watchers
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    resetForm()
    checkExistingSalary()
  }
})

// Methods
const resetForm = () => {
  // Reset common settings
  commonSettings.value = {
    defaultAccountId: principalAccount.value?.id || '',
    descriptionPrefix: formattedMonth.value
  }

  // Reset salaire section - autofill with default values
  const salaireDate = user.value?.jourPaie
    ? `${props.currentMonth}-${user.value.jourPaie.toString().padStart(2, '0')}`
    : new Date().toISOString().split('T')[0]

  salaire.value = {
    amount: user.value?.salaireMensuelNet || null,
    date: salaireDate
  }

  // Reset primes and freelances
  primes.value = []
  freelances.value = []

  error.value = ''
  hasSalaryForMonth.value = false
}

const addPrime = () => {
  primes.value.push({
    id: generateId(),
    amount: null,
    description: commonSettings.value.descriptionPrefix ? `Prime ${commonSettings.value.descriptionPrefix}` : '',
    date: new Date().toISOString().split('T')[0],
    accountId: commonSettings.value.defaultAccountId || principalAccount.value?.id || ''
  })
}

const removePrime = (id: string) => {
  primes.value = primes.value.filter(p => p.id !== id)
}

const addFreelance = () => {
  freelances.value.push({
    id: generateId(),
    amount: null,
    description: commonSettings.value.descriptionPrefix ? `Freelance ${commonSettings.value.descriptionPrefix}` : '',
    date: new Date().toISOString().split('T')[0],
    accountId: commonSettings.value.defaultAccountId || principalAccount.value?.id || ''
  })
}

const removeFreelance = (id: string) => {
  freelances.value = freelances.value.filter(f => f.id !== id)
}


const checkExistingSalary = async () => {
  try {
    // Check using the dashboard salaireValide flag
    await dashboardStore.loadDashboard()
    hasSalaryForMonth.value = dashboardStore.dashboardData?.salaireValide || false
  } catch (err) {
    logger.error('Error checking existing salary:', err)
    // Fallback to checking transactions
    try {
      const transactions = await apiService.getTransactions({ month: props.currentMonth })
      hasSalaryForMonth.value = transactions.some(t => t.type === 'SALAIRE')
    } catch (fallbackErr) {
      logger.error('Error checking transactions:', fallbackErr)
    }
  }
}

const handleSubmit = async () => {
  if (!isFormValid.value) return

  isLoading.value = true
  error.value = ''

  try {
    const transactions: ValidationSalaireRequest[] = []

    // Add salary transaction if filled
    if (salaire.value.date && salaire.value.amount && commonSettings.value.defaultAccountId) {
      const description = commonSettings.value.descriptionPrefix
        ? `Salaire ${commonSettings.value.descriptionPrefix}`
        : `Salaire ${formattedMonth.value}`

      transactions.push({
        mois: props.currentMonth,
        montant: salaire.value.amount,
        dateReception: salaire.value.date,
        compteId: commonSettings.value.defaultAccountId,
        type: 'SALAIRE',
        description
      })
    }

    // Add all prime transactions
    for (const prime of primes.value) {
      if (prime.amount !== null && prime.amount > 0 && prime.description && prime.date && prime.accountId) {
        transactions.push({
          mois: props.currentMonth,
          montant: prime.amount,
          dateReception: prime.date,
          compteId: prime.accountId,
          type: 'PRIME',
          description: prime.description
        })
      }
    }

    // Add all freelance transactions
    for (const freelance of freelances.value) {
      if (freelance.amount !== null && freelance.amount > 0 && freelance.description && freelance.date && freelance.accountId) {
        transactions.push({
          mois: props.currentMonth,
          montant: freelance.amount,
          dateReception: freelance.date,
          compteId: freelance.accountId,
          type: 'FREELANCE',
          description: freelance.description
        })
      }
    }

    // Submit all transactions
    for (const transaction of transactions) {
      await apiService.validateSalary(transaction)
    }

    logger.info(`${transactions.length} transaction(s) validated successfully`)

    // Close modal
    emit('update:modelValue', false)

    // Emit success immediately
    emit('success')

    // Force a full page reload to ensure dashboard refreshes
    setTimeout(() => {
      window.location.reload()
    }, 500)
  } catch (err: any) {
    logger.error('Error validating income:', err)
    error.value = err.response?.data?.message || 'Une erreur est survenue lors de la validation des revenus'
  } finally {
    isLoading.value = false
  }
}

const close = () => {
  emit('update:modelValue', false)
}
</script>

<template>
  <BaseModal
    :model-value="modelValue"
    title="Valider les revenus"
    size="large"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <form @submit.prevent="handleSubmit" class="salary-form">
      <!-- Warning if salary already exists -->
      <div v-if="hasSalaryForMonth" class="warning-banner">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="2"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"
          />
        </svg>
        <span>Un salaire a déjà été validé pour {{ formattedMonth }}.</span>
      </div>

      <!-- COMMON SETTINGS SECTION -->
      <div class="common-settings-section">
        <div class="section-header">
          <h3 class="section-title">⚙️ Paramètres communs</h3>
          <p class="section-hint">Appliqué par défaut à tous les revenus</p>
        </div>

        <div class="section-content">
          <div class="form-row">
            <div class="form-group">
              <label for="common-account" class="form-label">Compte de réception <span class="required">*</span></label>
              <select
                id="common-account"
                v-model="commonSettings.defaultAccountId"
                class="form-select"
                required
              >
                <option value="" disabled>Sélectionnez un compte</option>
                <option
                  v-for="compte in comptes"
                  :key="compte.id"
                  :value="compte.id"
                >
                  {{ compte.nom }} ({{ compte.banque.nom }}) - {{ compte.soldeTotal.toFixed(2) }} €
                </option>
              </select>
              <p class="form-hint">
                Utilisé pour tous les revenus (salaire, primes, freelance)
              </p>
            </div>

            <div class="form-group">
              <label for="common-description" class="form-label">Période / Description</label>
              <input
                id="common-description"
                v-model="commonSettings.descriptionPrefix"
                type="text"
                class="form-input"
                placeholder="Ex: janvier 2025"
              />
              <p class="form-hint">
                Ajouté automatiquement : "Salaire janvier 2025", "Prime janvier 2025"...
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- SECTION 1: Salaire -->
      <div class="income-section" :class="{ 'section-disabled': isSalaireSectionDisabled }">
        <div class="section-header">
          <h3 class="section-title">💼 Salaire</h3>
          <p v-if="!isSalaireSectionDisabled" class="section-hint">Optionnel - Laisser vide si déjà validé</p>
          <p v-else class="section-hint section-hint-disabled">Déjà validé pour {{ formattedMonth }}</p>
        </div>

        <div class="section-content">
          <div class="form-row">
            <div class="form-group">
              <label for="salaire-amount" class="form-label">Montant</label>
              <div class="input-with-suffix">
                <input
                  id="salaire-amount"
                  v-model.number="salaire.amount"
                  type="number"
                  step="0.01"
                  min="0"
                  class="form-input"
                  :placeholder="`${user?.salaireMensuelNet || 0}`"
                  :disabled="isSalaireSectionDisabled"
                />
                <span class="input-suffix">€</span>
              </div>
              <p class="form-hint">
                Défaut: {{ user?.salaireMensuelNet || 0 }} €
              </p>
            </div>

            <div class="form-group">
              <label for="salaire-date" class="form-label">Date de réception</label>
              <input
                id="salaire-date"
                v-model="salaire.date"
                type="date"
                class="form-input"
                :disabled="isSalaireSectionDisabled"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- SECTION 2: Primes -->
      <div class="income-section">
        <div class="section-header">
          <h3 class="section-title">🎁 Primes</h3>
          <button
            type="button"
            class="add-entry-btn"
            @click="addPrime"
          >
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            Ajouter une prime
          </button>
        </div>

        <div class="section-content">
          <div v-if="primes.length === 0" class="empty-state">
            Aucune prime ajoutée
          </div>

          <div v-for="prime in primes" :key="prime.id" class="entry-item">
            <div class="entry-header">
              <span class="entry-number">Prime</span>
              <button
                type="button"
                class="delete-btn"
                @click="removePrime(prime.id)"
                title="Supprimer"
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
                </svg>
              </button>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Montant <span class="required">*</span></label>
                <div class="input-with-suffix">
                  <input
                    v-model.number="prime.amount"
                    type="number"
                    step="0.01"
                    min="0"
                    class="form-input"
                    placeholder="0.00"
                    required
                  />
                  <span class="input-suffix">€</span>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Date <span class="required">*</span></label>
                <input
                  v-model="prime.date"
                  type="date"
                  class="form-input"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Description <span class="required">*</span></label>
              <input
                v-model="prime.description"
                type="text"
                class="form-input"
                placeholder="Ex: 13ème mois, Prime d'intéressement..."
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Compte <span class="required">*</span></label>
              <select
                v-model="prime.accountId"
                class="form-select"
                required
              >
                <option value="" disabled>Sélectionnez un compte</option>
                <option
                  v-for="compte in comptes"
                  :key="compte.id"
                  :value="compte.id"
                >
                  {{ compte.nom }} ({{ compte.banque.nom }})
                </option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- SECTION 3: Freelance -->
      <div class="income-section">
        <div class="section-header">
          <h3 class="section-title">💻 Revenus Freelance</h3>
          <button
            type="button"
            class="add-entry-btn"
            @click="addFreelance"
          >
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            Ajouter un revenu freelance
          </button>
        </div>

        <div class="section-content">
          <div v-if="freelances.length === 0" class="empty-state">
            Aucun revenu freelance ajouté
          </div>

          <div v-for="freelance in freelances" :key="freelance.id" class="entry-item">
            <div class="entry-header">
              <span class="entry-number">Freelance</span>
              <button
                type="button"
                class="delete-btn"
                @click="removeFreelance(freelance.id)"
                title="Supprimer"
              >
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="w-4 h-4">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
                </svg>
              </button>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Montant <span class="required">*</span></label>
                <div class="input-with-suffix">
                  <input
                    v-model.number="freelance.amount"
                    type="number"
                    step="0.01"
                    min="0"
                    class="form-input"
                    placeholder="0.00"
                    required
                  />
                  <span class="input-suffix">€</span>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Date <span class="required">*</span></label>
                <input
                  v-model="freelance.date"
                  type="date"
                  class="form-input"
                  required
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Description <span class="required">*</span></label>
              <input
                v-model="freelance.description"
                type="text"
                class="form-input"
                placeholder="Ex: Mission pour Client X, Projet Y..."
                required
              />
            </div>

            <div class="form-group">
              <label class="form-label">Compte <span class="required">*</span></label>
              <select
                v-model="freelance.accountId"
                class="form-select"
                required
              >
                <option value="" disabled>Sélectionnez un compte</option>
                <option
                  v-for="compte in comptes"
                  :key="compte.id"
                  :value="compte.id"
                >
                  {{ compte.nom }} ({{ compte.banque.nom }})
                </option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="error-banner">
        {{ error }}
      </div>
    </form>

    <template #footer>
      <button
        type="button"
        class="btn btn-secondary"
        @click="close"
        :disabled="isLoading"
      >
        Annuler
      </button>
      <button
        type="submit"
        class="btn btn-primary"
        @click="handleSubmit"
        :disabled="!isFormValid || isLoading"
      >
        <span v-if="isLoading">Validation en cours...</span>
        <span v-else>Valider les revenus</span>
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
@import './modal-styles.css';

.salary-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Common Settings Section */
.common-settings-section {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border: 2px solid rgba(102, 126, 234, 0.3);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.common-settings-section .section-title {
  color: #A5B4FC;
}

/* Income Sections */
.income-section {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  padding: var(--spacing-lg);
  backdrop-filter: blur(10px);
  transition: var(--transition-normal);
}

.income-section.section-disabled {
  opacity: 0.6;
  background: rgba(255, 255, 255, 0.03);
  pointer-events: none;
}

.section-hint-disabled {
  color: #FFA726 !important;
  font-weight: 600;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.section-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.section-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin: 0;
  font-style: italic;
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

/* Add Entry Button */
.add-entry-btn {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-lg);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition-fast);
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.add-entry-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.add-entry-btn svg {
  width: 1rem;
  height: 1rem;
}

/* Entry Items */
.entry-item {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  transition: var(--transition-fast);
}

.entry-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.15);
}

.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.entry-number {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.delete-btn {
  padding: 0.375rem;
  background: rgba(245, 101, 101, 0.2);
  border: 1px solid rgba(245, 101, 101, 0.3);
  border-radius: var(--radius-md);
  color: #FF8A80;
  cursor: pointer;
  transition: var(--transition-fast);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.delete-btn:hover {
  background: rgba(245, 101, 101, 0.3);
  border-color: rgba(245, 101, 101, 0.5);
  transform: scale(1.05);
}

.delete-btn svg {
  width: 1rem;
  height: 1rem;
}

/* Empty State */
.empty-state {
  padding: var(--spacing-lg);
  text-align: center;
  color: var(--text-muted);
  font-size: 0.875rem;
  font-style: italic;
}

/* Form Row for inline fields */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }

  .add-entry-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
