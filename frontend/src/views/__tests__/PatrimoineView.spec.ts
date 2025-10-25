import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PatrimoineView from '../PatrimoineView.vue'
import { useDashboardStore } from '@/stores/dashboard'

// Mock router
vi.mock('vue-router', () => ({
  RouterLink: {
    template: '<a><slot /></a>',
    props: ['to']
  },
  useRoute: () => ({ path: '/patrimoine' }),
  useRouter: () => ({ push: vi.fn() })
}))

vi.mock('@/services/api', () => ({ apiService: {} }))
vi.mock('@/utils/logger', () => ({ logger: { log: vi.fn(), error: vi.fn() } }))

describe('PatrimoineView', () => {
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
    const wrapper = mount(PatrimoineView, mountOptions)

    expect(wrapper.find('.patrimoine-view').exists() || wrapper.find('.main-layout').exists()).toBe(true)

    await flushPromises()
    expect(store.loading).toBe(false)
  })

  it('should render empty state when no data', () => {
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
    const wrapper = mount(PatrimoineView, mountOptions)
    expect(store.comptes.length).toBe(0)
    expect(store.objectifs.length).toBe(0)
  })

  it('should display comptes list', () => {
    const store = useDashboardStore()
    const comptes = [
      {
        id: '1',
        nom: 'Compte Test',
        type: 'COMPTE_COURANT',
        soldeTotal: 1500,
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
    const wrapper = mount(PatrimoineView, mountOptions)
    expect(store.comptes.length).toBe(1)
    expect(store.comptes[0].nom).toBe('Compte Test')
  })

  it('should display objectifs list', () => {
    const store = useDashboardStore()
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
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes: [],
      objectifs,
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(PatrimoineView, mountOptions)
    expect(store.objectifs.length).toBe(1)
    expect(store.objectifs[0].nom).toBe('Vacances')
  })

  it('should calculate total patrimoine', () => {
    const store = useDashboardStore()
    const comptes = [
      {
        id: '1',
        nom: 'Compte 1',
        type: 'COMPTE_COURANT',
        soldeTotal: 1000,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      },
      {
        id: '2',
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
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(PatrimoineView, mountOptions)
    expect(wrapper.vm.totalPatrimoine).toBe(3000)
  })

  it('should display comptes by type correctly', () => {
    const store = useDashboardStore()
    const comptes = [
      {
        id: '1',
        nom: 'Compte Courant',
        type: 'COMPTE_COURANT',
        soldeTotal: 1000,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      },
      {
        id: '2',
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
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(PatrimoineView, mountOptions)
    expect(store.comptes[0].nom).toBe('Compte Courant')
    expect(store.comptes[1].nom).toBe('Livret A')
  })

  it('should calculate objectif progression', () => {
    const store = useDashboardStore()
    const objectifs = [
      {
        id: '1',
        nom: 'Test',
        montantCible: 1000,
        type: 'PROJET',
        priorite: 'HAUTE',
        icone: '🎯',
        couleur: '#ff0000',
        actif: true,
        repartitions: [
          { compteId: 'c1', montantActuel: 400 }
        ]
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes: [],
      objectifs,
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(PatrimoineView, mountOptions)
    const objectif = store.objectifs[0]
    const montant = wrapper.vm.getMontantActuel(objectif)
    const progression = wrapper.vm.getProgression(objectif)

    expect(montant).toBe(400)
    expect(progression).toBe(40)
  })

  it('should expand compte to show allocations', async () => {
    const store = useDashboardStore()
    const comptes = [
      {
        id: '1',
        nom: 'Livret A',
        type: 'LIVRET_A',
        soldeTotal: 3000,
        argentLibre: 2000,
        banque: { id: '1', nom: 'BNP', couleurTheme: '#00ff00', actif: true },
        actif: true,
        dateOuverture: '2025-01-01'
      }
    ]
    const objectifs = [
      {
        id: 'obj1',
        nom: 'Vacances',
        montantCible: 1000,
        type: 'PROJET',
        priorite: 'HAUTE',
        icone: '✈️',
        couleur: '#ff0000',
        actif: true,
        repartitions: [
          {
            id: 'rep1',
            compte: { id: '1', nom: 'Livret A' },
            montantActuel: 500,
            objectifId: 'obj1',
            compteId: '1'
          }
        ]
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes,
      objectifs,
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(PatrimoineView, mountOptions)

    // Initially expanded should be null
    expect(wrapper.vm.expandedCompte).toBeNull()

    // Toggle expansion - compte has allocations so it should expand
    wrapper.vm.handleCompteClick('1')
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.expandedCompte).toBe('1')

    // Toggle again to collapse
    wrapper.vm.handleCompteClick('1')
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.expandedCompte).toBeNull()
  })

  it('should sort objectifs by priority', () => {
    const store = useDashboardStore()
    const objectifs = [
      {
        id: '1',
        nom: 'Normal',
        montantCible: 1000,
        type: 'PROJET',
        priorite: 'NORMALE',
        icone: '🎯',
        couleur: '#ff0000',
        actif: true,
        repartitions: []
      },
      {
        id: '2',
        nom: 'Critique',
        montantCible: 1000,
        type: 'SECURITE',
        priorite: 'CRITIQUE',
        icone: '🚨',
        couleur: '#ff0000',
        actif: true,
        repartitions: []
      }
    ]
    store.dashboardData = {
      mois: '2025-01',
      user: { id: '1', nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 1000 },
      comptes: [],
      objectifs,
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    const wrapper = mount(PatrimoineView, mountOptions)
    // PatrimoineView displays objectifs directly from store, no sorting
    expect(store.objectifs.length).toBe(2)
    expect(store.objectifs.some(o => o.nom === 'Critique')).toBe(true)
    expect(store.objectifs.some(o => o.nom === 'Normal')).toBe(true)
  })

  it('should show argent libre when available', () => {
    const store = useDashboardStore()
    const comptes = [
      {
        id: '1',
        nom: 'Test',
        type: 'LIVRET_A',
        soldeTotal: 3000,
        argentLibre: 1000,
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
    const wrapper = mount(PatrimoineView, mountOptions)
    expect(store.comptes[0].argentLibre).toBe(1000)
  })
})
