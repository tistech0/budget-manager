<template>
  <MainLayout>
    <div class="dashboard-view">
      <main class="dashboard-main">
        <div class="container">
          <!-- Navigation mensuelle -->
          <MonthSelector
            :month-label="currentMonthLabel"
            v-bind="periodInfoText ? { periodInfo: periodInfoText } : {}"
            :can-go-to-previous="!loading && canGoToPreviousMonth"
            :can-go-to-next="!loading && canGoToNextMonth"
            :show-delete="!!dashboardData?.salaireValide"
            :disabled="loading"
            @change-month="changeMonth"
            @delete-month="showDeleteMonthModal = true"
          />
  
          <!-- Loading State -->
          <LoadingState v-if="loading" message="Chargement de votre dashboard..." />
  
          <!-- Error State -->
          <div v-else-if="error" class="error-dashboard">
            <p>{{ error }}</p>
            <button @click="refreshDashboard" class="btn btn-primary">
              Réessayer
            </button>
          </div>

          <!-- Empty State: No Salary Validated -->
          <div v-else-if="dashboardData && !dashboardData.salaireValide" class="empty-dashboard">
            <!-- Call to Action Card -->
            <div class="empty-state-cta glass-card">
              <div class="cta-icon">💰</div>
              <h2 class="cta-title">Aucun salaire validé pour {{ currentMonthLabel }}</h2>
              <p class="cta-description">
                Pour afficher vos statistiques et gérer vos dépenses, commencez par valider votre salaire mensuel.
              </p>
              <button @click="showValidateSalaryModal = true" class="btn btn-primary btn-lg">
                <span>💼</span>
                Valider mon salaire de {{ currentMonthLabel }}
              </button>
            </div>

            <!-- Detailed Information Grid -->
            <div class="empty-state-grid">
              <!-- Accounts Section -->
              <div class="info-section glass-card">
                <div class="section-header">
                  <span class="section-icon">💳</span>
                  <h3>Vos Comptes</h3>
                  <span class="section-badge">{{ comptes.length }}</span>
                </div>
                <div class="section-content">
                  <div v-if="comptes.length > 0" class="accounts-list">
                    <div v-for="compte in comptes" :key="compte.id" class="account-item">
                      <div class="account-info">
                        <span class="account-icon">{{ getCompteIcon(compte.type) }}</span>
                        <div class="account-details">
                          <span class="account-name">{{ compte.nom }}</span>
                          <span class="account-type">{{ compte.type.replace(/_/g, ' ') }}</span>
                        </div>
                      </div>
                      <span class="account-balance">{{ formatCurrency(compte.soldeTotal) }}</span>
                    </div>
                    <div class="total-row">
                      <span class="total-label">Total</span>
                      <span class="total-value">{{ formatCurrency(totalSoldes) }}</span>
                    </div>
                  </div>
                  <div v-else class="empty-message">
                    <p>Aucun compte configuré</p>
                    <RouterLink to="/patrimoine" class="btn btn-sm">Voir mon patrimoine</RouterLink>
                  </div>
                </div>
              </div>

              <!-- Objectifs Section -->
              <div class="info-section glass-card">
                <div class="section-header">
                  <span class="section-icon">🎯</span>
                  <h3>Vos Objectifs</h3>
                  <span class="section-badge">{{ objectifs.length }}</span>
                </div>
                <div class="section-content">
                  <div v-if="objectifs.length > 0" class="objectifs-list">
                    <div v-for="objectif in objectifs" :key="objectif.id" class="objectif-item">
                      <div class="objectif-info">
                        <span class="objectif-icon">{{ objectif.icone || '🎯' }}</span>
                        <div class="objectif-details">
                          <span class="objectif-name">{{ objectif.nom }}</span>
                          <div class="objectif-progress-bar">
                            <div
                              class="objectif-progress-fill"
                              :style="{ width: `${getProgression(objectif)}%` }"
                            ></div>
                          </div>
                        </div>
                      </div>
                      <div class="objectif-amounts">
                        <span class="objectif-current">{{ formatCurrency(getMontantActuel(objectif)) }}</span>
                        <span class="objectif-target">/ {{ formatCurrency(objectif.montantCible) }}</span>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-message">
                    <p>Aucun objectif défini</p>
                    <RouterLink to="/patrimoine" class="btn btn-sm">Gérer les objectifs</RouterLink>
                  </div>
                </div>
              </div>

              <!-- Charges Fixes Section -->
              <div class="info-section glass-card">
                <div class="section-header">
                  <span class="section-icon">📅</span>
                  <h3>Charges Fixes</h3>
                  <span class="section-badge">{{ chargesFixes.length }}</span>
                </div>
                <div class="section-content">
                  <div v-if="chargesFixes.length > 0" class="charges-list">
                    <div v-for="charge in chargesFixes.slice(0, 5)" :key="charge.id" class="charge-item">
                      <div class="charge-info">
                        <div class="charge-day">
                          <span class="charge-day-number">{{ charge.jourPrelevement }}</span>
                          <span class="charge-day-label">du mois</span>
                        </div>
                        <div class="charge-details">
                          <span class="charge-name">{{ charge.nom }}</span>
                          <span class="charge-category">{{ charge.categorie }}</span>
                        </div>
                      </div>
                      <span class="charge-amount">{{ formatCurrency(charge.montant) }}</span>
                    </div>
                    <div v-if="chargesFixes.length > 5" class="more-items">
                      +{{ chargesFixes.length - 5 }} autres charges
                    </div>
                  </div>
                  <div v-else class="empty-message">
                    <p>Aucune charge fixe enregistrée</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Dashboard Content -->
          <div v-else-if="dashboardData && dashboardData.salaireValide" class="dashboard-content">
            
            <!-- User Welcome Card -->
            <div class="welcome-card glass-card">
              <div class="welcome-content">
                <div class="welcome-left">
                  <h2>👋 Bonjour {{ user?.prenom }}</h2>
                  <div class="user-stats">
                    <div class="stat">
                      <span class="stat-label">Jour de paie</span>
                      <span class="stat-value">{{ user?.jourPaie }}</span>
                    </div>
                    <div class="stat">
                      <span class="stat-label">Salaire net</span>
                      <span class="stat-value">{{ formatCurrency(user?.salaireMensuelNet) }}</span>
                    </div>
                    <div class="stat">
                      <span class="stat-label">Total épargne</span>
                      <span class="stat-value success">{{ formatCurrency(totalSoldes) }}</span>
                    </div>
                  </div>
                </div>
                <div class="welcome-right">
                  <h4>📊 Répartition Budget</h4>
                  <div class="chart-wrapper-compact">
                    <BudgetPieChart
                      :charges-fixes="user?.pourcentageChargesFixes || 50"
                      :depenses-variables="user?.pourcentageDepensesVariables || 30"
                      :epargne="user?.pourcentageEpargne || 20"
                    />
                  </div>
                </div>
              </div>
            </div>
  
            <!-- Jauges principales -->
            <BudgetGauges :gauges="budgetGaugesData" />

            <!-- Upcoming Charges Fixes List -->
            <div v-if="chargesFixesUpcoming.length > 0" class="upcoming-charges glass-card">
              <h3>📅 Charges fixes à venir ce mois</h3>
              <div class="charges-list">
                <div
                  v-for="charge in chargesFixesUpcoming"
                  :key="charge.id"
                  class="charge-item"
                >
                  <div class="charge-info">
                    <span class="charge-name">{{ charge.nom }}</span>
                    <span class="charge-date">Le {{ charge.jourPrelevement }}</span>
                  </div>
                  <div class="charge-amount">{{ formatCurrency(charge.montant) }}</div>
                </div>
              </div>
            </div>

            <!-- Actions Rapides -->
            <div class="quick-actions glass-card">
              <h3>⚡ Actions rapides</h3>
              <div class="actions-grid">
                <button class="action-btn primary" @click="showValidateSalaryModal = true">
                  <span class="action-icon">✅</span>
                  <span class="action-text">Valider le salaire</span>
                </button>
                <button class="action-btn" @click="showAddExpenseModal = true">
                  <span class="action-icon">➕</span>
                  <span class="action-text">Ajouter dépense</span>
                </button>
                <button class="action-btn" @click="showTransferModal = true">
                  <span class="action-icon">🔄</span>
                  <span class="action-text">Transférer</span>
                </button>
                <button class="action-btn" @click="showVersementModal = true">
                  <span class="action-icon">🎯</span>
                  <span class="action-text">Remplir objectif</span>
                </button>
              </div>
            </div>

            <!-- Transactions Link Section -->
            <div class="transactions-link-section glass-card">
              <div class="transactions-cta">
                <div class="transactions-header">
                  <span class="transactions-icon">📝</span>
                  <div class="transactions-text">
                    <h3>Vos Transactions</h3>
                    <p>Consultez, filtrez et gérez toutes vos transactions</p>
                  </div>
                </div>
                <RouterLink to="/transactions" class="btn btn-primary">
                  Voir toutes les transactions →
                </RouterLink>
              </div>

              <div class="transactions-summary">
                <div class="summary-item">
                  <span class="summary-label">📊 Ce mois</span>
                  <span class="summary-value">{{ transactions.length }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">💰 Revenus</span>
                  <span class="summary-value success">{{ formatCurrency(totalRevenus) }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">💸 Dépenses</span>
                  <span class="summary-value danger">{{ formatCurrency(Math.abs(totalDepenses)) }}</span>
                </div>
              </div>
            </div>

            <!-- Section Patrimoine Link -->
            <div class="patrimoine-link-section glass-card">
              <div class="patrimoine-cta">
                <div class="patrimoine-header">
                  <span class="patrimoine-icon">💎</span>
                  <div class="patrimoine-text">
                    <h3>Votre Patrimoine</h3>
                    <p>Consultez vos comptes et objectifs d'épargne en détail</p>
                  </div>
                </div>
                <RouterLink to="/patrimoine" class="btn btn-primary">
                  Voir mon patrimoine →
                </RouterLink>
              </div>

              <div class="patrimoine-summary">
                <div class="summary-item">
                  <span class="summary-label">💳 Comptes</span>
                  <span class="summary-value">{{ comptes.length }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">🎯 Objectifs</span>
                  <span class="summary-value">{{ objectifs.length }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">💰 Total</span>
                  <span class="summary-value">{{ formatCurrency(totalSoldes) }}</span>
                </div>
              </div>
            </div>
          </div>
  
          <!-- Empty State -->
          <div v-else class="empty-dashboard">
            <div class="empty-content">
              <span class="empty-icon">🎯</span>
              <h3>Bienvenue dans Budget Manager!</h3>
              <p>Commencez par configurer vos comptes et objectifs pour voir votre dashboard.</p>
              <div class="quick-actions">
                <RouterLink to="/comptes" class="btn btn-primary">
                  <span>💳</span> Ajouter un compte
                </RouterLink>
                <RouterLink to="/objectifs" class="btn btn-secondary">
                  <span>🎯</span> Créer un objectif
                </RouterLink>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- Modals -->
      <ValidateSalaryModal
        v-model="showValidateSalaryModal"
        :current-month="dashboardStore.currentMonth"
        @success="refreshDashboard"
      />

      <AddExpenseModal
        v-model="showAddExpenseModal"
        @success="refreshDashboard"
      />

      <TransferModal
        v-model="showTransferModal"
        @success="refreshDashboard"
      />

      <VersementModal
        v-model="showVersementModal"
        @success="refreshDashboard"
      />

      <DeleteMonthModal
        v-model="showDeleteMonthModal"
        :month-label="currentMonthLabel"
        :month-value="dashboardStore.currentMonth"
        :is-deleting="isDeletingMonth"
        @confirm="handleDeleteMonth"
        @cancel="handleCancelDeleteMonth"
      />
    </div>
  </MainLayout>
</template>
  
  <script setup lang="ts">
  import { ref, computed, onMounted, watch } from 'vue'
  import { storeToRefs } from 'pinia'
  import { useDashboardStore } from '@/stores/dashboard'
  import MainLayout from '@/layouts/MainLayout.vue'
  import JaugeCard from '@/components/JaugeCard.vue'
  import BudgetPieChart from '@/components/BudgetPieChart.vue'
  import MonthSelector from '@/components/dashboard/MonthSelector.vue'
  import BudgetGauges from '@/components/dashboard/BudgetGauges.vue'
  import type { BudgetGauge } from '@/components/dashboard/BudgetGauges.vue'
  import LoadingState from '@/components/common/LoadingState.vue'
  import ErrorBoundary from '@/components/common/ErrorBoundary.vue'
  import ValidateSalaryModal from '@/components/modals/ValidateSalaryModal.vue'
  import AddExpenseModal from '@/components/modals/AddExpenseModal.vue'
  import TransferModal from '@/components/modals/TransferModal.vue'
  import VersementModal from '@/components/modals/VersementModal.vue'
  import DeleteMonthModal from '@/components/modals/DeleteMonthModal.vue'
  import { apiService } from '@/services/api'
  import { logger } from '@/utils/logger'
  
  const dashboardStore = useDashboardStore()
  const {
    loading,
    error,
    dashboardData,
    user,
    comptes,
    objectifs,
    chargesFixes,
    transactions,
    totalSoldes,
    currentMonthLabel,
    canGoToPreviousMonth,
    canGoToNextMonth
  } = storeToRefs(dashboardStore)

  const { loadDashboard, changeMonth, refreshDashboard, loadAvailableMonths } = dashboardStore

  // Modal states
  const showValidateSalaryModal = ref(false)
  const showAddExpenseModal = ref(false)
  const showTransferModal = ref(false)
  const showVersementModal = ref(false)
  const showDeleteMonthModal = ref(false)
  const isDeletingMonth = ref(false)

  // Debug logging (development only)
  watch(dashboardData, (newData) => {
    if (newData) {
      logger.log('🔍 DASHBOARD DATA')
      logger.log('User:', newData.user)
      logger.log('Comptes:', newData.comptes)
      logger.log('Objectifs:', newData.objectifs)
      logger.log('⭐ salaireValide:', newData.salaireValide)
      logger.log('Timestamp:', newData.timestamp)
    }
  }, { immediate: true })
  
  // Helpers
  const formatCurrency = (value: number | undefined): string => {
    if (value === undefined || value === null) return '0,00 €'
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(value)
  }
  
  const getCompteIcon = (type: string): string => {
    const icons: Record<string, string> = {
      'COMPTE_COURANT': '💳',
      'LIVRET_A': '📗',
      'LDDS': '📘',
      'PEA': '📊',
      'ASSURANCE_VIE': '🛡️'
    }
    return icons[type] || '💰'
  }

  // Calculs de progression des objectifs (used in empty state)
  const getMontantActuel = (objectif: any): number => {
    if (!objectif.repartitions || objectif.repartitions.length === 0) return 0
    return objectif.repartitions.reduce((total: number, rep: any) => total + (rep.montantActuel || 0), 0)
  }

  const getProgression = (objectif: any): number => {
    const actuel = getMontantActuel(objectif)
    const cible = objectif.montantCible || 1
    return (actuel / cible) * 100
  }

  // Computed - Période
  const currentMonthNumber = computed(() => {
    return new Date(dashboardStore.currentMonth + '-01').getMonth() + 1
  })

  const nextMonthNumber = computed(() => {
    const date = new Date(dashboardStore.currentMonth + '-01')
    date.setMonth(date.getMonth() + 1)
    return date.getMonth() + 1
  })

  const nextMonthDay = computed(() => {
    return (user.value?.jourPaie || 1) - 1
  })

  const periodInfoText = computed(() => {
    if (!user.value?.jourPaie) return undefined
    return `Cycle du ${user.value.jourPaie}/${currentMonthNumber.value} au ${nextMonthDay.value}/${nextMonthNumber.value}`
  })


  // Jauge 1: Compte Courant - Sum all COMPTE_COURANT accounts
  const soldeCompteCourant = computed(() => {
    const comptescourants = comptes.value.filter(c => c.type === 'COMPTE_COURANT')
    return comptescourants.reduce((total, compte) => total + (compte.soldeTotal || 0), 0)
  })

  const pourcentageCompteCourant = computed(() => {
    const objectif = user.value?.objectifCompteCourant || 2000
    if (soldeCompteCourant.value <= 0) {
      // In overdraft - show as a warning percentage
      const decouvert = user.value?.decouvertAutorise || 100
      return Math.min((Math.abs(soldeCompteCourant.value) / decouvert) * 100, 100)
    }
    // Positive balance - show progress toward objectif
    return Math.min((soldeCompteCourant.value / objectif) * 100, 100)
  })

  const descriptionCompteCourant = computed(() => {
    if (soldeCompteCourant.value >= 0) {
      const objectif = user.value?.objectifCompteCourant || 2000
      return `${formatCurrency(soldeCompteCourant.value)} / ${formatCurrency(objectif)}`
    }
    return `Découvert: ${formatCurrency(Math.abs(soldeCompteCourant.value))}`
  })
  
  // Jauges 2 & 3: Charges et Dépenses (budgets calculés, dépenses à venir)
  const budgetChargesFixes = computed(() => {
    const pourcentage = (user.value?.pourcentageChargesFixes || 50) / 100
    return (user.value?.salaireMensuelNet || 0) * pourcentage
  })

  // Charges fixes for the current month
  const chargesFixesThisMonth = computed(() => {
    if (!chargesFixes.value) return []

    const currentDate = new Date()
    const currentDay = currentDate.getDate()

    return chargesFixes.value.map((charge: any) => {
      const isPaid = currentDay >= charge.jourPrelevement
      return {
        ...charge,
        isPaid
      }
    })
  })

  const chargesFixesPaid = computed(() => {
    return chargesFixesThisMonth.value.filter((c: any) => c.isPaid)
  })

  const chargesFixesUpcoming = computed(() => {
    return chargesFixesThisMonth.value.filter((c: any) => !c.isPaid)
  })

  const totalChargesFixesPaid = computed(() => {
    return chargesFixesPaid.value.reduce((total: number, charge: any) => total + charge.montant, 0)
  })

  const pourcentageChargesFixes = computed(() => {
    const budget = budgetChargesFixes.value
    if (budget === 0) return 0
    return Math.min((totalChargesFixesPaid.value / budget) * 100, 100)
  })

  const descriptionChargesFixes = computed(() => {
    if (chargesFixesThisMonth.value.length === 0) {
      return 'À configurer'
    }
    const paid = chargesFixesPaid.value.length
    const total = chargesFixesThisMonth.value.length
    return `${paid}/${total} payées - ${formatCurrency(totalChargesFixesPaid.value)} / ${formatCurrency(budgetChargesFixes.value)}`
  })

  const budgetDepensesVariables = computed(() => {
    const pourcentage = (user.value?.pourcentageDepensesVariables || 30) / 100
    return (user.value?.salaireMensuelNet || 0) * pourcentage
  })

  // Variable expenses types
  const VARIABLE_EXPENSE_TYPES = [
    'ALIMENTATION', 'RESTAURANT', 'TRANSPORT', 'ESSENCE',
    'SHOPPING', 'LOISIRS', 'SANTE', 'BEAUTE', 'MAISON',
    'EDUCATION', 'VOYAGE'
  ]

  // Dépenses variables for the current month
  const depensesVariablesTransactions = computed(() => {
    if (!transactions.value) return []
    return transactions.value.filter((t: any) =>
      VARIABLE_EXPENSE_TYPES.includes(t.type) && t.montant < 0
    )
  })

  const totalDepensesVariables = computed(() => {
    return Math.abs(depensesVariablesTransactions.value.reduce((total: number, t: any) => total + t.montant, 0))
  })

  const pourcentageDepensesVariables = computed(() => {
    const budget = budgetDepensesVariables.value
    if (budget === 0) return 0
    return Math.min((totalDepensesVariables.value / budget) * 100, 100)
  })

  const descriptionDepensesVariables = computed(() => {
    if (depensesVariablesTransactions.value.length === 0) {
      return 'Aucune dépense'
    }
    return `${depensesVariablesTransactions.value.length} dépense(s) - ${formatCurrency(totalDepensesVariables.value)} / ${formatCurrency(budgetDepensesVariables.value)}`
  })

  // Recent variable expenses (last 5)
  const recentDepensesVariables = computed(() => {
    return depensesVariablesTransactions.value
      .sort((a: any, b: any) => new Date(b.dateTransaction).getTime() - new Date(a.dateTransaction).getTime())
      .slice(0, 5)
  })

  // Budget Gauges for the BudgetGauges component
  const budgetGaugesData = computed((): BudgetGauge[] => {
    const objectifCompteCourant = user.value?.objectifCompteCourant || 2000
    const remainingCompteCourant = objectifCompteCourant - soldeCompteCourant.value

    return [
      {
        label: `💳 Compte Courant`,
        current: soldeCompteCourant.value,
        target: objectifCompteCourant,
        color: '#2196F3',
        remaining: remainingCompteCourant
      },
      {
        label: `🏠 Charges Fixes (${user.value?.pourcentageChargesFixes || 50}%)`,
        current: totalChargesFixesPaid.value,
        target: budgetChargesFixes.value,
        color: '#FF9800',
        remaining: budgetChargesFixes.value - totalChargesFixesPaid.value
      },
      {
        label: `🛍️ Dépenses Variables (${user.value?.pourcentageDepensesVariables || 30}%)`,
        current: totalDepensesVariables.value,
        target: budgetDepensesVariables.value,
        color: '#9C27B0',
        remaining: budgetDepensesVariables.value - totalDepensesVariables.value
      }
    ]
  })

  // Format date helper
  const formatDate = (dateString: string): string => {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit'
    })
  }

  // Jauge 4: Épargne de Sécurité
  const objectifEpargneSecurite = computed(() => {
    // 6 mois de charges fixes
    return budgetChargesFixes.value * 6
  })
  
  const montantEpargneSecurite = computed(() => {
    const epargneSecurite = objectifs.value.find(o => o.type === 'SECURITE')
    if (!epargneSecurite) {
      logger.warn('⚠️ Pas d\'objectif de sécurité trouvé')
      return 0
    }
    const montant = getMontantActuel(epargneSecurite)
    logger.log('💰 Montant épargne sécurité:', montant, 'Cible:', objectifEpargneSecurite.value)
    return montant
  })
  
  const pourcentageEpargneSecurite = computed(() => {
    if (objectifEpargneSecurite.value === 0) return 0
    const pct = (montantEpargneSecurite.value / objectifEpargneSecurite.value) * 100
    logger.log('📊 Pourcentage épargne sécurité:', pct.toFixed(2) + '%')
    return pct
  })

  // Transactions summary
  const totalRevenus = computed(() => {
    return transactions.value
      .filter((t: any) => t.montant > 0)
      .reduce((sum: number, t: any) => sum + t.montant, 0)
  })

  const totalDepenses = computed(() => {
    return transactions.value
      .filter((t: any) => t.montant < 0)
      .reduce((sum: number, t: any) => sum + t.montant, 0)
  })

  // Delete month handlers
  const handleDeleteMonth = async (monthValue: string) => {
    try {
      isDeletingMonth.value = true

      // Call API to delete the month
      await apiService.deleteValidatedMonth(monthValue)

      // Close modal
      showDeleteMonthModal.value = false

      // Show success notification
      logger.info('Month deleted successfully:', monthValue)
      alert('Le mois a été supprimé avec succès')

      // Navigate to previous month
      changeMonth('prev')

      // Reload dashboard
      await refreshDashboard()
    } catch (error: any) {
      logger.error('Failed to delete month:', error)
      alert(error.response?.data?.message || 'Erreur lors de la suppression du mois')
    } finally {
      isDeletingMonth.value = false
    }
  }

  const handleCancelDeleteMonth = () => {
    showDeleteMonthModal.value = false
  }

  onMounted(async () => {
    await loadAvailableMonths()

    // Process due charges fixes before loading dashboard
    try {
      const result = await apiService.processChargesDues()
      if (result.processed > 0) {
        logger.info(`✅ ${result.processed} charge(s) fixe(s) traitée(s) automatiquement`)
      }
    } catch (error) {
      logger.warn('Could not process charges fixes:', error)
    }

    await loadDashboard()
  })
  </script>
  
  <style scoped>
  .container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 1.5rem;
  }

  .dashboard-main {
    padding: 2rem 0;
  }
  
  .glass-card {
    background: var(--glass-bg);
    backdrop-filter: blur(20px);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-2xl);
    padding: var(--spacing-xl);
    box-shadow: var(--glass-shadow);
  }
  
  .welcome-card {
    margin-bottom: 2rem;
  }

  .welcome-content {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 3rem;
    align-items: center;
  }

  .welcome-left {
    flex: 1;
  }

  .welcome-content h2 {
    color: white;
    margin: 0 0 1.5rem 0;
    font-size: 1.8rem;
  }

  .welcome-right {
    min-width: 280px;
  }

  .welcome-right h4 {
    color: white;
    margin: 0 0 1rem 0;
    font-size: 1rem;
    font-weight: 600;
    text-align: center;
  }

  .chart-wrapper-compact {
    width: 280px;
    height: 240px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .user-stats {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1.5rem;
  }
  
  .stat {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .stat-label {
    color: rgba(255, 255, 255, 0.7);
    font-size: 0.9rem;
  }
  
  .stat-value {
    color: white;
    font-size: 1.5rem;
    font-weight: bold;
  }
  
  .stat-value.success {
    color: #4CAF50;
  }

  /* Upcoming Charges List */
  .upcoming-charges {
    margin-bottom: 2rem;
  }

  .upcoming-charges h3 {
    color: white;
    margin: 0 0 1rem 0;
    font-size: 1.2rem;
  }

  .charges-list {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  .charge-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s;
  }

  .charge-item:hover {
    background: rgba(255, 255, 255, 0.1);
    transform: translateX(4px);
  }

  .charge-info {
    display: flex;
    gap: 0.25rem;
  }

  .charge-name {
    color: white;
    font-weight: 500;
    font-size: 1rem;
  }

  .charge-date {
    color: rgba(255, 255, 255, 0.6);
    font-size: 0.85rem;
  }

  .charge-amount {
    color: #FF9800;
    font-weight: 600;
    font-size: 1.1rem;
  }

  .quick-actions {
    margin-bottom: 2rem;
  }
  
  .quick-actions h3 {
    color: white;
    margin: 0 0 1rem 0;
  }
  
  .actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }
  
  .action-btn {
    background: rgba(255, 255, 255, 0.15);
    border: 2px solid rgba(255, 255, 255, 0.25);
    color: white;
    padding: 1rem;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    font-size: 1rem;
    font-weight: 500;
  }
  
  .action-btn:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }
  
  .action-btn.primary {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border-color: rgba(255, 255, 255, 0.3);
    box-shadow: var(--shadow-md);
  }

  .action-btn.primary:hover {
    transform: translateY(-2px) scale(1.02);
    box-shadow: var(--shadow-lg);
  }
  
  .action-icon {
    font-size: 1.5rem;
  }
  
  .epargne-section {
    margin-top: 2rem;
  }
  
  .section-title {
    color: white;
    font-size: 1.8rem;
    margin-bottom: 1.5rem;
  }
  
  .comptes-epargne {
    margin-bottom: 2rem;
  }
  
  .comptes-epargne h4,
  .objectifs-epargne h4 {
    color: white;
    margin: 0 0 1rem 0;
  }
  
  .comptes-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  .compte-row {
    display: flex;
    flex-direction: column;
    padding: 0.75rem 1rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    transition: background 0.3s ease, border-color 0.3s ease;
    border: 2px solid transparent;
  }

  .compte-row:hover {
    background: rgba(255, 255, 255, 0.15);
  }

  .compte-row.clickable {
    cursor: pointer;
  }

  .compte-row.has-libre {
    border-color: #10b981;
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.05) 100%);
  }

  .compte-row.has-libre:hover {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(16, 185, 129, 0.08) 100%);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
  }

  .compte-row.has-allocations {
    border-color: rgba(102, 126, 234, 0.5);
  }

  .compte-row.expanded {
    background: rgba(102, 126, 234, 0.1);
    border-color: rgba(102, 126, 234, 0.7);
  }

  /* Main row: horizontal layout */
  .compte-main-row {
    display: flex;
    align-items: center;
    gap: 1rem;
    width: 100%;
  }

  .compte-icon {
    font-size: 1.8rem;
    flex-shrink: 0;
  }

  .compte-info {
    display: flex;
    flex-direction: column;
    gap: 0.15rem;
    flex: 1;
    min-width: 0;
  }

  .compte-name {
    color: white;
    font-weight: 500;
    font-size: 0.95rem;
  }

  .compte-bank {
    color: rgba(255, 255, 255, 0.5);
    font-size: 0.8rem;
  }

  .allocation-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    padding: 0.2rem 0.5rem;
    background: rgba(102, 126, 234, 0.3);
    border-radius: 10px;
    font-size: 0.75rem;
    color: rgba(255, 255, 255, 0.9);
    flex-shrink: 0;
  }

  .compte-amounts {
    display: flex;
    gap: 1.5rem;
    align-items: center;
    margin-left: auto;
  }

  .amount-group {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 0.15rem;
  }

  .amount-label {
    color: rgba(255, 255, 255, 0.5);
    font-size: 0.7rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .amount-value {
    color: white;
    font-size: 1.1rem;
    font-weight: 600;
  }

  .amount-value.success {
    color: #10b981;
    text-shadow: 0 2px 4px rgba(16, 185, 129, 0.3);
  }

  .amount-value.muted {
    color: rgba(255, 255, 255, 0.4);
    text-shadow: none;
  }

  /* Allocations expandables */
  .compte-allocations {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    animation: slideDown 0.3s ease;
  }

  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .allocations-header {
    margin-bottom: 0.75rem;
  }

  .allocations-title {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.9rem;
    font-weight: 500;
  }

  .allocations-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .allocation-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    transition: all 0.2s;
  }

  .allocation-item:hover {
    background: rgba(255, 255, 255, 0.1);
  }

  .allocation-objectif {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .allocation-objectif .objectif-icon {
    font-size: 1.2rem;
  }

  .allocation-objectif .objectif-nom {
    color: rgba(255, 255, 255, 0.9);
    font-weight: 500;
  }

  .allocation-montant {
    color: #667eea;
    font-weight: 600;
    font-size: 1.1rem;
  }
  
  .objectifs-list {
    display: grid;
    gap: 1.5rem;
  }
  
  .objectif-card {
    padding: 1.5rem;
    border: 2px solid rgba(255, 255, 255, 0.2);
  }
  
  .objectif-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1rem;
  }
  
  .objectif-title {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .objectif-icon {
    font-size: 2rem;
  }
  
  .objectif-title h5 {
    color: white;
    margin: 0 0 0.25rem 0;
  }
  
  .objectif-priority {
    font-size: 0.8rem;
    padding: 0.25rem 0.75rem;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.1);
    color: rgba(255, 255, 255, 0.8);
  }
  
  .objectif-priority.priority-1 {
    background: rgba(244, 67, 54, 0.2);
    color: #FF5252;
  }
  
  .objectif-priority.priority-2 {
    background: rgba(255, 152, 0, 0.2);
    color: #FFB74D;
  }
  
  .objectif-amount {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
  }
  
  .objectif-amount .current {
    color: white;
    font-size: 1.5rem;
    font-weight: bold;
  }
  
  .objectif-amount .separator {
    color: rgba(255, 255, 255, 0.5);
  }
  
  .objectif-amount .target {
    color: rgba(255, 255, 255, 0.7);
  }
  
  .objectif-progress {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1rem;
  }
  
  .progress-bar {
    flex: 1;
    height: 16px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    overflow: hidden;
    position: relative;
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2);
  }

  /* Milestone markers */
  .progress-bar::before,
  .progress-bar::after {
    content: '';
    position: absolute;
    top: 0;
    bottom: 0;
    width: 2px;
    background: rgba(255, 255, 255, 0.2);
    z-index: 1;
  }

  .progress-bar::before {
    left: 25%;
  }

  .progress-bar::after {
    left: 50%;
  }

  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #4CAF50, #66BB6A);
    transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
    border-radius: 8px;
    position: relative;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(76, 175, 80, 0.4);
  }

  /* Animated shine effect */
  .progress-fill::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    animation: shine 2s infinite;
  }

  @keyframes shine {
    0% {
      left: -100%;
    }
    50%, 100% {
      left: 100%;
    }
  }

  /* Pulsing glow for near-completion */
  .objectif-progress:has(.progress-fill[style*="width: 9"]) .progress-fill,
  .objectif-progress:has(.progress-fill[style*="width: 100"]) .progress-fill {
    animation: pulse-glow 2s ease-in-out infinite;
  }

  @keyframes pulse-glow {
    0%, 100% {
      box-shadow: 0 2px 8px rgba(76, 175, 80, 0.4);
    }
    50% {
      box-shadow: 0 2px 16px rgba(76, 175, 80, 0.8), 0 0 20px rgba(76, 175, 80, 0.4);
    }
  }

  .progress-percent {
    color: white;
    font-weight: bold;
    min-width: 55px;
    text-align: right;
    font-size: 1rem;
  }
  
  .repartitions {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .repartitions-header {
    color: rgba(255, 255, 255, 0.7);
    font-size: 0.9rem;
    margin-bottom: 0.75rem;
  }
  
  .repartitions-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .repartition-item {
    display: flex;
    justify-content: space-between;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
  }
  
  .repartition-compte {
    color: rgba(255, 255, 255, 0.8);
    font-size: 0.9rem;
  }
  
  .repartition-montant {
    color: white;
    font-weight: 500;
  }
  
  .empty-state {
    text-align: center;
    padding: 2rem;
    color: rgba(255, 255, 255, 0.7);
  }

  .error-dashboard,
  .empty-dashboard {
    text-align: center;
    padding: 4rem 2rem;
    color: white;
  }
  
  .empty-content {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 16px;
    padding: 3rem;
    max-width: 600px;
    margin: 0 auto;
  }
  
  .empty-icon {
    font-size: 4rem;
    display: block;
    margin-bottom: 1rem;
  }
  
  .empty-dashboard h3 {
    margin: 0 0 1rem 0;
    font-size: 1.8rem;
  }
  
  .empty-dashboard p {
    opacity: 0.8;
  }
  
  .btn {
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    font-size: 1rem;
    transition: all 0.3s;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .btn-primary {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    color: white;
    box-shadow: var(--shadow-md);
  }

  .btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }
  
  .btn-secondary {
    background: rgba(255, 255, 255, 0.2);
    color: white;
  }
  
  .btn-secondary:hover {
    background: rgba(255, 255, 255, 0.3);
  }
  
  @media (max-width: 1024px) {
    .header-content {
      flex-wrap: wrap;
    }

    .dashboard-nav {
      order: 3;
      width: 100%;
      overflow-x: auto;
      padding-bottom: var(--spacing-sm);
    }

    .nav-text {
      display: none;
    }

    .nav-item {
      padding: var(--spacing-sm);
      justify-content: center;
    }

    .nav-icon {
      font-size: 1.4rem;
    }
  }

  @media (max-width: 768px) {
    .logo-text {
      display: none;
    }

    .user-name {
      display: none;
    }

    .welcome-content {
      grid-template-columns: 1fr;
      gap: 2rem;
    }

    .welcome-right {
      min-width: unset;
    }

    .chart-wrapper-compact {
      width: 100%;
      height: 220px;
    }

    .actions-grid {
      grid-template-columns: 1fr;
    }

    .user-stats {
      grid-template-columns: 1fr;
    }

    .compte-soldes {
      flex-direction: column;
      gap: 0.5rem;
      align-items: flex-end;
    }
  }

  /* Empty Dashboard State */
  .empty-dashboard {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-xl);
    width: 100%;
    max-width: 1400px;
    margin: 0 auto;
  }

  /* Call to Action Card */
  .empty-state-cta {
    text-align: center;
    padding: 2rem;
    animation: fadeIn 0.5s ease-in;
  }

  .cta-icon {
    font-size: 3.5rem;
    margin-bottom: 1rem;
    animation: bounce 2s ease-in-out infinite;
  }

  @keyframes bounce {
    0%, 100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-10px);
    }
  }

  .cta-title {
    font-size: 1.75rem;
    font-weight: 700;
    color: white;
    margin-bottom: var(--spacing-md);
  }

  .cta-description {
    font-size: 1.05rem;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: var(--spacing-xl);
    line-height: 1.6;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
  }

  .btn-lg {
    padding: 1rem 2rem;
    font-size: 1.1rem;
    gap: var(--spacing-md);
  }

  .btn-lg span {
    font-size: 1.5rem;
  }

  /* Information Grid */
  .empty-state-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }

  .info-section {
    padding: 1.5rem;
    min-height: 300px;
    display: flex;
    flex-direction: column;
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1.25rem;
    padding-bottom: 0.75rem;
    border-bottom: 2px solid rgba(255, 255, 255, 0.15);
  }

  .section-icon {
    font-size: 1.75rem;
    flex-shrink: 0;
  }

  .section-header h3 {
    flex: 1;
    margin: 0;
    font-size: 1.1rem;
    font-weight: 700;
    color: white;
    text-align: left;
    letter-spacing: 0.3px;
  }

  .section-badge {
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.25), rgba(255, 255, 255, 0.15));
    color: white;
    padding: 0.35rem 0.85rem;
    border-radius: 14px;
    font-size: 0.85rem;
    font-weight: 700;
    flex-shrink: 0;
    border: 1px solid rgba(255, 255, 255, 0.2);
  }

  .section-content {
    color: rgba(255, 255, 255, 0.9);
    flex: 1;
    display: flex;
    flex-direction: column;
  }

  /* Accounts List */
  .accounts-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .account-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.85rem 1rem;
    background: rgba(255, 255, 255, 0.04);
    border-left: 3px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    transition: all 0.2s ease;
  }

  .account-item:hover {
    background: rgba(255, 255, 255, 0.08);
    border-left-color: var(--primary-color);
    transform: translateX(2px);
  }

  .account-info {
    display: flex;
    align-items: center;
    gap: 0.85rem;
    flex: 1;
    min-width: 0;
  }

  .account-icon {
    font-size: 1.75rem;
    flex-shrink: 0;
  }

  .account-details {
    display: flex;
    flex-direction: column;
    gap: 0.3rem;
    min-width: 0;
    text-align: left;
  }

  .account-name {
    font-weight: 600;
    color: white;
    font-size: 0.95rem;
    line-height: 1.2;
  }

  .account-type {
    font-size: 0.75rem;
    color: rgba(255, 255, 255, 0.5);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    font-weight: 500;
  }

  .account-balance {
    font-weight: 700;
    color: white;
    font-size: 1.05rem;
    flex-shrink: 0;
    text-align: right;
  }

  .total-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1rem;
    padding: 0.85rem 1rem;
    border-top: 2px solid rgba(255, 255, 255, 0.15);
    background: rgba(255, 255, 255, 0.03);
    border-radius: 8px;
  }

  .total-label {
    font-weight: 700;
    color: white;
    font-size: 1rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .total-value {
    font-weight: 800;
    color: #4CAF50;
    font-size: 1.25rem;
    text-align: right;
  }

  /* Objectifs List */
  .objectifs-list {
    display: flex;
    flex-direction: column;
    gap: 0.85rem;
  }

  .objectif-item {
    display: flex;
    flex-direction: column;
    gap: 0.65rem;
    padding: 0.85rem 1rem;
    background: rgba(255, 255, 255, 0.04);
    border-left: 3px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    transition: all 0.2s ease;
  }

  .objectif-item:hover {
    background: rgba(255, 255, 255, 0.08);
    border-left-color: var(--secondary-color);
  }

  .objectif-info {
    display: flex;
    align-items: flex-start;
    gap: 0.85rem;
  }

  .objectif-icon {
    font-size: 1.75rem;
    flex-shrink: 0;
    line-height: 1;
  }

  .objectif-details {
    flex: 1;
    min-width: 0;
  }

  .objectif-name {
    display: block;
    font-weight: 600;
    color: white;
    font-size: 0.95rem;
    margin-bottom: 0.65rem;
    line-height: 1.3;
    text-align: left;
  }

  .objectif-progress-bar {
    height: 6px;
    background: rgba(255, 255, 255, 0.12);
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 0.45rem;
  }

  .objectif-progress-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
    border-radius: 3px;
    transition: width 0.5s ease;
  }

  .objectif-amounts {
    display: flex;
    gap: 0.3rem;
    align-items: baseline;
    font-size: 0.85rem;
    text-align: left;
  }

  .objectif-current {
    font-weight: 700;
    color: white;
  }

  .objectif-target {
    color: rgba(255, 255, 255, 0.5);
    font-weight: 500;
  }

  /* Charges Fixes List */
  .charges-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .charge-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.85rem 1rem;
    background: rgba(255, 255, 255, 0.04);
    border-left: 3px solid rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    transition: all 0.2s ease;
  }

  .charge-item:hover {
    background: rgba(255, 255, 255, 0.08);
    border-left-color: var(--accent-color, #FF6B6B);
  }

  .charge-info {
    display: flex;
    align-items: center;
    gap: 0.85rem;
    flex: 1;
    min-width: 0;
  }

  .charge-day {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-width: 55px;
    padding: 0.5rem 0.4rem;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 8px;
    flex-shrink: 0;
    border: 1px solid rgba(255, 255, 255, 0.1);
    gap: 0.15rem;
  }

  .charge-day-number {
    font-weight: 800;
    color: white;
    font-size: 1.1rem;
    line-height: 1;
  }

  .charge-day-label {
    font-weight: 500;
    color: rgba(255, 255, 255, 0.6);
    font-size: 0.65rem;
    text-transform: lowercase;
    line-height: 1;
  }

  .charge-details {
    display: flex;
    flex-direction: column;
    gap: 0.3rem;
    min-width: 0;
    text-align: left;
  }

  .charge-name {
    font-weight: 600;
    color: white;
    font-size: 0.95rem;
    line-height: 1.2;
  }

  .charge-category {
    font-size: 0.75rem;
    color: rgba(255, 255, 255, 0.5);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    font-weight: 500;
  }

  .charge-amount {
    font-weight: 700;
    color: white;
    font-size: 1.05rem;
    flex-shrink: 0;
    text-align: right;
  }

  .more-items {
    text-align: left;
    padding: 0.75rem 1rem;
    color: rgba(255, 255, 255, 0.5);
    font-size: 0.85rem;
    font-style: italic;
    margin-top: 0.25rem;
  }

  /* Empty Message */
  .empty-message {
    text-align: center;
    padding: 2rem 1rem;
  }

  .empty-message p {
    color: rgba(255, 255, 255, 0.5);
    margin-bottom: 1rem;
    font-size: 0.9rem;
  }

  .btn-sm {
    padding: 0.6rem 1.2rem;
    font-size: 0.85rem;
    font-weight: 600;
  }

  /* Responsive */
  @media (max-width: 1024px) {
    .empty-state-grid {
      grid-template-columns: 1fr;
      gap: 1rem;
    }

    .info-section {
      min-height: auto;
    }
  }

  @media (max-width: 768px) {
    .empty-state-cta {
      padding: 1.5rem;
    }

    .cta-icon {
      font-size: 3rem;
    }

    .cta-title {
      font-size: 1.4rem;
    }
  }

  /* Transactions Link Section */
  .transactions-link-section {
    margin-bottom: 2rem;
    padding: 2rem;
    background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(56, 142, 60, 0.1) 100%);
    border: 1px solid rgba(76, 175, 80, 0.2);
  }

  .transactions-cta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.5rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .transactions-header {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .transactions-icon {
    font-size: 3rem;
    filter: drop-shadow(0 2px 8px rgba(76, 175, 80, 0.3));
  }

  .transactions-text h3 {
    margin: 0 0 0.5rem 0;
    font-size: 1.5rem;
    font-weight: 700;
    background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .transactions-text p {
    margin: 0;
    color: rgba(255, 255, 255, 0.7);
    font-size: 0.875rem;
  }

  .transactions-summary {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }

  .transactions-summary .summary-item .summary-value.success {
    color: #4CAF50;
  }

  .transactions-summary .summary-item .summary-value.danger {
    color: #F44336;
  }

  /* Patrimoine Link Section */
  .patrimoine-link-section {
    margin-top: 2rem;
    padding: 2rem;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    border: 1px solid rgba(102, 126, 234, 0.2);
  }

  .patrimoine-cta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1.5rem;
    padding-bottom: 1.5rem;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .patrimoine-header {
    display: flex;
    align-items: center;
    gap: 1rem;
  }

  .patrimoine-icon {
    font-size: 3rem;
    filter: drop-shadow(0 2px 8px rgba(102, 126, 234, 0.3));
  }

  .patrimoine-text h3 {
    margin: 0 0 0.5rem 0;
    font-size: 1.5rem;
    font-weight: 700;
    background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .patrimoine-text p {
    margin: 0;
    color: rgba(255, 255, 255, 0.7);
    font-size: 0.875rem;
  }

  .patrimoine-summary {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
  }

  .summary-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    text-align: center;
  }

  .summary-label {
    font-size: 0.875rem;
    color: rgba(255, 255, 255, 0.6);
    margin-bottom: 0.5rem;
  }

  .summary-value {
    font-size: 1.5rem;
    font-weight: 700;
    color: #E8EAF6;
  }

  @media (max-width: 768px) {
    .transactions-cta {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
    }

    .transactions-summary {
      grid-template-columns: 1fr;
      gap: 1rem;
    }

    .patrimoine-cta {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
    }

    .patrimoine-summary {
      grid-template-columns: 1fr;
      gap: 1rem;
    }
  }
  </style>