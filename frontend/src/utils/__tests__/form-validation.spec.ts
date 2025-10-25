import { describe, it, expect } from 'vitest'
import {
  UserProfileFormSchema,
  CreateCompteFormSchema,
  UpdateCompteFormSchema,
  CreateObjectifFormSchema,
  UpdateObjectifFormSchema,
  CreateChargeFixeFormSchema,
  CreateTransactionFormSchema,
  BudgetConfigFormSchema,
  ValidationSalaireFormSchema,
  CreateCompteTransfertFormSchema,
  CreateVersementObjectifFormSchema
} from '../form-validation'

describe('form validation schemas', () => {
  describe('UserProfileFormSchema', () => {
    const validProfile = {
      nom: 'Dupont',
      prenom: 'Jean',
      jourPaie: 15,
      salaireMensuelNet: 2500,
      decouvertAutorise: 500,
      objectifCompteCourant: 1000,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }

    it('should validate a valid user profile', () => {
      expect(() => UserProfileFormSchema.parse(validProfile)).not.toThrow()
    })

    it('should reject nom too short', () => {
      expect(() => UserProfileFormSchema.parse({ ...validProfile, nom: 'D' })).toThrow()
    })

    it('should reject nom too long', () => {
      expect(() => UserProfileFormSchema.parse({ ...validProfile, nom: 'a'.repeat(51) })).toThrow()
    })

    it('should reject nom with numbers', () => {
      expect(() => UserProfileFormSchema.parse({ ...validProfile, nom: 'Dupont123' })).toThrow()
    })

    it('should accept nom with accents and hyphens', () => {
      expect(() => UserProfileFormSchema.parse({ ...validProfile, nom: 'Dûpont-Martin' })).not.toThrow()
    })

    it('should reject jourPaie out of range', () => {
      expect(() => UserProfileFormSchema.parse({ ...validProfile, jourPaie: 0 })).toThrow()
      expect(() => UserProfileFormSchema.parse({ ...validProfile, jourPaie: 32 })).toThrow()
    })

    it('should reject negative salaire', () => {
      expect(() => UserProfileFormSchema.parse({ ...validProfile, salaireMensuelNet: -100 })).toThrow()
    })

    it('should reject salaire too high', () => {
      expect(() => UserProfileFormSchema.parse({ ...validProfile, salaireMensuelNet: 1000001 })).toThrow()
    })

    it('should reject percentages that do not sum to 100', () => {
      const invalidProfile = {
        ...validProfile,
        pourcentageChargesFixes: 50,
        pourcentageDepensesVariables: 30,
        pourcentageEpargne: 30
      }
      expect(() => UserProfileFormSchema.parse(invalidProfile)).toThrow()
    })

    it('should accept when percentages sum to 100', () => {
      const validPercentages = {
        ...validProfile,
        pourcentageChargesFixes: 40,
        pourcentageDepensesVariables: 40,
        pourcentageEpargne: 20
      }
      expect(() => UserProfileFormSchema.parse(validPercentages)).not.toThrow()
    })

    it('should accept when percentages are not provided', () => {
      const { pourcentageChargesFixes, pourcentageDepensesVariables, pourcentageEpargne, ...minimalProfile } = validProfile
      expect(() => UserProfileFormSchema.parse(minimalProfile)).not.toThrow()
    })
  })

  describe('CreateCompteFormSchema', () => {
    const validCompte = {
      banqueId: '123e4567-e89b-12d3-a456-426614174000',
      nom: 'Compte Courant',
      type: 'COMPTE_COURANT' as const,
      soldeTotal: 1000,
      taux: 1.5,
      plafond: 5000,
      dateOuverture: '2024-01-01',
      principalChargesFixes: true
    }

    it('should validate a valid compte', () => {
      expect(() => CreateCompteFormSchema.parse(validCompte)).not.toThrow()
    })

    it('should reject invalid banqueId UUID', () => {
      expect(() => CreateCompteFormSchema.parse({ ...validCompte, banqueId: 'invalid' })).toThrow()
    })

    it('should reject nom too short', () => {
      expect(() => CreateCompteFormSchema.parse({ ...validCompte, nom: 'A' })).toThrow()
    })

    it('should reject invalid compte type', () => {
      expect(() => CreateCompteFormSchema.parse({ ...validCompte, type: 'INVALID' })).toThrow()
    })

    it('should accept all valid compte types', () => {
      const types = [
        'COMPTE_COURANT', 'LIVRET_A', 'LDDS', 'LIVRET_JEUNE', 'LEP',
        'PEL', 'CEL', 'CSL', 'PEA', 'PEA_PME', 'ASSURANCE_VIE',
        'COMPTE_TITRE', 'CRYPTO', 'OR_METAUX', 'AUTRE'
      ]
      types.forEach(type => {
        expect(() => CreateCompteFormSchema.parse({ ...validCompte, type })).not.toThrow()
      })
    })

    it('should reject solde too low', () => {
      expect(() => CreateCompteFormSchema.parse({ ...validCompte, soldeTotal: -1000001 })).toThrow()
    })

    it('should reject solde too high', () => {
      expect(() => CreateCompteFormSchema.parse({ ...validCompte, soldeTotal: 10000001 })).toThrow()
    })

    it('should reject invalid date format', () => {
      expect(() => CreateCompteFormSchema.parse({ ...validCompte, dateOuverture: '01/01/2024' })).toThrow()
    })
  })

  describe('CreateObjectifFormSchema', () => {
    const validObjectif = {
      nom: 'Vacances',
      montantCible: 5000,
      priorite: 'HAUTE' as const,
      type: 'PLAISIR' as const,
      couleur: '#FF5722',
      icone: '✈️',
      description: 'Vacances d\'été'
    }

    it('should validate a valid objectif', () => {
      expect(() => CreateObjectifFormSchema.parse(validObjectif)).not.toThrow()
    })

    it('should reject nom too short', () => {
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, nom: 'V' })).toThrow()
    })

    it('should reject negative montantCible', () => {
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, montantCible: -100 })).toThrow()
    })

    it('should reject zero montantCible', () => {
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, montantCible: 0 })).toThrow()
    })

    it('should reject invalid color format', () => {
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, couleur: 'red' })).toThrow()
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, couleur: '#FF57' })).toThrow()
    })

    it('should accept valid hex colors', () => {
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, couleur: '#FF5722' })).not.toThrow()
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, couleur: '#abc123' })).not.toThrow()
    })

    it('should reject invalid priorite', () => {
      expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, priorite: 'INVALID' })).toThrow()
    })

    it('should accept all valid priorites', () => {
      const priorites = ['CRITIQUE', 'TRES_HAUTE', 'HAUTE', 'NORMALE', 'BASSE', 'TRES_BASSE', 'SUSPENDU']
      priorites.forEach(priorite => {
        expect(() => CreateObjectifFormSchema.parse({ ...validObjectif, priorite })).not.toThrow()
      })
    })
  })

  describe('CreateChargeFixeFormSchema', () => {
    const validChargeFixe = {
      compteId: '123e4567-e89b-12d3-a456-426614174000',
      nom: 'Loyer',
      description: 'Loyer mensuel',
      montant: 800,
      categorie: 'LOYER' as const,
      jourPrelevement: 5,
      frequence: 'MENSUELLE' as const,
      dateDebut: '2024-01-01',
      dateFin: '2024-12-31'
    }

    it('should validate a valid charge fixe', () => {
      expect(() => CreateChargeFixeFormSchema.parse(validChargeFixe)).not.toThrow()
    })

    it('should reject invalid compteId UUID', () => {
      expect(() => CreateChargeFixeFormSchema.parse({ ...validChargeFixe, compteId: 'invalid' })).toThrow()
    })

    it('should reject nom too short', () => {
      expect(() => CreateChargeFixeFormSchema.parse({ ...validChargeFixe, nom: 'L' })).toThrow()
    })

    it('should reject negative montant', () => {
      expect(() => CreateChargeFixeFormSchema.parse({ ...validChargeFixe, montant: -100 })).toThrow()
    })

    it('should reject jourPrelevement out of range', () => {
      expect(() => CreateChargeFixeFormSchema.parse({ ...validChargeFixe, jourPrelevement: 0 })).toThrow()
      expect(() => CreateChargeFixeFormSchema.parse({ ...validChargeFixe, jourPrelevement: 32 })).toThrow()
    })

    it('should reject dateFin before dateDebut', () => {
      const invalidDates = {
        ...validChargeFixe,
        dateDebut: '2024-12-31',
        dateFin: '2024-01-01'
      }
      expect(() => CreateChargeFixeFormSchema.parse(invalidDates)).toThrow()
    })

    it('should accept dateFin after dateDebut', () => {
      const validDates = {
        ...validChargeFixe,
        dateDebut: '2024-01-01',
        dateFin: '2024-12-31'
      }
      expect(() => CreateChargeFixeFormSchema.parse(validDates)).not.toThrow()
    })

    it('should accept without dateFin', () => {
      const { dateFin, ...chargeWithoutEnd } = validChargeFixe
      expect(() => CreateChargeFixeFormSchema.parse(chargeWithoutEnd)).not.toThrow()
    })
  })

  describe('CreateTransactionFormSchema', () => {
    const validTransaction = {
      compteId: '123e4567-e89b-12d3-a456-426614174000',
      objectifId: '123e4567-e89b-12d3-a456-426614174001',
      montant: 50,
      description: 'Courses alimentaires',
      type: 'ALIMENTATION' as const,
      dateTransaction: '2024-01-15'
    }

    it('should validate a valid transaction', () => {
      expect(() => CreateTransactionFormSchema.parse(validTransaction)).not.toThrow()
    })

    it('should reject invalid compteId UUID', () => {
      expect(() => CreateTransactionFormSchema.parse({ ...validTransaction, compteId: 'invalid' })).toThrow()
    })

    it('should reject montant too low', () => {
      expect(() => CreateTransactionFormSchema.parse({ ...validTransaction, montant: 0 })).toThrow()
    })

    it('should reject empty description', () => {
      expect(() => CreateTransactionFormSchema.parse({ ...validTransaction, description: '' })).toThrow()
    })

    it('should accept transaction without objectifId', () => {
      const { objectifId, ...transactionWithoutObjectif } = validTransaction
      expect(() => CreateTransactionFormSchema.parse(transactionWithoutObjectif)).not.toThrow()
    })
  })

  describe('BudgetConfigFormSchema', () => {
    const validBudgetConfig = {
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20
    }

    it('should validate when percentages sum to 100', () => {
      expect(() => BudgetConfigFormSchema.parse(validBudgetConfig)).not.toThrow()
    })

    it('should reject when percentages do not sum to 100', () => {
      const invalidConfig = {
        pourcentageChargesFixes: 50,
        pourcentageDepensesVariables: 30,
        pourcentageEpargne: 30
      }
      expect(() => BudgetConfigFormSchema.parse(invalidConfig)).toThrow()
    })

    it('should reject negative percentages', () => {
      const invalidConfig = {
        pourcentageChargesFixes: -10,
        pourcentageDepensesVariables: 60,
        pourcentageEpargne: 50
      }
      expect(() => BudgetConfigFormSchema.parse(invalidConfig)).toThrow()
    })

    it('should reject percentages over 100', () => {
      const invalidConfig = {
        pourcentageChargesFixes: 101,
        pourcentageDepensesVariables: 0,
        pourcentageEpargne: 0
      }
      expect(() => BudgetConfigFormSchema.parse(invalidConfig)).toThrow()
    })
  })

  describe('ValidationSalaireFormSchema', () => {
    const validSalaire = {
      mois: '2024-01',
      montant: 2500,
      dateReception: '2024-01-15',
      compteId: '123e4567-e89b-12d3-a456-426614174000',
      type: 'SALAIRE' as const,
      description: 'Salaire janvier'
    }

    it('should validate a valid salaire validation', () => {
      expect(() => ValidationSalaireFormSchema.parse(validSalaire)).not.toThrow()
    })

    it('should reject invalid mois format', () => {
      expect(() => ValidationSalaireFormSchema.parse({ ...validSalaire, mois: '2024/01' })).toThrow()
      expect(() => ValidationSalaireFormSchema.parse({ ...validSalaire, mois: '01-2024' })).toThrow()
    })

    it('should reject negative montant', () => {
      expect(() => ValidationSalaireFormSchema.parse({ ...validSalaire, montant: -100 })).toThrow()
    })

    it('should accept minimal salaire validation', () => {
      const minimal = { mois: '2024-01' }
      expect(() => ValidationSalaireFormSchema.parse(minimal)).not.toThrow()
    })
  })

  describe('CreateCompteTransfertFormSchema', () => {
    const validTransfert = {
      compteSourceId: '123e4567-e89b-12d3-a456-426614174000',
      compteDestinationId: '123e4567-e89b-12d3-a456-426614174001',
      montant: 500,
      description: 'Transfert interne',
      dateTransfert: '2024-01-15'
    }

    it('should validate a valid transfert', () => {
      expect(() => CreateCompteTransfertFormSchema.parse(validTransfert)).not.toThrow()
    })

    it('should reject same source and destination', () => {
      const sameAccount = {
        ...validTransfert,
        compteDestinationId: validTransfert.compteSourceId
      }
      expect(() => CreateCompteTransfertFormSchema.parse(sameAccount)).toThrow()
    })

    it('should reject invalid UUID for source', () => {
      expect(() => CreateCompteTransfertFormSchema.parse({ ...validTransfert, compteSourceId: 'invalid' })).toThrow()
    })

    it('should reject invalid UUID for destination', () => {
      expect(() => CreateCompteTransfertFormSchema.parse({ ...validTransfert, compteDestinationId: 'invalid' })).toThrow()
    })

    it('should reject zero montant', () => {
      expect(() => CreateCompteTransfertFormSchema.parse({ ...validTransfert, montant: 0 })).toThrow()
    })

    it('should reject negative montant', () => {
      expect(() => CreateCompteTransfertFormSchema.parse({ ...validTransfert, montant: -100 })).toThrow()
    })
  })

  describe('CreateVersementObjectifFormSchema', () => {
    const validVersement = {
      objectifId: '123e4567-e89b-12d3-a456-426614174000',
      compteId: '123e4567-e89b-12d3-a456-426614174001',
      montant: 200,
      description: 'Versement mensuel',
      dateVersement: '2024-01-15'
    }

    it('should validate a valid versement', () => {
      expect(() => CreateVersementObjectifFormSchema.parse(validVersement)).not.toThrow()
    })

    it('should reject invalid objectifId UUID', () => {
      expect(() => CreateVersementObjectifFormSchema.parse({ ...validVersement, objectifId: 'invalid' })).toThrow()
    })

    it('should reject invalid compteId UUID', () => {
      expect(() => CreateVersementObjectifFormSchema.parse({ ...validVersement, compteId: 'invalid' })).toThrow()
    })

    it('should reject zero montant', () => {
      expect(() => CreateVersementObjectifFormSchema.parse({ ...validVersement, montant: 0 })).toThrow()
    })

    it('should reject negative montant', () => {
      expect(() => CreateVersementObjectifFormSchema.parse({ ...validVersement, montant: -50 })).toThrow()
    })

    it('should accept versement without optional fields', () => {
      const minimal = {
        objectifId: validVersement.objectifId,
        compteId: validVersement.compteId,
        montant: validVersement.montant
      }
      expect(() => CreateVersementObjectifFormSchema.parse(minimal)).not.toThrow()
    })
  })
})
