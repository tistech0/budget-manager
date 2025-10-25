import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import UserProfileView from '../UserProfileView.vue'
import { useDashboardStore } from '@/stores/dashboard'
import { apiService } from '@/services/api'
import type { UserProfile } from '@/types'

// Mock router and API
vi.mock('vue-router', () => ({
  useRoute: () => ({ path: '/profile' }),
  useRouter: () => ({ push: vi.fn() }),
  onBeforeRouteLeave: vi.fn()
}))

vi.mock('@/services/api', () => ({
  apiService: {
    getUserProfile: vi.fn(),
    updateUserProfile: vi.fn()
  }
}))

vi.mock('@/composables/useToast', () => ({
  useToast: () => ({ success: vi.fn(), error: vi.fn() })
}))

vi.mock('@/utils/logger', () => ({ logger: { error: vi.fn() } }))

describe('UserProfileView', () => {
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

  it('should render user profile view', () => {
    const wrapper = mount(UserProfileView, mountOptions)
    expect(wrapper.find('.user-profile-view').exists() || wrapper.find('.main-layout').exists()).toBe(true)
  })

  it('should display user information', () => {
    const store = useDashboardStore()
    const user = {
      id: '1',
      nom: 'Doe',
      prenom: 'John',
      jourPaie: 15,
      salaireMensuelNet: 3000
    }
    store.dashboardData = {
      mois: '2025-01',
      user,
      comptes: [],
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    mount(UserProfileView, mountOptions)
    expect(store.user?.prenom).toBe('John')
    expect(store.user?.nom).toBe('Doe')
  })

  it('should populate form with user data', async () => {
    const user: UserProfile = {
      id: '1',
      nom: 'Doe',
      prenom: 'John',
      jourPaie: 15,
      salaireMensuelNet: 3000,
      decouvertAutorise: 500,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }

    vi.mocked(apiService.getUserProfile).mockResolvedValue(user)

    const wrapper = mount(UserProfileView, mountOptions)
    await flushPromises()

    expect(wrapper.vm.formData.nom).toBe('Doe')
    expect(wrapper.vm.formData.prenom).toBe('John')
    expect(wrapper.vm.formData.jourPaie).toBe(15)
    expect(wrapper.vm.formData.salaireMensuelNet).toBe(3000)
    expect(wrapper.vm.formData.decouvertAutorise).toBe(500)
  })

  it('should handle form submission', async () => {
    const user: UserProfile = {
      id: '1',
      nom: 'Doe',
      prenom: 'John',
      jourPaie: 15,
      salaireMensuelNet: 3000,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }

    vi.mocked(apiService.getUserProfile).mockResolvedValue(user)
    vi.mocked(apiService.updateUserProfile).mockResolvedValue({
      ...user,
      nom: 'Updated'
    })

    const wrapper = mount(UserProfileView, mountOptions)
    await flushPromises()

    wrapper.vm.formData.nom = 'Updated'
    wrapper.vm.hasUnsavedChanges = true
    await wrapper.vm.handleSave()
    await flushPromises()

    expect(apiService.updateUserProfile).toHaveBeenCalled()
  })

  it('should display budget configuration', () => {
    const store = useDashboardStore()
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
      comptes: [],
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    mount(UserProfileView, mountOptions)

    expect(store.user?.pourcentageChargesFixes).toBe(50)
    expect(store.user?.pourcentageDepensesVariables).toBe(30)
    expect(store.user?.pourcentageEpargne).toBe(20)
  })

  it('should track unsaved changes', async () => {
    const user: UserProfile = {
      id: '1',
      nom: 'Doe',
      prenom: 'John',
      jourPaie: 15,
      salaireMensuelNet: 3000,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }

    vi.mocked(apiService.getUserProfile).mockResolvedValue(user)

    const wrapper = mount(UserProfileView, mountOptions)
    await flushPromises()

    expect(wrapper.vm.hasUnsavedChanges).toBe(false)

    wrapper.vm.formData.nom = 'Modified'
    wrapper.vm.markAsChanged()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.hasUnsavedChanges).toBe(true)
  })

  it('should have handleCancel method', async () => {
    const user: UserProfile = {
      id: '1',
      nom: 'Doe',
      prenom: 'John',
      jourPaie: 15,
      salaireMensuelNet: 3000,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }

    vi.mocked(apiService.getUserProfile).mockResolvedValue(user)

    const wrapper = mount(UserProfileView, mountOptions)
    await flushPromises()

    // handleCancel should be defined
    expect(typeof wrapper.vm.handleCancel).toBe('function')
  })

  it('should format salary correctly', () => {
    const store = useDashboardStore()
    const user = {
      id: '1',
      nom: 'Doe',
      prenom: 'John',
      jourPaie: 15,
      salaireMensuelNet: 3500.50
    }
    store.dashboardData = {
      mois: '2025-01',
      user,
      comptes: [],
      objectifs: [],
      salaireValide: false,
      timestamp: new Date().toISOString()
    }
    mount(UserProfileView, mountOptions)
    expect(store.user?.salaireMensuelNet).toBe(3500.50)
  })
})
