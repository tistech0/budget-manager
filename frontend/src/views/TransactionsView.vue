<template>
  <MainLayout>
    <div class="transactions-view">
      <!-- Page Title Header -->
    <header class="page-header">
      <div class="container">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">📝 Gestion des Transactions</h1>
            <p class="page-subtitle">Visualisez et gérez toutes vos transactions</p>
          </div>
          <button @click="showAddTransactionModal = true" class="btn btn-primary">
            ➕ Nouvelle transaction
          </button>
        </div>
      </div>
    </header>

    <main class="page-main">
      <div class="container">
        <!-- Filters & Search Section -->
        <div class="filters-section glass-card">
          <div class="search-row">
            <div class="search-input-wrapper">
              <span class="search-icon">🔍</span>
              <input
                v-model="searchQuery"
                type="text"
                class="search-input"
                placeholder="Rechercher une transaction..."
              >
            </div>
          </div>

          <div class="filters-row">
            <div class="filter-group">
              <label class="filter-label">Type</label>
              <select v-model="filterType" class="filter-select">
                <option value="ALL">Tous</option>
                <option value="REVENU">Revenus</option>
                <option value="DEPENSE">Dépenses</option>
                <option value="TRANSFERT">Transferts</option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">Compte</label>
              <select v-model="filterCompteId" class="filter-select">
                <option value="ALL">Tous les comptes</option>
                <option v-for="compte in comptes" :key="compte.id" :value="compte.id">
                  {{ compte.nom }}
                </option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">Période</label>
              <select v-model="filterDateRange" class="filter-select">
                <option value="THIS_MONTH">Ce mois</option>
                <option value="LAST_MONTH">Le mois dernier</option>
                <option value="LAST_3_MONTHS">3 derniers mois</option>
                <option value="THIS_YEAR">Cette année</option>
                <option value="ALL">Toutes</option>
              </select>
            </div>

            <div class="filter-group">
              <label class="filter-label">Trier par</label>
              <select v-model="sortBy" class="filter-select">
                <option value="date_desc">Plus récent</option>
                <option value="date_asc">Plus ancien</option>
                <option value="amount_desc">Montant (décroissant)</option>
                <option value="amount_asc">Montant (croissant)</option>
                <option value="description">Description (A-Z)</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Summary Cards -->
        <div class="summary-cards">
          <div class="summary-card glass-card total">
            <div class="summary-header">
              <span class="summary-icon">💰</span>
              <span class="summary-label">Total</span>
            </div>
            <div class="summary-amount" :class="{ 'positive': summaryTotal > 0, 'negative': summaryTotal < 0 }">
              {{ summaryTotal >= 0 ? '+' : '' }}{{ formatCurrency(summaryTotal) }}
            </div>
          </div>

          <div class="summary-card glass-card revenus">
            <div class="summary-header">
              <span class="summary-icon">📈</span>
              <span class="summary-label">Revenus</span>
            </div>
            <div class="summary-amount positive">
              +{{ formatCurrency(summaryRevenus) }}
            </div>
          </div>

          <div class="summary-card glass-card depenses">
            <div class="summary-header">
              <span class="summary-icon">📉</span>
              <span class="summary-label">Dépenses</span>
            </div>
            <div class="summary-amount negative">
              {{ formatCurrency(summaryDepenses) }}
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Chargement des transactions...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="filteredAndSortedTransactions.length === 0 && !loading" class="empty-state glass-card">
          <span class="empty-icon">📝</span>
          <h3>Aucune transaction trouvée</h3>
          <p v-if="hasActiveFilters">Aucun résultat pour ces filtres</p>
          <p v-else>Ajoutez votre première transaction pour commencer</p>
          <button @click="showAddTransactionModal = true" class="btn btn-primary btn-lg">
            ➕ Ajouter une transaction
          </button>
        </div>

        <!-- Transactions List -->
        <div v-else class="transactions-section">
          <div class="transactions-header">
            <h3>📋 Transactions ({{ filteredAndSortedTransactions.length }})</h3>
          </div>

          <div class="transactions-list">
            <div
              v-for="transaction in paginatedTransactions"
              :key="transaction.id"
              class="transaction-card glass-card"
              :class="{
                'credit': transaction.montant > 0,
                'debit': transaction.montant < 0
              }"
              @click="openEditTransaction(transaction)"
            >
              <div class="transaction-icon">
                {{ getTransactionIcon(transaction.type) }}
              </div>
              <div class="transaction-info">
                <h4 class="transaction-description">{{ transaction.description }}</h4>
                <p class="transaction-meta">
                  <span class="transaction-account">{{ transaction.compte?.nom || 'Compte inconnu' }}</span>
                  <span class="separator">•</span>
                  <span class="transaction-date">{{ formatDate(transaction.dateTransaction) }}</span>
                </p>
              </div>
              <div class="transaction-amount" :class="{ 'positive': transaction.montant > 0, 'negative': transaction.montant < 0 }">
                {{ transaction.montant > 0 ? '+' : '' }}{{ formatCurrency(transaction.montant) }}
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div class="pagination" v-if="totalPages > 1">
            <button
              class="pagination-btn"
              :disabled="currentPage === 1"
              @click="changePage(currentPage - 1)"
            >
              ← Précédent
            </button>
            <div class="pagination-info">
              Page {{ currentPage }} sur {{ totalPages }}
            </div>
            <button
              class="pagination-btn"
              :disabled="currentPage === totalPages"
              @click="changePage(currentPage + 1)"
            >
              Suivant →
            </button>
          </div>
        </div>

        <!-- Bank Statement Upload -->
        <div class="upload-section">
          <BankStatementUpload
            :comptes="comptes"
            @upload-success="handleUploadSuccess"
          />
        </div>
      </div>
    </main>

    <!-- Edit Transaction Modal -->
    <Teleport to="body">
      <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
        <div class="modal-content">
          <h3>Modifier la Transaction</h3>
          <form @submit.prevent="updateTransaction">
            <div class="form-group">
              <label>Type</label>
              <select v-model="editTransactionData.type" required>
                <optgroup label="💰 Revenus">
                  <option value="SALAIRE">Salaire</option>
                  <option value="PRIME">Prime</option>
                  <option value="FREELANCE">Freelance</option>
                  <option value="ALLOCATION">Allocation</option>
                  <option value="REMBOURSEMENT">Remboursement</option>
                </optgroup>
                <optgroup label="🏠 Charges Fixes">
                  <option value="LOYER">Loyer</option>
                  <option value="ASSURANCE">Assurance</option>
                  <option value="ABONNEMENT">Abonnement</option>
                </optgroup>
                <optgroup label="🛍️ Dépenses Variables">
                  <option value="ALIMENTATION">Alimentation</option>
                  <option value="RESTAURANT">Restaurant</option>
                  <option value="TRANSPORT">Transport</option>
                  <option value="SHOPPING">Shopping</option>
                  <option value="LOISIRS">Loisirs</option>
                  <option value="SANTE">Santé</option>
                </optgroup>
                <optgroup label="💎 Épargne">
                  <option value="EPARGNE">Épargne</option>
                  <option value="INVESTISSEMENT">Investissement</option>
                </optgroup>
              </select>
            </div>
            <div class="form-group">
              <label>Montant (€)</label>
              <input v-model.number="editTransactionData.montant" type="number" step="0.01" required />
            </div>
            <div class="form-group">
              <label>Description</label>
              <input v-model="editTransactionData.description" type="text" required />
            </div>
            <div class="form-group">
              <label>Date</label>
              <input v-model="editTransactionData.dateTransaction" type="date" required />
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

    <!-- Add Transaction Modal -->
    <AddExpenseModal
      v-model="showAddTransactionModal"
      @success="handleUploadSuccess"
    />
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import MainLayout from '@/layouts/MainLayout.vue'
import BankStatementUpload from '@/components/BankStatementUpload.vue'
import AddExpenseModal from '@/components/modals/AddExpenseModal.vue'
import { logger } from '@/utils/logger'
import { formatCurrency } from '@/utils/formatters'
import { useTransactionIcons } from '@/composables/useTransactionIcons'
import { useToast } from '@/composables/useToast'
import type { Transaction, TypeTransaction } from '@/types'
import { apiService } from '@/services/api'

const { getTransactionIcon } = useTransactionIcons()
const toast = useToast()

const router = useRouter()
const dashboardStore = useDashboardStore()
const { user, comptes, transactions, loading } = storeToRefs(dashboardStore)
const { loadDashboard } = dashboardStore

// Navigation
const navigateToPatrimoine = () => {
  router.push('/patrimoine')
}

// Filters & Search
const searchQuery = ref('')
const filterType = ref('ALL')
const filterCompteId = ref('ALL')
const filterDateRange = ref('THIS_MONTH')
const sortBy = ref('date_desc')

// Pagination
const currentPage = ref(1)
const itemsPerPage = ref(20)

// Modals
const showAddTransactionModal = ref(false)
const showEditModal = ref(false)
const editTransactionData = ref({
  id: '',
  type: '' as TypeTransaction,
  montant: 0,
  description: '',
  dateTransaction: ''
})

// Computed - Active Filters
const hasActiveFilters = computed(() => {
  return searchQuery.value !== '' ||
    filterType.value !== 'ALL' ||
    filterCompteId.value !== 'ALL' ||
    filterDateRange.value !== 'THIS_MONTH'
})

// Computed - Filtered Transactions
const filteredAndSortedTransactions = computed(() => {
  let filtered = [...(transactions.value || [])]

  // Search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(t =>
      t.description?.toLowerCase().includes(query)
    )
  }

  // Type filter
  if (filterType.value !== 'ALL') {
    filtered = filtered.filter(t => {
      if (filterType.value === 'REVENU') return t.montant > 0
      if (filterType.value === 'DEPENSE') return t.montant < 0
      if (filterType.value === 'TRANSFERT') return t.type === 'VIREMENT_INTERNE'
      return true
    })
  }

  // Account filter
  if (filterCompteId.value !== 'ALL') {
    filtered = filtered.filter(t => t.compte?.id === filterCompteId.value)
  }

  // Date range filter
  if (filterDateRange.value !== 'ALL') {
    const now = new Date()
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
    const startOfLastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
    const endOfLastMonth = new Date(now.getFullYear(), now.getMonth(), 0)
    const threeMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 3, 1)
    const startOfYear = new Date(now.getFullYear(), 0, 1)

    filtered = filtered.filter(t => {
      const transDate = new Date(t.dateTransaction)
      if (filterDateRange.value === 'THIS_MONTH') {
        return transDate >= startOfMonth
      }
      if (filterDateRange.value === 'LAST_MONTH') {
        return transDate >= startOfLastMonth && transDate <= endOfLastMonth
      }
      if (filterDateRange.value === 'LAST_3_MONTHS') {
        return transDate >= threeMonthsAgo
      }
      if (filterDateRange.value === 'THIS_YEAR') {
        return transDate >= startOfYear
      }
      return true
    })
  }

  // Sort
  filtered.sort((a, b) => {
    if (sortBy.value === 'date_desc') {
      return new Date(b.dateTransaction).getTime() - new Date(a.dateTransaction).getTime()
    }
    if (sortBy.value === 'date_asc') {
      return new Date(a.dateTransaction).getTime() - new Date(b.dateTransaction).getTime()
    }
    if (sortBy.value === 'amount_desc') {
      return Math.abs(b.montant) - Math.abs(a.montant)
    }
    if (sortBy.value === 'amount_asc') {
      return Math.abs(a.montant) - Math.abs(b.montant)
    }
    if (sortBy.value === 'description') {
      return (a.description || '').localeCompare(b.description || '')
    }
    return 0
  })

  return filtered
})

// Computed - Summary Statistics
const summaryTotal = computed(() => {
  return filteredAndSortedTransactions.value.reduce((sum, t) => sum + t.montant, 0)
})

const summaryRevenus = computed(() => {
  return filteredAndSortedTransactions.value
    .filter(t => t.montant > 0)
    .reduce((sum, t) => sum + t.montant, 0)
})

const summaryDepenses = computed(() => {
  return filteredAndSortedTransactions.value
    .filter(t => t.montant < 0)
    .reduce((sum, t) => sum + t.montant, 0)
})

// Computed - Pagination
const totalPages = computed(() => {
  return Math.ceil(filteredAndSortedTransactions.value.length / itemsPerPage.value)
})

const paginatedTransactions = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredAndSortedTransactions.value.slice(start, end)
})

// Methods
const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric'
  })
}

const changePage = (page: number) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const openEditTransaction = (transaction: Transaction) => {
  editTransactionData.value = {
    id: transaction.id,
    type: transaction.type,
    montant: transaction.montant,
    description: transaction.description,
    dateTransaction: transaction.dateTransaction
  }
  showEditModal.value = true
}

const updateTransaction = async () => {
  try {
    await apiService.updateTransaction(editTransactionData.value.id, {
      type: editTransactionData.value.type,
      montant: editTransactionData.value.montant,
      description: editTransactionData.value.description,
      dateTransaction: editTransactionData.value.dateTransaction
    })

    logger.info('Transaction updated successfully')
    toast.success('Transaction modifiée avec succès')
    showEditModal.value = false
    await loadDashboard()
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
    await apiService.deleteTransaction(editTransactionData.value.id)

    logger.info('Transaction deleted successfully')
    toast.success('Transaction supprimée avec succès')
    showEditModal.value = false
    await loadDashboard()
  } catch (error) {
    logger.error('Error deleting transaction:', error)
    toast.error('Erreur lors de la suppression de la transaction')
  }
}

const handleUploadSuccess = async () => {
  logger.info('Bank statement uploaded successfully, refreshing transactions')
  await loadDashboard()
}

// Lifecycle
onMounted(async () => {
  await loadDashboard()
})
</script>

<style scoped>
/* Page Header */
.page-header {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding: 2rem 0;
  position: sticky;
  top: 73px;
  z-index: 100;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}

.header-text {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.95rem;
  margin: 0;
}

/* Main Content */
.page-main {
  padding: 2rem 0 4rem 0;
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

/* Filters Section */
.filters-section {
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.search-row {
  margin-bottom: 1rem;
}

.search-input-wrapper {
  position: relative;
  width: 100%;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.25rem;
  opacity: 0.6;
}

.search-input {
  width: 100%;
  padding: 0.875rem 1rem 0.875rem 3rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: #E8EAF6;
  font-size: 0.95rem;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.15);
  border-color: #667EEA;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.filters-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-select {
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #E8EAF6;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-select:hover {
  background: rgba(255, 255, 255, 0.15);
}

.filter-select:focus {
  outline: none;
  border-color: #667EEA;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.filter-select option {
  background: #1a1a2e;
  color: #E8EAF6;
}

/* Summary Cards */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.summary-card {
  padding: 1.5rem;
  transition: all 0.3s;
}

.summary-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.summary-icon {
  font-size: 1.75rem;
}

.summary-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-amount {
  font-size: 2rem;
  font-weight: 700;
  color: #E8EAF6;
}

.summary-amount.positive {
  color: #4CAF50;
}

.summary-amount.negative {
  color: #F44336;
}

/* Loading & Empty States */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: rgba(255, 255, 255, 0.7);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #667EEA;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
  opacity: 0.5;
}

.empty-state h3 {
  color: #E8EAF6;
  margin: 0 0 0.75rem 0;
  font-size: 1.5rem;
}

.empty-state p {
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 2rem 0;
  max-width: 500px;
}

/* Transactions Section */
.transactions-section {
  margin-bottom: 2rem;
}

.transactions-header {
  margin-bottom: 1.5rem;
}

.transactions-header h3 {
  color: #E8EAF6;
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.transaction-card {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 4px solid transparent;
}

.transaction-card:hover {
  transform: translateX(4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 0.08);
}

.transaction-card.credit {
  border-left-color: #4CAF50;
}

.transaction-card.debit {
  border-left-color: #F44336;
}

.transaction-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.transaction-info {
  flex: 1;
  min-width: 0;
}

.transaction-description {
  color: #E8EAF6;
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  flex-wrap: wrap;
}

.separator {
  opacity: 0.5;
}

.transaction-badge {
  padding: 0.25rem 0.5rem;
  background: rgba(76, 175, 80, 0.2);
  border-radius: 6px;
  font-size: 0.75rem;
  color: #4CAF50;
  font-weight: 600;
}

.transaction-amount {
  font-size: 1.25rem;
  font-weight: 700;
  flex-shrink: 0;
}

.transaction-amount.positive {
  color: #4CAF50;
}

.transaction-amount.negative {
  color: #F44336;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  margin-top: 2rem;
  padding: 1.5rem;
}

.pagination-btn {
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: #E8EAF6;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.pagination-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

/* Upload Section */
.upload-section {
  margin-top: 3rem;
}

/* Buttons */
.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  white-space: nowrap;
}

.btn-primary {
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.btn-lg {
  padding: 1rem 2rem;
  font-size: 1rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  width: 100%;
  max-width: 500px;
  padding: 2rem;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  color: #E8EAF6;
}

.btn-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-size: 1.25rem;
  transition: all 0.2s;
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* Responsive */
@media (max-width: 1024px) {
  .header-wrapper {
    flex-wrap: wrap;
  }

  .main-nav {
    order: 3;
    width: 100%;
  }

  .filters-row {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .summary-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .logo-text {
    display: none;
  }

  .main-nav {
    gap: 0.25rem;
    padding: 0.25rem;
  }

  .nav-item {
    flex: 1;
    justify-content: center;
    padding: 0.75rem 0.5rem;
  }

  .nav-text {
    display: none;
  }

  .nav-icon {
    font-size: 1.5rem;
  }

  .user-name {
    display: none;
  }

  .page-header {
    top: 66px;
  }

  .header-content {
    flex-direction: column;
    align-items: stretch;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .filters-row {
    grid-template-columns: 1fr;
  }

  .summary-amount {
    font-size: 1.5rem;
  }

  .transaction-card {
    flex-wrap: wrap;
  }

  .transaction-amount {
    width: 100%;
    text-align: right;
  }
}

/* Modal Form Styles */
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
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 1rem;
  transition: all 0.3s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.15);
  border-color: #667eea;
}

.form-group select option {
  background: #2d3748;
  color: white;
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
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-cancel:hover {
  background: rgba(255, 255, 255, 0.2);
}

.btn-submit {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.btn-submit:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.btn-delete {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

.btn-delete:hover {
  background: rgba(220, 38, 38, 1);
  transform: translateY(-2px);
}
</style>
