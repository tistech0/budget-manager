import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { nextTick } from 'vue'
import { useDashboardStore } from '../dashboard'
import { apiService } from '@/services/api'
import {
  createMockDashboardData,
  createMockChargeFixe,
  createMockTransaction,
  flushPromises
} from '@/__tests__/utils/test-helpers'

// Mock the API service
vi.mock('@/services/api', () => ({
  apiService: {
    getDashboard: vi.fn(),
    getChargesFixes: vi.fn(),
    getTransactions: vi.fn()
  }
}))

// Mock the logger
vi.mock('@/utils/logger', () => ({
  logger: {
    info: vi.fn(),
    error: vi.fn(),
    warn: vi.fn()
  }
}))

describe('Dashboard Store', () => {
  beforeEach(() => {
    // Create a fresh pinia instance before each test
    setActivePinia(createPinia())
    // Clear all mocks
    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const store = useDashboardStore()

      expect(store.loading).toBe(false)
      expect(store.error).toBe(null)
      expect(store.dashboardData).toBe(null)
      expect(store.availableMonths).toEqual([])
      expect(store.currentMonth).toMatch(/^\d{4}-\d{2}$/) // YYYY-MM format
    })

    it('should initialize currentMonth to current month', () => {
      const store = useDashboardStore()
      const now = new Date()
      const expectedMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

      expect(store.currentMonth).toBe(expectedMonth)
    })
  })

  describe('Computed Properties', () => {
    it('should return null/empty values when dashboardData is null', () => {
      const store = useDashboardStore()

      expect(store.user).toBe(null)
      expect(store.comptes).toEqual([])
      expect(store.objectifs).toEqual([])
      expect(store.chargesFixes).toEqual([])
      expect(store.transactions).toEqual([])
    })

    it('should compute correct values from dashboardData', () => {
      const store = useDashboardStore()
      const mockData = createMockDashboardData()

      store.dashboardData = mockData

      expect(store.user).toEqual(mockData.user)
      expect(store.comptes).toEqual(mockData.comptes)
      expect(store.objectifs).toEqual(mockData.objectifs)
      expect(store.chargesFixes).toEqual(mockData.chargesFixes)
      expect(store.transactions).toEqual(mockData.transactions)
    })

    it('should calculate totalSoldes correctly', () => {
      const store = useDashboardStore()
      const mockData = createMockDashboardData({
        comptes: [
          { id: '1', soldeTotal: 1000 } as any,
          { id: '2', soldeTotal: 2500 } as any,
          { id: '3', soldeTotal: 500 } as any
        ]
      })

      store.dashboardData = mockData

      expect(store.totalSoldes).toBe(4000)
    })

    it('should calculate totalObjectifsActuel correctly', () => {
      const store = useDashboardStore()
      const mockData = createMockDashboardData({
        objectifs: [
          { id: '1', montantActuel: 500 } as any,
          { id: '2', montantActuel: 1500 } as any,
          { id: '3', montantActuel: 1000 } as any
        ]
      })

      store.dashboardData = mockData

      expect(store.totalObjectifsActuel).toBe(3000)
    })

    it('should calculate totalObjectifsCible correctly', () => {
      const store = useDashboardStore()
      const mockData = createMockDashboardData({
        objectifs: [
          { id: '1', montantCible: 5000 } as any,
          { id: '2', montantCible: 10000 } as any,
          { id: '3', montantCible: 2000 } as any
        ]
      })

      store.dashboardData = mockData

      expect(store.totalObjectifsCible).toBe(17000)
    })

    it('should format currentMonthLabel correctly in French', () => {
      const store = useDashboardStore()
      store.currentMonth = '2025-01'

      expect(store.currentMonthLabel).toBe('janvier 2025')
    })

    it('should return true for hasTransactions when availableMonths has items', () => {
      const store = useDashboardStore()
      store.availableMonths = ['2025-01', '2024-12']

      expect(store.hasTransactions).toBe(true)
    })

    it('should return false for hasTransactions when availableMonths is empty', () => {
      const store = useDashboardStore()
      store.availableMonths = []

      expect(store.hasTransactions).toBe(false)
    })

    it('should always allow going to previous month', () => {
      const store = useDashboardStore()

      expect(store.canGoToPreviousMonth).toBe(true)
    })

    it('should not allow going to future months', () => {
      const store = useDashboardStore()
      const now = new Date()
      const currentYearMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

      store.currentMonth = currentYearMonth

      expect(store.canGoToNextMonth).toBe(false)
    })

    it('should allow going to next month when viewing past month', () => {
      const store = useDashboardStore()
      store.currentMonth = '2024-06'

      expect(store.canGoToNextMonth).toBe(true)
    })
  })

  describe('loadDashboard', () => {
    it('should load dashboard data successfully', async () => {
      const store = useDashboardStore()
      const mockDashboard = createMockDashboardData()
      const mockCharges = [createMockChargeFixe()]
      const mockTransactions = [createMockTransaction()]

      vi.mocked(apiService.getDashboard).mockResolvedValue(mockDashboard)
      vi.mocked(apiService.getChargesFixes).mockResolvedValue(mockCharges)
      vi.mocked(apiService.getTransactions).mockResolvedValue(mockTransactions)

      await store.loadDashboard()

      expect(apiService.getDashboard).toHaveBeenCalledWith(store.currentMonth)
      expect(apiService.getChargesFixes).toHaveBeenCalled()
      expect(apiService.getTransactions).toHaveBeenCalledWith({ month: store.currentMonth })
      expect(store.dashboardData).toEqual({
        ...mockDashboard,
        chargesFixes: mockCharges,
        transactions: mockTransactions
      })
      expect(store.loading).toBe(false)
      expect(store.error).toBe(null)
    })

    it('should set loading state correctly', async () => {
      const store = useDashboardStore()
      const mockDashboard = createMockDashboardData()

      vi.mocked(apiService.getDashboard).mockResolvedValue(mockDashboard)
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      expect(store.loading).toBe(false)

      const loadPromise = store.loadDashboard()

      // Should be loading during API call
      expect(store.loading).toBe(true)

      await loadPromise

      // Should not be loading after completion
      expect(store.loading).toBe(false)
    })

    it('should handle API errors gracefully', async () => {
      const store = useDashboardStore()
      const errorMessage = 'Network error'

      vi.mocked(apiService.getDashboard).mockRejectedValue(new Error(errorMessage))

      await store.loadDashboard()

      expect(store.error).toBe('Erreur lors du chargement du dashboard')
      expect(store.loading).toBe(false)
    })

    it('should clear previous error on successful load', async () => {
      const store = useDashboardStore()
      const mockDashboard = createMockDashboardData()

      // Set initial error
      store.error = 'Previous error'

      vi.mocked(apiService.getDashboard).mockResolvedValue(mockDashboard)
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      await store.loadDashboard()

      expect(store.error).toBe(null)
    })

    it('should load available months after dashboard load', async () => {
      const store = useDashboardStore()
      const mockTransactions = [
        createMockTransaction({ dateTransaction: '2025-01-15' }),
        createMockTransaction({ dateTransaction: '2025-01-20' }),
        createMockTransaction({ dateTransaction: '2024-12-10' })
      ]

      vi.mocked(apiService.getDashboard).mockResolvedValue(createMockDashboardData())
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue(mockTransactions)

      await store.loadDashboard()

      // Should extract unique months from transactions
      expect(store.availableMonths).toEqual(['2025-01', '2024-12'])
    })
  })

  describe('changeMonth', () => {
    it('should navigate to previous month', async () => {
      const store = useDashboardStore()
      store.currentMonth = '2025-03'

      vi.mocked(apiService.getDashboard).mockResolvedValue(createMockDashboardData())
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      store.changeMonth('prev')
      await flushPromises()

      expect(store.currentMonth).toBe('2025-02')
      expect(apiService.getDashboard).toHaveBeenCalledWith('2025-02')
    })

    it('should navigate to next month', async () => {
      const store = useDashboardStore()
      const initialMonth = '2024-03'
      store.currentMonth = initialMonth

      vi.mocked(apiService.getDashboard).mockResolvedValue(createMockDashboardData())
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      // Manually compute what the next month should be
      const date = new Date(initialMonth + '-01')
      date.setMonth(date.getMonth() + 1)
      const expectedMonth = date.toISOString().substring(0, 7)

      store.changeMonth('next')
      await nextTick()
      await flushPromises()

      // The store should update the month
      expect(store.currentMonth).toBe(expectedMonth)
      expect(apiService.getDashboard).toHaveBeenCalledWith(expectedMonth)
    })

    it('should handle year boundary when going to previous month', async () => {
      const store = useDashboardStore()
      store.currentMonth = '2025-01'

      vi.mocked(apiService.getDashboard).mockResolvedValue(createMockDashboardData())
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      store.changeMonth('prev')
      await flushPromises()

      expect(store.currentMonth).toBe('2024-12')
    })

    it('should handle year boundary when going to next month', async () => {
      const store = useDashboardStore()
      store.currentMonth = '2024-12'

      vi.mocked(apiService.getDashboard).mockResolvedValue(createMockDashboardData())
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      store.changeMonth('next')
      await flushPromises()

      expect(store.currentMonth).toBe('2025-01')
    })
  })

  describe('setMonth', () => {
    it('should set month and reload dashboard', async () => {
      const store = useDashboardStore()
      const targetMonth = '2024-06'

      vi.mocked(apiService.getDashboard).mockResolvedValue(createMockDashboardData())
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      store.setMonth(targetMonth)
      await flushPromises()

      expect(store.currentMonth).toBe(targetMonth)
      expect(apiService.getDashboard).toHaveBeenCalledWith(targetMonth)
    })
  })

  describe('refreshDashboard', () => {
    it('should reload current dashboard data', async () => {
      const store = useDashboardStore()
      store.currentMonth = '2025-02'

      vi.mocked(apiService.getDashboard).mockResolvedValue(createMockDashboardData())
      vi.mocked(apiService.getChargesFixes).mockResolvedValue([])
      vi.mocked(apiService.getTransactions).mockResolvedValue([])

      store.refreshDashboard()
      await flushPromises()

      expect(apiService.getDashboard).toHaveBeenCalledWith('2025-02')
    })
  })

  describe('loadAvailableMonths', () => {
    it('should extract unique months from transactions', async () => {
      const store = useDashboardStore()
      const mockTransactions = [
        createMockTransaction({ dateTransaction: '2025-01-15' }),
        createMockTransaction({ dateTransaction: '2025-01-20' }),
        createMockTransaction({ dateTransaction: '2024-12-10' }),
        createMockTransaction({ dateTransaction: '2024-11-05' }),
        createMockTransaction({ dateTransaction: '2024-12-25' })
      ]

      store.dashboardData = createMockDashboardData({ transactions: mockTransactions })

      await store.loadAvailableMonths()

      expect(store.availableMonths).toEqual(['2025-01', '2024-12', '2024-11'])
    })

    it('should sort months in descending order', async () => {
      const store = useDashboardStore()
      const mockTransactions = [
        createMockTransaction({ dateTransaction: '2024-06-15' }),
        createMockTransaction({ dateTransaction: '2025-03-20' }),
        createMockTransaction({ dateTransaction: '2024-12-10' }),
        createMockTransaction({ dateTransaction: '2025-01-05' })
      ]

      store.dashboardData = createMockDashboardData({ transactions: mockTransactions })

      await store.loadAvailableMonths()

      expect(store.availableMonths).toEqual(['2025-03', '2025-01', '2024-12', '2024-06'])
    })

    it('should include current month when no transactions exist', async () => {
      const store = useDashboardStore()
      const now = new Date()
      const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

      store.dashboardData = createMockDashboardData({ transactions: [] })

      await store.loadAvailableMonths()

      expect(store.availableMonths).toEqual([currentMonth])
    })

    it('should handle null transactions gracefully', async () => {
      const store = useDashboardStore()
      const now = new Date()
      const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

      store.dashboardData = null

      await store.loadAvailableMonths()

      expect(store.availableMonths).toEqual([currentMonth])
    })
  })
})
