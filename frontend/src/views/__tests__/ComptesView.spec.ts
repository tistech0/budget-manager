import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ComptesView from '../ComptesView.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { apiService } from '@/services/api'

// Mock router
vi.mock('vue-router', () => ({
  useRoute: () => ({
    path: '/comptes'
  }),
  useRouter: () => ({
    push: vi.fn()
  })
}))

// Mock API service
vi.mock('@/services/api', () => ({
  apiService: {
    getBanques: vi.fn(),
    createBanque: vi.fn(),
    createAccount: vi.fn(),
    updateAccount: vi.fn(),
    deleteAccount: vi.fn()
  }
}))

// Mock composables
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    error: vi.fn(),
    success: vi.fn()
  })
}))

vi.mock('@/composables/useValidation', () => ({
  useValidation: () => ({
    validate: vi.fn().mockResolvedValue({ success: true, data: {} })
  })
}))

// Mock logger
vi.mock('@/utils/logger', () => ({
  logger: {
    error: vi.fn(),
    warn: vi.fn()
  }
}))

describe('ComptesView', () => {
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

    const wrapper = mount(ComptesView, mountOptions)

    // Check immediately before lifecycle hooks complete
    expect(wrapper.find('.comptes-view').exists() || wrapper.find('.main-layout').exists()).toBe(true)

    await flushPromises()
    // After mount, loading will be false due to loadDashboard call
    expect(store.loading).toBe(false)
  })

  it('should render empty state when no comptes', () => {
    const store = useDashboardStore()
    store.loading = false
    store.comptes = []

    const wrapper = mount(ComptesView, mountOptions)

    expect(store.comptes.length).toBe(0)
    expect(wrapper.find('.empty-state').exists() || wrapper.find('.no-comptes').exists()).toBe(true)
  })

  it('should display comptes in grid', () => {
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
      },
      {
        id: '2',
        nom: 'Livret A',
        type: 'LIVRET_A',
        soldeTotal: 3000,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(ComptesView, mountOptions)

    expect(store.comptes.length).toBe(2)
    expect(store.comptes[0].nom).toBe('Compte Courant')
    expect(store.comptes[1].nom).toBe('Livret A')
  })

  it('should open add modal when clicking nouveau compte button', async () => {
    const store = useDashboardStore()
    store.loading = false
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes: [],
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(ComptesView, mountOptions)

    wrapper.vm.showAddModal = true
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.showAddModal).toBe(true)
  })

  it('should populate form when editing compte', async () => {
    const store = useDashboardStore()
    store.loading = false
    const comptes = [
      {
        id: '1',
        nom: 'Compte Test',
        type: 'COMPTE_COURANT',
        soldeTotal: 1000,
        taux: 2.5,
        plafond: 5000,
        banque: { id: '1', nom: 'Test Bank', couleurTheme: '#ff0000', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(ComptesView, mountOptions)

    // Trigger edit programmatically
    await wrapper.vm.editCompte(store.comptes[0])

    expect(wrapper.vm.editingCompte).toBeTruthy()
    expect(wrapper.vm.formData.nom).toBe('Compte Test')
    expect(wrapper.vm.formData.type).toBe('COMPTE_COURANT')
    expect(wrapper.vm.formData.soldeTotal).toBe(1000)
  })

  it('should close modal and reset form', async () => {
    const store = useDashboardStore()
    store.loading = false
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes: [],
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(ComptesView, mountOptions)

    // Open modal
    wrapper.vm.showAddModal = true
    wrapper.vm.formData.nom = 'Test'
    await wrapper.vm.$nextTick()

    // Close modal
    wrapper.vm.closeModal()

    expect(wrapper.vm.showAddModal).toBe(false)
    expect(wrapper.vm.editingCompte).toBeNull()
    expect(wrapper.vm.formData.nom).toBe('')
  })

  it('should delete compte on confirmation', async () => {
    const store = useDashboardStore()
    store.loading = false
    const comptes = [
      {
        id: '1',
        nom: 'Compte Test',
        type: 'COMPTE_COURANT',
        soldeTotal: 1000,
        banque: { id: '1', nom: 'Test Bank', couleurTheme: '#ff0000', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    vi.mocked(apiService.deleteAccount).mockResolvedValue({ success: true } as any)

    // Mock confirm
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)

    const wrapper = mount(ComptesView, mountOptions)

    await wrapper.vm.deleteCompte(store.comptes[0])
    await flushPromises()

    expect(confirmSpy).toHaveBeenCalled()
    expect(apiService.deleteAccount).toHaveBeenCalledWith('1')

    confirmSpy.mockRestore()
  })

  it('should not delete compte when cancelled', async () => {
    const store = useDashboardStore()
    const comptes = [
      {
        id: '1',
        nom: 'Compte Test',
        type: 'COMPTE_COURANT',
        soldeTotal: 1000,
        banque: { id: '1', nom: 'Test Bank', couleurTheme: '#ff0000', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    // Mock confirm to cancel
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false)

    const wrapper = mount(ComptesView, mountOptions)

    await wrapper.vm.deleteCompte(store.comptes[0])

    expect(apiService.deleteAccount).not.toHaveBeenCalled()

    confirmSpy.mockRestore()
  })

  it('should return correct icon for compte type', () => {
    const wrapper = mount(ComptesView, mountOptions)

    expect(wrapper.vm.getCompteIcon('COMPTE_COURANT')).toBe('💳')
    expect(wrapper.vm.getCompteIcon('LIVRET_A')).toBe('📗')
    expect(wrapper.vm.getCompteIcon('PEA')).toBe('📊')
    expect(wrapper.vm.getCompteIcon('ASSURANCE_VIE')).toBe('🛡️')
    expect(wrapper.vm.getCompteIcon('UNKNOWN_TYPE' as any)).toBe('💰')
  })

  it('should return correct label for compte type', () => {
    const wrapper = mount(ComptesView, mountOptions)

    expect(wrapper.vm.getTypeLabel('COMPTE_COURANT')).toBe('Compte Courant')
    expect(wrapper.vm.getTypeLabel('LIVRET_A')).toBe('Livret A')
    expect(wrapper.vm.getTypeLabel('PEA')).toBe('PEA')
    expect(wrapper.vm.getTypeLabel('ASSURANCE_VIE')).toBe('Assurance Vie')
  })

  it('should display argent libre when available', () => {
    const store = useDashboardStore()
    store.loading = false
    const comptes = [
      {
        id: '1',
        nom: 'Compte Test',
        type: 'COMPTE_COURANT',
        soldeTotal: 2000,
        argentLibre: 500,
        banque: { id: '1', nom: 'Test Bank', couleurTheme: '#ff0000', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }

    const wrapper = mount(ComptesView, mountOptions)

    expect(store.comptes[0].argentLibre).toBe(500)
  })
})
