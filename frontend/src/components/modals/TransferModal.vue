<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import BaseModal from './BaseModal.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { apiService } from '@/services/api'
import type { CreateCompteTransfertRequest, CreateTransfertObjectifRequest } from '@/types'
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

// Tab state
type TabType = 'compte' | 'objectif'
const activeTab = ref<TabType>('compte')

// Account transfer form
const accountSourceId = ref<string>('')
const accountDestinationId = ref<string>('')
const accountAmount = ref<number | null>(null)
const accountDescription = ref<string>('')
const accountDate = ref<string>('')

// Objectif transfer form
const objectifSourceId = ref<string>('')
const objectifDestinationId = ref<string>('')
const objectifSourceAccountId = ref<string>('')
const objectifDestinationAccountId = ref<string>('')
const objectifAmount = ref<number | null>(null)
const objectifMotif = ref<string>('')

// UI state
const isLoading = ref(false)
const error = ref<string>('')

// Computed
const comptes = computed(() => dashboardStore.comptes.filter(c => c.actif))
const objectifs = computed(() => dashboardStore.objectifs.filter(o => o.actif))

const sourceAccount = computed(() =>
  comptes.value.find(c => c.id === accountSourceId.value)
)

const destinationAccount = computed(() =>
  comptes.value.find(c => c.id === accountDestinationId.value)
)

const sourceObjectif = computed(() =>
  objectifs.value.find(o => o.id === objectifSourceId.value)
)

const destinationObjectif = computed(() =>
  objectifs.value.find(o => o.id === objectifDestinationId.value)
)

const availableSourceAccounts = computed(() => {
  if (!objectifSourceId.value) return []
  const objectif = objectifs.value.find(o => o.id === objectifSourceId.value)
  if (!objectif?.repartitions) return []

  return objectif.repartitions
    .filter(r => r.montantActuel > 0)
    .map(r => r.compte)
})

const availableDestinationAccounts = computed(() => {
  if (!objectifDestinationId.value) return []
  const objectif = objectifs.value.find(o => o.id === objectifDestinationId.value)
  if (!objectif?.repartitions) return []

  return objectif.repartitions.map(r => r.compte)
})

const maxTransferAmount = computed(() => {
  if (activeTab.value === 'compte') {
    return sourceAccount.value?.soldeTotal || 0
  } else {
    if (!objectifSourceAccountId.value || !sourceObjectif.value) return 0
    const repartition = sourceObjectif.value.repartitions?.find(
      r => r.compte.id === objectifSourceAccountId.value
    )
    return repartition?.montantActuel || 0
  }
})

const isAccountTransferValid = computed(() => {
  return (
    accountSourceId.value !== '' &&
    accountDestinationId.value !== '' &&
    accountSourceId.value !== accountDestinationId.value &&
    accountAmount.value !== null &&
    accountAmount.value > 0 &&
    accountAmount.value <= maxTransferAmount.value &&
    accountDate.value !== ''
  )
})

const isObjectifTransferValid = computed(() => {
  return (
    objectifSourceId.value !== '' &&
    objectifDestinationId.value !== '' &&
    objectifSourceId.value !== objectifDestinationId.value &&
    objectifSourceAccountId.value !== '' &&
    objectifDestinationAccountId.value !== '' &&
    objectifAmount.value !== null &&
    objectifAmount.value > 0 &&
    objectifAmount.value <= maxTransferAmount.value
  )
})

// Watchers
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    resetForm()
  }
})

watch(activeTab, () => {
  error.value = ''
})

watch(accountSourceId, () => {
  updateAccountDescription()
})

watch(accountDestinationId, () => {
  updateAccountDescription()
})

watch(objectifSourceId, () => {
  objectifSourceAccountId.value = ''
})

watch(objectifDestinationId, () => {
  objectifDestinationAccountId.value = ''
})

// Methods
const resetForm = () => {
  // Account transfer
  accountSourceId.value = ''
  accountDestinationId.value = ''
  accountAmount.value = null
  accountDescription.value = ''
  accountDate.value = new Date().toISOString().split('T')[0]

  // Objectif transfer
  objectifSourceId.value = ''
  objectifDestinationId.value = ''
  objectifSourceAccountId.value = ''
  objectifDestinationAccountId.value = ''
  objectifAmount.value = null
  objectifMotif.value = ''

  error.value = ''
  activeTab.value = 'compte'
}

const updateAccountDescription = () => {
  const source = sourceAccount.value
  const destination = destinationAccount.value
  if (source && destination) {
    accountDescription.value = `Transfert de ${source.nom} vers ${destination.nom}`
  }
}

const handleAccountTransfer = async () => {
  if (!isAccountTransferValid.value) return

  isLoading.value = true
  error.value = ''

  try {
    const request: CreateCompteTransfertRequest = {
      compteSourceId: accountSourceId.value,
      compteDestinationId: accountDestinationId.value,
      montant: accountAmount.value!,
      ...(accountDescription.value && { description: accountDescription.value }),
      dateTransfert: accountDate.value
    }

    await apiService.transferBetweenAccounts(request)

    logger.info('Account transfer successful')

    // Refresh dashboard data
    await dashboardStore.refreshDashboard()

    // Emit success and close
    emit('success')
    emit('update:modelValue', false)
  } catch (err: any) {
    logger.error('Error transferring between accounts:', err)
    error.value = err.response?.data?.message || 'Une erreur est survenue lors du transfert'
  } finally {
    isLoading.value = false
  }
}

const handleObjectifTransfer = async () => {
  if (!isObjectifTransferValid.value) return

  isLoading.value = true
  error.value = ''

  try {
    const request: CreateTransfertObjectifRequest = {
      objectifSourceId: objectifSourceId.value,
      objectifDestinationId: objectifDestinationId.value,
      compteSourceId: objectifSourceAccountId.value,
      compteDestinationId: objectifDestinationAccountId.value,
      montant: objectifAmount.value!,
      ...(objectifMotif.value && { motif: objectifMotif.value })
    }

    await apiService.transferBetweenObjectifs(request)

    logger.info('Objectif transfer successful')

    // Refresh dashboard data
    await dashboardStore.refreshDashboard()

    // Emit success and close
    emit('success')
    emit('update:modelValue', false)
  } catch (err: any) {
    logger.error('Error transferring between objectifs:', err)
    error.value = err.response?.data?.message || 'Une erreur est survenue lors du transfert'
  } finally {
    isLoading.value = false
  }
}

const handleSubmit = () => {
  if (activeTab.value === 'compte') {
    handleAccountTransfer()
  } else {
    handleObjectifTransfer()
  }
}

const close = () => {
  emit('update:modelValue', false)
}
</script>

<template>
  <BaseModal
    :model-value="modelValue"
    title="Effectuer un transfert"
    size="large"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <!-- Tab Navigation -->
    <div class="tab-navigation">
      <button
        type="button"
        :class="['tab-btn', { active: activeTab === 'compte' }]"
        @click="activeTab = 'compte'"
      >
        💳 Entre comptes
      </button>
      <button
        type="button"
        :class="['tab-btn', { active: activeTab === 'objectif' }]"
        @click="activeTab = 'objectif'"
      >
        🎯 Entre objectifs
      </button>
    </div>

    <!-- Account Transfer Tab -->
    <form v-if="activeTab === 'compte'" @submit.prevent="handleAccountTransfer" class="transfer-form">
      <div class="form-row">
        <!-- Source Account -->
        <div class="form-group">
          <label for="account-source" class="form-label">Compte source <span class="required">*</span></label>
          <select
            id="account-source"
            v-model="accountSourceId"
            class="form-select"
            required
          >
            <option value="" disabled>Sélectionnez un compte</option>
            <option
              v-for="compte in comptes"
              :key="compte.id"
              :value="compte.id"
              :disabled="compte.id === accountDestinationId"
            >
              {{ compte.nom }} ({{ compte.banque.nom }}) - {{ compte.soldeTotal.toFixed(2) }} €
            </option>
          </select>
          <p v-if="sourceAccount" class="form-hint">
            Solde disponible: {{ sourceAccount.soldeTotal.toFixed(2) }} €
          </p>
        </div>

        <!-- Arrow -->
        <div class="arrow-container">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="2"
            stroke="currentColor"
            class="arrow-icon"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
          </svg>
        </div>

        <!-- Destination Account -->
        <div class="form-group">
          <label for="account-destination" class="form-label">Compte destination <span class="required">*</span></label>
          <select
            id="account-destination"
            v-model="accountDestinationId"
            class="form-select"
            required
          >
            <option value="" disabled>Sélectionnez un compte</option>
            <option
              v-for="compte in comptes"
              :key="compte.id"
              :value="compte.id"
              :disabled="compte.id === accountSourceId"
            >
              {{ compte.nom }} ({{ compte.banque.nom }}) - {{ compte.soldeTotal.toFixed(2) }} €
            </option>
          </select>
          <p v-if="destinationAccount" class="form-hint">
            Solde actuel: {{ destinationAccount.soldeTotal.toFixed(2) }} €
          </p>
        </div>
      </div>

      <!-- Amount -->
      <div class="form-group">
        <label for="account-amount" class="form-label">Montant <span class="required">*</span></label>
        <div class="input-with-suffix">
          <input
            id="account-amount"
            v-model.number="accountAmount"
            type="number"
            step="0.01"
            min="0.01"
            :max="maxTransferAmount"
            class="form-input"
            placeholder="0.00"
            required
          />
          <span class="input-suffix">€</span>
        </div>
        <p v-if="maxTransferAmount > 0" class="form-hint">
          Maximum: {{ maxTransferAmount.toFixed(2) }} €
        </p>
      </div>

      <!-- Description -->
      <div class="form-group">
        <label for="account-description" class="form-label">Description</label>
        <input
          id="account-description"
          v-model="accountDescription"
          type="text"
          class="form-input"
          placeholder="Description du transfert"
        />
      </div>

      <!-- Date -->
      <div class="form-group">
        <label for="account-date" class="form-label">Date <span class="required">*</span></label>
        <input
          id="account-date"
          v-model="accountDate"
          type="date"
          class="form-input"
          required
        />
      </div>

      <!-- Error Message -->
      <div v-if="error" class="error-banner">
        {{ error }}
      </div>
    </form>

    <!-- Objectif Transfer Tab -->
    <form v-else @submit.prevent="handleObjectifTransfer" class="transfer-form">
      <div class="form-row">
        <!-- Source Objectif -->
        <div class="form-group">
          <label for="objectif-source" class="form-label">Objectif source <span class="required">*</span></label>
          <select
            id="objectif-source"
            v-model="objectifSourceId"
            class="form-select"
            required
          >
            <option value="" disabled>Sélectionnez un objectif</option>
            <option
              v-for="objectif in objectifs"
              :key="objectif.id"
              :value="objectif.id"
              :disabled="objectif.id === objectifDestinationId || (objectif.montantActuel || 0) === 0"
            >
              {{ objectif.icone }} {{ objectif.nom }} - {{ objectif.montantActuel?.toFixed(2) || 0 }} €
            </option>
          </select>
        </div>

        <!-- Arrow -->
        <div class="arrow-container">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="2"
            stroke="currentColor"
            class="arrow-icon"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
          </svg>
        </div>

        <!-- Destination Objectif -->
        <div class="form-group">
          <label for="objectif-destination" class="form-label">Objectif destination <span class="required">*</span></label>
          <select
            id="objectif-destination"
            v-model="objectifDestinationId"
            class="form-select"
            required
          >
            <option value="" disabled>Sélectionnez un objectif</option>
            <option
              v-for="objectif in objectifs"
              :key="objectif.id"
              :value="objectif.id"
              :disabled="objectif.id === objectifSourceId"
            >
              {{ objectif.icone }} {{ objectif.nom }} - {{ objectif.montantActuel?.toFixed(2) || 0 }} / {{ objectif.montantCible.toFixed(2) }} €
            </option>
          </select>
        </div>
      </div>

      <!-- Source Account (within objectif) -->
      <div v-if="objectifSourceId" class="form-group">
        <label for="objectif-source-account" class="form-label">Compte source <span class="required">*</span></label>
        <select
          id="objectif-source-account"
          v-model="objectifSourceAccountId"
          class="form-select"
          required
        >
          <option value="" disabled>Sélectionnez un compte</option>
          <option
            v-for="repartition in sourceObjectif?.repartitions"
            :key="repartition.id"
            :value="repartition.compte.id"
            :disabled="repartition.montantActuel === 0"
          >
            {{ repartition.compte.nom }} - {{ repartition.montantActuel.toFixed(2) }} €
          </option>
        </select>
      </div>

      <!-- Destination Account (within objectif) -->
      <div v-if="objectifDestinationId" class="form-group">
        <label for="objectif-destination-account" class="form-label">Compte destination <span class="required">*</span></label>
        <select
          id="objectif-destination-account"
          v-model="objectifDestinationAccountId"
          class="form-select"
          required
        >
          <option value="" disabled>Sélectionnez un compte</option>
          <option
            v-for="repartition in destinationObjectif?.repartitions"
            :key="repartition.id"
            :value="repartition.compte.id"
          >
            {{ repartition.compte.nom }} - {{ repartition.montantActuel.toFixed(2) }} €
          </option>
        </select>
      </div>

      <!-- Amount -->
      <div class="form-group">
        <label for="objectif-amount" class="form-label">Montant <span class="required">*</span></label>
        <div class="input-with-suffix">
          <input
            id="objectif-amount"
            v-model.number="objectifAmount"
            type="number"
            step="0.01"
            min="0.01"
            :max="maxTransferAmount"
            class="form-input"
            placeholder="0.00"
            required
          />
          <span class="input-suffix">€</span>
        </div>
        <p v-if="maxTransferAmount > 0" class="form-hint">
          Maximum: {{ maxTransferAmount.toFixed(2) }} €
        </p>
      </div>

      <!-- Motif -->
      <div class="form-group">
        <label for="objectif-motif" class="form-label">Motif</label>
        <input
          id="objectif-motif"
          v-model="objectifMotif"
          type="text"
          class="form-input"
          placeholder="Motif du transfert"
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
        :disabled="(activeTab === 'compte' ? !isAccountTransferValid : !isObjectifTransferValid) || isLoading"
      >
        <span v-if="isLoading">Transfert en cours...</span>
        <span v-else>Effectuer le transfert</span>
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
@import './modal-styles.css';

.transfer-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
</style>
