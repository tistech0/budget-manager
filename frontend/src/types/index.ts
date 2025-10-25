// ===============================================
// TYPES SYNCHRONISÉS AVEC OPENAPI 3.1
// Budget Manager v2.0 API
// ===============================================

// ===============================================
// ENUMS (exactement comme dans le backend)
// ===============================================

export type TypeCompte = 
  | 'COMPTE_COURANT'
  | 'LIVRET_A'
  | 'LDDS'
  | 'LIVRET_JEUNE'
  | 'LEP'
  | 'PEL'
  | 'CEL'
  | 'CSL'
  | 'PEA'
  | 'PEA_PME'
  | 'ASSURANCE_VIE'
  | 'COMPTE_TITRE'
  | 'CRYPTO'
  | 'OR_METAUX'
  | 'AUTRE'

export type TypeObjectif = 
  | 'SECURITE'
  | 'COURT_TERME'
  | 'MOYEN_TERME'
  | 'LONG_TERME'
  | 'PLAISIR'
  | 'FAMILLE'
  | 'FORMATION'
  | 'INVESTISSEMENT'
  | 'PROJET_IMMOBILIER'
  | 'TRANSPORT'
  | 'SANTE'
  | 'TECHNOLOGIE'
  | 'OPPORTUNITE'
  | 'DIVERS'

export type TypeTransaction =
  // Revenus
  | 'SALAIRE'
  | 'PRIME'
  | 'FREELANCE'
  | 'ALLOCATION'
  | 'REMBOURSEMENT'
  | 'GAIN_INVESTISSEMENT'
  | 'CADEAU_RECU'
  | 'VENTE'
  // Charges fixes
  | 'LOYER'
  | 'ASSURANCE'
  | 'ABONNEMENT'
  | 'CREDIT_IMMOBILIER'
  | 'CREDIT_CONSO'
  | 'IMPOTS'
  | 'MUTUELLE'
  // Dépenses variables
  | 'ALIMENTATION'
  | 'RESTAURANT'
  | 'TRANSPORT'
  | 'ESSENCE'
  | 'SHOPPING'
  | 'LOISIRS'
  | 'SANTE'
  | 'BEAUTE'
  | 'MAISON'
  | 'EDUCATION'
  | 'VOYAGE'
  // Épargne et investissement
  | 'EPARGNE'
  | 'INVESTISSEMENT'
  | 'VIREMENT_INTERNE'
  | 'TRANSFERT_OBJECTIF'
  | 'VERSEMENT_OBJECTIF'
  // Autres
  | 'RETRAIT_ESPECES'
  | 'FRAIS_BANCAIRE'
  | 'COMMISSION'
  | 'AUTRE'

export type PrioriteObjectif = 
  | 'CRITIQUE'
  | 'TRES_HAUTE'
  | 'HAUTE'
  | 'NORMALE'
  | 'BASSE'
  | 'TRES_BASSE'
  | 'SUSPENDU'

export type FrequenceCharge = 
  | 'MENSUELLE'
  | 'BIMESTRIELLE'
  | 'TRIMESTRIELLE'
  | 'SEMESTRIELLE'
  | 'ANNUELLE'

// ===============================================
// ENTITÉS PRINCIPALES (Schemas OpenAPI)
// ===============================================

/**
 * User - Schema complet avec configuration budget intégrée
 */
export interface User {
  id: string
  nom: string
  prenom: string
  jourPaie: number
  salaireMensuelNet: number
  decouvertAutorise?: number
  objectifCompteCourant?: number
  // Configuration budget intégrée dans User
  pourcentageChargesFixes?: number
  pourcentageDepensesVariables?: number
  pourcentageEpargne?: number
  createdAt: string
  updatedAt: string
}

/**
 * Banque
 */
export interface Banque {
  id: string
  nom: string
  couleurTheme: string
  logoUrl?: string
  actif: boolean
}

/**
 * Compte
 */
export interface Compte {
  id: string
  user?: User
  banque: Banque
  nom: string
  type: TypeCompte
  soldeTotal: number
  taux?: number
  plafond?: number
  dateOuverture: string
  actif: boolean
  // Champs calculés côté frontend
  argentLibre?: number
}

/**
 * Objectif
 */
export interface Objectif {
  id: string
  user?: User
  nom: string
  montantCible: number
  couleur: string
  icone?: string
  description?: string
  priorite: PrioriteObjectif
  type: TypeObjectif
  actif: boolean
  // Champs calculés
  montantActuel?: number
  pourcentageProgression?: number
  repartitions?: ObjectifRepartition[]
}

/**
 * ObjectifRepartition
 */
export interface ObjectifRepartition {
  id: string
  objectif?: Objectif
  compte: Compte
  montantActuel: number
  pourcentageCible?: number
  ordre: number
}

/**
 * ChargeFixe
 */
export interface ChargeFixe {
  id: string
  userId?: string
  compte: Compte
  nom: string
  description?: string
  montant: number
  categorie: TypeTransaction
  jourPrelevement: number
  frequence: FrequenceCharge
  dateDebut: string
  dateFin?: string
  actif: boolean
  createdAt?: string
  updatedAt?: string
}

/**
 * Transaction (à venir)
 */
export interface Transaction {
  id: string
  user?: User
  compte: Compte
  objectif?: Objectif
  montant: number
  description: string
  type: TypeTransaction
  dateTransaction: string
  createdAt: string
}

// ===============================================
// REQUEST BODIES (CreateXxxRequest)
// ===============================================

/**
 * CreateCompteRequest
 */
export interface CreateCompteRequest {
  banqueId: string
  nom: string
  type: TypeCompte
  soldeTotal: number
  taux?: number
  plafond?: number
  dateOuverture?: string
  principalChargesFixes?: boolean
}

/**
 * UpdateCompteRequest - différent de Create
 */
export interface UpdateCompteRequest {
  nom?: string
  soldeTotal?: number
  taux?: number
  plafond?: number
  principalChargesFixes?: boolean
}

/**
 * CreateObjectifRequest
 */
export interface CreateObjectifRequest {
  nom: string
  montantCible: number
  priorite: PrioriteObjectif
  type: TypeObjectif
  couleur: string
  icone?: string
  description?: string
  repartitions?: InitialRepartition[]
}

/**
 * InitialRepartition for objectif creation
 */
export interface InitialRepartition {
  accountId: string  // UUID of the account
  montant: number
}

/**
 * UpdateObjectifRequest - différent de Create
 */
export interface UpdateObjectifRequest {
  nom?: string
  montantCible?: number
  priorite?: PrioriteObjectif
  couleur?: string
  icone?: string
  description?: string
}

/**
 * CreateChargeFixeRequest
 */
export interface CreateChargeFixeRequest {
  compteId: string
  nom: string
  description?: string
  montant: number
  categorie: TypeTransaction
  jourPrelevement: number
  frequence: FrequenceCharge
  dateDebut: string
  dateFin?: string
}

/**
 * BudgetConfigRequest
 */
export interface BudgetConfigRequest {
  pourcentageChargesFixes: number
  pourcentageDepensesVariables: number
  pourcentageEpargne: number
}

/**
 * ValidationSalaireRequest
 * Pour valider le salaire mensuel ou autres revenus
 */
export interface ValidationSalaireRequest {
  mois: string                    // Format: "2025-01" ou "Janvier 2025"
  montant?: number                // Optionnel pour SALAIRE, requis pour PRIME/FREELANCE
  dateReception?: string          // Format: "YYYY-MM-DD", optionnel
  compteId?: string               // UUID, optionnel (utilise compte principal par défaut)
  type?: TypeTransaction          // SALAIRE, PRIME, FREELANCE (défaut: SALAIRE)
  description?: string            // Optionnel, auto-généré si non fourni
}

/**
 * ValidationSalaireResponse
 */
export interface ValidationSalaireResponse {
  transactionId: string
  message: string
  nouveauSolde: number
}

/**
 * CreateCompteTransfertRequest
 * Pour créer un transfert entre deux comptes
 */
export interface CreateCompteTransfertRequest {
  compteSourceId: string
  compteDestinationId: string
  montant: number
  description?: string
  dateTransfert?: string          // Format: "YYYY-MM-DD", optionnel
}

/**
 * CreateTransfertObjectifRequest
 * Pour créer un transfert entre deux objectifs
 */
export interface CreateTransfertObjectifRequest {
  objectifSourceId: string
  objectifDestinationId: string
  compteSourceId: string
  compteDestinationId: string
  montant: number
  motif?: string
}

/**
 * CreateTransactionRequest
 * Pour créer une transaction (dépense, revenu, versement objectif)
 */
export interface CreateTransactionRequest {
  compteId: string
  objectifId?: string             // Optionnel, pour lier à un objectif
  montant: number
  description: string
  type: TypeTransaction
  dateTransaction?: string        // Format: "YYYY-MM-DD", optionnel
}

/**
 * CreateVersementObjectifRequest
 * Pour effectuer un versement vers un objectif
 */
export interface CreateVersementObjectifRequest {
  objectifId: string
  compteId: string
  montant: number
  description?: string
  dateVersement?: string          // Format: "YYYY-MM-DD", optionnel
}

// ===============================================
// RESPONSES
// ===============================================

/**
 * SimpleDashboardData - ce que retourne GET /api/dashboard/{mois}
 */
export interface SimpleDashboardData {
  mois: string
  user: User
  comptes: Compte[]
  objectifs: Objectif[]
  salaireValide?: boolean  // Indique si le salaire a été validé pour ce mois
  chargesFixes?: ChargeFixe[]
  transactions?: Transaction[]
  timestamp: string
}

/**
 * DashboardData - Extended dashboard data with all required fields
 * (alias for SimpleDashboardData with guaranteed chargesFixes and transactions)
 */
export type DashboardData = SimpleDashboardData & {
  chargesFixes: ChargeFixe[]
  transactions: Transaction[]
}

/**
 * TestConnectionResponse - ce que retourne GET /api/dashboard/test
 */
export interface TestConnectionResponse {
  message: string
  timestamp: string
  version: string
  status: string
}

/**
 * ErrorResponse
 */
export interface ErrorResponse {
  message: string
  error?: string
  details?: Record<string, string>
  status?: number
}

/**
 * BudgetConfig (entité complète si elle existe côté backend)
 */
export interface BudgetConfig {
  id: string
  userId: string
  pourcentageChargesFixes: number
  pourcentageDepensesVariables: number
  pourcentageEpargne: number
  createdAt?: string
  updatedAt?: string
}

// ===============================================
// TYPES POUR FORMULAIRES
// ===============================================

/**
 * UserFormData - Pour les formulaires de création/modification user
 */
export interface UserFormData {
  nom: string
  prenom: string
  jourPaie: number
  salaireMensuelNet: number
  decouvertAutorise?: number
  objectifCompteCourant?: number
  pourcentageChargesFixes?: number
  pourcentageDepensesVariables?: number
  pourcentageEpargne?: number
}

// ===============================================
// TYPES UTILITAIRES
// ===============================================

/**
 * SelectOption - Pour les sélecteurs
 */
export interface SelectOption {
  value: string
  label: string
}

/**
 * Badge - Pour les statuts visuels
 */
export interface Badge {
  text: string
  class: 'success' | 'warning' | 'danger' | 'info'
}

/**
 * ApiError - Gestion des erreurs
 */
export interface ApiError {
  error: string
  message?: string
  details?: Record<string, string>
}

// ===============================================
// TYPES POUR DASHBOARD (calculés côté frontend)
// ===============================================

export interface JaugeData {
  titre: string
  valeur: number
  maximum: number
  pourcentage: number
  couleur: string
  description?: string
  badge?: Badge
}

export interface Periode {
  mois: string
  libelle: string
  dateDebut: string
  dateFin: string
  joursCycle: number
  positionCycle: string
}

// ===============================================
// TYPES POUR ONBOARDING
// ===============================================

export interface TempAccount {
  tempId: string
  banque: Banque
  nom: string
  type: TypeCompte
  soldeTotal: number
  taux?: number
}

export interface TempObjectif {
  nom: string
  montantCible: number
  type: TypeObjectif
  priorite: PrioriteObjectif
  icone?: string
  couleur: string
  description?: string
}

// ===============================================
// CONSTANTES UTILES
// ===============================================

export const COULEURS_PREDEFINIES = [
  '#F44336', '#E91E63', '#9C27B0', '#673AB7',
  '#3F51B5', '#2196F3', '#03A9F4', '#00BCD4',
  '#009688', '#4CAF50', '#8BC34A', '#CDDC39',
  '#FFEB3B', '#FFC107', '#FF9800', '#FF5722',
  '#795548', '#9E9E9E', '#607D8B'
] as const

export const ICONES_PREDEFINIES = [
  '🎯', '💰', '🏠', '🚗', '✈️',
  '🎓', '💍', '👶', '🏥', '💻',
  '🎮', '🎸', '📚', '🏖️', '🍔',
  '👕', '⚽', '🎭', '🎨', '📱',
  '🛡️', '💎', '🔑', '🎁', '⭐'
] as const

// ===============================================
// LABELS FRANÇAIS
// ===============================================

export const LABELS_TYPE_COMPTE: Record<TypeCompte, string> = {
  'COMPTE_COURANT': 'Compte Courant',
  'LIVRET_A': 'Livret A',
  'LDDS': 'LDDS',
  'LIVRET_JEUNE': 'Livret Jeune',
  'LEP': 'LEP',
  'PEL': 'PEL',
  'CEL': 'CEL',
  'CSL': 'CSL',
  'PEA': 'PEA',
  'PEA_PME': 'PEA-PME',
  'ASSURANCE_VIE': 'Assurance Vie',
  'COMPTE_TITRE': 'Compte Titre',
  'CRYPTO': 'Crypto',
  'OR_METAUX': 'Or & Métaux',
  'AUTRE': 'Autre'
}

export const LABELS_TYPE_OBJECTIF: Record<TypeObjectif, string> = {
  'SECURITE': 'Épargne de Sécurité',
  'COURT_TERME': 'Court Terme',
  'MOYEN_TERME': 'Moyen Terme',
  'LONG_TERME': 'Long Terme',
  'PLAISIR': 'Plaisir',
  'FAMILLE': 'Famille',
  'FORMATION': 'Formation',
  'INVESTISSEMENT': 'Investissement',
  'PROJET_IMMOBILIER': 'Projet Immobilier',
  'TRANSPORT': 'Transport',
  'SANTE': 'Santé',
  'TECHNOLOGIE': 'Technologie',
  'OPPORTUNITE': 'Opportunité',
  'DIVERS': 'Divers'
}

export const LABELS_PRIORITE: Record<PrioriteObjectif, string> = {
  'CRITIQUE': 'Critique',
  'TRES_HAUTE': 'Très Haute',
  'HAUTE': 'Haute',
  'NORMALE': 'Normale',
  'BASSE': 'Basse',
  'TRES_BASSE': 'Très Basse',
  'SUSPENDU': 'Suspendu'
}

export const LABELS_FREQUENCE: Record<FrequenceCharge, string> = {
  'MENSUELLE': 'Mensuelle',
  'BIMESTRIELLE': 'Bimestrielle',
  'TRIMESTRIELLE': 'Trimestrielle',
  'SEMESTRIELLE': 'Semestrielle',
  'ANNUELLE': 'Annuelle'
}

// ===============================================
// TYPE GUARDS
// ===============================================

export function isUser(obj: any): obj is User {
  return obj && typeof obj.id === 'string' && typeof obj.nom === 'string'
}

export function isCompte(obj: any): obj is Compte {
  return obj && typeof obj.id === 'string' && typeof obj.soldeTotal === 'number'
}

export function isObjectif(obj: any): obj is Objectif {
  return obj && typeof obj.id === 'string' && typeof obj.montantCible === 'number'
}

export function isBanque(obj: any): obj is Banque {
  return obj && typeof obj.id === 'string' && typeof obj.nom === 'string'
}