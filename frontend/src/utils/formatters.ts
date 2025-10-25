/**
 * Utilitaires de formatage pour Budget Manager
 */

/**
 * Formate un montant en euros
 */
export const formatCurrency = (amount: number | null | undefined): string => {
    if (amount === null || amount === undefined) {
      return '0,00 €'
    }
    
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(amount)
  }
  
  /**
   * Formate un pourcentage
   */
  export const formatPercentage = (value: number | null | undefined): string => {
    if (value === null || value === undefined) {
      return '0%'
    }
    
    return new Intl.NumberFormat('fr-FR', {
      style: 'percent',
      minimumFractionDigits: 0,
      maximumFractionDigits: 1
    }).format(value / 100)
  }
  
  /**
   * Formate une date
   */
  export const formatDate = (date: string | Date): string => {
    const d = typeof date === 'string' ? new Date(date) : date
    
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(d)
  }
  
  /**
   * Formate une date avec l'heure
   */
  export const formatDateTime = (date: string | Date): string => {
    const d = typeof date === 'string' ? new Date(date) : date
    
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(d)
  }
  
  /**
   * Formate un mois (YYYY-MM) en texte lisible
   */
  export const formatMonth = (monthStr: string): string => {
    const [year, month] = monthStr.split('-')
    const date = new Date(parseInt(year), parseInt(month) - 1, 1)
    
    return new Intl.DateTimeFormat('fr-FR', {
      month: 'long',
      year: 'numeric'
    }).format(date)
  }
  
  /**
   * Tronque un texte avec ellipses
   */
  export const truncateText = (text: string, maxLength: number): string => {
    if (text.length <= maxLength) {
      return text
    }
    return text.substring(0, maxLength - 3) + '...'
  }
  
  /**
   * Génère une couleur aléatoire
   */
  export const generateRandomColor = (): string => {
    const colors = [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
      '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9'
    ]
    return colors[Math.floor(Math.random() * colors.length)]
  }
  
  /**
   * Valide un email
   */
  export const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  }
  
  /**
   * Valide un montant (positif)
   */
  export const isValidAmount = (amount: number): boolean => {
    return typeof amount === 'number' && amount >= 0 && !isNaN(amount)
  }
  
  /**
   * Convertit une string en slug (URL-friendly)
   */
  export const slugify = (text: string): string => {
    return text
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '') // Supprime les accents
      .replace(/[^a-z0-9 -]/g, '') // Supprime les caractères spéciaux
      .replace(/\s+/g, '-') // Remplace les espaces par des tirets
      .replace(/-+/g, '-') // Supprime les tirets multiples
      .trim()
  }
  
  /**
   * Calcule la différence en jours entre deux dates
   */
  export const daysDifference = (date1: Date, date2: Date): number => {
    const timeDiff = Math.abs(date2.getTime() - date1.getTime())
    return Math.ceil(timeDiff / (1000 * 3600 * 24))
  }
  
  /**
   * Formate un nombre avec des séparateurs de milliers
   */
  export const formatNumber = (num: number): string => {
    return new Intl.NumberFormat('fr-FR').format(num)
  }
  
  /**
   * Génère un ID temporaire unique
   */
  export const generateTempId = (): string => {
    return `temp_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }