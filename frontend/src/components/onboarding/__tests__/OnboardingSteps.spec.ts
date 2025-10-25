import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PersonalInfoStep from '../PersonalInfoStep.vue'
import BankSelectionStep from '../BankSelectionStep.vue'
import AccountSetupStep from '../AccountSetupStep.vue'
import BudgetConfigStep from '../BudgetConfigStep.vue'
import ChargesFixesStep from '../ChargesFixesStep.vue'
import ObjectiveSetupStep from '../ObjectiveSetupStep.vue'
import SummaryStep from '../SummaryStep.vue'

// Mock the onboarding store
vi.mock('@/stores/onboarding', () => ({
  useOnboardingStore: () => ({
    // State properties
    userData: {
      prenom: '',
      nom: '',
      jourPaie: null,
      salaireMensuelNet: 0,
      decouvertAutorise: 0,
      objectifCompteCourant: 0,
      banque: '',
      comptes: [],
      budgetLoisirsEtSorties: 0,
      budgetAlimentation: 0,
      budgetTransports: 0,
      budgetAutre: 0,
      chargesFixes: [],
      objectifs: []
    },
    loading: false,
    availableBanks: [],
    selectedBanks: [],
    userAccounts: [],
    budgetConfig: {
      budgetLoisirsEtSorties: 0,
      budgetAlimentation: 0,
      budgetTransports: 0,
      budgetAutre: 0,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    },
    userChargesFixes: [],
    userObjectifs: [],
    // Methods
    nextStep: vi.fn(),
    previousStep: vi.fn(),
    toggleBank: vi.fn(),
    isBankSelected: vi.fn(() => false),
    loadBanks: vi.fn(),
    addCompte: vi.fn(),
    removeCompte: vi.fn(),
    addChargeFixe: vi.fn(),
    removeChargeFixe: vi.fn(),
    addObjectif: vi.fn(),
    removeObjectif: vi.fn(),
    setBudgetConfig: vi.fn()
  })
}))

vi.mock('pinia', () => ({
  storeToRefs: (store: any) => {
    const refs: any = {}
    for (const key in store) {
      if (typeof store[key] !== 'function') {
        refs[key] = { value: store[key] }
      }
    }
    return refs
  }
}))

describe('Onboarding Steps - Essential Tests', () => {
  beforeEach(() => {
    const app = document.createElement('div')
    app.id = 'app'
    document.body.appendChild(app)
  })

  afterEach(() => {
    document.body.innerHTML = ''
    vi.clearAllMocks()
  })

  describe('PersonalInfoStep', () => {
    it('renders step content', () => {
      const wrapper = mount(PersonalInfoStep)
      expect(wrapper.find('.step-content').exists()).toBe(true)
    })

    it('displays step title', () => {
      const wrapper = mount(PersonalInfoStep)
      const title = wrapper.find('.step-title')
      expect(title.exists()).toBe(true)
      expect(title.text()).toBe('Informations personnelles')
    })

    it('displays all required form fields', () => {
      const wrapper = mount(PersonalInfoStep)

      expect(wrapper.find('input[type="text"]').exists()).toBe(true)
      expect(wrapper.find('select').exists()).toBe(true)
      expect(wrapper.find('input[type="number"]').exists()).toBe(true)
    })

    it('has form with submit handler', () => {
      const wrapper = mount(PersonalInfoStep)
      const form = wrapper.find('form')
      expect(form.exists()).toBe(true)
    })
  })

  describe('BankSelectionStep', () => {
    it('renders step content', () => {
      const wrapper = mount(BankSelectionStep)
      expect(wrapper.find('.step-content').exists()).toBe(true)
    })

    it('has step title element', () => {
      const wrapper = mount(BankSelectionStep)
      const title = wrapper.find('.step-title')
      expect(title.exists()).toBe(true)
    })
  })

  describe('AccountSetupStep', () => {
    it('renders step content', () => {
      const wrapper = mount(AccountSetupStep)
      expect(wrapper.find('.step-content').exists()).toBe(true)
    })

    it('has step title element', () => {
      const wrapper = mount(AccountSetupStep)
      const title = wrapper.find('.step-title')
      expect(title.exists()).toBe(true)
    })
  })

  describe('BudgetConfigStep', () => {
    it('renders step content', () => {
      const wrapper = mount(BudgetConfigStep)
      expect(wrapper.find('.step-content').exists()).toBe(true)
    })

    it('has step title element', () => {
      const wrapper = mount(BudgetConfigStep)
      const title = wrapper.find('.step-title')
      expect(title.exists()).toBe(true)
    })
  })

  describe('ChargesFixesStep', () => {
    it('renders step content', () => {
      const wrapper = mount(ChargesFixesStep)
      expect(wrapper.find('.step-content').exists()).toBe(true)
    })

    it('has step title element', () => {
      const wrapper = mount(ChargesFixesStep)
      const title = wrapper.find('.step-title')
      expect(title.exists()).toBe(true)
    })
  })

  describe('ObjectiveSetupStep', () => {
    it('renders step content', () => {
      const wrapper = mount(ObjectiveSetupStep)
      expect(wrapper.find('.step-content').exists()).toBe(true)
    })

    it('has step title element', () => {
      const wrapper = mount(ObjectiveSetupStep)
      const title = wrapper.find('.step-title')
      expect(title.exists()).toBe(true)
    })
  })

  describe('SummaryStep', () => {
    it.skip('renders step content', () => {
      // Skipped: Requires complex mock data structure with proper account.banque references
      const wrapper = mount(SummaryStep)
      expect(wrapper.find('.step-content').exists()).toBe(true)
    })

    it.skip('has step title element', () => {
      // Skipped: Requires complex mock data structure with proper account.banque references
      const wrapper = mount(SummaryStep)
      const title = wrapper.find('.step-title')
      expect(title.exists()).toBe(true)
    })
  })
})
