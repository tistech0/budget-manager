import { describe, it, expect } from 'vitest'
import {
  UserSchema,
  BanqueSchema,
  CompteSchema,
  ObjectifSchema,
  ChargeFixeSchema,
  BudgetConfigSchema,
  SimpleDashboardDataSchema,
  validateData,
  safeValidateData,
  isValidUser,
  isValidBanque,
  isValidCompte,
  isValidObjectif,
  isValidChargeFixe,
  isValidDashboardData,
  isValidBudgetConfig
} from '../validation'

describe('validation schemas', () => {
  describe('UserSchema', () => {
    const validUser = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      nom: 'Dupont',
      prenom: 'Jean',
      jourPaie: 15,
      salaireMensuelNet: 2500,
      decouvertAutorise: 500,
      objectifCompteCourant: 1000,
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }

    it('should validate a valid user', () => {
      expect(() => UserSchema.parse(validUser)).not.toThrow()
    })

    it('should reject user with invalid UUID', () => {
      expect(() => UserSchema.parse({ ...validUser, id: 'invalid-uuid' })).toThrow()
    })

    it('should reject user with empty nom', () => {
      expect(() => UserSchema.parse({ ...validUser, nom: '' })).toThrow()
    })

    it('should reject user with empty prenom', () => {
      expect(() => UserSchema.parse({ ...validUser, prenom: '' })).toThrow()
    })

    it('should reject jourPaie less than 1', () => {
      expect(() => UserSchema.parse({ ...validUser, jourPaie: 0 })).toThrow()
    })

    it('should reject jourPaie greater than 31', () => {
      expect(() => UserSchema.parse({ ...validUser, jourPaie: 32 })).toThrow()
    })

    it('should reject negative salaireMensuelNet', () => {
      expect(() => UserSchema.parse({ ...validUser, salaireMensuelNet: -100 })).toThrow()
    })

    it('should reject invalid percentage values', () => {
      expect(() => UserSchema.parse({ ...validUser, pourcentageChargesFixes: -10 })).toThrow()
      expect(() => UserSchema.parse({ ...validUser, pourcentageChargesFixes: 101 })).toThrow()
    })

    it('should accept user with optional fields missing', () => {
      const minimalUser = {
        id: '123e4567-e89b-12d3-a456-426614174000',
        nom: 'Dupont',
        prenom: 'Jean',
        jourPaie: 15,
        salaireMensuelNet: 2500
      }
      expect(() => UserSchema.parse(minimalUser)).not.toThrow()
    })
  })

  describe('BanqueSchema', () => {
    const validBanque = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      nom: 'BNP Paribas',
      couleurTheme: '#00FF00',
      logoUrl: 'https://example.com/logo.png',
      actif: true
    }

    it('should validate a valid banque', () => {
      expect(() => BanqueSchema.parse(validBanque)).not.toThrow()
    })

    it('should reject banque with invalid UUID', () => {
      expect(() => BanqueSchema.parse({ ...validBanque, id: 'invalid' })).toThrow()
    })

    it('should reject banque with empty nom', () => {
      expect(() => BanqueSchema.parse({ ...validBanque, nom: '' })).toThrow()
    })

    it('should accept banque without logoUrl', () => {
      const { logoUrl, ...banqueWithoutLogo } = validBanque
      expect(() => BanqueSchema.parse(banqueWithoutLogo)).not.toThrow()
    })
  })

  describe('CompteSchema', () => {
    const validCompte = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      banque: {
        id: '123e4567-e89b-12d3-a456-426614174001',
        nom: 'BNP Paribas',
        couleurTheme: '#00FF00',
        actif: true
      },
      nom: 'Compte Courant',
      type: 'COMPTE_COURANT',
      soldeTotal: 1000,
      taux: 1.5,
      plafond: 5000,
      dateOuverture: '2024-01-01',
      actif: true,
      principalChargesFixes: true,
      argentLibre: 500
    }

    it('should validate a valid compte', () => {
      expect(() => CompteSchema.parse(validCompte)).not.toThrow()
    })

    it('should reject compte with invalid UUID', () => {
      expect(() => CompteSchema.parse({ ...validCompte, id: 'invalid' })).toThrow()
    })

    it('should reject compte with empty nom', () => {
      expect(() => CompteSchema.parse({ ...validCompte, nom: '' })).toThrow()
    })

    it('should accept compte with negative soldeTotal', () => {
      expect(() => CompteSchema.parse({ ...validCompte, soldeTotal: -500 })).not.toThrow()
    })

    it('should accept compte with optional fields as null', () => {
      expect(() => CompteSchema.parse({ ...validCompte, taux: null, plafond: null })).not.toThrow()
    })
  })

  describe('ObjectifSchema', () => {
    const validObjectif = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      nom: 'Vacances',
      montantCible: 5000,
      couleur: '#FF5722',
      icone: '✈️',
      description: 'Vacances d\'été',
      priorite: 'HAUTE',
      type: 'PLAISIR',
      actif: true,
      montantActuel: 1000,
      pourcentageProgression: 20,
      repartitions: []
    }

    it('should validate a valid objectif', () => {
      expect(() => ObjectifSchema.parse(validObjectif)).not.toThrow()
    })

    it('should reject objectif with invalid UUID', () => {
      expect(() => ObjectifSchema.parse({ ...validObjectif, id: 'invalid' })).toThrow()
    })

    it('should reject objectif with empty nom', () => {
      expect(() => ObjectifSchema.parse({ ...validObjectif, nom: '' })).toThrow()
    })

    it('should reject negative montantCible', () => {
      expect(() => ObjectifSchema.parse({ ...validObjectif, montantCible: -100 })).toThrow()
    })

    it('should reject zero montantCible', () => {
      expect(() => ObjectifSchema.parse({ ...validObjectif, montantCible: 0 })).toThrow()
    })
  })

  describe('ChargeFixeSchema', () => {
    const validChargeFixe = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      userId: '123e4567-e89b-12d3-a456-426614174001',
      compte: {
        id: '123e4567-e89b-12d3-a456-426614174002',
        banque: {
          id: '123e4567-e89b-12d3-a456-426614174003',
          nom: 'BNP Paribas',
          couleurTheme: '#00FF00',
          actif: true
        },
        nom: 'Compte Courant',
        type: 'COMPTE_COURANT',
        soldeTotal: 1000,
        actif: true
      },
      nom: 'Loyer',
      description: 'Loyer mensuel',
      montant: 800,
      categorie: 'LOYER',
      jourPrelevement: 5,
      frequence: 'MENSUELLE',
      dateDebut: '2024-01-01',
      dateFin: '2024-12-31',
      actif: true,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }

    it('should validate a valid charge fixe', () => {
      expect(() => ChargeFixeSchema.parse(validChargeFixe)).not.toThrow()
    })

    it('should reject charge fixe with invalid UUID', () => {
      expect(() => ChargeFixeSchema.parse({ ...validChargeFixe, id: 'invalid' })).toThrow()
    })

    it('should reject charge fixe with empty nom', () => {
      expect(() => ChargeFixeSchema.parse({ ...validChargeFixe, nom: '' })).toThrow()
    })

    it('should reject invalid jourPrelevement', () => {
      expect(() => ChargeFixeSchema.parse({ ...validChargeFixe, jourPrelevement: 0 })).toThrow()
      expect(() => ChargeFixeSchema.parse({ ...validChargeFixe, jourPrelevement: 32 })).toThrow()
    })
  })

  describe('BudgetConfigSchema', () => {
    const validBudgetConfig = {
      id: '123e4567-e89b-12d3-a456-426614174000',
      userId: '123e4567-e89b-12d3-a456-426614174001',
      pourcentageChargesFixes: 50,
      pourcentageDepensesVariables: 30,
      pourcentageEpargne: 20,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }

    it('should validate a valid budget config', () => {
      expect(() => BudgetConfigSchema.parse(validBudgetConfig)).not.toThrow()
    })

    it('should reject invalid percentages', () => {
      expect(() => BudgetConfigSchema.parse({ ...validBudgetConfig, pourcentageChargesFixes: -10 })).toThrow()
      expect(() => BudgetConfigSchema.parse({ ...validBudgetConfig, pourcentageChargesFixes: 101 })).toThrow()
    })
  })
})

describe('validation helpers', () => {
  describe('validateData', () => {
    it('should validate and return data when valid', () => {
      const validBanque = {
        id: '123e4567-e89b-12d3-a456-426614174000',
        nom: 'BNP Paribas',
        couleurTheme: '#00FF00',
        actif: true
      }
      const result = validateData(BanqueSchema, validBanque)
      expect(result).toEqual(validBanque)
    })

    it('should throw error when data is invalid', () => {
      const invalidBanque = {
        id: 'invalid-uuid',
        nom: 'BNP Paribas',
        couleurTheme: '#00FF00',
        actif: true
      }
      expect(() => validateData(BanqueSchema, invalidBanque)).toThrow()
    })
  })

  describe('safeValidateData', () => {
    it('should return data when valid', () => {
      const validBanque = {
        id: '123e4567-e89b-12d3-a456-426614174000',
        nom: 'BNP Paribas',
        couleurTheme: '#00FF00',
        actif: true
      }
      const result = safeValidateData(BanqueSchema, validBanque)
      expect(result).toEqual(validBanque)
    })

    it('should return null when data is invalid', () => {
      const invalidBanque = {
        id: 'invalid-uuid',
        nom: 'BNP Paribas',
        couleurTheme: '#00FF00',
        actif: true
      }
      const result = safeValidateData(BanqueSchema, invalidBanque)
      expect(result).toBeNull()
    })
  })

  describe('type guards', () => {
    describe('isValidUser', () => {
      it('should return true for valid user', () => {
        const validUser = {
          id: '123e4567-e89b-12d3-a456-426614174000',
          nom: 'Dupont',
          prenom: 'Jean',
          jourPaie: 15,
          salaireMensuelNet: 2500
        }
        expect(isValidUser(validUser)).toBe(true)
      })

      it('should return false for invalid user', () => {
        const invalidUser = {
          id: 'invalid',
          nom: 'Dupont'
        }
        expect(isValidUser(invalidUser)).toBe(false)
      })
    })

    describe('isValidBanque', () => {
      it('should return true for valid banque', () => {
        const validBanque = {
          id: '123e4567-e89b-12d3-a456-426614174000',
          nom: 'BNP Paribas',
          couleurTheme: '#00FF00',
          actif: true
        }
        expect(isValidBanque(validBanque)).toBe(true)
      })

      it('should return false for invalid banque', () => {
        expect(isValidBanque({ id: 'invalid' })).toBe(false)
      })
    })

    describe('isValidCompte', () => {
      it('should return true for valid compte', () => {
        const validCompte = {
          id: '123e4567-e89b-12d3-a456-426614174000',
          banque: {
            id: '123e4567-e89b-12d3-a456-426614174001',
            nom: 'BNP Paribas',
            couleurTheme: '#00FF00',
            actif: true
          },
          nom: 'Compte Courant',
          type: 'COMPTE_COURANT',
          soldeTotal: 1000,
          actif: true
        }
        expect(isValidCompte(validCompte)).toBe(true)
      })

      it('should return false for invalid compte', () => {
        expect(isValidCompte({ id: 'invalid' })).toBe(false)
      })
    })

    describe('isValidObjectif', () => {
      it('should return true for valid objectif', () => {
        const validObjectif = {
          id: '123e4567-e89b-12d3-a456-426614174000',
          nom: 'Vacances',
          montantCible: 5000,
          couleur: '#FF5722',
          priorite: 'HAUTE',
          type: 'PLAISIR',
          actif: true
        }
        expect(isValidObjectif(validObjectif)).toBe(true)
      })

      it('should return false for invalid objectif', () => {
        expect(isValidObjectif({ id: 'invalid' })).toBe(false)
      })
    })

    describe('isValidChargeFixe', () => {
      it('should return true for valid charge fixe', () => {
        const validChargeFixe = {
          id: '123e4567-e89b-12d3-a456-426614174000',
          compte: {
            id: '123e4567-e89b-12d3-a456-426614174002',
            banque: {
              id: '123e4567-e89b-12d3-a456-426614174003',
              nom: 'BNP Paribas',
              couleurTheme: '#00FF00',
              actif: true
            },
            nom: 'Compte Courant',
            type: 'COMPTE_COURANT',
            soldeTotal: 1000,
            actif: true
          },
          nom: 'Loyer',
          montant: 800,
          categorie: 'LOYER',
          jourPrelevement: 5,
          frequence: 'MENSUELLE',
          dateDebut: '2024-01-01',
          actif: true
        }
        expect(isValidChargeFixe(validChargeFixe)).toBe(true)
      })

      it('should return false for invalid charge fixe', () => {
        expect(isValidChargeFixe({ id: 'invalid' })).toBe(false)
      })
    })

    describe('isValidBudgetConfig', () => {
      it('should return true for valid budget config', () => {
        const validBudgetConfig = {
          id: '123e4567-e89b-12d3-a456-426614174000',
          userId: '123e4567-e89b-12d3-a456-426614174001',
          pourcentageChargesFixes: 50,
          pourcentageDepensesVariables: 30,
          pourcentageEpargne: 20
        }
        expect(isValidBudgetConfig(validBudgetConfig)).toBe(true)
      })

      it('should return false for invalid budget config', () => {
        expect(isValidBudgetConfig({ id: 'invalid' })).toBe(false)
      })
    })

    describe('isValidDashboardData', () => {
      it('should return true for valid dashboard data', () => {
        const validDashboardData = {
          mois: '2024-01',
          user: {
            id: '123e4567-e89b-12d3-a456-426614174000',
            nom: 'Dupont',
            prenom: 'Jean',
            jourPaie: 15,
            salaireMensuelNet: 2500
          },
          comptes: [],
          objectifs: [],
          salaireValide: true,
          timestamp: '2024-01-01T00:00:00Z'
        }
        expect(isValidDashboardData(validDashboardData)).toBe(true)
      })

      it('should return false for invalid dashboard data', () => {
        expect(isValidDashboardData({ mois: 'invalid' })).toBe(false)
      })
    })
  })
})
