import axios, { type AxiosResponse, type AxiosError } from 'axios'
import type {
  User,
  UserFormData,
  Banque,
  Compte,
  CreateCompteRequest,
  UpdateCompteRequest,
  Objectif,
  CreateObjectifRequest,
  UpdateObjectifRequest,
  SimpleDashboardData,
  TestConnectionResponse,
  BudgetConfig,
  BudgetConfigRequest,
  ChargeFixe,
  CreateChargeFixeRequest,
  Transaction,
  ErrorResponse,
  ValidationSalaireRequest,
  ValidationSalaireResponse,
  CreateCompteTransfertRequest,
  CreateTransfertObjectifRequest,
  CreateTransactionRequest,
  CreateVersementObjectifRequest
} from '@/types'
import { logger } from '@/utils/logger'
import {
  UserSchema,
  BanqueSchema,
  CompteSchema,
  ObjectifSchema,
  ChargeFixeSchema,
  SimpleDashboardDataSchema,
  BudgetConfigSchema
} from '@/utils/validation'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

// Configuration axios
const apiClient = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: Number(import.meta.env.VITE_API_TIMEOUT) || 10000
})

// Request interceptor - Add auth token and CSRF protection
apiClient.interceptors.request.use(
  (config) => {
    // Add authentication token
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // Add CSRF token for state-changing requests
    if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase() || '')) {
      const csrfToken = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content')
      if (csrfToken) {
        config.headers['X-CSRF-Token'] = csrfToken
      }
    }

    return config
  },
  (error) => {
    logger.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor - Handle errors and auth
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError<ErrorResponse>) => {
    const errorMessage = error.response?.data?.message || error.message

    // Log only in development
    logger.error('API Error:', {
      url: error.config?.url,
      status: error.response?.status,
      message: errorMessage,
      details: error.response?.data
    })

    // Handle 401 Unauthorized - clear auth and redirect
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      // Router will handle redirect via navigation guard
      if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
        // Will be handled by router guard
      }
    }

    // Handle 403 Forbidden
    if (error.response?.status === 403) {
      logger.error('Access forbidden')
    }

    // Handle 429 Rate Limit
    if (error.response?.status === 429) {
      logger.warn('Rate limit exceeded')
    }

    return Promise.reject(error)
  }
)

export const apiService = {
  
  // ===============================================
  // USER RESOURCE
  // ===============================================

  /**
   * GET /api/user/exists
   * Check if a user exists in the database (mono-user app)
   */
  async checkUserExists(): Promise<boolean> {
    try {
      const response = await apiClient.get<{ exists: boolean }>('/user/exists')
      return response.data.exists
    } catch (error) {
      logger.error('Error checking user existence:', error)
      return false
    }
  },

  /**
   * GET /api/user/profile
   * Récupérer le profil utilisateur
   */
  async getUserProfile(): Promise<User> {
    const response = await apiClient.get<User>('/user/profile')
    const parsed = UserSchema.parse(response.data)
    // Remove undefined properties for exactOptionalPropertyTypes
    const cleaned = {
      id: parsed.id,
      nom: parsed.nom,
      prenom: parsed.prenom,
      jourPaie: parsed.jourPaie,
      salaireMensuelNet: parsed.salaireMensuelNet,
      ...(parsed.decouvertAutorise != null && { decouvertAutorise: parsed.decouvertAutorise }),
      ...(parsed.objectifCompteCourant != null && { objectifCompteCourant: parsed.objectifCompteCourant }),
      ...(parsed.pourcentageChargesFixes != null && { pourcentageChargesFixes: parsed.pourcentageChargesFixes }),
      ...(parsed.pourcentageDepensesVariables != null && { pourcentageDepensesVariables: parsed.pourcentageDepensesVariables }),
      ...(parsed.pourcentageEpargne != null && { pourcentageEpargne: parsed.pourcentageEpargne }),
      ...(parsed.createdAt != null && { createdAt: parsed.createdAt }),
      ...(parsed.updatedAt != null && { updatedAt: parsed.updatedAt })
    } as User
    return cleaned
  },

  /**
   * POST /api/user/profile
   * Créer un nouveau profil utilisateur
   */
  async createUserProfile(userData: UserFormData | User): Promise<User> {
    const response = await apiClient.post<User>('/user/profile', userData)
    const parsed = UserSchema.parse(response.data)
    const cleaned = {
      id: parsed.id,
      nom: parsed.nom,
      prenom: parsed.prenom,
      jourPaie: parsed.jourPaie,
      salaireMensuelNet: parsed.salaireMensuelNet,
      ...(parsed.decouvertAutorise != null && { decouvertAutorise: parsed.decouvertAutorise }),
      ...(parsed.objectifCompteCourant != null && { objectifCompteCourant: parsed.objectifCompteCourant }),
      ...(parsed.pourcentageChargesFixes != null && { pourcentageChargesFixes: parsed.pourcentageChargesFixes }),
      ...(parsed.pourcentageDepensesVariables != null && { pourcentageDepensesVariables: parsed.pourcentageDepensesVariables }),
      ...(parsed.pourcentageEpargne != null && { pourcentageEpargne: parsed.pourcentageEpargne }),
      ...(parsed.createdAt != null && { createdAt: parsed.createdAt }),
      ...(parsed.updatedAt != null && { updatedAt: parsed.updatedAt })
    } as User
    return cleaned
  },

  /**
   * PUT /api/user/profile
   * Mettre à jour le profil utilisateur
   */
  async updateUserProfile(userData: Partial<User>): Promise<User> {
    const response = await apiClient.put<User>('/user/profile', userData)
    const parsed = UserSchema.parse(response.data)
    const cleaned = {
      id: parsed.id,
      nom: parsed.nom,
      prenom: parsed.prenom,
      jourPaie: parsed.jourPaie,
      salaireMensuelNet: parsed.salaireMensuelNet,
      ...(parsed.decouvertAutorise != null && { decouvertAutorise: parsed.decouvertAutorise }),
      ...(parsed.objectifCompteCourant != null && { objectifCompteCourant: parsed.objectifCompteCourant }),
      ...(parsed.pourcentageChargesFixes != null && { pourcentageChargesFixes: parsed.pourcentageChargesFixes }),
      ...(parsed.pourcentageDepensesVariables != null && { pourcentageDepensesVariables: parsed.pourcentageDepensesVariables }),
      ...(parsed.pourcentageEpargne != null && { pourcentageEpargne: parsed.pourcentageEpargne }),
      ...(parsed.createdAt != null && { createdAt: parsed.createdAt }),
      ...(parsed.updatedAt != null && { updatedAt: parsed.updatedAt })
    } as User
    return cleaned
  },

  /**
   * GET /api/user/budget-config
   * Récupérer la configuration budgétaire
   */
  async getBudgetConfig(): Promise<BudgetConfig> {
    const response = await apiClient.get<BudgetConfig>('/user/budget-config')
    const parsed = BudgetConfigSchema.parse(response.data)
    const cleaned: BudgetConfig = {
      id: parsed.id,
      userId: parsed.userId,
      pourcentageChargesFixes: parsed.pourcentageChargesFixes,
      pourcentageDepensesVariables: parsed.pourcentageDepensesVariables,
      pourcentageEpargne: parsed.pourcentageEpargne,
      ...(parsed.createdAt != null && { createdAt: parsed.createdAt }),
      ...(parsed.updatedAt != null && { updatedAt: parsed.updatedAt })
    }
    return cleaned
  },
  
  /**
   * PUT /api/user/budget-config
   * Mettre à jour la configuration budgétaire (50/30/20)
   */
  async updateBudgetConfig(configData: BudgetConfigRequest): Promise<BudgetConfig> {
    const response = await apiClient.put<BudgetConfig>('/user/budget-config', configData)
    return response.data
  },

  // ===============================================
  // BANQUE RESOURCE
  // ===============================================
  
  /**
   * GET /api/banques
   * Récupérer toutes les banques
   */
  async getBanques(): Promise<Banque[]> {
    const response = await apiClient.get<Banque[]>('/banques')
    return response.data
  },
  
  /**
   * POST /api/banques
   * Créer une nouvelle banque
   */
  async createBanque(banqueData: Partial<Banque>): Promise<Banque> {
    const response = await apiClient.post<Banque>('/banques', banqueData)
    return response.data
  },

  /**
   * GET /api/banques/populate
   * Peupler la base avec les banques par défaut
   */
  async populateBanques(): Promise<{ message: string }> {
    const response = await apiClient.get<{ message: string }>('/banques/populate')
    return response.data
  },

  // ===============================================
  // COMPTE RESOURCE
  // ===============================================
  
  /**
   * GET /api/comptes
   * Récupérer tous les comptes de l'utilisateur
   */
  async getComptes(): Promise<Compte[]> {
    const response = await apiClient.get<Compte[]>('/comptes')
    return response.data
  },

  /**
   * GET /api/comptes/{id}
   * Récupérer un compte par ID
   */
  async getCompte(id: string): Promise<Compte> {
    const response = await apiClient.get<Compte>(`/comptes/${id}`)
    return response.data
  },

  /**
   * POST /api/comptes
   * Créer un nouveau compte
   */
  async createCompte(compteData: CreateCompteRequest): Promise<Compte> {
    const response = await apiClient.post<Compte>('/comptes', compteData)
    return response.data
  },

  /**
   * PUT /api/comptes/{id}
   * Mettre à jour un compte
   */
  async updateCompte(id: string, compteData: UpdateCompteRequest): Promise<Compte> {
    const response = await apiClient.put<Compte>(`/comptes/${id}`, compteData)
    return response.data
  },

  /**
   * DELETE /api/comptes/{id}
   * Supprimer un compte
   */
  async deleteCompte(id: string): Promise<{ message: string }> {
    const response = await apiClient.delete<{ message: string }>(`/comptes/${id}`)
    return response.data
  },

  /**
   * GET /api/comptes/principal-charges-fixes
   * Récupérer le compte principal pour les charges fixes
   */
  async getComptePrincipalChargesFixes(): Promise<Compte> {
    const response = await apiClient.get<Compte>('/comptes/principal-charges-fixes')
    return response.data
  },

  // ===============================================
  // OBJECTIF RESOURCE
  // ===============================================
  
  /**
   * GET /api/objectifs
   * Récupérer tous les objectifs
   */
  async getObjectifs(): Promise<Objectif[]> {
    const response = await apiClient.get<Objectif[]>('/objectifs')
    return response.data
  },

  /**
   * GET /api/objectifs/{id}
   * Récupérer un objectif par ID
   */
  async getObjectif(id: string): Promise<Objectif> {
    const response = await apiClient.get<Objectif>(`/objectifs/${id}`)
    return response.data
  },

  /**
   * POST /api/objectifs
   * Créer un nouvel objectif
   */
  async createObjectif(objectifData: CreateObjectifRequest): Promise<Objectif> {
    const response = await apiClient.post<Objectif>('/objectifs', objectifData)
    return response.data
  },

  /**
   * PUT /api/objectifs/{id}
   * Mettre à jour un objectif
   */
  async updateObjectif(id: string, objectifData: UpdateObjectifRequest): Promise<Objectif> {
    const response = await apiClient.put<Objectif>(`/objectifs/${id}`, objectifData)
    return response.data
  },

  /**
   * DELETE /api/objectifs/{id}
   * Supprimer un objectif
   */
  async deleteObjectif(id: string): Promise<{ message: string }> {
    const response = await apiClient.delete<{ message: string }>(`/objectifs/${id}`)
    return response.data
  },

  // ===============================================
  // CHARGE FIXE RESOURCE
  // ===============================================
  
  /**
   * GET /api/charges-fixes
   * Récupérer toutes les charges fixes
   */
  async getChargesFixes(): Promise<ChargeFixe[]> {
    const response = await apiClient.get<ChargeFixe[]>('/charges-fixes')
    return response.data
  },

  /**
   * POST /api/charges-fixes
   * Créer une nouvelle charge fixe
   */
  async createChargeFixe(chargeData: CreateChargeFixeRequest): Promise<ChargeFixe> {
    const response = await apiClient.post<ChargeFixe>('/charges-fixes', chargeData)
    return response.data
  },

  /**
   * PUT /api/charges-fixes/{id}
   * Mettre à jour une charge fixe
   */
  async updateChargeFixe(id: string, chargeData: CreateChargeFixeRequest): Promise<ChargeFixe> {
    const response = await apiClient.put<ChargeFixe>(`/charges-fixes/${id}`, chargeData)
    return response.data
  },

  /**
   * DELETE /api/charges-fixes/{id}
   * Supprimer une charge fixe
   */
  async deleteChargeFixe(id: string): Promise<{ message: string }> {
    const response = await apiClient.delete<{ message: string }>(`/charges-fixes/${id}`)
    return response.data
  },

  // ===============================================
  // TRANSACTIONS RESOURCE
  // ===============================================

  /**
   * GET /api/transactions
   * Récupérer toutes les transactions (avec filtres optionnels)
   */
  async getTransactions(params?: { month?: string; limit?: number }): Promise<Transaction[]> {
    const queryParams = new URLSearchParams()
    if (params?.month) queryParams.append('month', params.month)
    if (params?.limit) queryParams.append('limit', params.limit.toString())
    const url = `/transactions${queryParams.toString() ? '?' + queryParams.toString() : ''}`
    const response = await apiClient.get<Transaction[]>(url)
    return response.data
  },

  /**
   * POST /api/transactions
   * Créer une nouvelle transaction (dépense, revenu, versement objectif)
   */
  async createTransaction(request: CreateTransactionRequest): Promise<Transaction> {
    const response = await apiClient.post<Transaction>('/transactions', request)
    return response.data
  },

  /**
   * POST /api/transactions/salaire
   * Valider le salaire mensuel ou autres revenus (action rapide)
   */
  async validateSalary(request: ValidationSalaireRequest): Promise<ValidationSalaireResponse> {
    const response = await apiClient.post<ValidationSalaireResponse>('/transactions/salaire', request)
    return response.data
  },

  /**
   * POST /api/transactions/upload
   * Upload bank statement CSV file
   */
  async uploadBankStatement(file: File, compteId: string): Promise<{ message: string; transactions: Transaction[] }> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('compteId', compteId)

    const response = await apiClient.post<{ message: string; transactions: Transaction[] }>('/transactions/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  },

  /**
   * POST /api/transactions/bulk
   * Create multiple transactions at once
   */
  async createBulkTransactions(transactions: Partial<Transaction>[]): Promise<{ message: string; count: number }> {
    const response = await apiClient.post<{ message: string; count: number }>('/transactions/bulk', {
      transactions
    })
    return response.data
  },

  /**
   * PUT /api/transactions/{id}
   * Update an existing transaction
   */
  async updateTransaction(id: string, data: Partial<Transaction>): Promise<Transaction> {
    const response = await apiClient.put<Transaction>(`/transactions/${id}`, data)
    return response.data
  },

  /**
   * DELETE /api/transactions/{id}
   * Delete a transaction
   */
  async deleteTransaction(id: string): Promise<void> {
    await apiClient.delete(`/transactions/${id}`)
  },

  // ===============================================
  // TRANSFERTS RESOURCE
  // ===============================================

  /**
   * POST /api/transferts/comptes
   * Créer un transfert entre deux comptes
   */
  async transferBetweenAccounts(request: CreateCompteTransfertRequest): Promise<{ message: string }> {
    const response = await apiClient.post<{ message: string }>('/transferts/comptes', request)
    return response.data
  },

  /**
   * POST /api/transferts/objectifs
   * Créer un transfert entre deux objectifs
   */
  async transferBetweenObjectifs(request: CreateTransfertObjectifRequest): Promise<{ message: string }> {
    const response = await apiClient.post<{ message: string }>('/transferts/objectifs', request)
    return response.data
  },

  /**
   * POST /api/objectifs/{id}/versement
   * Effectuer un versement vers un objectif
   */
  async addVersementToObjectif(request: CreateVersementObjectifRequest): Promise<{ message: string; objectif: Objectif }> {
    const response = await apiClient.post<{ message: string; objectif: Objectif }>(`/objectifs/${request.objectifId}/versement`, {
      compteId: request.compteId,
      montant: request.montant,
      description: request.description,
      dateVersement: request.dateVersement
    })
    return response.data
  },

  // ===============================================
  // DASHBOARD RESOURCE
  // ===============================================
  
  /**
   * GET /api/dashboard/{mois}
   * Récupérer les données du dashboard pour un mois donné
   * @param mois Format YYYY-MM (ex: "2025-01")
   */
  async getDashboard(mois: string): Promise<SimpleDashboardData> {
    const response = await apiClient.get<SimpleDashboardData>(`/dashboard/${mois}`)
    const parsed = SimpleDashboardDataSchema.parse(response.data)
    // Clean user object to remove undefined properties
    const cleanedUser = {
      id: parsed.user.id,
      nom: parsed.user.nom,
      prenom: parsed.user.prenom,
      jourPaie: parsed.user.jourPaie,
      salaireMensuelNet: parsed.user.salaireMensuelNet,
      ...(parsed.user.decouvertAutorise != null && { decouvertAutorise: parsed.user.decouvertAutorise }),
      ...(parsed.user.objectifCompteCourant != null && { objectifCompteCourant: parsed.user.objectifCompteCourant }),
      ...(parsed.user.pourcentageChargesFixes != null && { pourcentageChargesFixes: parsed.user.pourcentageChargesFixes }),
      ...(parsed.user.pourcentageDepensesVariables != null && { pourcentageDepensesVariables: parsed.user.pourcentageDepensesVariables }),
      ...(parsed.user.pourcentageEpargne != null && { pourcentageEpargne: parsed.user.pourcentageEpargne }),
      ...(parsed.user.createdAt != null && { createdAt: parsed.user.createdAt }),
      ...(parsed.user.updatedAt != null && { updatedAt: parsed.user.updatedAt })
    } as User
    const result = {
      mois: parsed.mois,
      user: cleanedUser,
      comptes: parsed.comptes as Compte[],
      objectifs: parsed.objectifs as Objectif[],
      timestamp: parsed.timestamp,
      ...(parsed.salaireValide != null && { salaireValide: parsed.salaireValide }),
      ...(parsed.chargesFixes && { chargesFixes: parsed.chargesFixes }),
      ...(parsed.transactions && { transactions: parsed.transactions })
    } as SimpleDashboardData
    return result
  },

  /**
   * GET /api/dashboard/test
   * Tester la connexion au backend
   */
  async testConnection(): Promise<TestConnectionResponse> {
    const response = await apiClient.get<TestConnectionResponse>('/dashboard/test')
    return response.data
  },

  /**
   * DELETE /api/dashboard/month/{month}
   * Supprimer un mois validé (salaire, transactions, etc.)
   * @param month Format YYYY-MM (ex: "2025-01")
   */
  async deleteValidatedMonth(month: string): Promise<void> {
    await apiClient.delete(`/dashboard/month/${month}`)
  },

  // ===============================================
  // HELPERS & ALIASES (pour compatibilité)
  // ===============================================

  // Alias pour getBanques
  getBanks: async () => apiService.getBanques(),
  
  // Alias pour getComptes
  getAccounts: async () => apiService.getComptes(),
  
  // Alias pour getCompte
  getAccount: async (id: string) => apiService.getCompte(id),
  
  // Alias pour createCompte
  createAccount: async (data: CreateCompteRequest) => apiService.createCompte(data),
  
  // Alias pour updateCompte
  updateAccount: async (id: string, data: UpdateCompteRequest) => apiService.updateCompte(id, data),
  
  // Alias pour deleteCompte
  deleteAccount: async (id: string) => apiService.deleteCompte(id),
  
  // Alias pour populateBanques
  populateBanks: async () => apiService.populateBanques(),
}

// Export par défaut
export default apiService