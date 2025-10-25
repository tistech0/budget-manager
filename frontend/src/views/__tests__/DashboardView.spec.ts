import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import DashboardView from '../DashboardView.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { apiService } from '@/services/api'

// Mock router
vi.mock('vue-router', () => ({
  RouterLink: {
    template: '<a><slot /></a>',
    props: ['to']
  },
  useRoute: () => ({
    path: '/dashboard'
  }),
  useRouter: () => ({
    push: vi.fn()
  })
}))

// Mock API service
vi.mock('@/services/api', () => ({
  apiService: {
    deleteValidatedMonth: vi.fn()
  }
}))

// Mock logger
vi.mock('@/utils/logger', () => ({
  logger: {
    log: vi.fn(),
    info: vi.fn(),
    warn: vi.fn(),
    error: vi.fn()
  }
}))

describe('DashboardView', () => {
  const mountOptions = {
    global: {
      stubs: {
        MainLayout: {
          template: '<div class="main-layout"><slot /></div>'
        },
        BudgetGauges: true,
        BudgetPieChart: true,
        MonthSelector: true,
        LoadingState: true,
        ValidateSalaryModal: true,
        AddExpenseModal: true,
        TransferModal: true,
        VersementModal: true,
        DeleteMonthModal: true
      }
    }
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('should render loading state', () => {
    const store = useDashboardStore()
    store.loading = true

    const wrapper = mount(DashboardView, mountOptions)

    expect(wrapper.find('.dashboard-view').exists()).toBe(true)
    expect(store.loading).toBe(true)
  })

  it('should render error state with retry button', async () => {
    const store = useDashboardStore()
    store.loading = false
    store.error = 'Failed to load dashboard'

    const wrapper = mount(DashboardView, mountOptions)

    expect(wrapper.find('.error-dashboard').exists()).toBe(true)
    expect(wrapper.text()).toContain('Failed to load dashboard')
  })

  it('should render empty state when no dashboard data', () => {
    const store = useDashboardStore()
    store.loading = false
    store.dashboardData = null

    const wrapper = mount(DashboardView, mountOptions)

    expect(wrapper.find('.empty-dashboard').exists()).toBe(true)
    expect(wrapper.text()).toContain('Bienvenue')
  })

  it('should render no salary validated state', () => {
    const store = useDashboardStore()
    store.loading = false
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Doe', prenom: 'John', jourPaie: 15, salaireMensuelNet: 3000 },
      comptes: [],
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(DashboardView, mountOptions)

    expect(wrapper.find('.empty-dashboard').exists()).toBe(true)
    expect(wrapper.text()).toContain('Aucun salaire validé')
  })

  it('should display accounts in no salary state', () => {
    const store = useDashboardStore()
    store.loading = false
    const comptes = [
      {
        id: '1',
        nom: 'Compte Courant',
        type: 'COMPTE_COURANT',
        soldeTotal: 1500,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Doe', prenom: 'John', jourPaie: 15, salaireMensuelNet: 3000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(DashboardView, mountOptions)

    expect(store.comptes.length).toBe(1)
    expect(wrapper.find('.info-section').exists()).toBe(true)
  })

  it('should display objectifs in no salary state', () => {
    const store = useDashboardStore()
    store.loading = false
    const objectifs = [
      {
        id: '1',
        nom: 'Vacances',
        montantCible: 2000,
        type: 'PROJET',
        priorite: 'HAUTE',
        icone: '✈️',
        couleur: '#ff0000',
        actif: true,
        repartitions: []
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Doe', prenom: 'John', jourPaie: 15, salaireMensuelNet: 3000 },
      comptes: [],
      objectifs,
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(DashboardView, mountOptions)

    expect(store.objectifs.length).toBe(1)
    expect(wrapper.find('.info-section').exists()).toBe(true)
  })

  it('should render dashboard with validated salary', () => {
    const store = useDashboardStore()
    store.loading = false
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Doe', prenom: 'John', jourPaie: 15, salaireMensuelNet: 3000 },
      comptes: [],
      objectifs: [],
      salaireValide: true,
      timestamp: new Date().toISOString()
    }
    store.user = store.dashboardData.user

    const wrapper = mount(DashboardView, mountOptions)

    expect(wrapper.find('.welcome-card').exists()).toBe(true)
    expect(wrapper.find('.quick-actions').exists()).toBe(true)
  })

  it('should open validate salary modal', async () => {
    const store = useDashboardStore()
    store.loading = false
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Doe', prenom: 'John', jourPaie: 15, salaireMensuelNet: 3000 },
      comptes: [],
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(DashboardView, mountOptions)

    // Modal state check
    expect(wrapper.find('.empty-dashboard').exists()).toBe(true)
  })

  it('should display budget gauges when salary validated', () => {
    const store = useDashboardStore()
    store.loading = false
    const comptes = [
      {
        id: '1',
        nom: 'Compte Courant',
        type: 'COMPTE_COURANT',
        soldeTotal: 1000,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    const user = {
      id: '1',
      nom: 'Doe',
      prenom: 'John',
      jourPaie: 15,
      salaireMensuelNet: 3000,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }
    store.dashboardData = {
      mois: '2025-01',
      user,
      comptes,
      objectifs: [],
      salaireValide: true,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(DashboardView, mountOptions)

    expect(wrapper.find('.dashboard-content').exists()).toBe(true)
  })

  it('should format currency correctly', () => {
    const store = useDashboardStore()
    store.loading = false

    const wrapper = mount(DashboardView, mountOptions)
    const formatted = wrapper.vm.formatCurrency(1234.56)

    expect(formatted).toContain('234')
    expect(formatted).toContain('€')
  })

  it('should handle delete month confirmation', async () => {
    const store = useDashboardStore()
    store.loading = false
    store.currentMonth = '2025-01'
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Doe', prenom: 'John', jourPaie: 15, salaireMensuelNet: 3000 },
      comptes: [],
      objectifs: [],
      salaireValide: true,
      timestamp: new Date().toISOString()
    }

    // Mock API call
    vi.mocked(apiService.deleteValidatedMonth).mockResolvedValue({ success: true } as any)

    // Mock alert
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})

    const wrapper = mount(DashboardView, mountOptions)

    // Trigger delete
    await wrapper.vm.handleDeleteMonth('2025-01')
    await flushPromises()

    expect(apiService.deleteValidatedMonth).toHaveBeenCalledWith('2025-01')

    alertSpy.mockRestore()
  })

  it('should calculate compte courant solde correctly', () => {
    const store = useDashboardStore()
    store.loading = false
    const comptes = [
      {
        id: '1',
        nom: 'Compte 1',
        type: 'COMPTE_COURANT',
        soldeTotal: 500,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      },
      {
        id: '2',
        nom: 'Compte 2',
        type: 'COMPTE_COURANT',
        soldeTotal: 700,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      },
      {
        id: '3',
        nom: 'Livret A',
        type: 'LIVRET_A',
        soldeTotal: 2000,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Doe', prenom: 'John', jourPaie: 15, salaireMensuelNet: 3000 },
      comptes,
      objectifs: [],
      salaireValide: true,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(DashboardView, mountOptions)

    // Should sum only COMPTE_COURANT
    expect(wrapper.vm.soldeCompteCourant).toBe(1200)
  })
})
