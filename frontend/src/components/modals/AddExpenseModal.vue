<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import BaseModal from './BaseModal.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { apiService } from '@/services/api'
import type { CreateTransactionRequest, TypeTransaction } from '@/types'
import { logger } from '@/utils/logger'

interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const dashboardStore = useDashboardStore()

// Expense categories (variable expenses only)
const EXPENSE_CATEGORIES: { value: TypeTransaction; label: string; icon: string }[] = [
  { value: 'ALIMENTATION', label: 'Alimentation', icon: '🛒' },
  { value: 'RESTAURANT', label: 'Restaurant', icon: '🍽️' },
  { value: 'TRANSPORT', label: 'Transport', icon: '🚇' },
  { value: 'ESSENCE', label: 'Essence', icon: '⛽' },
  { value: 'SHOPPING', label: 'Shopping', icon: '🛍️' },
  { value: 'LOISIRS', label: 'Loisirs', icon: '🎮' },
  { value: 'SANTE', label: 'Santé', icon: '🏥' },
  { value: 'BEAUTE', label: 'Beauté', icon: '💄' },
  { value: 'MAISON', label: 'Maison', icon: '🏠' },
  { value: 'EDUCATION', label: 'Éducation', icon: '📚' },
  { value: 'VOYAGE', label: 'Voyage', icon: '✈️' },
  { value: 'RETRAIT_ESPECES', label: 'Retrait espèces', icon: '💵' },
  { value: 'AUTRE', label: 'Autre', icon: '📝' }
]

// Form state
const selectedAccountId = ref<string>('')
const selectedCategory = ref<TypeTransaction>('ALIMENTATION')
const amount = ref<number | null>(null)
const description = ref<string>('')
const date = ref<string>('')
const linkedObjectifId = ref<string>('')

// UI state
const isLoading = ref(false)
const error = ref<string>('')

// Computed
const comptes = computed(() => dashboardStore.comptes.filter(c => c.actif))
const objectifs = computed(() => dashboardStore.objectifs.filter(o => o.actif))

const isFormValid = computed(() => {
  return (
    selectedAccountId.value !== '' &&
    selectedCategory.value !== null &&
    amount.value !== null &&
    amount.value > 0 &&
    description.value.trim() !== '' &&
    date.value !== ''
  )
})

const selectedCategoryInfo = computed(() => {
  return EXPENSE_CATEGORIES.find(c => c.value === selectedCategory.value)
})

// Watchers
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    resetForm()
  }
})

// Methods
const resetForm = () => {
  selectedAccountId.value = comptes.value[0]?.id || ''
  selectedCategory.value = 'ALIMENTATION'
  amount.value = null
  description.value = ''
  date.value = new Date().toISOString().split('T')[0]
  linkedObjectifId.value = ''
  error.value = ''
}

const handleSubmit = async () => {
  if (!isFormValid.value) return

  isLoading.value = true
  error.value = ''

  try {
    const request: CreateTransactionRequest = {
      compteId: selectedAccountId.value,
      ...(linkedObjectifId.value && { objectifId: linkedObjectifId.value }),
      montant: -(amount.value!), // Negative for expense
      description: description.value,
      type: selectedCategory.value,
      dateTransaction: date.value
    }

    await apiService.createTransaction(request)

    logger.info('Expense created successfully')

    // Refresh dashboard data
    await dashboardStore.refreshDashboard()

    // Emit success and close
    emit('success')
    emit('update:modelValue', false)
  } catch (err: any) {
    logger.error('Error creating expense:', err)
    error.value = err.response?.data?.message || 'Une erreur est survenue lors de l\'ajout de la dépense'
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
    title="Ajouter une dépense"
    size="medium"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <form @submit.prevent="handleSubmit" class="expense-form">
      <!-- Account Selector -->
      <div class="form-group">
        <label for="account" class="form-label">Compte <span class="required">*</span></label>
        <select
          id="account"
          v-model="selectedAccountId"
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
      </div>

      <!-- Category Selector -->
      <div class="form-group">
        <label class="form-label">Catégorie <span class="required">*</span></label>
        <div class="category-grid">
          <button
            v-for="category in EXPENSE_CATEGORIES"
            :key="category.value"
            type="button"
            :class="['category-btn', { active: selectedCategory === category.value }]"
            @click="selectedCategory = category.value"
          >
            <span class="category-icon">{{ category.icon }}</span>
            <span class="category-label">{{ category.label }}</span>
          </button>
        </div>
      </div>

      <!-- Amount -->
      <div class="form-group">
        <label for="amount" class="form-label">Montant <span class="required">*</span></label>
        <div class="input-with-suffix">
          <input
            id="amount"
            v-model.number="amount"
            type="number"
            step="0.01"
            min="0.01"
            class="form-input"
            placeholder="0.00"
            required
          />
          <span class="input-suffix">€</span>
        </div>
      </div>

      <!-- Description -->
      <div class="form-group">
        <label for="description" class="form-label">Description <span class="required">*</span></label>
        <input
          id="description"
          v-model="description"
          type="text"
          class="form-input"
          placeholder="Ex: Courses du samedi"
          required
        />
      </div>

      <!-- Date -->
      <div class="form-group">
        <label for="date" class="form-label">Date <span class="required">*</span></label>
        <input
          id="date"
          v-model="date"
          type="date"
          class="form-input"
          required
        />
      </div>

      <!-- Optional: Link to Objectif -->
      <div class="form-group">
        <label for="objectif" class="form-label">
          Lier à un objectif (optionnel)
        </label>
        <select
          id="objectif"
          v-model="linkedObjectifId"
          class="form-select"
        >
          <option value="">Aucun objectif</option>
          <option
            v-for="objectif in objectifs"
            :key="objectif.id"
            :value="objectif.id"
          >
            {{ objectif.icone }} {{ objectif.nom }} ({{ objectif.montantActuel?.toFixed(2) || 0 }} / {{ objectif.montantCible.toFixed(2) }} €)
          </option>
        </select>
        <p class="form-hint">
          Cette dépense sera liée à l'objectif pour le suivi, mais ne réduira pas son montant actuel.
        </p>
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
        <span v-if="isLoading">Ajout en cours...</span>
        <span v-else>Ajouter la dépense</span>
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
@import './modal-styles.css';

.expense-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
</style>
