import { z } from 'zod'

// ===============================================
// FORM VALIDATION SCHEMAS
// These schemas validate user input in forms
// ===============================================

/**
 * User Profile Form Validation
 */
export const UserProfileFormSchema = z.object({
  nom: z.string()
    .min(2, 'Le nom doit contenir au moins 2 caractères')
    .max(50, 'Le nom ne peut pas dépasser 50 caractères')
    .regex(/^[a-zA-ZÀ-ÿ\s'-]+$/, 'Le nom ne peut contenir que des lettres'),

  prenom: z.string()
    .min(2, 'Le prénom doit contenir au moins 2 caractères')
    .max(50, 'Le prénom ne peut pas dépasser 50 caractères')
    .regex(/^[a-zA-ZÀ-ÿ\s'-]+$/, 'Le prénom ne peut contenir que des lettres'),

  jourPaie: z.number()
    .int('Le jour de paie doit être un nombre entier')
    .min(1, 'Le jour de paie doit être entre 1 et 31')
    .max(31, 'Le jour de paie doit être entre 1 et 31'),

  salaireMensuelNet: z.number()
    .positive('Le salaire doit être positif')
    .min(1, 'Le salaire doit être supérieur à 0')
    .max(1000000, 'Le salaire ne peut pas dépasser 1 000 000€'),

  decouvertAutorise: z.number()
    .min(0, 'Le découvert autorisé ne peut pas être négatif')
    .max(50000, 'Le découvert autorisé ne peut pas dépasser 50 000€')
    .optional()
    .nullable(),

  objectifCompteCourant: z.number()
    .positive('L\'objectif compte courant doit être positif')
    .min(0, 'L\'objectif compte courant ne peut pas être négatif')
    .max(100000, 'L\'objectif compte courant ne peut pas dépasser 100 000€')
    .optional()
    .nullable(),

  pourcentageChargesFixes: z.number()
    .min(0, 'Le pourcentage doit être entre 0 et 100')
    .max(100, 'Le pourcentage doit être entre 0 et 100')
    .optional(),

  pourcentageDepensesVariables: z.number()
    .min(0, 'Le pourcentage doit être entre 0 et 100')
    .max(100, 'Le pourcentage doit être entre 0 et 100')
    .optional(),

  pourcentageEpargne: z.number()
    .min(0, 'Le pourcentage doit être entre 0 et 100')
    .max(100, 'Le pourcentage doit être entre 0 et 100')
    .optional()
}).refine(
  (data) => {
    // If budget percentages are provided, they must sum to 100
    if (data.pourcentageChargesFixes !== undefined &&
        data.pourcentageDepensesVariables !== undefined &&
        data.pourcentageEpargne !== undefined) {
      const total = data.pourcentageChargesFixes + data.pourcentageDepensesVariables + data.pourcentageEpargne
      return total === 100
    }
    return true
  },
  {
    message: 'Les pourcentages de budget doivent totaliser 100%',
    path: ['pourcentageChargesFixes']
  }
)

/**
 * Compte Creation Form Validation
 */
export const CreateCompteFormSchema = z.object({
  banqueId: z.string()
    .uuid('L\'ID de la banque est invalide')
    .min(1, 'Veuillez sélectionner une banque'),

  nom: z.string()
    .min(2, 'Le nom du compte doit contenir au moins 2 caractères')
    .max(100, 'Le nom du compte ne peut pas dépasser 100 caractères'),

  type: z.enum([
    'COMPTE_COURANT', 'LIVRET_A', 'LDDS', 'LIVRET_JEUNE', 'LEP',
    'PEL', 'CEL', 'CSL', 'PEA', 'PEA_PME', 'ASSURANCE_VIE',
    'COMPTE_TITRE', 'CRYPTO', 'OR_METAUX', 'AUTRE'
  ], {
    message: 'Type de compte invalide'
  }),

  soldeTotal: z.number()
    .min(-1000000, 'Le solde ne peut pas être inférieur à -1 000 000€')
    .max(10000000, 'Le solde ne peut pas dépasser 10 000 000€'),

  taux: z.number()
    .min(0, 'Le taux ne peut pas être négatif')
    .max(100, 'Le taux ne peut pas dépasser 100%')
    .optional()
    .nullable(),

  plafond: z.number()
    .positive('Le plafond doit être positif')
    .max(10000000, 'Le plafond ne peut pas dépasser 10 000 000€')
    .optional()
    .nullable(),

  dateOuverture: z.string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Format de date invalide (YYYY-MM-DD)')
    .optional()
    .nullable(),

  principalChargesFixes: z.boolean().optional()
})

/**
 * Compte Update Form Validation
 */
export const UpdateCompteFormSchema = z.object({
  nom: z.string()
    .min(2, 'Le nom du compte doit contenir au moins 2 caractères')
    .max(100, 'Le nom du compte ne peut pas dépasser 100 caractères')
    .optional(),

  soldeTotal: z.number()
    .min(-1000000, 'Le solde ne peut pas être inférieur à -1 000 000€')
    .max(10000000, 'Le solde ne peut pas dépasser 10 000 000€')
    .optional(),

  taux: z.number()
    .min(0, 'Le taux ne peut pas être négatif')
    .max(100, 'Le taux ne peut pas dépasser 100%')
    .optional()
    .nullable(),

  plafond: z.number()
    .positive('Le plafond doit être positif')
    .max(10000000, 'Le plafond ne peut pas dépasser 10 000 000€')
    .optional()
    .nullable(),

  principalChargesFixes: z.boolean().optional()
})

/**
 * Objectif Creation Form Validation
 */
export const CreateObjectifFormSchema = z.object({
  nom: z.string()
    .min(2, 'Le nom de l\'objectif doit contenir au moins 2 caractères')
    .max(100, 'Le nom de l\'objectif ne peut pas dépasser 100 caractères'),

  montantCible: z.number()
    .positive('Le montant cible doit être positif')
    .min(1, 'Le montant cible doit être supérieur à 0')
    .max(100000000, 'Le montant cible ne peut pas dépasser 100 000 000€'),

  priorite: z.enum([
    'CRITIQUE', 'TRES_HAUTE', 'HAUTE', 'NORMALE', 'BASSE', 'TRES_BASSE', 'SUSPENDU'
  ], {
    message: 'Priorité invalide'
  }),

  type: z.enum([
    'SECURITE', 'COURT_TERME', 'MOYEN_TERME', 'LONG_TERME', 'PLAISIR',
    'FAMILLE', 'FORMATION', 'INVESTISSEMENT', 'PROJET_IMMOBILIER',
    'TRANSPORT', 'SANTE', 'TECHNOLOGIE', 'OPPORTUNITE', 'DIVERS'
  ], {
    message: 'Type d\'objectif invalide'
  }),

  couleur: z.string()
    .regex(/^#[0-9A-Fa-f]{6}$/, 'Format de couleur invalide (ex: #FF5722)'),

  icone: z.string()
    .max(10, 'L\'icône ne peut pas dépasser 10 caractères')
    .optional()
    .nullable(),

  description: z.string()
    .max(500, 'La description ne peut pas dépasser 500 caractères')
    .optional()
    .nullable(),

  repartitions: z.array(z.object({
    accountId: z.string().uuid('ID de compte invalide'),
    montant: z.number().positive('Le montant doit être positif')
  })).optional()
})

/**
 * Objectif Update Form Validation
 */
export const UpdateObjectifFormSchema = z.object({
  nom: z.string()
    .min(2, 'Le nom de l\'objectif doit contenir au moins 2 caractères')
    .max(100, 'Le nom de l\'objectif ne peut pas dépasser 100 caractères')
    .optional(),

  montantCible: z.number()
    .positive('Le montant cible doit être positif')
    .min(1, 'Le montant cible doit être supérieur à 0')
    .max(100000000, 'Le montant cible ne peut pas dépasser 100 000 000€')
    .optional(),

  priorite: z.enum([
    'CRITIQUE', 'TRES_HAUTE', 'HAUTE', 'NORMALE', 'BASSE', 'TRES_BASSE', 'SUSPENDU'
  ]).optional(),

  couleur: z.string()
    .regex(/^#[0-9A-Fa-f]{6}$/, 'Format de couleur invalide (ex: #FF5722)')
    .optional(),

  icone: z.string()
    .max(10, 'L\'icône ne peut pas dépasser 10 caractères')
    .optional()
    .nullable(),

  description: z.string()
    .max(500, 'La description ne peut pas dépasser 500 caractères')
    .optional()
    .nullable()
})

/**
 * Charge Fixe Form Validation
 */
export const CreateChargeFixeFormSchema = z.object({
  compteId: z.string()
    .uuid('L\'ID du compte est invalide')
    .min(1, 'Veuillez sélectionner un compte'),

  nom: z.string()
    .min(2, 'Le nom de la charge doit contenir au moins 2 caractères')
    .max(100, 'Le nom de la charge ne peut pas dépasser 100 caractères'),

  description: z.string()
    .max(500, 'La description ne peut pas dépasser 500 caractères')
    .optional()
    .nullable(),

  montant: z.number()
    .positive('Le montant doit être positif')
    .min(0.01, 'Le montant doit être supérieur à 0')
    .max(1000000, 'Le montant ne peut pas dépasser 1 000 000€'),

  categorie: z.enum([
    'LOYER', 'ASSURANCE', 'ABONNEMENT', 'CREDIT_IMMOBILIER',
    'CREDIT_CONSO', 'IMPOTS', 'MUTUELLE'
  ], {
    message: 'Catégorie invalide'
  }),

  jourPrelevement: z.number()
    .int('Le jour de prélèvement doit être un nombre entier')
    .min(1, 'Le jour de prélèvement doit être entre 1 et 31')
    .max(31, 'Le jour de prélèvement doit être entre 1 et 31'),

  frequence: z.enum([
    'MENSUELLE', 'BIMESTRIELLE', 'TRIMESTRIELLE', 'SEMESTRIELLE', 'ANNUELLE'
  ], {
    message: 'Fréquence invalide'
  }),

  dateDebut: z.string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Format de date invalide (YYYY-MM-DD)'),

  dateFin: z.string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Format de date invalide (YYYY-MM-DD)')
    .optional()
    .nullable()
}).refine(
  (data) => {
    // If dateFin is provided, it must be after dateDebut
    if (data.dateFin) {
      return new Date(data.dateFin) > new Date(data.dateDebut)
    }
    return true
  },
  {
    message: 'La date de fin doit être après la date de début',
    path: ['dateFin']
  }
)

/**
 * Transaction Form Validation
 */
export const CreateTransactionFormSchema = z.object({
  compteId: z.string()
    .uuid('L\'ID du compte est invalide')
    .min(1, 'Veuillez sélectionner un compte'),

  objectifId: z.string()
    .uuid('L\'ID de l\'objectif est invalide')
    .optional()
    .nullable(),

  montant: z.number()
    .min(0.01, 'Le montant doit être supérieur à 0')
    .max(10000000, 'Le montant ne peut pas dépasser 10 000 000€'),

  description: z.string()
    .min(1, 'La description est requise')
    .max(500, 'La description ne peut pas dépasser 500 caractères'),

  type: z.enum([
    // Revenus
    'SALAIRE', 'PRIME', 'FREELANCE', 'ALLOCATION', 'REMBOURSEMENT',
    'GAIN_INVESTISSEMENT', 'CADEAU_RECU', 'VENTE',
    // Charges fixes
    'LOYER', 'ASSURANCE', 'ABONNEMENT', 'CREDIT_IMMOBILIER',
    'CREDIT_CONSO', 'IMPOTS', 'MUTUELLE',
    // Dépenses variables
    'ALIMENTATION', 'RESTAURANT', 'TRANSPORT', 'ESSENCE',
    'SHOPPING', 'LOISIRS', 'SANTE', 'BEAUTE', 'MAISON',
    'EDUCATION', 'VOYAGE',
    // Épargne et investissement
    'EPARGNE', 'INVESTISSEMENT', 'VIREMENT_INTERNE',
    'TRANSFERT_OBJECTIF', 'VERSEMENT_OBJECTIF',
    // Autres
    'RETRAIT_ESPECES', 'FRAIS_BANCAIRE', 'COMMISSION', 'AUTRE'
  ], {
    message: 'Type de transaction invalide'
  }),

  dateTransaction: z.string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Format de date invalide (YYYY-MM-DD)')
    .optional()
    .nullable()
})

/**
 * Budget Config Form Validation
 */
export const BudgetConfigFormSchema = z.object({
  pourcentageChargesFixes: z.number()
    .min(0, 'Le pourcentage doit être entre 0 et 100')
    .max(100, 'Le pourcentage doit être entre 0 et 100'),

  pourcentageDepensesVariables: z.number()
    .min(0, 'Le pourcentage doit être entre 0 et 100')
    .max(100, 'Le pourcentage doit être entre 0 et 100'),

  pourcentageEpargne: z.number()
    .min(0, 'Le pourcentage doit être entre 0 et 100')
    .max(100, 'Le pourcentage doit être entre 0 et 100')
}).refine(
  (data) => {
    const total = data.pourcentageChargesFixes +
                  data.pourcentageDepensesVariables +
                  data.pourcentageEpargne
    return total === 100
  },
  {
    message: 'Les pourcentages doivent totaliser 100%',
    path: ['pourcentageChargesFixes']
  }
)

/**
 * Salary Validation Form
 */
export const ValidationSalaireFormSchema = z.object({
  mois: z.string()
    .regex(/^\d{4}-\d{2}$/, 'Format de mois invalide (YYYY-MM)'),

  montant: z.number()
    .positive('Le montant doit être positif')
    .optional(),

  dateReception: z.string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Format de date invalide (YYYY-MM-DD)')
    .optional(),

  compteId: z.string()
    .uuid('L\'ID du compte est invalide')
    .optional(),

  type: z.enum(['SALAIRE', 'PRIME', 'FREELANCE']).optional(),

  description: z.string()
    .max(500, 'La description ne peut pas dépasser 500 caractères')
    .optional()
})

/**
 * Transfert Between Accounts Form Validation
 */
export const CreateCompteTransfertFormSchema = z.object({
  compteSourceId: z.string()
    .uuid('L\'ID du compte source est invalide')
    .min(1, 'Veuillez sélectionner un compte source'),

  compteDestinationId: z.string()
    .uuid('L\'ID du compte destination est invalide')
    .min(1, 'Veuillez sélectionner un compte destination'),

  montant: z.number()
    .positive('Le montant doit être positif')
    .min(0.01, 'Le montant doit être supérieur à 0')
    .max(10000000, 'Le montant ne peut pas dépasser 10 000 000€'),

  description: z.string()
    .max(500, 'La description ne peut pas dépasser 500 caractères')
    .optional(),

  dateTransfert: z.string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Format de date invalide (YYYY-MM-DD)')
    .optional()
}).refine(
  (data) => data.compteSourceId !== data.compteDestinationId,
  {
    message: 'Le compte source et destination doivent être différents',
    path: ['compteDestinationId']
  }
)

/**
 * Versement to Objectif Form Validation
 */
export const CreateVersementObjectifFormSchema = z.object({
  objectifId: z.string()
    .uuid('L\'ID de l\'objectif est invalide')
    .min(1, 'Veuillez sélectionner un objectif'),

  compteId: z.string()
    .uuid('L\'ID du compte est invalide')
    .min(1, 'Veuillez sélectionner un compte'),

  montant: z.number()
    .positive('Le montant doit être positif')
    .min(0.01, 'Le montant doit être supérieur à 0')
    .max(10000000, 'Le montant ne peut pas dépasser 10 000 000€'),

  description: z.string()
    .max(500, 'La description ne peut pas dépasser 500 caractères')
    .optional(),

  dateVersement: z.string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Format de date invalide (YYYY-MM-DD)')
    .optional()
})
