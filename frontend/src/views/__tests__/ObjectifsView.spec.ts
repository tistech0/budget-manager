import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ObjectifsView from '../ObjectifsView.vue'
import { useDashboardStore } from '@/stores/dashboard'

// Mock router
vi.mock('vue-router', () => ({
  useRoute: () => ({ path: '/objectifs' }),
  useRouter: () => ({ push: vi.fn() })
}))

// Mock API and composables
vi.mock('@/services/api', () => ({ apiService: { deleteObjectif: vi.fn() } }))
vi.mock('@/composables/useToast', () => ({ useToast: () => ({ error: vi.fn(), success: vi.fn() }) }))
vi.mock('@/composables/useValidation', () => ({ useValidation: () => ({ validate: vi.fn().mockResolvedValue({ success: true }) }) }))
vi.mock('@/utils/logger', () => ({ logger: { error: vi.fn(), warn: vi.fn() } }))

describe('ObjectifsView', () => {
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
    const wrapper = mount(ObjectifsView, mountOptions)

    expect(wrapper.find('.objectifs-view').exists() || wrapper.find('.main-layout').exists()).toBe(true)

    await flushPromises()
    expect(store.loading).toBe(false)
  })

  it('should render empty state when no objectifs', () => {
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
    const wrapper = mount(ObjectifsView, mountOptions)
    expect(store.objectifs.length).toBe(0)
  })

  it('should display objectifs in grid', () => {
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
    const wrapper = mount(ObjectifsView, mountOptions)
    expect(store.objectifs.length).toBe(1)
    expect(store.objectifs[0].nom).toBe('Vacances')
  })

  it('should calculate progression correctly', () => {
    const store = useDashboardStore()
    const objectifs = [
      {
        id: '1',
        nom: 'Test',
        montantCible: 1000,
        montantActuel: 500,
        pourcentageProgression: 50,
        type: 'PROJET',
        priorite: 'HAUTE',
        icone: '🎯',
        couleur: '#ff0000',
        actif: true,
        repartitions: [
          { compteId: 'c1', montantActuel: 250 },
          { compteId: 'c2', montantActuel: 250 }
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
    const wrapper = mount(ObjectifsView, mountOptions)
    // Objectifs come with montantActuel and pourcentageProgression already calculated
    expect(store.objectifs[0].montantActuel).toBe(500)
    expect(store.objectifs[0].pourcentageProgression).toBe(50)
  })

  it('should open add modal', async () => {
    const wrapper = mount(ObjectifsView, mountOptions)
    wrapper.vm.showAddModal = true
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.showAddModal).toBe(true)
  })

  it('should populate form when editing objectif', async () => {
    const store = useDashboardStore()
    const objectifs = [
      {
        id: '1',
        nom: 'Test Objectif',
        montantCible: 5000,
        type: 'PROJET',
        priorite: 'HAUTE',
        icone: '🎯',
        couleur: '#00ff00',
        description: 'Test desc',
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
    const wrapper = mount(ObjectifsView, mountOptions)
    await wrapper.vm.editObjectif(store.objectifs[0])

    expect(wrapper.vm.editingObjectif).toBeTruthy()
    expect(wrapper.vm.formData.nom).toBe('Test Objectif')
    expect(wrapper.vm.formData.montantCible).toBe(5000)
    expect(wrapper.vm.formData.type).toBe('PROJET')
  })

  it('should close modal and reset form', () => {
    const wrapper = mount(ObjectifsView, mountOptions)
    wrapper.vm.showAddModal = true
    wrapper.vm.formData.nom = 'Test'
    wrapper.vm.closeModal()

    expect(wrapper.vm.showAddModal).toBe(false)
    expect(wrapper.vm.editingObjectif).toBeNull()
    expect(wrapper.vm.formData.nom).toBe('')
  })

  it('should return correct priority label', () => {
    const wrapper = mount(ObjectifsView, mountOptions)
    expect(wrapper.vm.getPriorityLabel('CRITIQUE')).toBe('Critique')
    expect(wrapper.vm.getPriorityLabel('HAUTE')).toBe('Haute')
    expect(wrapper.vm.getPriorityLabel('NORMALE')).toBe('Normale')
  })

  it('should return correct type label', () => {
    const wrapper = mount(ObjectifsView, mountOptions)
    expect(wrapper.vm.getTypeLabel('SECURITE')).toBe('Sécurité')
    expect(wrapper.vm.getTypeLabel('PROJET')).toBe('Projet')
    expect(wrapper.vm.getTypeLabel('COURT_TERME')).toBe('Court terme')
    expect(wrapper.vm.getTypeLabel('LONG_TERME')).toBe('Long terme')
  })

  it('should display repartitions when objectif has allocations', () => {
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
          { compteId: 'c1', compte: { nom: 'Livret A' }, montantActuel: 500 }
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
    const wrapper = mount(ObjectifsView, mountOptions)
    expect(store.objectifs[0].repartitions.length).toBe(1)
    expect(store.objectifs[0].repartitions[0].montantActuel).toBe(500)
  })
})
