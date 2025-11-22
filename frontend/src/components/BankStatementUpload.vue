<template>
  <div class="bank-statement-upload">
    <!-- Upload Section -->
    <div v-if="!parsedTransactions.length" class="upload-section glass-card">
      <h3>📄 Importer un Relevé Bancaire</h3>
      <p class="upload-desc">Téléchargez votre relevé bancaire (PDF ou CSV) et nous extrairons automatiquement les transactions</p>

      <div class="compte-selector">
        <label>Compte</label>
        <select v-model="selectedCompteId" required>
          <option value="">Sélectionner un compte</option>
          <option v-for="compte in comptes" :key="compte.id" :value="compte.id">
            {{ compte.nom }} - {{ compte.banque?.nom }}
          </option>
        </select>
      </div>

      <div
        class="dropzone"
        :class="{ 'dragging': isDragging, 'disabled': !selectedCompteId }"
        @drop.prevent="handleDrop"
        @dragover.prevent="isDragging = true"
        @dragleave.prevent="isDragging = false"
        @click="handleDropzoneClick"
      >
        <div class="dropzone-content">
          <span class="dropzone-icon">📎</span>
          <p class="dropzone-text">
            <template v-if="!selectedCompteId">
              Veuillez d'abord sélectionner un compte
            </template>
            <template v-else>
              Glissez-déposez votre relevé PDF ou CSV ici<br>
              ou <span class="click-text">cliquez pour parcourir</span>
            </template>
          </p>
          <p class="dropzone-hint">Formats acceptés: PDF, CSV</p>
        </div>
        <input
          ref="fileInput"
          type="file"
          accept=".pdf,.csv,application/pdf,text/csv"
          @change="handleFileSelect"
          style="display: none"
        >
      </div>

      <div v-if="uploading" class="upload-progress">
        <div class="progress-bar">
          <div class="progress-fill"></div>
        </div>
        <p>Analyse du relevé en cours...</p>
      </div>

      <div v-if="errorMessage" class="error-message">
        ❌ {{ errorMessage }}
      </div>
    </div>

    <!-- Review Section -->
    <div v-if="parsedTransactions.length" class="review-section glass-card">
      <div class="review-header">
        <h3>✅ Transactions Détectées ({{ validTransactions.length }})</h3>
        <button class="btn-back" @click="resetUpload">← Retour</button>
      </div>

      <p class="review-desc">
        Vérifiez et modifiez les transactions avant de les enregistrer.
        Vous pouvez supprimer celles que vous ne souhaitez pas importer.
      </p>

      <div class="transactions-table-wrapper">
        <table class="transactions-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Description</th>
              <th>Montant</th>
              <th>Type</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(transaction, index) in parsedTransactions"
              :key="index"
              :class="{ 'removed': transaction.removed }"
            >
              <td>
                <input
                  v-model="transaction.date"
                  type="date"
                  class="table-input"
                  :disabled="transaction.removed"
                >
              </td>
              <td>
                <input
                  v-model="transaction.description"
                  type="text"
                  class="table-input"
                  :disabled="transaction.removed"
                >
              </td>
              <td :class="transaction.montant >= 0 ? 'positive' : 'negative'">
                <input
                  v-model.number="transaction.montant"
                  type="number"
                  step="0.01"
                  class="table-input amount-input"
                  :disabled="transaction.removed"
                >
              </td>
              <td>
                <select
                  v-model="transaction.type"
                  class="table-select"
                  :disabled="transaction.removed"
                >
                  <option value="SALAIRE">Salaire</option>
                  <option value="ALIMENTATION">Alimentation</option>
                  <option value="RESTAURANT">Restaurant</option>
                  <option value="TRANSPORT">Transport</option>
                  <option value="ESSENCE">Essence</option>
                  <option value="SHOPPING">Shopping</option>
                  <option value="LOYER">Loyer</option>
                  <option value="ASSURANCE">Assurance</option>
                  <option value="ABONNEMENT">Abonnement</option>
                  <option value="LOISIRS">Loisirs</option>
                  <option value="SANTE">Santé</option>
                  <option value="EPARGNE">Épargne</option>
                  <option value="RETRAIT_ESPECES">Retrait</option>
                  <option value="FRAIS_BANCAIRE">Frais bancaires</option>
                  <option value="AUTRE">Autre</option>
                </select>
              </td>
              <td>
                <button
                  class="btn-remove"
                  :class="{ 'btn-restore': transaction.removed }"
                  @click="toggleRemove(index)"
                  :title="transaction.removed ? 'Restaurer' : 'Supprimer'"
                >
                  {{ transaction.removed ? '↶' : '🗑️' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="review-actions">
        <div class="review-summary">
          <p>
            <strong>{{ validTransactions.length }}</strong> transaction(s) à importer
            <span v-if="removedCount > 0" class="removed-info">
              ({{ removedCount }} supprimée(s))
            </span>
          </p>
          <p class="total-impact">
            Impact total: <span :class="totalImpact >= 0 ? 'positive' : 'negative'">
              {{ formatCurrency(totalImpact) }}
            </span>
          </p>
        </div>
        <div class="review-buttons">
          <button class="btn-cancel" @click="resetUpload">Annuler</button>
          <button
            class="btn-validate"
            :disabled="validTransactions.length === 0 || saving"
            @click="validateTransactions"
          >
            {{ saving ? 'Enregistrement...' : 'Valider et Enregistrer' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { logger } from '@/utils/logger'
import { apiService } from '@/services/api'
import type { TypeTransaction } from '@/types'

interface Compte {
  id: string
  nom: string
  banque?: {
    nom: string
  }
}

interface ParsedTransaction {
  date: string
  description: string
  montant: number
  type: string
  isDebit: boolean
  rawLine: string
  removed?: boolean
}

const props = defineProps<{
  comptes: Compte[]
}>()

const emit = defineEmits<{
  (e: 'upload-success'): void
}>()

const selectedCompteId = ref('')
const isDragging = ref(false)
const uploading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const parsedTransactions = ref<ParsedTransaction[]>([])
const fileInput = ref<HTMLInputElement>()

const validTransactions = computed(() => {
  return parsedTransactions.value.filter(t => !t.removed)
})

const removedCount = computed(() => {
  return parsedTransactions.value.filter(t => t.removed).length
})

const totalImpact = computed(() => {
  return validTransactions.value.reduce((sum, t) => sum + t.montant, 0)
})

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    uploadFile(file)
  }
}

const handleDropzoneClick = () => {
  if (selectedCompteId.value) {
    fileInput.value?.click()
  }
}

const handleDrop = (event: DragEvent) => {
  isDragging.value = false

  if (!selectedCompteId.value) {
    errorMessage.value = 'Veuillez d\'abord sélectionner un compte'
    return
  }

  const file = event.dataTransfer?.files[0]
  if (file) {
    const validTypes = ['application/pdf', 'text/csv', 'application/csv', 'text/plain']
    const validExtensions = ['.pdf', '.csv']
    const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'))

    if (!validTypes.includes(file.type) && !validExtensions.includes(fileExtension)) {
      errorMessage.value = 'Le fichier doit être un PDF ou un CSV'
      return
    }
    uploadFile(file)
  }
}

const uploadFile = async (file: File) => {
  errorMessage.value = ''
  uploading.value = true

  try {
    const result = await apiService.uploadBankStatement(file, selectedCompteId.value)

    parsedTransactions.value = (result.transactions as any as ParsedTransaction[]).map((t: ParsedTransaction) => ({
      ...t,
      removed: false
    }))

    logger.info(`Successfully parsed ${result.transactions.length} transactions`)

  } catch (error) {
    logger.error('Upload error:', error)
    errorMessage.value = error instanceof Error ? error.message : 'Erreur lors de l\'upload du fichier'
  } finally {
    uploading.value = false
  }
}

const toggleRemove = (index: number) => {
  parsedTransactions.value[index].removed = !parsedTransactions.value[index].removed
}

const validateTransactions = async () => {
  if (validTransactions.value.length === 0) return

  saving.value = true
  errorMessage.value = ''

  try {
    const transactionsToCreate = validTransactions.value.map(t => ({
      compteId: selectedCompteId.value,
      montant: t.montant,
      description: t.description,
      type: t.type as TypeTransaction,
      dateTransaction: t.date
    }))

    await apiService.createBulkTransactions(transactionsToCreate)

    logger.info(`Successfully created ${validTransactions.value.length} transactions`)
    emit('upload-success')
    resetUpload()

  } catch (error) {
    logger.error('Save error:', error)
    errorMessage.value = error instanceof Error ? error.message : 'Erreur lors de l\'enregistrement'
  } finally {
    saving.value = false
  }
}

const resetUpload = () => {
  parsedTransactions.value = []
  errorMessage.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const formatCurrency = (amount: number): string => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(amount)
}
</script>

<style scoped>
.bank-statement-upload {
  padding: 1rem;
}

.upload-section,
.review-section {
  padding: 2rem;
}

h3 {
  color: white;
  margin: 0 0 1rem 0;
  font-size: 1.3rem;
}

.upload-desc,
.review-desc {
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 1.5rem;
}

.compte-selector {
  margin-bottom: 1.5rem;
}

.compte-selector label {
  display: block;
  color: white;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.compte-selector select {
  width: 100%;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 1rem;
  cursor: pointer;
}

.compte-selector select option {
  background: #2d3748;
  color: white;
}

.dropzone {
  border: 2px dashed rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.05);
}

.dropzone:not(.disabled):hover {
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(102, 126, 234, 0.1);
}

.dropzone.dragging {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.2);
}

.dropzone.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dropzone-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 1rem;
}

.dropzone-text {
  color: white;
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.click-text {
  color: #667eea;
  text-decoration: underline;
}

.dropzone-hint {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.9rem;
}

.upload-progress {
  margin-top: 1.5rem;
  text-align: center;
}

.progress-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  animation: progress-animation 1.5s infinite;
}

@keyframes progress-animation {
  0% { width: 0%; }
  50% { width: 70%; }
  100% { width: 100%; }
}

.upload-progress p {
  color: rgba(255, 255, 255, 0.7);
}

.error-message {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.5);
  border-radius: 8px;
  color: #ef4444;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.btn-back {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-back:hover {
  background: rgba(255, 255, 255, 0.2);
}

.transactions-table-wrapper {
  overflow-x: auto;
  margin: 1.5rem 0;
  border-radius: 8px;
  max-height: 500px;
  overflow-y: auto;
}

.transactions-table {
  width: 100%;
  border-collapse: collapse;
  background: rgba(255, 255, 255, 0.05);
}

.transactions-table thead {
  background: rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
}

.transactions-table th {
  padding: 1rem;
  text-align: left;
  color: white;
  font-weight: 600;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
}

.transactions-table td {
  padding: 0.75rem 1rem;
  color: white;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.transactions-table tr.removed {
  opacity: 0.4;
  text-decoration: line-through;
}

.table-input,
.table-select {
  width: 100%;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: white;
  font-size: 0.9rem;
}

.table-input:disabled,
.table-select:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.table-select option {
  background: #2d3748;
  color: white;
}

.amount-input {
  text-align: right;
  font-weight: 600;
}

.positive {
  color: #22c55e;
}

.negative {
  color: #ef4444;
}

.btn-remove {
  padding: 0.5rem;
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.5);
  border-radius: 4px;
  color: #ef4444;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 1rem;
}

.btn-remove:hover {
  background: rgba(239, 68, 68, 0.3);
}

.btn-restore {
  background: rgba(34, 197, 94, 0.2);
  border-color: rgba(34, 197, 94, 0.5);
  color: #22c55e;
}

.btn-restore:hover {
  background: rgba(34, 197, 94, 0.3);
}

.review-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.review-summary {
  color: white;
}

.review-summary p {
  margin: 0.5rem 0;
}

.removed-info {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.9rem;
}

.total-impact {
  font-size: 1.1rem;
}

.review-buttons {
  display: flex;
  gap: 1rem;
}

.btn-cancel,
.btn-validate {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.2);
}

.btn-validate {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-validate:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-validate:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
