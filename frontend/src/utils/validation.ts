import { z } from 'zod'
import type {
  User,
  Banque,
  Compte,
  Objectif,
  ChargeFixe,
  SimpleDashboardData,
  BudgetConfig
} from '@/types'

// ===============================================
// VALIDATION SCHEMAS
// ===============================================

/**
 * User Schema
 */
// Lenient UUID pattern for test data compatibility
const uuidPattern = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/

export const UserSchema = z.object({
  id: z.string().regex(uuidPattern, 'Invalid UUID format'),
  nom: z.string().min(1),
  prenom: z.string().min(1),
  jourPaie: z.number().int().min(1).max(31),
  salaireMensuelNet: z.number().positive(),
  decouvertAutorise: z.number().nullable().optional(),
  objectifCompteCourant: z.number().positive().nullable().optional(),
  pourcentageChargesFixes: z.number().min(0).max(100).optional(),
  pourcentageDepensesVariables: z.number().min(0).max(100).optional(),
  pourcentageEpargne: z.number().min(0).max(100).optional(),
  createdAt: z.string().nullable().optional(),
  updatedAt: z.string().nullable().optional()
})

/**
 * Banque Schema
 */
export const BanqueSchema = z.object({
  id: z.string().regex(uuidPattern, 'Invalid UUID format'),
  nom: z.string().min(1),
  couleurTheme: z.string(),
  logoUrl: z.string().nullable().optional(),
  actif: z.boolean()
})

/**
 * Compte Schema
 */
export const CompteSchema = z.object({
  id: z.string().regex(uuidPattern, 'Invalid UUID format'),
  user: UserSchema.optional(),
  banque: BanqueSchema,
  nom: z.string().min(1),
  type: z.string(),
  soldeTotal: z.number(),
  taux: z.number().nullable().optional(),
  plafond: z.number().nullable().optional(),
  dateOuverture: z.string().nullable().optional(),
  actif: z.boolean(),
  principalChargesFixes: z.boolean().optional(),
  argentLibre: z.number().nullable().optional()
})

/**
 * ObjectifRepartition Schema
 */
export const ObjectifRepartitionSchema = z.object({
  id: z.string().regex(uuidPattern, 'Invalid UUID format'),
  objectif: z.any().optional(), // Avoid circular reference
  compte: CompteSchema,
  montantActuel: z.number(),
  pourcentageCible: z.number().nullable().optional(),
  ordre: z.number().int()
})

/**
 * Objectif Schema
 */
export const ObjectifSchema = z.object({
  id: z.string().regex(uuidPattern, 'Invalid UUID format'),
  user: UserSchema.optional(),
  nom: z.string().min(1),
  montantCible: z.number().positive(),
  couleur: z.string(),
  icone: z.string().nullable().optional(),
  description: z.string().nullable().optional(),
  priorite: z.string(),
  type: z.string(),
  actif: z.boolean(),
  montantActuel: z.number().nullable().optional(),
  pourcentageProgression: z.number().nullable().optional(),
  repartitions: z.array(ObjectifRepartitionSchema).nullable().optional()
})

/**
 * ChargeFixe Schema
 */
export const ChargeFixeSchema = z.object({
  id: z.string().regex(uuidPattern, 'Invalid UUID format'),
  userId: z.string().regex(uuidPattern, 'Invalid UUID format').optional(),
  compte: CompteSchema,
  nom: z.string().min(1),
  description: z.string().optional(),
  montant: z.number(),
  categorie: z.string(),
  jourPrelevement: z.number().int().min(1).max(31),
  frequence: z.string(),
  dateDebut: z.string(),
  dateFin: z.string().optional(),
  actif: z.boolean(),
  createdAt: z.string().nullable().optional(),
  updatedAt: z.string().nullable().optional()
})

/**
 * BudgetConfig Schema
 */
export const BudgetConfigSchema = z.object({
  id: z.string().regex(uuidPattern, 'Invalid UUID format'),
  userId: z.string().regex(uuidPattern, 'Invalid UUID format'),
  pourcentageChargesFixes: z.number().min(0).max(100),
  pourcentageDepensesVariables: z.number().min(0).max(100),
  pourcentageEpargne: z.number().min(0).max(100),
  createdAt: z.string().nullable().optional(),
  updatedAt: z.string().nullable().optional()
})

/**
 * SimpleDashboardData Schema
 */
export const SimpleDashboardDataSchema = z.object({
  mois: z.string(),
  user: UserSchema,
  comptes: z.array(CompteSchema),
  objectifs: z.array(ObjectifSchema),
  salaireValide: z.boolean().optional(),
  chargesFixes: z.array(ChargeFixeSchema).optional(),
  transactions: z.array(z.any()).optional(),
  timestamp: z.string()
})

// ===============================================
// VALIDATION HELPERS
// ===============================================

/**
 * Validate and parse data with a schema
 */
export function validateData<T>(schema: z.ZodSchema<T>, data: unknown): T {
  return schema.parse(data)
}

/**
 * Safely validate data, returning null on error
 */
export function safeValidateData<T>(schema: z.ZodSchema<T>, data: unknown): T | null {
  const result = schema.safeParse(data)
  return result.success ? result.data : null
}

/**
 * Type guards with runtime validation
 */
export function isValidUser(data: unknown): data is User {
  return UserSchema.safeParse(data).success
}

export function isValidBanque(data: unknown): data is Banque {
  return BanqueSchema.safeParse(data).success
}

export function isValidCompte(data: unknown): data is Compte {
  return CompteSchema.safeParse(data).success
}

export function isValidObjectif(data: unknown): data is Objectif {
  return ObjectifSchema.safeParse(data).success
}

export function isValidChargeFixe(data: unknown): data is ChargeFixe {
  return ChargeFixeSchema.safeParse(data).success
}

export function isValidDashboardData(data: unknown): data is SimpleDashboardData {
  return SimpleDashboardDataSchema.safeParse(data).success
}

export function isValidBudgetConfig(data: unknown): data is BudgetConfig {
  return BudgetConfigSchema.safeParse(data).success
}
