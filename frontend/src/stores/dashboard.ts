import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiService } from '@/services/api'
import type { DashboardData, User, Compte, Objectif, ChargeFixe, Transaction } from '@/types'
import { logger } from '@/utils/logger'

export const useDashboardStore = defineStore('dashboard', () => {
  // État
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)
  const dashboardData = ref<DashboardData | null>(null)
  const currentMonth = ref<string>(new Date().toISOString().substring(0, 7)) // YYYY-MM
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

  // Actions
  const loadDashboard = async (): Promise<void> => {
    try {
      loading.value = true
      error.value = null
      const [dashboard, charges, transactionsData] = await Promise.all([
        apiService.getDashboard(currentMonth.value),
        apiService.getChargesFixes(),
        apiService.getTransactions({ month: currentMonth.value })
      ])
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
    const date = new Date(currentMonth.value + '-01')
    if (direction === 'prev') {
      date.setMonth(date.getMonth() - 1)
    } else {
      date.setMonth(date.getMonth() + 1)
    }
    currentMonth.value = date.toISOString().substring(0, 7)
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
        // If no transactions, include current month
        const currentMonth = new Date().toISOString().substring(0, 7)
        availableMonths.value = [currentMonth]
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
    currentMonth,
    availableMonths,

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
