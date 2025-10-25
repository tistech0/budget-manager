/**
 * Composable pour gérer les icônes et catégories des transactions
 */

import type { TypeTransaction } from '@/types'

/**
 * Mapping des types de transactions vers leurs icônes
 */
const TRANSACTION_ICONS: Record<string, string> = {
  // Revenus
  SALAIRE: '💰',
  PRIME: '🎁',
  FREELANCE: '💻',
  ALLOCATION: '🏛️',
  REMBOURSEMENT: '↩️',
  GAIN_INVESTISSEMENT: '📈',
  CADEAU_RECU: '🎁',
  VENTE: '💵',

  // Charges fixes
  LOYER: '🏠',
  ASSURANCE: '🛡️',
  ABONNEMENT: '📱',
  CREDIT_IMMOBILIER: '🏡',
  CREDIT_CONSO: '💳',
  IMPOTS: '🏛️',
  MUTUELLE: '⚕️',

  // Dépenses variables
  ALIMENTATION: '🛒',
  RESTAURANT: '🍽️',
  TRANSPORT: '🚗',
  ESSENCE: '⛽',
  SHOPPING: '🛍️',
  LOISIRS: '🎮',
  SANTE: '💊',
  BEAUTE: '💄',
  MAISON: '🏠',
  EDUCATION: '📚',
  VOYAGE: '✈️',

  // Épargne & Investissement
  EPARGNE: '💎',
  INVESTISSEMENT: '📊',
  VIREMENT_INTERNE: '🔄',
  TRANSFERT_OBJECTIF: '🎯',

  // Autres
  RETRAIT_ESPECES: '💵',
  FRAIS_BANCAIRE: '🏦',
  COMMISSION: '📋',
  AUTRE: '📝',

  // Fallback
  default: '📝'
}

/**
 * Mapping des catégories de transactions
 */
const TRANSACTION_CATEGORIES: Record<string, { label: string; color: string }> = {
  // Revenus
  SALAIRE: { label: 'Salaire', color: '#22c55e' },
  PRIME: { label: 'Prime', color: '#22c55e' },
  FREELANCE: { label: 'Freelance', color: '#22c55e' },
  ALLOCATION: { label: 'Allocation', color: '#22c55e' },
  REMBOURSEMENT: { label: 'Remboursement', color: '#22c55e' },
  GAIN_INVESTISSEMENT: { label: 'Gain Investissement', color: '#22c55e' },
  CADEAU_RECU: { label: 'Cadeau Reçu', color: '#22c55e' },
  VENTE: { label: 'Vente', color: '#22c55e' },

  // Charges fixes
  LOYER: { label: 'Loyer', color: '#ef4444' },
  ASSURANCE: { label: 'Assurance', color: '#ef4444' },
  ABONNEMENT: { label: 'Abonnement', color: '#ef4444' },
  CREDIT_IMMOBILIER: { label: 'Crédit Immobilier', color: '#ef4444' },
  CREDIT_CONSO: { label: 'Crédit Conso', color: '#ef4444' },
  IMPOTS: { label: 'Impôts', color: '#ef4444' },
  MUTUELLE: { label: 'Mutuelle', color: '#ef4444' },

  // Dépenses variables
  ALIMENTATION: { label: 'Alimentation', color: '#f97316' },
  RESTAURANT: { label: 'Restaurant', color: '#f97316' },
  TRANSPORT: { label: 'Transport', color: '#f97316' },
  ESSENCE: { label: 'Essence', color: '#f97316' },
  SHOPPING: { label: 'Shopping', color: '#f97316' },
  LOISIRS: { label: 'Loisirs', color: '#f97316' },
  SANTE: { label: 'Santé', color: '#f97316' },
  BEAUTE: { label: 'Beauté', color: '#f97316' },
  MAISON: { label: 'Maison', color: '#f97316' },
  EDUCATION: { label: 'Éducation', color: '#f97316' },
  VOYAGE: { label: 'Voyage', color: '#f97316' },

  // Épargne & Investissement
  EPARGNE: { label: 'Épargne', color: '#3b82f6' },
  INVESTISSEMENT: { label: 'Investissement', color: '#3b82f6' },
  VIREMENT_INTERNE: { label: 'Virement Interne', color: '#6366f1' },
  TRANSFERT_OBJECTIF: { label: 'Transfert Objectif', color: '#6366f1' },

  // Autres
  RETRAIT_ESPECES: { label: 'Retrait Espèces', color: '#6b7280' },
  FRAIS_BANCAIRE: { label: 'Frais Bancaire', color: '#6b7280' },
  COMMISSION: { label: 'Commission', color: '#6b7280' },
  AUTRE: { label: 'Autre', color: '#6b7280' }
}

export function useTransactionIcons() {
  /**
   * Récupère l'icône correspondant à un type de transaction
   */
  const getTransactionIcon = (type: string | TypeTransaction): string => {
    return TRANSACTION_ICONS[type] || TRANSACTION_ICONS.default
  }

  /**
   * Récupère le label d'une catégorie de transaction
   */
  const getTransactionLabel = (type: string | TypeTransaction): string => {
    return TRANSACTION_CATEGORIES[type]?.label || type
  }

  /**
   * Récupère la couleur d'une catégorie de transaction
   */
  const getTransactionColor = (type: string | TypeTransaction): string => {
    return TRANSACTION_CATEGORIES[type]?.color || '#6b7280'
  }

  /**
   * Vérifie si une transaction est un revenu
   */
  const isRevenue = (type: string | TypeTransaction): boolean => {
    const revenueTypes = [
      'SALAIRE',
      'PRIME',
      'FREELANCE',
      'ALLOCATION',
      'REMBOURSEMENT',
      'GAIN_INVESTISSEMENT',
      'CADEAU_RECU',
      'VENTE'
    ]
    return revenueTypes.includes(type)
  }

  /**
   * Vérifie si une transaction est une dépense
   */
  const isExpense = (type: string | TypeTransaction): boolean => {
    return !isRevenue(type) && !isTransfer(type)
  }

  /**
   * Vérifie si une transaction est un transfert
   */
  const isTransfer = (type: string | TypeTransaction): boolean => {
    const transferTypes = ['VIREMENT_INTERNE', 'TRANSFERT_OBJECTIF']
    return transferTypes.includes(type)
  }

  return {
    getTransactionIcon,
    getTransactionLabel,
    getTransactionColor,
    isRevenue,
    isExpense,
    isTransfer
  }
}
