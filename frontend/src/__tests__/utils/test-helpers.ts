import { vi } from 'vitest'
import type {
  User,
  Banque,
  Compte,
  Objectif,
  ChargeFixe,
  Transaction,
  SimpleDashboardData,
  TypeCompte,
  TypeObjectif,
  TypeTransaction,
  PrioriteObjectif,
  FrequenceCharge
} from '@/types'

/**
 * Mock User Data
 */
export const createMockUser = (overrides?: Partial<User>): User => ({
  id: 'user-123',
  nom: 'Dupont',
  prenom: 'Jean',
  jourPaie: 1,
  salaireMensuelNet: 3000,
  decouvertAutorise: 500,
  objectifCompteCourant: 1000,
  pourcentageChargesFixes: 50,
  pourcentageDepensesVariables: 30,
  pourcentageEpargne: 20,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
  ...overrides
})

/**
 * Mock Banque Data
 */
export const createMockBanque = (overrides?: Partial<Banque>): Banque => ({
  id: 'banque-123',
  nom: 'Banque Populaire',
  couleurTheme: '#0066CC',
  logoUrl: 'https://example.com/logo.png',
  actif: true,
  ...overrides
})

/**
 * Mock Compte Data
 */
export const createMockCompte = (overrides?: Partial<Compte>): Compte => ({
  id: 'compte-123',
  banque: createMockBanque(),
  nom: 'Compte Courant',
  type: 'COMPTE_COURANT' as TypeCompte,
  soldeTotal: 2500,
  taux: 0,
  plafond: undefined,
  dateOuverture: '2023-01-01',
  actif: true,
  argentLibre: 1500,
  ...overrides
})

/**
 * Mock Objectif Data
 */
export const createMockObjectif = (overrides?: Partial<Objectif>): Objectif => ({
  id: 'objectif-123',
  nom: 'Vacances',
  montantCible: 5000,
  couleur: '#FF5722',
  icone: '✈️',
  description: 'Vacances d\'été',
  priorite: 'HAUTE' as PrioriteObjectif,
  type: 'PLAISIR' as TypeObjectif,
  actif: true,
  montantActuel: 2000,
  pourcentageProgression: 40,
  repartitions: [],
  ...overrides
})

/**
 * Mock ChargeFixe Data
 */
export const createMockChargeFixe = (overrides?: Partial<ChargeFixe>): ChargeFixe => ({
  id: 'charge-123',
  userId: 'user-123',
  compte: createMockCompte(),
  nom: 'Loyer',
  description: 'Loyer mensuel',
  montant: 800,
  categorie: 'LOYER' as TypeTransaction,
  jourPrelevement: 5,
  frequence: 'MENSUELLE' as FrequenceCharge,
  dateDebut: '2023-01-01',
  dateFin: undefined,
  actif: true,
  createdAt: '2023-01-01T00:00:00Z',
  updatedAt: '2023-01-01T00:00:00Z',
  ...overrides
})

/**
 * Mock Transaction Data
 */
export const createMockTransaction = (overrides?: Partial<Transaction>): Transaction => ({
  id: 'transaction-123',
  user: createMockUser(),
  compte: createMockCompte(),
  objectif: undefined,
  montant: 50,
  description: 'Courses',
  type: 'ALIMENTATION' as TypeTransaction,
  dateTransaction: '2025-01-15',
  createdAt: '2025-01-15T10:00:00Z',
  ...overrides
})

/**
 * Mock SimpleDashboardData
 */
export const createMockDashboardData = (
  overrides?: Partial<SimpleDashboardData>
): SimpleDashboardData => ({
  mois: '2025-01',
  user: createMockUser(),
  comptes: [createMockCompte()],
  objectifs: [createMockObjectif()],
  salaireValide: false,
  chargesFixes: [createMockChargeFixe()],
  transactions: [createMockTransaction()],
  timestamp: '2025-01-15T12:00:00Z',
  ...overrides
})

/**
 * Mock API Service
 */
export const createMockApiService = () => ({
  // Dashboard
  getDashboard: vi.fn().mockResolvedValue(createMockDashboardData()),
  testConnection: vi.fn().mockResolvedValue({
    message: 'OK',
    timestamp: new Date().toISOString(),
    version: '2.0',
    status: 'success'
  }),

  // User
  getUser: vi.fn().mockResolvedValue(createMockUser()),
  updateUser: vi.fn().mockResolvedValue(createMockUser()),

  // Comptes
  getComptes: vi.fn().mockResolvedValue([createMockCompte()]),
  createCompte: vi.fn().mockResolvedValue(createMockCompte()),
  updateCompte: vi.fn().mockResolvedValue(createMockCompte()),
  deleteCompte: vi.fn().mockResolvedValue(undefined),

  // Objectifs
  getObjectifs: vi.fn().mockResolvedValue([createMockObjectif()]),
  createObjectif: vi.fn().mockResolvedValue(createMockObjectif()),
  updateObjectif: vi.fn().mockResolvedValue(createMockObjectif()),
  deleteObjectif: vi.fn().mockResolvedValue(undefined),

  // Charges Fixes
  getChargesFixes: vi.fn().mockResolvedValue([createMockChargeFixe()]),
  createChargeFixe: vi.fn().mockResolvedValue(createMockChargeFixe()),
  updateChargeFixe: vi.fn().mockResolvedValue(createMockChargeFixe()),
  deleteChargeFixe: vi.fn().mockResolvedValue(undefined),

  // Transactions
  getTransactions: vi.fn().mockResolvedValue([createMockTransaction()]),
  createTransaction: vi.fn().mockResolvedValue(createMockTransaction()),
  updateTransaction: vi.fn().mockResolvedValue(createMockTransaction()),
  deleteTransaction: vi.fn().mockResolvedValue(undefined),
  createBulkTransactions: vi.fn().mockResolvedValue([createMockTransaction()]),

  // Banques
  getBanques: vi.fn().mockResolvedValue([createMockBanque()]),

  // Bank statements
  uploadBankStatement: vi.fn().mockResolvedValue({
    message: 'Upload successful',
    transactionsCount: 5
  }),

  // Versements & Transferts
  createVersementObjectif: vi.fn().mockResolvedValue(createMockTransaction()),
  createTransfertComptes: vi.fn().mockResolvedValue({
    transactionSource: createMockTransaction(),
    transactionDestination: createMockTransaction()
  }),
  createTransfertObjectifs: vi.fn().mockResolvedValue({
    transactionSource: createMockTransaction(),
    transactionDestination: createMockTransaction()
  }),

  // Salary validation
  validateSalaire: vi.fn().mockResolvedValue({
    transactionId: 'transaction-123',
    message: 'Salaire validé',
    nouveauSolde: 5500
  })
})

/**
 * Wait for async operations in tests
 */
export const flushPromises = () => {
  return new Promise((resolve) => {
    setTimeout(resolve, 0)
  })
}

/**
 * Create a delayed promise for testing loading states
 */
export const createDelayedPromise = <T>(value: T, delay = 100): Promise<T> => {
  return new Promise((resolve) => {
    setTimeout(() => resolve(value), delay)
  })
}

/**
 * Create a rejected promise for testing error states
 */
export const createRejectedPromise = (error: Error | string, delay = 100): Promise<never> => {
  return new Promise((_, reject) => {
    setTimeout(() => reject(typeof error === 'string' ? new Error(error) : error), delay)
  })
}
