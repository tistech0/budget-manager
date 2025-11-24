import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiService } from '@/services/api'
import type { DashboardData, User, Compte, Objectif, ChargeFixe, Transaction, MonthSnapshot } from '@/types'
import { logger } from '@/utils/logger'

export const useDashboardStore = defineStore('dashboard', () => {
  // État
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)
  const dashboardData = ref<DashboardData | null>(null)
  const monthSnapshot = ref<MonthSnapshot | null>(null) // Snapshot for past months
  // Use local time for current month (consistent with canGoToNextMonth)
  const now = new Date()
  const currentMonth = ref<string>(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`) // YYYY-MM
  const availableMonths = ref<string[]>([]) // Months with transactions

  // Computed
  const user = computed<User | null>(() => dashboardData.value?.user || null)
  const comptes = computed<Compte[]>(() => dashboardData.value?.comptes || [])
  const objectifs = computed<Objectif[]>(() => dashboardData.value?.objectifs || [])
  const chargesFixes = computed<ChargeFixe[]>(() => dashboardData.value?.chargesFixes || [])
  const transactions = computed<Transaction[]>(() => dashboardData.value?.transactions || [])
  
  const totalSoldes = computed<number>(() => {
    return comptes.value.reduce((total, compte) => total + compte.soldeTotal, 0)
  })

  const totalObjectifsActuel = computed<number>(() => {
    return objectifs.value.reduce((total, obj) => total + (obj.montantActuel || 0), 0)
  })

  const totalObjectifsCible = computed<number>(() => {
    return objectifs.value.reduce((total, obj) => total + obj.montantCible, 0)
  })

  const currentMonthLabel = computed<string>(() => {
    const date = new Date(currentMonth.value + '-01')
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long'
    })
  })

  const hasTransactions = computed<boolean>(() => {
    return availableMonths.value.length > 0
  })

  const canGoToPreviousMonth = computed<boolean>(() => {
    // Allow navigating to any past month (no limit)
    return true
  })

  const canGoToNextMonth = computed<boolean>(() => {
    // Only allow going up to current month, not future
    const now = new Date()
    const currentYearMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    return currentMonth.value !== currentYearMonth
  })

  // Helper to calculate budget cycle dates based on jourPaie
  const calculateBudgetCycleDates = (jourPaie: number, targetMonth: string): { dateDebut: string; dateFin: string } => {
    const [year, month] = targetMonth.split('-').map(Number)
    const today = new Date()

    // Create date objects for the target month
    const targetDate = new Date(year, month - 1, 15) // Middle of target month

    let cycleStart: Date
    let cycleEnd: Date

    // Budget cycle runs from jourPaie of one month to jourPaie-1 of the next
    if (targetDate.getDate() >= jourPaie || targetDate.getMonth() === today.getMonth()) {
      // Cycle starts this month
      cycleStart = new Date(year, month - 1, jourPaie)
      cycleEnd = new Date(year, month, jourPaie - 1)
    } else {
      // Cycle started last month
      cycleStart = new Date(year, month - 2, jourPaie)
      cycleEnd = new Date(year, month - 1, jourPaie - 1)
    }

    return {
      dateDebut: cycleStart.toISOString().split('T')[0],
      dateFin: cycleEnd.toISOString().split('T')[0]
    }
  }

  // Check if viewing a past month (not current)
  const isViewingPastMonth = computed<boolean>(() => {
    const now = new Date()
    const currentYearMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    return currentMonth.value !== currentYearMonth
  })

  // Actions
  const loadDashboard = async (): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      monthSnapshot.value = null // Reset snapshot

      // First, get dashboard to retrieve user info (including jourPaie)
      const [dashboard, charges] = await Promise.all([
        apiService.getDashboard(currentMonth.value),
        apiService.getChargesFixes()
      ])

      // Calculate budget cycle dates based on user's jourPaie
      const jourPaie = dashboard.user?.jourPaie || 1
      const { dateDebut, dateFin } = calculateBudgetCycleDates(jourPaie, currentMonth.value)

      logger.info(`Loading transactions for budget cycle: ${dateDebut} to ${dateFin}`)

      // Try to fetch snapshot for this month (available for past months)
      const snapshot = await apiService.getMonthSnapshot(currentMonth.value)
      if (snapshot) {
        monthSnapshot.value = snapshot
        logger.info(`Snapshot found for ${currentMonth.value}:`, snapshot)
      }

      // Now fetch transactions for the budget cycle
      const transactionsData = await apiService.getTransactions({ dateDebut, dateFin })

      dashboardData.value = {
        ...dashboard,
        chargesFixes: charges,
        transactions: transactionsData
      }

      // Load available months from transactions
      await loadAvailableMonths()
    } catch (err: any) {
      error.value = 'Erreur lors du chargement du dashboard'
      logger.error('Error loading dashboard:', err)
    } finally {
      loading.value = false
    }
  }

  const changeMonth = (direction: 'prev' | 'next'): void => {
    // Parse current month as year and month numbers to avoid timezone issues
    const [year, month] = currentMonth.value.split('-').map(Number)

    let newYear = year
    let newMonth = month

    if (direction === 'prev') {
      newMonth -= 1
      if (newMonth < 1) {
        newMonth = 12
        newYear -= 1
      }
    } else {
      newMonth += 1
      if (newMonth > 12) {
        newMonth = 1
        newYear += 1
      }
    }

    currentMonth.value = `${newYear}-${String(newMonth).padStart(2, '0')}`
    loadDashboard()
  }

  const setMonth = (month: string): void => {
    currentMonth.value = month
    loadDashboard()
  }

  const refreshDashboard = (): void => {
    loadDashboard()
  }

  const loadAvailableMonths = async (): Promise<void> => {
    try {
      // Extract unique months from transactions
      if (transactions.value && transactions.value.length > 0) {
        const monthsSet = new Set<string>()
        transactions.value.forEach(transaction => {
          // Extract YYYY-MM from transaction date
          const month = transaction.dateTransaction.substring(0, 7)
          monthsSet.add(month)
        })

        // Sort months in descending order (most recent first)
        availableMonths.value = Array.from(monthsSet).sort().reverse()
      } else {
        // If no transactions, include current month (use local time)
        const now = new Date()
        const currentMonthStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
        availableMonths.value = [currentMonthStr]
      }

      logger.info('Available months loaded:', availableMonths.value)
    } catch (err: any) {
      logger.error('Error loading available months:', err)
      availableMonths.value = []
    }
  }

  return {
    // État
    loading,
    error,
    dashboardData,
    monthSnapshot,
    currentMonth,
    availableMonths,
    isViewingPastMonth,

    // Computed
    user,
    comptes,
    objectifs,
    chargesFixes,
    transactions,
    totalSoldes,
    totalObjectifsActuel,
    totalObjectifsCible,
    currentMonthLabel,
    hasTransactions,
    canGoToPreviousMonth,
    canGoToNextMonth,

    // Actions
    loadDashboard,
    changeMonth,
    setMonth,
    refreshDashboard,
    loadAvailableMonths
  }
})
