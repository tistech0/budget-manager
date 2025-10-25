<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import BaseModal from './BaseModal.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { apiService } from '@/services/api'
import type { CreateVersementObjectifRequest } from '@/types'
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

// Form state
const selectedObjectifId = ref<string>('')
const selectedAccountId = ref<string>('')
const amount = ref<number | null>(null)
const description = ref<string>('')
const date = ref<string>('')

// UI state
const isLoading = ref(false)
const error = ref<string>('')

// Computed
const comptes = computed(() => dashboardStore.comptes.filter(c => c.actif))
const objectifs = computed(() => dashboardStore.objectifs.filter(o => o.actif))

const selectedObjectif = computed(() =>
  objectifs.value.find(o => o.id === selectedObjectifId.value)
)

const selectedCompte = computed(() =>
  comptes.value.find(c => c.id === selectedAccountId.value)
)

// Available accounts for the selected objectif
const availableAccounts = computed(() => {
  if (!selectedObjectifId.value) return []

  const objectif = objectifs.value.find(o => o.id === selectedObjectifId.value)
  if (!objectif?.repartitions) return []

  // Return accounts that are part of the objectif's repartitions
  return objectif.repartitions.map(r => r.compte)
})

const objectifProgress = computed(() => {
  if (!selectedObjectif.value) return 0
  const current = selectedObjectif.value.montantActuel || 0
  const target = selectedObjectif.value.montantCible || 1
  return Math.min((current / target) * 100, 100)
})

const remainingAmount = computed(() => {
  if (!selectedObjectif.value) return 0
  const current = selectedObjectif.value.montantActuel || 0
  const target = selectedObjectif.value.montantCible
  return Math.max(target - current, 0)
})

const isFormValid = computed(() => {
  return (
    selectedObjectifId.value !== '' &&
    selectedAccountId.value !== '' &&
    amount.value !== null &&
    amount.value > 0 &&
    date.value !== ''
  )
})

// Watchers
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    resetForm()
  }
})

watch(selectedObjectifId, (newValue) => {
  // Reset account selection when objectif changes
  selectedAccountId.value = ''

  // Auto-update description
  updateDescription()
})

watch(selectedAccountId, () => {
  updateDescription()
})

// Methods
const resetForm = () => {
  selectedObjectifId.value = objectifs.value[0]?.id || ''
  selectedAccountId.value = ''
  amount.value = null
  description.value = ''
  date.value = new Date().toISOString().split('T')[0]
  error.value = ''

  // Set default account if objectif is selected
  if (selectedObjectifId.value && availableAccounts.value.length > 0) {
    selectedAccountId.value = availableAccounts.value[0].id
  }

  updateDescription()
}

const updateDescription = () => {
  const objectif = selectedObjectif.value
  const compte = selectedCompte.value

  if (objectif && compte) {
    description.value = `Versement pour ${objectif.nom}`
  }
}

const setRemainingAmount = () => {
  amount.value = remainingAmount.value
}

const handleSubmit = async () => {
  if (!isFormValid.value) return

  isLoading.value = true
  error.value = ''

  try {
    const request: CreateVersementObjectifRequest = {
      objectifId: selectedObjectifId.value,
      compteId: selectedAccountId.value,
      montant: amount.value!,
      ...(description.value && { description: description.value }),
      dateVersement: date.value
    }

    await apiService.addVersementToObjectif(request)

    logger.info('Versement added successfully')

    // Refresh dashboard data
    await dashboardStore.refreshDashboard()

    // Emit success and close
    emit('success')
    emit('update:modelValue', false)
  } catch (err: any) {
    logger.error('Error adding versement:', err)
    error.value = err.response?.data?.message || 'Une erreur est survenue lors du versement'
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
    title="Remplir un objectif"
    size="medium"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <form @submit.prevent="handleSubmit" class="versement-form">
      <!-- Objectif Selector -->
      <div class="form-group">
        <label for="objectif" class="form-label">Objectif <span class="required">*</span></label>
        <select
          id="objectif"
          v-model="selectedObjectifId"
          class="form-select"
          required
        >
          <option value="" disabled>Sélectionnez un objectif</option>
          <option
            v-for="objectif in objectifs"
            :key="objectif.id"
            :value="objectif.id"
          >
            {{ objectif.icone }} {{ objectif.nom }} - {{ objectif.montantActuel?.toFixed(2) || 0 }} / {{ objectif.montantCible.toFixed(2) }} €
          </option>
        </select>
      </div>

      <!-- Objectif Progress -->
      <div v-if="selectedObjectif" class="objectif-info">
        <div class="progress-header">
          <span class="progress-label">Progression</span>
          <span class="progress-percent">{{ objectifProgress.toFixed(0) }}%</span>
        </div>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{
              width: `${objectifProgress}%`,
              backgroundColor: selectedObjectif.couleur || '#4CAF50'
            }"
          ></div>
        </div>
        <div class="remaining-info">
          <span class="remaining-label">Montant restant:</span>
          <span class="remaining-amount">{{ remainingAmount.toFixed(2) }} €</span>
          <button
            v-if="remainingAmount > 0"
            type="button"
            class="quick-fill-btn"
            @click="setRemainingAmount"
          >
            Compléter
          </button>
        </div>
      </div>

      <!-- Account Selector -->
      <div class="form-group">
        <label for="account" class="form-label">Compte source <span class="required">*</span></label>
        <select
          id="account"
          v-model="selectedAccountId"
          class="form-select"
          required
        >
          <option value="" disabled>Sélectionnez un compte</option>
          <option
            v-for="compte in availableAccounts"
            :key="compte.id"
            :value="compte.id"
          >
            {{ compte.nom }} ({{ compte.banque.nom }}) - {{ compte.soldeTotal.toFixed(2) }} €
          </option>
        </select>
        <p v-if="availableAccounts.length === 0 && selectedObjectifId" class="form-hint warning">
          Cet objectif n'a pas de répartition configurée sur des comptes.
        </p>
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

      <!-- Description -->
      <div class="form-group">
        <label for="description" class="form-label">Description</label>
        <input
          id="description"
          v-model="description"
          type="text"
          class="form-input"
          placeholder="Description du versement"
        />
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
        <span v-if="isLoading">Versement en cours...</span>
        <span v-else>Effectuer le versement</span>
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
@import './modal-styles.css';

.versement-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
</style>
