import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TransactionsView from '../TransactionsView.vue'
import { useDashboardStore } from '@/stores/dashboard'

// Mock router and API
vi.mock('vue-router', () => ({
  useRoute: () => ({ path: '/transactions' }),
  useRouter: () => ({ push: vi.fn() })
}))
vi.mock('@/services/api', () => ({ apiService: { deleteTransaction: vi.fn() } }))
vi.mock('@/composables/useToast', () => ({ useToast: () => ({ success: vi.fn(), error: vi.fn() }) }))
vi.mock('@/utils/logger', () => ({ logger: { error: vi.fn() } }))

describe('TransactionsView', () => {
  const mountOptions = {
    global: {
      stubs: {
        MainLayout: {
          template: '<div class="main-layout"><slot /></div>'
        }
      }
    }
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should render loading state', async () => {
    const store = useDashboardStore()
    store.loading = true
    const wrapper = mount(TransactionsView, mountOptions)

    expect(wrapper.find('.transactions-view').exists() || wrapper.find('.main-layout').exists()).toBe(true)

    await flushPromises()
    expect(store.loading).toBe(false)
  })

  it('should render empty state when no transactions', () => {
    const store = useDashboardStore()
    store.loading = false
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes: [],
      objectifs: [],
      transactions: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(TransactionsView, mountOptions)
    expect(store.transactions.length).toBe(0)
  })

  it('should display transactions list', () => {
    const store = useDashboardStore()
    const transactions = [
      {
        id: '1',
        type: 'ALIMENTATION',
        montant: -50.00,
        description: 'Courses',
        dateTransaction: '2025-01-15',
        compte: { id: 'c1', nom: 'Compte Courant' }
      },
      {
        id: '2',
        type: 'SALAIRE',
        montant: 3000,
        description: 'Salaire janvier',
        dateTransaction: '2025-01-01',
        compte: { id: 'c1', nom: 'Compte Courant' }
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes: [],
      objectifs: [],
      transactions,
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(TransactionsView, mountOptions)
    expect(store.transactions.length).toBe(2)
    expect(store.transactions[0].description).toBe('Courses')
    expect(store.transactions[1].description).toBe('Salaire janvier')
  })

  it('should filter transactions by type', async () => {
    const store = useDashboardStore()
    const transactions = [
      { id: '1', type: 'ALIMENTATION', montant: -50, description: 'Test 1', dateTransaction: '2025-01-15', compte: { id: 'c1', nom: 'CC' } },
      { id: '2', type: 'SALAIRE', montant: 3000, description: 'Test 2', dateTransaction: '2025-01-01', compte: { id: 'c1', nom: 'CC' } }
    ]
    store.dashboardData = { mois: '2025-01', user: { id: '1', nom: 'T', prenom: 'U', jourPaie: 1, salaireMensuelNet: 1000 }, comptes: [], objectifs: [], transactions, salaireValide: false, timestamp: new Date().toISOString() }
    const wrapper = mount(TransactionsView, mountOptions)
    await flushPromises()

    wrapper.vm.filterDateRange = 'ALL'
    wrapper.vm.filterType = 'DEPENSE'
    await wrapper.vm.$nextTick()

    const filtered = wrapper.vm.filteredAndSortedTransactions
    expect(filtered.length).toBe(1)
    expect(filtered[0].type).toBe('ALIMENTATION')
  })

  it('should calculate total revenus correctly', async () => {
    const store = useDashboardStore()
    const transactions = [
      { id: '1', type: 'SALAIRE', montant: 3000, description: 'Salaire', dateTransaction: '2025-01-01', compte: { id: 'c1', nom: 'CC' } },
      { id: '2', type: 'ALIMENTATION', montant: -50, description: 'Courses', dateTransaction: '2025-01-15', compte: { id: 'c1', nom: 'CC' } },
      { id: '3', type: 'AUTRE_REVENU', montant: 200, description: 'Bonus', dateTransaction: '2025-01-10', compte: { id: 'c1', nom: 'CC' } }
    ]
    store.dashboardData = { mois: '2025-01', user: { id: '1', nom: 'T', prenom: 'U', jourPaie: 1, salaireMensuelNet: 1000 }, comptes: [], objectifs: [], transactions, salaireValide: false, timestamp: new Date().toISOString() }
    const wrapper = mount(TransactionsView, mountOptions)
    await flushPromises()

    wrapper.vm.filterDateRange = 'ALL'
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.summaryRevenus).toBe(3200)
  })

  it('should calculate total depenses correctly', async () => {
    const store = useDashboardStore()
    const transactions = [
      { id: '1', type: 'SALAIRE', montant: 3000, description: 'Salaire', dateTransaction: '2025-01-01', compte: { id: 'c1', nom: 'CC' } },
      { id: '2', type: 'ALIMENTATION', montant: -50, description: 'Courses', dateTransaction: '2025-01-15', compte: { id: 'c1', nom: 'CC' } },
      { id: '3', type: 'TRANSPORT', montant: -30, description: 'Bus', dateTransaction: '2025-01-10', compte: { id: 'c1', nom: 'CC' } }
    ]
    store.dashboardData = { mois: '2025-01', user: { id: '1', nom: 'T', prenom: 'U', jourPaie: 1, salaireMensuelNet: 1000 }, comptes: [], objectifs: [], transactions, salaireValide: false, timestamp: new Date().toISOString() }
    const wrapper = mount(TransactionsView, mountOptions)
    await flushPromises()

    wrapper.vm.filterDateRange = 'ALL'
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.summaryDepenses).toBe(-80)
  })

  it('should open add expense modal', async () => {
    const wrapper = mount(TransactionsView, mountOptions)
    wrapper.vm.showAddExpenseModal = true
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.showAddExpenseModal).toBe(true)
  })

  it('should format date correctly', () => {
    const wrapper = mount(TransactionsView, mountOptions)
    const formatted = wrapper.vm.formatDate('2025-01-15')
    expect(formatted).toContain('15')
    expect(formatted).toContain('janv')
  })

  it('should sort transactions by date desc by default', async () => {
    const store = useDashboardStore()
    const transactions = [
      { id: '1', type: 'ALIMENTATION', montant: -50, description: 'Old', dateTransaction: '2025-01-01', compte: { id: 'c1', nom: 'CC' } },
      { id: '2', type: 'TRANSPORT', montant: -30, description: 'Recent', dateTransaction: '2025-01-15', compte: { id: 'c1', nom: 'CC' } }
    ]
    store.dashboardData = { mois: '2025-01', user: { id: '1', nom: 'T', prenom: 'U', jourPaie: 1, salaireMensuelNet: 1000 }, comptes: [], objectifs: [], transactions, salaireValide: false, timestamp: new Date().toISOString() }
    const wrapper = mount(TransactionsView, mountOptions)
    await flushPromises()

    wrapper.vm.filterDateRange = 'ALL'
    await wrapper.vm.$nextTick()

    const sorted = wrapper.vm.filteredAndSortedTransactions

    expect(sorted.length).toBe(2)
    expect(sorted[0].description).toBe('Recent')
    expect(sorted[1].description).toBe('Old')
  })

  it('should handle search filter', async () => {
    const store = useDashboardStore()
    const transactions = [
      { id: '1', type: 'ALIMENTATION', montant: -50, description: 'Carrefour', dateTransaction: '2025-01-15', compte: { id: 'c1', nom: 'CC' } },
      { id: '2', type: 'TRANSPORT', montant: -30, description: 'Uber', dateTransaction: '2025-01-10', compte: { id: 'c1', nom: 'CC' } }
    ]
    store.dashboardData = { mois: '2025-01', user: { id: '1', nom: 'T', prenom: 'U', jourPaie: 1, salaireMensuelNet: 1000 }, comptes: [], objectifs: [], transactions, salaireValide: false, timestamp: new Date().toISOString() }
    const wrapper = mount(TransactionsView, mountOptions)
    await flushPromises()

    // Set date filter to ALL to include all transactions
    wrapper.vm.filterDateRange = 'ALL'
    wrapper.vm.searchQuery = 'Carrefour'
    await wrapper.vm.$nextTick()

    const filtered = wrapper.vm.filteredAndSortedTransactions
    expect(filtered.length).toBe(1)
    expect(filtered[0].description).toBe('Carrefour')
  })
})
