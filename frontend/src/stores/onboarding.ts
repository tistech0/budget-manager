import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { apiService } from '@/services/api'
import { logger } from '@/utils/logger'
import type {
  User,
  UserFormData,
  Banque,
  CreateCompteRequest,
  CreateObjectifRequest,
  CreateChargeFixeRequest,
  BudgetConfigRequest,
  TypeCompte,
  TypeObjectif,
  PrioriteObjectif,
  TypeTransaction,
  FrequenceCharge
} from '@/types'

export interface AccountFormData extends Omit<CreateCompteRequest, 'banqueId'> {
    banque: Banque
    tempId?: string
    isMainForCharges?: boolean
  }

export interface ChargeFixeFormData extends Omit<CreateChargeFixeRequest, 'compteId'> {
  id?: string
  compte?: AccountFormData
  tempId?: string
  actif?: boolean
}

export interface ChargeFixeInput extends CreateChargeFixeRequest {
  // Input format from the form
}

export interface BudgetConfigFormData {
  pourcentageChargesFixes: number
  pourcentageDepensesVariables: number
  pourcentageEpargne: number
}

export const useOnboardingStore = defineStore('onboarding', () => {
  // État
  const currentStep = ref<number>(1)
  const totalSteps = ref<number>(7)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)
  const isCompleted = ref<boolean>(false)
  const currentUser = ref<User | null>(null)
  
  // Données utilisateur
  const userData = ref<UserFormData>({
    nom: '',
    prenom: '',
    jourPaie: 0,
    salaireMensuelNet: 0
  })
  
  const selectedBanks = ref<Banque[]>([])
  const availableBanks = ref<Banque[]>([])
  const userAccounts = ref<AccountFormData[]>([])
  const userObjectifs = ref<CreateObjectifRequest[]>([])
  
  // NOUVEAU - Étapes 6 et 7
  const budgetConfig = ref<BudgetConfigFormData>({
    pourcentageChargesFixes: 50,
    pourcentageDepensesVariables: 30,
    pourcentageEpargne: 20
  })
  
  const userChargesFixes = ref<ChargeFixeFormData[]>([])
  
  // Computed
  const progress = computed<number>(() => (currentStep.value / totalSteps.value) * 100)
  
  const canGoNext = computed<boolean>(() => {
    switch (currentStep.value) {
      case 1:
        return !!(userData.value.nom && 
                 userData.value.prenom && 
                 userData.value.jourPaie && 
                 userData.value.salaireMensuelNet)
      case 2:
        return selectedBanks.value.length > 0
      case 3:
        return userAccounts.value.length > 0
      case 4:
        return userObjectifs.value.length > 0
      case 5:
        // Étape budget config - doit totaliser 100%
        const budgetTotal = budgetConfig.value.pourcentageChargesFixes +
                           budgetConfig.value.pourcentageDepensesVariables +
                           budgetConfig.value.pourcentageEpargne
        return budgetTotal === 100
      case 6:
        // Étape charges fixes - optionnelle, toujours valide
        return true
      case 7:
        // Étape summary - toujours valide
        return true
      default:
        return true
    }
  })
  
  // Actions
  const nextStep = (): void => {
    if (currentStep.value < totalSteps.value && canGoNext.value) {
      currentStep.value++
    }
  }
  
  const prevStep = (): void => {
    if (currentStep.value > 1) {
      currentStep.value--
    }
  }
  
  const goToStep = (step: number): void => {
    if (step >= 1 && step <= totalSteps.value) {
      currentStep.value = step
    }
  }
  
  const setError = (message: string): void => {
    error.value = message
    setTimeout(() => { error.value = null }, 5000)
  }
  
  const clearError = (): void => {
    error.value = null
  }
  
  const loadBanks = async (): Promise<void> => {
    try {
      loading.value = true
      // D'abord initialiser les banques si nécessaire
      await apiService.populateBanks()
      // Puis les charger
      availableBanks.value = await apiService.getBanks()
    } catch (err: any) {
      setError('Erreur lors du chargement des banques')
      logger.error('Error loading banks:', err)
    } finally {
      loading.value = false
    }
  }
  
  const toggleBank = (bank: Banque): void => {
    const index = selectedBanks.value.findIndex(b => b.id === bank.id)
    if (index > -1) {
      selectedBanks.value.splice(index, 1)
    } else {
      selectedBanks.value.push(bank)
    }
  }
  
  const isBankSelected = (bank: Banque): boolean => {
    return selectedBanks.value.some(b => b.id === bank.id)
  }
  
  const addAccount = (accountData: AccountFormData): void => {
    const newAccount: AccountFormData = {
      ...accountData,
      tempId: `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    }
    userAccounts.value.push(newAccount)
  }
  
  const removeAccount = (index: number): void => {
    if (index >= 0 && index < userAccounts.value.length) {
      userAccounts.value.splice(index, 1)
    }
  }
  
  const updateAccount = (index: number, accountData: Partial<AccountFormData>): void => {
    if (index >= 0 && index < userAccounts.value.length) {
      userAccounts.value[index] = { ...userAccounts.value[index], ...accountData }
    }
  }

  const addObjectif = (objectifData: CreateObjectifRequest): void => {
    userObjectifs.value.push(objectifData)
  }

  const removeObjectif = (index: number): void => {
    if (index >= 0 && index < userObjectifs.value.length) {
      userObjectifs.value.splice(index, 1)
    }
  }

  const updateObjectif = (index: number, objectifData: Partial<CreateObjectifRequest>): void => {
    if (index >= 0 && index < userObjectifs.value.length) {
      userObjectifs.value[index] = { ...userObjectifs.value[index], ...objectifData }
    }
  }

  const setBudgetConfig = (config: BudgetConfigFormData): void => {
    budgetConfig.value = { ...config }
  }

  const addChargeFixe = (chargeData: Omit<ChargeFixeInput, 'id'>): void => {
    const { compteId, ...rest } = chargeData
    const foundCompte = userAccounts.value.find(c => c.tempId === compteId)
    const newCharge: ChargeFixeFormData = {
      id: Date.now().toString(),
      actif: true,
      ...rest,
      ...(foundCompte && { compte: foundCompte })
    }
    userChargesFixes.value.push(newCharge)
  }

  const updateChargeFixe = (chargeId: string, chargeData: Partial<ChargeFixeInput>): void => {
    const index = userChargesFixes.value.findIndex(c => c.id === chargeId)
    if (index > -1) {
      const { compteId, ...rest } = chargeData
      const foundCompte = compteId ? userAccounts.value.find(c => c.tempId === compteId) : undefined
      userChargesFixes.value[index] = {
        ...userChargesFixes.value[index],
        ...rest,
        ...(foundCompte && { compte: foundCompte })
      }
    }
  }
  
  const removeChargeFixe = (chargeId: string): void => {
    const index = userChargesFixes.value.findIndex(c => c.id === chargeId)
    if (index > -1) {
      userChargesFixes.value.splice(index, 1)
    }
  }

  const removeChargeFixeByIndex = (index: number): void => {
    if (index >= 0 && index < userChargesFixes.value.length) {
      userChargesFixes.value.splice(index, 1)
    }
  }
  
  const updateChargeFixeByIndex = (index: number, chargeData: Partial<ChargeFixeFormData>): void => {
    if (index >= 0 && index < userChargesFixes.value.length) {
      userChargesFixes.value[index] = { ...userChargesFixes.value[index], ...chargeData }
    }
  }
  
  const submitOnboarding = async (): Promise<boolean> => {
    try {
      loading.value = true

      // 1. Créer le profil utilisateur
      const createdUser = await apiService.createUserProfile(userData.value)

      // Mark onboarding as completed (mono-user app, no real auth needed)
      currentUser.value = createdUser
      isCompleted.value = true
      localStorage.setItem('onboarding-completed', 'true')
      localStorage.setItem('user-id', createdUser.id)

      // 2. Mettre à jour la configuration budgétaire
      const budgetConfigRequest: BudgetConfigRequest = {
        pourcentageChargesFixes: budgetConfig.value.pourcentageChargesFixes,
        pourcentageDepensesVariables: budgetConfig.value.pourcentageDepensesVariables,
        pourcentageEpargne: budgetConfig.value.pourcentageEpargne
      }
      await apiService.updateBudgetConfig(budgetConfigRequest)
      
      // 3. Créer les comptes et créer une map des temp IDs -> real IDs
      const createdAccounts = []
      const accountIdMap = new Map<string, string>() // tempId -> realId

      for (const account of userAccounts.value) {
        const accountRequest: CreateCompteRequest = {
          banqueId: account.banque.id,
          nom: account.nom,
          type: account.type,
          soldeTotal: account.soldeTotal,
          ...(account.taux != null && { taux: account.taux }),
          ...(account.plafond != null && { plafond: account.plafond }),
          ...(account.dateOuverture && { dateOuverture: account.dateOuverture })
        }
        const createdAccount = await apiService.createAccount(accountRequest)
        createdAccounts.push(createdAccount)

        // Mapper temp ID -> real ID
        if (account.tempId) {
          accountIdMap.set(account.tempId, createdAccount.id)
        }
      }

      // 4. Créer les objectifs avec les repartitions mappées aux vrais IDs
      for (const objectif of userObjectifs.value) {
        // Si l'objectif a des répartitions, remplacer les temp IDs par les vrais IDs
        const mappedObjectif = { ...objectif }

        if (objectif.repartitions && objectif.repartitions.length > 0) {
          mappedObjectif.repartitions = objectif.repartitions.map(rep => ({
            ...rep,
            accountId: accountIdMap.get(rep.accountId) || rep.accountId
          }))
        }

        await apiService.createObjectif(mappedObjectif)
      }
      
      // 5. Créer les charges fixes
      for (const charge of userChargesFixes.value) {
        // Trouver le compte réel correspondant
        const realAccount = createdAccounts.find(acc =>
          acc.nom === charge.compte?.nom &&
          acc.banque?.id === charge.compte?.banque?.id
        )

        if (realAccount) {
          const chargeRequest: CreateChargeFixeRequest = {
            compteId: realAccount.id,
            nom: charge.nom,
            ...(charge.description && { description: charge.description }),
            montant: charge.montant,
            categorie: charge.categorie,
            jourPrelevement: charge.jourPrelevement,
            frequence: charge.frequence,
            dateDebut: charge.dateDebut,
            ...(charge.dateFin && { dateFin: charge.dateFin })
          }
          await apiService.createChargeFixe(chargeRequest)
        }
      }

      // 6. Valider automatiquement le salaire pour le mois en cours
      const now = new Date()
      const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`

      try {
        await apiService.validateSalary({
          mois: currentMonth,
          type: 'SALAIRE'
        })
        logger.info('Salary automatically validated for month:', currentMonth)
      } catch (err: any) {
        // Don't fail the onboarding if salary validation fails
        logger.warn('Could not auto-validate salary:', err)
      }

      return true
    } catch (err: any) {
      setError('Erreur lors de la création du profil')
      logger.error('Error submitting onboarding:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  const resetOnboarding = (): void => {
    currentStep.value = 1
    error.value = null
    userData.value = {
      nom: '',
      prenom: '',
      jourPaie: 0,
      salaireMensuelNet: 0
    }
    selectedBanks.value = []
    userAccounts.value = []
    userObjectifs.value = []
    budgetConfig.value = {
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }
    userChargesFixes.value = []
  }

  // Load onboarding state from localStorage
  const loadState = async (): Promise<void> => {
    const completed = localStorage.getItem('onboarding-completed') === 'true'
    const userId = localStorage.getItem('user-id')

    isCompleted.value = completed

    if (completed && userId) {
      try {
        // Load user profile from backend
        currentUser.value = await apiService.getUserProfile()
        logger.log('User profile loaded from backend:', currentUser.value)
      } catch (error) {
        logger.error('Failed to load user profile:', error)
        // Clear invalid state
        isCompleted.value = false
        currentUser.value = null
        localStorage.removeItem('onboarding-completed')
        localStorage.removeItem('user-id')
      }
    }
  }

  // Mark onboarding as completed
  const markCompleted = (user: User): void => {
    isCompleted.value = true
    currentUser.value = user
    localStorage.setItem('onboarding-completed', 'true')
    localStorage.setItem('user-id', user.id)
  }

  // Clear onboarding state
  const clearState = (): void => {
    isCompleted.value = false
    currentUser.value = null
    localStorage.removeItem('onboarding-completed')
    localStorage.removeItem('user-id')
  }

  return {
    // État
    currentStep,
    totalSteps,
    loading,
    error,
    isCompleted,
    currentUser,
    userData,
    selectedBanks,
    availableBanks,
    userAccounts,
    userObjectifs,
    budgetConfig,
    userChargesFixes,

    // Computed
    progress,
    canGoNext,

    // Actions
    nextStep,
    prevStep,
    goToStep,
    setError,
    clearError,
    loadBanks,
    toggleBank,
    isBankSelected,
    addAccount,
    removeAccount,
    updateAccount,
    addObjectif,
    removeObjectif,
    updateObjectif,
    setBudgetConfig,
    addChargeFixe,
    removeChargeFixe,
    updateChargeFixe,
    submitOnboarding,
    resetOnboarding,
    removeChargeFixeByIndex,
    updateChargeFixeByIndex,
    loadState,
    markCompleted,
    clearState
  }
})