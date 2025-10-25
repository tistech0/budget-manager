import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useOnboardingStore } from '../onboarding'
import type { AccountFormData, ChargeFixeFormData } from '../onboarding'
import { apiService } from '@/services/api'
import {
  createMockUser,
  createMockBanque,
  createMockCompte,
  flushPromises
} from '@/__tests__/utils/test-helpers'

// Mock the API service
vi.mock('@/services/api', () => ({
  apiService: {
    populateBanks: vi.fn(),
    getBanks: vi.fn(),
    createUserProfile: vi.fn(),
    getUserProfile: vi.fn(),
    updateBudgetConfig: vi.fn(),
    createAccount: vi.fn(),
    createObjectif: vi.fn(),
    createChargeFixe: vi.fn(),
    validateSalary: vi.fn()
  }
}))

// Mock the logger
vi.mock('@/utils/logger', () => ({
  logger: {
    info: vi.fn(),
    error: vi.fn(),
    warn: vi.fn(),
    log: vi.fn()
  }
}))

describe('Onboarding Store', () => {
  beforeEach(() => {
    // Create a fresh pinia instance before each test
    setActivePinia(createPinia())
    // Clear all mocks
    vi.clearAllMocks()
    // Clear localStorage
    localStorage.clear()
  })

  afterEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const store = useOnboardingStore()

      expect(store.currentStep).toBe(1)
      expect(store.totalSteps).toBe(7)
      expect(store.loading).toBe(false)
      expect(store.error).toBe(null)
      expect(store.isCompleted).toBe(false)
      expect(store.currentUser).toBe(null)
      expect(store.userData).toEqual({
        nom: '',
        prenom: '',
        jourPaie: 0,
        salaireMensuelNet: 0
      })
      expect(store.selectedBanks).toEqual([])
      expect(store.availableBanks).toEqual([])
      expect(store.userAccounts).toEqual([])
      expect(store.userObjectifs).toEqual([])
      expect(store.budgetConfig).toEqual({
        pourcentageChargesFixes: 50,
        pourcentageDepensesVariables: 30,
        pourcentageEpargne: 20
      })
      expect(store.userChargesFixes).toEqual([])
    })
  })

  describe('Computed Properties', () => {
    it('should calculate progress correctly', () => {
      const store = useOnboardingStore()

      store.currentStep = 1
      expect(store.progress).toBeCloseTo(14.28, 1)

      store.currentStep = 4
      expect(store.progress).toBeCloseTo(57.14, 1)

      store.currentStep = 7
      expect(store.progress).toBe(100)
    })

    describe('canGoNext', () => {
      it('should validate step 1 - requires user data', () => {
        const store = useOnboardingStore()
        store.currentStep = 1

        expect(store.canGoNext).toBe(false)

        store.userData = {
          nom: 'Dupont',
          prenom: 'Jean',
          jourPaie: 1,
          salaireMensuelNet: 3000
        }

        expect(store.canGoNext).toBe(true)
      })

      it('should validate step 2 - requires at least one bank', () => {
        const store = useOnboardingStore()
        store.currentStep = 2

        expect(store.canGoNext).toBe(false)

        store.selectedBanks = [createMockBanque()]

        expect(store.canGoNext).toBe(true)
      })

      it('should validate step 3 - requires at least one account', () => {
        const store = useOnboardingStore()
        store.currentStep = 3

        expect(store.canGoNext).toBe(false)

        store.userAccounts = [
          {
            banque: createMockBanque(),
            nom: 'Compte Courant',
            type: 'COMPTE_COURANT',
            soldeTotal: 1000
          } as AccountFormData
        ]

        expect(store.canGoNext).toBe(true)
      })

      it('should validate step 4 - requires at least one objectif', () => {
        const store = useOnboardingStore()
        store.currentStep = 4

        expect(store.canGoNext).toBe(false)

        store.userObjectifs = [
          {
            nom: 'Vacances',
            montantCible: 5000,
            priorite: 'HAUTE',
            type: 'PLAISIR',
            couleur: '#FF5722'
          }
        ]

        expect(store.canGoNext).toBe(true)
      })

      it('should validate step 5 - budget must total 100%', () => {
        const store = useOnboardingStore()
        store.currentStep = 5

        // Default is 100% (50 + 30 + 20)
        expect(store.canGoNext).toBe(true)

        store.budgetConfig = {
          pourcentageChargesFixes: 40,
          pourcentageDepensesVariables: 40,
          pourcentageEpargne: 10
        }

        expect(store.canGoNext).toBe(false)

        store.budgetConfig = {
          pourcentageChargesFixes: 60,
          pourcentageDepensesVariables: 25,
          pourcentageEpargne: 15
        }

        expect(store.canGoNext).toBe(true)
      })

      it('should always allow step 6 and 7', () => {
        const store = useOnboardingStore()

        store.currentStep = 6
        expect(store.canGoNext).toBe(true)

        store.currentStep = 7
        expect(store.canGoNext).toBe(true)
      })
    })
  })

  describe('Navigation', () => {
    it('should navigate to next step when canGoNext is true', () => {
      const store = useOnboardingStore()
      store.currentStep = 1
      store.userData = {
        nom: 'Dupont',
        prenom: 'Jean',
        jourPaie: 1,
        salaireMensuelNet: 3000
      }

      store.nextStep()

      expect(store.currentStep).toBe(2)
    })

    it('should not navigate to next step when canGoNext is false', () => {
      const store = useOnboardingStore()
      store.currentStep = 1
      // userData incomplete

      store.nextStep()

      expect(store.currentStep).toBe(1)
    })

    it('should not go beyond totalSteps', () => {
      const store = useOnboardingStore()
      store.currentStep = 7

      store.nextStep()

      expect(store.currentStep).toBe(7)
    })

    it('should navigate to previous step', () => {
      const store = useOnboardingStore()
      store.currentStep = 5

      store.prevStep()

      expect(store.currentStep).toBe(4)
    })

    it('should not go below step 1', () => {
      const store = useOnboardingStore()
      store.currentStep = 1

      store.prevStep()

      expect(store.currentStep).toBe(1)
    })

    it('should go to specific step', () => {
      const store = useOnboardingStore()

      store.goToStep(5)
      expect(store.currentStep).toBe(5)

      store.goToStep(2)
      expect(store.currentStep).toBe(2)
    })

    it('should not go to invalid steps', () => {
      const store = useOnboardingStore()
      store.currentStep = 3

      store.goToStep(0)
      expect(store.currentStep).toBe(3)

      store.goToStep(10)
      expect(store.currentStep).toBe(3)
    })
  })

  describe('Error Handling', () => {
    it('should set and clear error', () => {
      const store = useOnboardingStore()

      store.setError('Test error')

      expect(store.error).toBe('Test error')

      store.clearError()

      expect(store.error).toBe(null)
    })

    it('should auto-clear error after 5 seconds', async () => {
      vi.useFakeTimers()
      const store = useOnboardingStore()

      store.setError('Test error')
      expect(store.error).toBe('Test error')

      vi.advanceTimersByTime(5000)

      expect(store.error).toBe(null)

      vi.useRealTimers()
    })
  })

  describe('Banks Management', () => {
    it('should load banks successfully', async () => {
      const store = useOnboardingStore()
      const mockBanks = [createMockBanque(), createMockBanque({ id: 'banque-456', nom: 'BNP' })]

      vi.mocked(apiService.populateBanks).mockResolvedValue(undefined)
      vi.mocked(apiService.getBanks).mockResolvedValue(mockBanks)

      await store.loadBanks()

      expect(apiService.populateBanks).toHaveBeenCalled()
      expect(apiService.getBanks).toHaveBeenCalled()
      expect(store.availableBanks).toEqual(mockBanks)
      expect(store.loading).toBe(false)
    })

    it('should handle bank loading errors', async () => {
      const store = useOnboardingStore()

      vi.mocked(apiService.populateBanks).mockRejectedValue(new Error('Network error'))

      await store.loadBanks()

      expect(store.error).toBe('Erreur lors du chargement des banques')
      expect(store.loading).toBe(false)
    })

    it('should toggle bank selection', () => {
      const store = useOnboardingStore()
      const bank1 = createMockBanque()
      const bank2 = createMockBanque({ id: 'banque-456' })

      store.toggleBank(bank1)
      expect(store.selectedBanks).toHaveLength(1)
      expect(store.isBankSelected(bank1)).toBe(true)

      store.toggleBank(bank2)
      expect(store.selectedBanks).toHaveLength(2)

      store.toggleBank(bank1)
      expect(store.selectedBanks).toHaveLength(1)
      expect(store.isBankSelected(bank1)).toBe(false)
    })
  })

  describe('Accounts Management', () => {
    it('should add account with temp ID', () => {
      const store = useOnboardingStore()
      const accountData: AccountFormData = {
        banque: createMockBanque(),
        nom: 'Compte Courant',
        type: 'COMPTE_COURANT',
        soldeTotal: 1500
      }

      store.addAccount(accountData)

      expect(store.userAccounts).toHaveLength(1)
      expect(store.userAccounts[0].nom).toBe('Compte Courant')
      expect(store.userAccounts[0].tempId).toBeDefined()
      expect(store.userAccounts[0].tempId).toMatch(/^temp_/)
    })

    it('should remove account by index', () => {
      const store = useOnboardingStore()
      store.userAccounts = [
        { nom: 'Account 1' } as AccountFormData,
        { nom: 'Account 2' } as AccountFormData,
        { nom: 'Account 3' } as AccountFormData
      ]

      store.removeAccount(1)

      expect(store.userAccounts).toHaveLength(2)
      expect(store.userAccounts[0].nom).toBe('Account 1')
      expect(store.userAccounts[1].nom).toBe('Account 3')
    })

    it('should update account by index', () => {
      const store = useOnboardingStore()
      store.userAccounts = [
        { nom: 'Account 1', soldeTotal: 1000 } as AccountFormData
      ]

      store.updateAccount(0, { soldeTotal: 2000 })

      expect(store.userAccounts[0].soldeTotal).toBe(2000)
      expect(store.userAccounts[0].nom).toBe('Account 1')
    })
  })

  describe('Objectifs Management', () => {
    it('should add objectif', () => {
      const store = useOnboardingStore()
      const objectif = {
        nom: 'Vacances',
        montantCible: 5000,
        priorite: 'HAUTE' as const,
        type: 'PLAISIR' as const,
        couleur: '#FF5722'
      }

      store.addObjectif(objectif)

      expect(store.userObjectifs).toHaveLength(1)
      expect(store.userObjectifs[0]).toEqual(objectif)
    })

    it('should remove objectif by index', () => {
      const store = useOnboardingStore()
      store.userObjectifs = [
        { nom: 'Objectif 1' } as any,
        { nom: 'Objectif 2' } as any,
        { nom: 'Objectif 3' } as any
      ]

      store.removeObjectif(1)

      expect(store.userObjectifs).toHaveLength(2)
      expect(store.userObjectifs[0].nom).toBe('Objectif 1')
      expect(store.userObjectifs[1].nom).toBe('Objectif 3')
    })

    it('should update objectif by index', () => {
      const store = useOnboardingStore()
      store.userObjectifs = [
        { nom: 'Objectif 1', montantCible: 5000 } as any
      ]

      store.updateObjectif(0, { montantCible: 7000 })

      expect(store.userObjectifs[0].montantCible).toBe(7000)
      expect(store.userObjectifs[0].nom).toBe('Objectif 1')
    })
  })

  describe('Budget Config', () => {
    it('should set budget config', () => {
      const store = useOnboardingStore()
      const config = {
        pourcentageChargesFixes: 60,
        pourcentageDepensesVariables: 25,
        pourcentageEpargne: 15
      }

      store.setBudgetConfig(config)

      expect(store.budgetConfig).toEqual(config)
    })
  })

  describe('Charges Fixes Management', () => {
    it('should add charge fixe', () => {
      const store = useOnboardingStore()
      store.userAccounts = [
        { tempId: 'temp-123', nom: 'Compte Courant' } as AccountFormData
      ]

      const chargeData = {
        compteId: 'temp-123',
        nom: 'Loyer',
        montant: 800,
        categorie: 'LOYER' as const,
        jourPrelevement: 5,
        frequence: 'MENSUELLE' as const,
        dateDebut: '2025-01-01'
      }

      store.addChargeFixe(chargeData)

      expect(store.userChargesFixes).toHaveLength(1)
      expect(store.userChargesFixes[0].nom).toBe('Loyer')
      expect(store.userChargesFixes[0].id).toBeDefined()
      expect(store.userChargesFixes[0].actif).toBe(true)
      expect(store.userChargesFixes[0].compte?.nom).toBe('Compte Courant')
    })

    it('should remove charge fixe by ID', () => {
      const store = useOnboardingStore()
      store.userChargesFixes = [
        { id: '1', nom: 'Charge 1' } as ChargeFixeFormData,
        { id: '2', nom: 'Charge 2' } as ChargeFixeFormData
      ]

      store.removeChargeFixe('1')

      expect(store.userChargesFixes).toHaveLength(1)
      expect(store.userChargesFixes[0].id).toBe('2')
    })

    it('should update charge fixe by ID', () => {
      const store = useOnboardingStore()
      store.userAccounts = [
        { tempId: 'temp-456', nom: 'New Account' } as AccountFormData
      ]
      store.userChargesFixes = [
        { id: '1', nom: 'Charge 1', montant: 100 } as ChargeFixeFormData
      ]

      store.updateChargeFixe('1', { montant: 200, compteId: 'temp-456' })

      expect(store.userChargesFixes[0].montant).toBe(200)
      expect(store.userChargesFixes[0].compte?.nom).toBe('New Account')
    })
  })

  describe('Submit Onboarding', () => {
    it('should successfully complete onboarding', async () => {
      const store = useOnboardingStore()
      const mockUser = createMockUser()
      const mockAccount = createMockCompte()

      store.userData = {
        nom: 'Dupont',
        prenom: 'Jean',
        jourPaie: 1,
        salaireMensuelNet: 3000
      }

      store.budgetConfig = {
        pourcentageChargesFixes: 50,
        pourcentageDepensesVariables: 30,
        pourcentageEpargne: 20
      }

      store.userAccounts = [
        {
          tempId: 'temp-123',
          banque: createMockBanque(),
          nom: 'Compte Courant',
          type: 'COMPTE_COURANT',
          soldeTotal: 1500
        } as AccountFormData
      ]

      store.userObjectifs = [
        {
          nom: 'Vacances',
          montantCible: 5000,
          priorite: 'HAUTE',
          type: 'PLAISIR',
          couleur: '#FF5722'
        }
      ]

      vi.mocked(apiService.createUserProfile).mockResolvedValue(mockUser)
      vi.mocked(apiService.updateBudgetConfig).mockResolvedValue(undefined)
      vi.mocked(apiService.createAccount).mockResolvedValue(mockAccount)
      vi.mocked(apiService.createObjectif).mockResolvedValue(undefined as any)
      vi.mocked(apiService.validateSalary).mockResolvedValue({
        transactionId: 'tx-123',
        message: 'Success',
        nouveauSolde: 4500
      })

      const result = await store.submitOnboarding()

      expect(result).toBe(true)
      expect(store.isCompleted).toBe(true)
      expect(store.currentUser).toEqual(mockUser)
      expect(localStorage.getItem('onboarding-completed')).toBe('true')
      expect(localStorage.getItem('user-id')).toBe(mockUser.id)
      expect(apiService.createUserProfile).toHaveBeenCalledWith(store.userData)
      expect(apiService.updateBudgetConfig).toHaveBeenCalled()
      expect(apiService.createAccount).toHaveBeenCalled()
      expect(apiService.createObjectif).toHaveBeenCalled()
      expect(apiService.validateSalary).toHaveBeenCalled()
    })

    it('should handle onboarding submission errors', async () => {
      const store = useOnboardingStore()
      store.userData = {
        nom: 'Dupont',
        prenom: 'Jean',
        jourPaie: 1,
        salaireMensuelNet: 3000
      }

      vi.mocked(apiService.createUserProfile).mockRejectedValue(new Error('Server error'))

      const result = await store.submitOnboarding()

      expect(result).toBe(false)
      expect(store.error).toBe('Erreur lors de la création du profil')
      expect(store.isCompleted).toBe(false)
      expect(store.loading).toBe(false)
    })

    it('should continue onboarding even if salary validation fails', async () => {
      const store = useOnboardingStore()
      const mockUser = createMockUser()

      store.userData = {
        nom: 'Dupont',
        prenom: 'Jean',
        jourPaie: 1,
        salaireMensuelNet: 3000
      }

      vi.mocked(apiService.createUserProfile).mockResolvedValue(mockUser)
      vi.mocked(apiService.updateBudgetConfig).mockResolvedValue(undefined)
      vi.mocked(apiService.validateSalary).mockRejectedValue(new Error('Salary validation failed'))

      const result = await store.submitOnboarding()

      // Should still succeed despite salary validation failure
      expect(result).toBe(true)
      expect(store.isCompleted).toBe(true)
    })
  })

  describe('State Management', () => {
    it('should reset onboarding state', () => {
      const store = useOnboardingStore()

      // Set some state
      store.currentStep = 5
      store.error = 'Some error'
      store.userData = { nom: 'Test', prenom: 'User', jourPaie: 1, salaireMensuelNet: 3000 }
      store.selectedBanks = [createMockBanque()]
      store.userAccounts = [{ nom: 'Account' } as AccountFormData]

      store.resetOnboarding()

      expect(store.currentStep).toBe(1)
      expect(store.error).toBe(null)
      expect(store.userData).toEqual({
        nom: '',
        prenom: '',
        jourPaie: 0,
        salaireMensuelNet: 0
      })
      expect(store.selectedBanks).toEqual([])
      expect(store.userAccounts).toEqual([])
      expect(store.budgetConfig).toEqual({
        pourcentageChargesFixes: 50,
        pourcentageDepensesVariables: 30,
        pourcentageEpargne: 20
      })
    })

    it('should load state from localStorage', async () => {
      const store = useOnboardingStore()
      const mockUser = createMockUser()

      localStorage.setItem('onboarding-completed', 'true')
      localStorage.setItem('user-id', mockUser.id)

      vi.mocked(apiService.getUserProfile).mockResolvedValue(mockUser)

      await store.loadState()

      expect(store.isCompleted).toBe(true)
      expect(store.currentUser).toEqual(mockUser)
    })

    it('should handle invalid state in localStorage', async () => {
      const store = useOnboardingStore()

      localStorage.setItem('onboarding-completed', 'true')
      localStorage.setItem('user-id', 'invalid-id')

      vi.mocked(apiService.getUserProfile).mockRejectedValue(new Error('User not found'))

      await store.loadState()

      expect(store.isCompleted).toBe(false)
      expect(store.currentUser).toBe(null)
      expect(localStorage.getItem('onboarding-completed')).toBe(null)
      expect(localStorage.getItem('user-id')).toBe(null)
    })

    it('should mark onboarding as completed', () => {
      const store = useOnboardingStore()
      const mockUser = createMockUser()

      store.markCompleted(mockUser)

      expect(store.isCompleted).toBe(true)
      expect(store.currentUser).toEqual(mockUser)
      expect(localStorage.getItem('onboarding-completed')).toBe('true')
      expect(localStorage.getItem('user-id')).toBe(mockUser.id)
    })

    it('should clear onboarding state', () => {
      const store = useOnboardingStore()

      localStorage.setItem('onboarding-completed', 'true')
      localStorage.setItem('user-id', 'user-123')
      store.isCompleted = true
      store.currentUser = createMockUser()

      store.clearState()

      expect(store.isCompleted).toBe(false)
      expect(store.currentUser).toBe(null)
      expect(localStorage.getItem('onboarding-completed')).toBe(null)
      expect(localStorage.getItem('user-id')).toBe(null)
    })
  })
})
