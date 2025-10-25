<template>
  <div class="recent-transactions glass-card">
    <div class="transactions-header">
      <h4>📝 Transactions Récentes</h4>
      <div class="header-actions">
        <select v-model="filterType" class="filter-select">
          <option value="all">Toutes</option>
          <option value="SALAIRE">Salaires</option>
          <option value="ALIMENTATION">Alimentation</option>
          <option value="RESTAURANT">Restaurant</option>
          <option value="TRANSPORT">Transport</option>
          <option value="SHOPPING">Shopping</option>
          <option value="EPARGNE">Épargne</option>
        </select>
        <button class="btn-add-transaction" @click="showAddModal = true">
          <span>➕</span> Ajouter
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <p>Chargement...</p>
    </div>

    <div v-else-if="filteredTransactions.length === 0" class="empty-state">
      <p>Aucune transaction</p>
    </div>

    <div v-else class="transactions-list">
      <div
        v-for="transaction in filteredTransactions"
        :key="transaction.id"
        class="transaction-item"
        :class="{ 'credit': transaction.montant > 0, 'debit': transaction.montant < 0 }"
        @click="openEditModal(transaction)"
      >
        <div class="transaction-icon">
          {{ getTransactionIcon(transaction.type) }}
        </div>
        <div class="transaction-details">
          <span class="transaction-desc">{{ transaction.description }}</span>
          <span class="transaction-info">
            {{ transaction.compte?.nom }} • {{ formatDate(transaction.dateTransaction) }}
          </span>
        </div>
        <div class="transaction-amount" :class="{ 'positive': transaction.montant > 0, 'negative': transaction.montant < 0 }">
          {{ transaction.montant > 0 ? '+' : '' }}{{ formatCurrency(transaction.montant) }}
        </div>
      </div>
    </div>

    <!-- Add Transaction Modal -->
    <Teleport to="body">
      <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
        <div class="modal-content">
        <h3>Ajouter une Transaction</h3>
        <form @submit.prevent="addTransaction">
          <div class="form-group">
            <label>Compte</label>
            <select v-model="newTransaction.compteId" required>
              <option value="" disabled>Sélectionnez un compte</option>
              <option v-for="compte in comptes" :key="compte.id" :value="compte.id">
                {{ compte.nom }} ({{ compte.banque.nom }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Type</label>
            <select v-model="newTransaction.type" required>
              <optgroup label="💰 Revenus">
                <option value="SALAIRE">Salaire</option>
                <option value="PRIME">Prime</option>
                <option value="FREELANCE">Freelance</option>
                <option value="ALLOCATION">Allocation</option>
                <option value="REMBOURSEMENT">Remboursement</option>
                <option value="GAIN_INVESTISSEMENT">Gain Investissement</option>
                <option value="CADEAU_RECU">Cadeau Reçu</option>
                <option value="VENTE">Vente</option>
              </optgroup>
              <optgroup label="🏠 Charges Fixes">
                <option value="LOYER">Loyer</option>
                <option value="ASSURANCE">Assurance</option>
                <option value="ABONNEMENT">Abonnement</option>
                <option value="CREDIT_IMMOBILIER">Crédit Immobilier</option>
                <option value="CREDIT_CONSO">Crédit Conso</option>
                <option value="IMPOTS">Impôts</option>
                <option value="MUTUELLE">Mutuelle</option>
              </optgroup>
              <optgroup label="🛍️ Dépenses Variables">
                <option value="ALIMENTATION">Alimentation</option>
                <option value="RESTAURANT">Restaurant</option>
                <option value="TRANSPORT">Transport</option>
                <option value="ESSENCE">Essence</option>
                <option value="SHOPPING">Shopping</option>
                <option value="LOISIRS">Loisirs</option>
                <option value="SANTE">Santé</option>
                <option value="BEAUTE">Beauté</option>
                <option value="MAISON">Maison</option>
                <option value="EDUCATION">Éducation</option>
                <option value="VOYAGE">Voyage</option>
              </optgroup>
              <optgroup label="💎 Épargne & Investissement">
                <option value="EPARGNE">Épargne</option>
                <option value="INVESTISSEMENT">Investissement</option>
                <option value="VIREMENT_INTERNE">Virement Interne</option>
                <option value="TRANSFERT_OBJECTIF">Transfert Objectif</option>
              </optgroup>
              <optgroup label="📝 Autres">
                <option value="RETRAIT_ESPECES">Retrait Espèces</option>
                <option value="FRAIS_BANCAIRE">Frais Bancaire</option>
                <option value="COMMISSION">Commission</option>
                <option value="AUTRE">Autre</option>
              </optgroup>
            </select>
          </div>
          <div class="form-group">
            <label>Montant (€)</label>
            <input v-model.number="newTransaction.montant" type="number" step="0.01" required />
          </div>
          <div class="form-group">
            <label>Description</label>
            <input v-model="newTransaction.description" type="text" required />
          </div>
          <div class="form-group">
            <label>Date</label>
            <input v-model="newTransaction.dateTransaction" type="date" required />
          </div>
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="showAddModal = false">Annuler</button>
            <button type="submit" class="btn-submit">Ajouter</button>
          </div>
        </form>
        </div>
      </div>
    </Teleport>

    <!-- Edit Transaction Modal -->
    <Teleport to="body">
      <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
        <div class="modal-content">
        <h3>Modifier la Transaction</h3>
        <form @submit.prevent="updateTransaction">
          <div class="form-group">
            <label>Type</label>
            <select v-model="editTransaction.type" required>
              <optgroup label="💰 Revenus">
                <option value="SALAIRE">Salaire</option>
                <option value="PRIME">Prime</option>
                <option value="FREELANCE">Freelance</option>
                <option value="ALLOCATION">Allocation</option>
                <option value="REMBOURSEMENT">Remboursement</option>
                <option value="GAIN_INVESTISSEMENT">Gain Investissement</option>
                <option value="CADEAU_RECU">Cadeau Reçu</option>
                <option value="VENTE">Vente</option>
              </optgroup>
              <optgroup label="🏠 Charges Fixes">
                <option value="LOYER">Loyer</option>
                <option value="ASSURANCE">Assurance</option>
                <option value="ABONNEMENT">Abonnement</option>
                <option value="CREDIT_IMMOBILIER">Crédit Immobilier</option>
                <option value="CREDIT_CONSO">Crédit Conso</option>
                <option value="IMPOTS">Impôts</option>
                <option value="MUTUELLE">Mutuelle</option>
              </optgroup>
              <optgroup label="🛍️ Dépenses Variables">
                <option value="ALIMENTATION">Alimentation</option>
                <option value="RESTAURANT">Restaurant</option>
                <option value="TRANSPORT">Transport</option>
                <option value="ESSENCE">Essence</option>
                <option value="SHOPPING">Shopping</option>
                <option value="LOISIRS">Loisirs</option>
                <option value="SANTE">Santé</option>
                <option value="BEAUTE">Beauté</option>
                <option value="MAISON">Maison</option>
                <option value="EDUCATION">Éducation</option>
                <option value="VOYAGE">Voyage</option>
              </optgroup>
              <optgroup label="💎 Épargne & Investissement">
                <option value="EPARGNE">Épargne</option>
                <option value="INVESTISSEMENT">Investissement</option>
                <option value="VIREMENT_INTERNE">Virement Interne</option>
                <option value="TRANSFERT_OBJECTIF">Transfert Objectif</option>
              </optgroup>
              <optgroup label="📝 Autres">
                <option value="RETRAIT_ESPECES">Retrait Espèces</option>
                <option value="FRAIS_BANCAIRE">Frais Bancaire</option>
                <option value="COMMISSION">Commission</option>
                <option value="AUTRE">Autre</option>
              </optgroup>
            </select>
          </div>
          <div class="form-group">
            <label>Montant (€)</label>
            <input v-model.number="editTransaction.montant" type="number" step="0.01" required />
          </div>
          <div class="form-group">
            <label>Description</label>
            <input v-model="editTransaction.description" type="text" required />
          </div>
          <div class="form-group">
            <label>Date</label>
            <input v-model="editTransaction.dateTransaction" type="date" required />
          </div>
          <div class="form-actions">
            <button type="button" class="btn-delete" @click="deleteTransaction">Supprimer</button>
            <button type="button" class="btn-cancel" @click="showEditModal = false">Annuler</button>
            <button type="submit" class="btn-submit">Enregistrer</button>
          </div>
        </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import { logger } from '@/utils/logger'
import { apiService } from '@/services/api'
import { formatCurrency } from '@/utils/formatters'
import { useTransactionIcons } from '@/composables/useTransactionIcons'
import { useToast } from '@/composables/useToast'
import type { TypeTransaction } from '@/types'

const { getTransactionIcon } = useTransactionIcons()
const toast = useToast()

const dashboardStore = useDashboardStore()
const { comptes } = storeToRefs(dashboardStore)

interface Transaction {
  id: string
  compte?: { nom: string }
  montant: number
  type: string
  description: string
  dateTransaction: string
}

const loading = ref(false)
const filterType = ref('all')
const showAddModal = ref(false)
const showEditModal = ref(false)
const transactions = ref<Transaction[]>([])

const newTransaction = ref({
  type: 'ALIMENTATION' as TypeTransaction,
  montant: 0,
  description: '',
  compteId: '',
  dateTransaction: new Date().toISOString().split('T')[0]
})

const editTransaction = ref({
  id: '',
  type: 'ALIMENTATION' as TypeTransaction,
  montant: 0,
  description: '',
  dateTransaction: ''
})

const filteredTransactions = computed(() => {
  if (filterType.value === 'all') {
    return transactions.value
  }
  return transactions.value.filter(t => t.type === filterType.value)
})

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short'
  })
}

const loadTransactions = async () => {
  loading.value = true
  try {
    const data = await apiService.getTransactions({ limit: 10 })
    transactions.value = data
    logger.info('Loaded transactions:', data.length)
  } catch (error) {
    logger.error('Error loading transactions:', error)
    transactions.value = []
  } finally {
    loading.value = false
  }
}

const addTransaction = async () => {
  try {
    await apiService.createTransaction({
      compteId: newTransaction.value.compteId,
      type: newTransaction.value.type,
      montant: newTransaction.value.montant,
      description: newTransaction.value.description,
      dateTransaction: newTransaction.value.dateTransaction
    })

    logger.info('Transaction created successfully')
    toast.success('Transaction créée avec succès')

    // Reload transactions
    await loadTransactions()

    // Close modal and reset form
    showAddModal.value = false
    newTransaction.value = {
      type: 'ALIMENTATION',
      montant: 0,
      description: '',
      compteId: '',
      dateTransaction: new Date().toISOString().split('T')[0]
    }
  } catch (error) {
    logger.error('Error creating transaction:', error)
    toast.error('Erreur lors de la création de la transaction')
  }
}

const openEditModal = (transaction: Transaction) => {
  editTransaction.value = {
    id: transaction.id,
    type: transaction.type as TypeTransaction,
    montant: transaction.montant,
    description: transaction.description,
    dateTransaction: transaction.dateTransaction
  }
  showEditModal.value = true
}

const updateTransaction = async () => {
  try {
    await apiService.updateTransaction(editTransaction.value.id, {
      type: editTransaction.value.type,
      montant: editTransaction.value.montant,
      description: editTransaction.value.description,
      dateTransaction: editTransaction.value.dateTransaction
    })

    logger.info('Transaction updated successfully')
    toast.success('Transaction modifiée avec succès')
    showEditModal.value = false
    await loadTransactions()
  } catch (error) {
    logger.error('Error updating transaction:', error)
    toast.error('Erreur lors de la modification de la transaction')
  }
}

const deleteTransaction = async () => {
  if (!confirm('Êtes-vous sûr de vouloir supprimer cette transaction ?')) {
    return
  }

  try {
    await apiService.deleteTransaction(editTransaction.value.id)

    logger.info('Transaction deleted successfully')
    toast.success('Transaction supprimée avec succès')
    showEditModal.value = false
    await loadTransactions()
  } catch (error) {
    logger.error('Error deleting transaction:', error)
    toast.error('Erreur lors de la suppression de la transaction')
  }
}

onMounted(() => {
  loadTransactions()
})
</script>

<style scoped>
.recent-transactions {
  padding: 1.5rem;
}

.transactions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.transactions-header h4 {
  color: white;
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.filter-select {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-select:hover {
  background: rgba(255, 255, 255, 0.15);
}

.filter-select option {
  background: #2d3748;
  color: white;
}

.btn-add-transaction {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-add-transaction:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.25);
  border-radius: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.transaction-item:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.transaction-item.credit {
  border-color: rgba(34, 197, 94, 0.3);
}

.transaction-item.debit {
  border-color: rgba(239, 68, 68, 0.3);
}

.transaction-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.transaction-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
  min-width: 0;
}

.transaction-desc {
  color: white;
  font-weight: 500;
  font-size: 0.95rem;
}

.transaction-info {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
}

.transaction-amount {
  font-size: 1.1rem;
  font-weight: 600;
  flex-shrink: 0;
}

.transaction-amount.positive {
  color: #22c55e;
}

.transaction-amount.negative {
  color: #ef4444;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 2rem;
  color: rgba(255, 255, 255, 0.6);
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background:
    radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(118, 75, 162, 0.15) 0%, transparent 50%),
    rgba(26, 32, 44, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
  padding: 2rem;
  border-radius: 20px;
  min-width: 450px;
  max-width: 90%;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-content h3 {
  color: white;
  margin: 0 0 1.5rem 0;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  color: white;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  color: white;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(102, 126, 234, 0.8);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

/* Remove number input arrows */
.form-group input[type="number"]::-webkit-inner-spin-button,
.form-group input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.form-group input[type="number"] {
  -moz-appearance: textfield;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-cancel,
.btn-submit,
.btn-delete {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-weight: 600;
}

.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.btn-submit {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
  font-weight: 700;
}

.btn-submit:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.6);
}

.btn-delete {
  background: rgba(239, 68, 68, 0.9);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-weight: 600;
}

.btn-delete:hover {
  background: rgba(220, 38, 38, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.6);
}
</style>
