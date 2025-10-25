<template>
    <div class="step-content">
      <div class="step-header">
        <h1 class="step-title">Résumé de votre configuration</h1>
        <p class="step-subtitle">
          Vérifiez vos informations avant de finaliser la configuration
        </p>
      </div>
  
      <div class="summary-sections">
        <!-- Profil utilisateur -->
        <div class="summary-card">
          <h3 class="card-title">👤 Votre profil</h3>
          <div class="summary-content">
            <div class="summary-item">
              <span class="label">Nom :</span>
              <span class="value">{{ userData.prenom }} {{ userData.nom }}</span>
            </div>
            <div class="summary-item">
              <span class="label">Jour de paie :</span>
              <span class="value">{{ userData.jourPaie }}{{ userData.jourPaie === 1 ? 'er' : '' }} du mois</span>
            </div>
            <div class="summary-item">
              <span class="label">Salaire net :</span>
              <span class="value">{{ formatCurrency(userData.salaireMensuelNet) }}</span>
            </div>
            <div v-if="userData.decouvertAutorise" class="summary-item">
              <span class="label">Découvert autorisé :</span>
              <span class="value">{{ formatCurrency(userData.decouvertAutorise) }}</span>
            </div>
          </div>
        </div>
  
        <!-- Banques sélectionnées -->
        <div class="summary-card">
          <h3 class="card-title">🏦 Banques sélectionnées</h3>
          <div class="summary-content">
            <div v-if="selectedBanks.length === 0" class="empty-state-small">
              Aucune banque sélectionnée
            </div>
            <div v-else class="banks-grid">
              <div 
                v-for="bank in selectedBanks"
                :key="bank.id"
                class="bank-summary-card"
              >
                <div 
                  class="bank-summary-color"
                  :style="{ backgroundColor: bank.couleurTheme }"
                ></div>
                <span class="bank-summary-name">{{ bank.nom }}</span>
              </div>
            </div>
            <div v-if="selectedBanks.length > 0" class="summary-count">
              {{ selectedBanks.length }} banque{{ selectedBanks.length > 1 ? 's' : '' }} sélectionnée{{ selectedBanks.length > 1 ? 's' : '' }}
            </div>
          </div>
        </div>
  
        <!-- Comptes -->
        <div class="summary-card">
          <h3 class="card-title">💳 Vos comptes</h3>
          <div class="summary-content">
            <div v-if="userAccounts.length === 0" class="empty-state-small">
              Aucun compte ajouté
            </div>
            <div v-else>
              <div class="accounts-grid">
                <div 
                  v-for="account in userAccounts"
                  :key="account.tempId"
                  class="account-summary-card"
                >
                  <div class="account-summary-header">
                    <div 
                      class="account-summary-color"
                      :style="{ backgroundColor: account.banque.couleurTheme }"
                    ></div>
                    <div class="account-summary-info">
                      <span class="account-summary-name">{{ account.nom }}</span>
                      <span class="account-summary-bank">{{ account.banque.nom }}</span>
                    </div>
                  </div>
                  <div class="account-summary-details">
                    <span class="account-summary-type">{{ getTypeLabel(account.type) }}</span>
                    <span class="account-summary-amount">{{ formatCurrency(account.soldeTotal) }}</span>
                  </div>
                </div>
              </div>
              <div class="summary-total">
                <span class="total-label">Total patrimoine :</span>
                <span class="total-value">{{ formatCurrency(totalComptes) }}</span>
              </div>
            </div>
          </div>
        </div>
  
        <!-- Configuration du budget -->
        <div class="summary-card">
          <h3 class="card-title">📊 Répartition de votre budget</h3>
          <div class="summary-content">
            <div v-if="!budgetConfig || !budgetConfig.pourcentageChargesFixes" class="empty-state-small">
              Configuration budget non définie
            </div>
            <div v-else>
              <div class="budget-repartition-grid">
                <div class="budget-item charges-fixes">
                  <div class="budget-item-header">
                    <span class="budget-icon">🏠</span>
                    <span class="budget-category">Charges fixes</span>
                  </div>
                  <div class="budget-values">
                    <span class="budget-percentage">{{ budgetConfig.pourcentageChargesFixes }}%</span>
                    <span class="budget-amount">{{ formatCurrency(getBudgetMontant('chargesFixes')) }}</span>
                  </div>
                </div>
                
                <div class="budget-item depenses-variables">
                  <div class="budget-item-header">
                    <span class="budget-icon">🛍️</span>
                    <span class="budget-category">Dépenses variables</span>
                  </div>
                  <div class="budget-values">
                    <span class="budget-percentage">{{ budgetConfig.pourcentageDepensesVariables }}%</span>
                    <span class="budget-amount">{{ formatCurrency(getBudgetMontant('depensesVariables')) }}</span>
                  </div>
                </div>
                
                <div class="budget-item epargne">
                  <div class="budget-item-header">
                    <span class="budget-icon">💰</span>
                    <span class="budget-category">Épargne</span>
                  </div>
                  <div class="budget-values">
                    <span class="budget-percentage">{{ budgetConfig.pourcentageEpargne }}%</span>
                    <span class="budget-amount">{{ formatCurrency(getBudgetMontant('epargne')) }}</span>
                  </div>
                </div>
              </div>
              
              <div class="budget-method-info">
                <span class="method-label">Méthode :</span>
                <span class="method-value">{{ getBudgetMethodLabel() }}</span>
              </div>
            </div>
          </div>
        </div>
  
        <!-- Charges fixes -->
        <div class="summary-card">
          <h3 class="card-title">🏠 Charges fixes configurées</h3>
          <div class="summary-content">
            <div v-if="!userChargesFixes || userChargesFixes.length === 0" class="empty-state-small">
              Aucune charge fixe configurée
            </div>
            <div v-else>
              <div class="charges-grid">
                <div 
                  v-for="charge in userChargesFixes"
                  :key="charge.id"
                  class="charge-summary-card"
                >
                  <div class="charge-summary-header">
                    <span class="charge-summary-icon">{{ getCategoryIcon(charge.categorie) }}</span>
                    <div class="charge-summary-info">
                      <span class="charge-summary-name">{{ charge.nom }}</span>
                      <span class="charge-summary-account">{{ charge.compte?.nom }}</span>
                    </div>
                  </div>
                  <div class="charge-summary-details">
                    <div class="charge-amount-section">
                      <span class="charge-summary-amount">{{ formatCurrency(charge.montant) }}</span>
                      <span class="charge-summary-frequency">{{ getFrequencyLabel(charge.frequence) }}</span>
                    </div>
                    <span class="charge-summary-date">{{ charge.jourPrelevement }}{{ charge.jourPrelevement === 1 ? 'er' : '' }}</span>
                  </div>
                </div>
              </div>
              
              <div class="charges-summary-total">
                <div class="charges-total-card">
                  <span class="charges-total-label">Total mensuel estimé :</span>
                  <span class="charges-total-amount">{{ formatCurrency(totalChargesMensuelles) }}</span>
                </div>
                <div v-if="budgetConfig && budgetConfig.pourcentageChargesFixes" class="charges-budget-comparison">
                  <div class="comparison-bar">
                    <div 
                      class="comparison-fill"
                      :style="{ 
                        width: `${Math.min(getChargesProgression(), 100)}%`,
                        backgroundColor: getChargesProgression() > 100 ? '#f56565' : '#48bb78'
                      }"
                    ></div>
                  </div>
                  <div class="comparison-text">
                    <span>{{ getChargesProgression() }}% du budget charges fixes</span>
                    <span v-if="getChargesProgression() > 100" class="budget-warning">
                      (Dépassement de {{ formatCurrency(totalChargesMensuelles - getBudgetMontant('chargesFixes')) }})
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
  
        <!-- Objectifs -->
        <div class="summary-card">
          <h3 class="card-title">🎯 Vos objectifs</h3>
          <div class="summary-content">
            <div v-if="userObjectifs.length === 0" class="empty-state-small">
              Aucun objectif défini
            </div>
            <div v-else>
              <div class="objectives-grid">
                <div 
                  v-for="objectif in userObjectifs"
                  :key="objectif.nom"
                  class="objective-summary-card"
                >
                  <div class="objective-summary-header">
                    <span 
                      class="objective-summary-icon"
                      :style="{ color: objectif.couleur }"
                    >
                      {{ objectif.icone || '🎯' }}
                    </span>
                    <div class="objective-summary-info">
                      <span class="objective-summary-name">{{ objectif.nom }}</span>
                      <span class="objective-summary-type">{{ getTypeLabel(objectif.type) }}</span>
                    </div>
                  </div>
                  <div class="objective-summary-details">
                    <div class="objective-progress-section">
                      <div class="objective-amounts">
                        <span class="objective-summary-priority">{{ getPriorityIcon(objectif.priorite) }} {{ getPriorityLabel(objectif.priorite) }}</span>
                        <span class="objective-summary-amount">{{ formatCurrency(objectif.montantCible) }}</span>
                      </div>
                      <div v-if="getObjectifMontantActuel(objectif) > 0" class="objective-progress">
                        <div class="progress-info">
                          <span class="progress-current">{{ formatCurrency(getObjectifMontantActuel(objectif)) }}</span>
                          <span class="progress-percentage">{{ getObjectifProgression(objectif) }}%</span>
                        </div>
                        <div class="progress-bar-container">
                          <div class="progress-bar-fill" :style="{ 
                            width: `${Math.min(getObjectifProgression(objectif), 100)}%`,
                            backgroundColor: objectif.couleur 
                          }"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="summary-total">
                <span class="total-label">Total objectifs :</span>
                <span class="total-value">{{ formatCurrency(totalObjectifs) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { computed } from 'vue'
  import { storeToRefs } from 'pinia'
  import { useOnboardingStore } from '@/stores/onboarding'
  import { formatCurrency } from '@/utils/formatters'
  import type { TypeCompte, TypeObjectif, PrioriteObjectif } from '@/types'
  
  const onboardingStore = useOnboardingStore()
  const { userData, selectedBanks, userAccounts, userObjectifs, budgetConfig, userChargesFixes } = storeToRefs(onboardingStore)
  
  const totalComptes = computed(() => {
    return userAccounts.value.reduce((total, account) => total + account.soldeTotal, 0)
  })
  
  const totalObjectifs = computed(() => {
    return userObjectifs.value.reduce((total, objectif) => total + objectif.montantCible, 0)
  })
  
  const totalChargesMensuelles = computed(() => {
  if (!userChargesFixes.value || !Array.isArray(userChargesFixes.value)) {
    return 0
  }
  return userChargesFixes.value.reduce((total, charge) => {
    const montantMensuel = getMontantMensuel(charge.montant, charge.frequence)
    return total + montantMensuel
  }, 0)
})
  
  const getTypeLabel = (type: TypeCompte | TypeObjectif): string => {
    const labels = {
      // Types de compte complets
      COMPTE_COURANT: 'Compte Courant',
      LIVRET_A: 'Livret A',
      LDDS: 'LDDS',
      LIVRET_JEUNE: 'Livret Jeune',
      LEP: 'LEP',
      PEL: 'PEL',
      CEL: 'CEL',
      CSL: 'CSL',
      PEA: 'PEA',
      PEA_PME: 'PEA-PME',
      ASSURANCE_VIE: 'Assurance Vie',
      COMPTE_TITRE: 'Compte Titre',
      CRYPTO: 'Crypto',
      OR_METAUX: 'Or & Métaux',
      AUTRE: 'Autre',
      // Types d'objectif complets
      SECURITE: 'Sécurité',
      COURT_TERME: 'Court terme',
      MOYEN_TERME: 'Moyen terme',
      LONG_TERME: 'Long terme',
      PLAISIR: 'Plaisir',
      FAMILLE: 'Famille',
      FORMATION: 'Formation',
      INVESTISSEMENT: 'Investissement',
      PROJET_IMMOBILIER: 'Immobilier',
      TRANSPORT: 'Transport',
      SANTE: 'Santé',
      TECHNOLOGIE: 'Technologie',
      OPPORTUNITE: 'Opportunité',
      DIVERS: 'Divers'
    } as const
    
    return labels[type] || type
  }
  
  const getPriorityLabel = (priority: PrioriteObjectif): string => {
    const labels: Record<PrioriteObjectif, string> = {
      CRITIQUE: 'Critique',
      TRES_HAUTE: 'Très haute',
      HAUTE: 'Haute',
      NORMALE: 'Normale',
      BASSE: 'Basse',
      TRES_BASSE: 'Très basse',
      SUSPENDU: 'Suspendu'
    }
    return labels[priority] || priority
  }

  const getPriorityIcon = (priority: PrioriteObjectif): string => {
    const icons: Record<PrioriteObjectif, string> = {
      CRITIQUE: '🔴',
      TRES_HAUTE: '🟠',
      HAUTE: '🟡',
      NORMALE: '🟢',
      BASSE: '🔵',
      TRES_BASSE: '🟣',
      SUSPENDU: '⚪'
    }
    return icons[priority] || '⚪'
  }
  
  const getObjectifMontantActuel = (objectif: any): number => {
    if (!objectif.repartitions || !Array.isArray(objectif.repartitions)) {
      return 0
    }
    return objectif.repartitions.reduce((total: number, rep: any) => total + (rep.montant || 0), 0)
  }
  
  const getObjectifProgression = (objectif: any): number => {
    const montantActuel = getObjectifMontantActuel(objectif)
    if (objectif.montantCible === 0) return 0
    return Math.round((montantActuel / objectif.montantCible) * 100)
  }
  
  // Nouvelles méthodes pour budget et charges fixes
  const getBudgetMontant = (type: 'chargesFixes' | 'depensesVariables' | 'epargne'): number => {
    const salaire = userData.value.salaireMensuelNet || 0
    if (!budgetConfig.value) return 0
    
    const pourcentages = {
      chargesFixes: budgetConfig.value.pourcentageChargesFixes || 0,
      depensesVariables: budgetConfig.value.pourcentageDepensesVariables || 0,
      epargne: budgetConfig.value.pourcentageEpargne || 0
    }
    return salaire * (pourcentages[type] / 100)
  }
  
  const getBudgetMethodLabel = (): string => {
    const config = budgetConfig.value
    if (!config) return 'Non configuré'
    
    if (config.pourcentageChargesFixes === 50 && 
        config.pourcentageDepensesVariables === 30 && 
        config.pourcentageEpargne === 20) {
      return 'Règle 50/30/20'
    }
    return 'Personnalisé'
  }
  
  const getCategoryIcon = (categorie: string): string => {
    const icons: Record<string, string> = {
      LOYER: '🏠',
      ASSURANCE: '🛡️',
      ABONNEMENT: '📱',
      CREDIT_IMMOBILIER: '🏡',
      CREDIT_CONSO: '💳',
      IMPOTS: '🏛️',
      MUTUELLE: '🏥',
      AUTRE: '📦'
    }
    return icons[categorie] || '📦'
  }

  const getFrequencyLabel = (frequence: string): string => {
    const labels: Record<string, string> = {
      MENSUELLE: 'Mensuelle',
      BIMESTRIELLE: 'Bimestrielle',
      TRIMESTRIELLE: 'Trimestrielle',
      SEMESTRIELLE: 'Semestrielle',
      ANNUELLE: 'Annuelle'
    }
    return labels[frequence] || frequence
  }

  const getMontantMensuel = (montant: number, frequence: string): number => {
    const coefficients: Record<string, number> = {
      MENSUELLE: 1,
      BIMESTRIELLE: 1/2,
      TRIMESTRIELLE: 1/3,
      SEMESTRIELLE: 1/6,
      ANNUELLE: 1/12
    }
    return montant * (coefficients[frequence] || 1)
  }
  
  const getChargesProgression = (): number => {
    const budgetCharges = getBudgetMontant('chargesFixes')
    if (budgetCharges === 0) return 0
    return Math.round((totalChargesMensuelles.value / budgetCharges) * 100)
  }
  </script>
  
  <style scoped>
  .summary-sections {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .summary-card {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-xl);
    padding: 1.5rem;
    transition: all var(--transition-fast);
  }
  
  .summary-card:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.15);
  }
  
  .card-title {
    color: var(--text-primary);
    font-size: 1.2rem;
    font-weight: 600;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .summary-content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  /* Profil styles */
  .summary-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  }
  
  .summary-item:last-child {
    border-bottom: none;
  }
  
  .summary-item .label {
    color: var(--text-secondary);
    font-weight: 500;
  }
  
  .summary-item .value {
    color: var(--text-primary);
    font-weight: 600;
  }
  
  /* Banques styles */
  .banks-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 0.75rem;
  }
  
  .bank-summary-card {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: var(--radius-md);
    transition: all var(--transition-fast);
  }
  
  .bank-summary-card:hover {
    background: rgba(255, 255, 255, 0.06);
    transform: translateY(-1px);
  }
  
  .bank-summary-color {
    width: 4px;
    height: 32px;
    border-radius: var(--radius-sm);
    flex-shrink: 0;
  }
  
  .bank-summary-name {
    color: var(--text-primary);
    font-weight: 500;
    font-size: 0.9rem;
  }
  
  /* Comptes styles */
  .accounts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1rem;
  }
  
  .account-summary-card {
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: var(--radius-md);
    padding: 1rem;
    transition: all var(--transition-fast);
  }
  
  .account-summary-card:hover {
    background: rgba(255, 255, 255, 0.06);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
  
  .account-summary-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.5rem;
  }
  
  .account-summary-color {
    width: 4px;
    height: 36px;
    border-radius: var(--radius-sm);
    flex-shrink: 0;
  }
  
  .account-summary-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    flex: 1;
  }
  
  .account-summary-name {
    color: var(--text-primary);
    font-weight: 600;
    font-size: 0.95rem;
  }
  
  .account-summary-bank {
    color: var(--text-secondary);
    font-size: 0.8rem;
  }
  
  .account-summary-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .account-summary-type {
    color: var(--text-secondary);
    font-size: 0.85rem;
    padding: 0.25rem 0.5rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: var(--radius-sm);
  }
  
  .account-summary-amount {
    color: var(--success-color);
    font-weight: 600;
    font-size: 1rem;
  }
  
  /* Configuration du budget */
  .budget-repartition-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
    margin-bottom: 1rem;
  }
  
  .budget-item {
    padding: 1rem;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: var(--radius-md);
    transition: all var(--transition-fast);
  }
  
  .budget-item:hover {
    background: rgba(255, 255, 255, 0.06);
    transform: translateY(-1px);
  }
  
  .budget-item-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.75rem;
  }
  
  .budget-icon {
    font-size: 1.2rem;
  }
  
  .budget-category {
    color: var(--text-primary);
    font-weight: 500;
    font-size: 0.9rem;
  }
  
  .budget-values {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .budget-percentage {
    color: var(--primary-color);
    font-weight: 700;
    font-size: 1.1rem;
  }
  
  .budget-amount {
    color: var(--success-color);
    font-weight: 600;
  }
  
  .budget-method-info {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem;
    background: rgba(102, 126, 234, 0.1);
    border-radius: var(--radius-md);
    margin-top: 1rem;
  }
  
  .method-label {
    color: var(--text-secondary);
    font-weight: 500;
  }
  
  .method-value {
    color: var(--primary-color);
    font-weight: 600;
  }
  
  /* Charges fixes */
  .charges-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    gap: 1rem;
    margin-bottom: 1.5rem;
  }
  
  .charge-summary-card {
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: var(--radius-md);
    padding: 1rem;
    transition: all var(--transition-fast);
  }
  
  .charge-summary-card:hover {
    background: rgba(255, 255, 255, 0.06);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
  
  .charge-summary-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.75rem;
  }
  
  .charge-summary-icon {
    font-size: 1.3rem;
    flex-shrink: 0;
  }
  
  .charge-summary-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    flex: 1;
  }
  
  .charge-summary-name {
    color: var(--text-primary);
    font-weight: 600;
    font-size: 0.95rem;
  }
  
  .charge-summary-account {
    color: var(--text-secondary);
    font-size: 0.8rem;
  }
  
  .charge-summary-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .charge-amount-section {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .charge-summary-amount {
    color: var(--success-color);
    font-weight: 600;
    font-size: 1rem;
  }
  
  .charge-summary-frequency {
    color: var(--text-secondary);
    font-size: 0.8rem;
  }
  
  .charge-summary-date {
    color: var(--text-secondary);
    font-size: 0.85rem;
    background: rgba(255, 255, 255, 0.05);
    padding: 0.25rem 0.5rem;
    border-radius: var(--radius-sm);
  }
  
  .charges-summary-total {
    margin-top: 1rem;
  }
  
  .charges-total-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
    margin-bottom: 0.75rem;
  }
  
  .charges-total-label {
    color: var(--text-primary);
    font-weight: 600;
  }
  
  .charges-total-amount {
    color: var(--success-color);
    font-weight: 700;
    font-size: 1.1rem;
  }
  
  .charges-budget-comparison {
    background: rgba(255, 255, 255, 0.03);
    padding: 1rem;
    border-radius: var(--radius-md);
  }
  
  .comparison-bar {
    width: 100%;
    height: 6px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 0.5rem;
  }
  
  .comparison-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.3s ease;
  }
  
  .comparison-text {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    font-size: 0.9rem;
    text-align: center;
  }
  
  .comparison-text span:first-child {
    color: var(--text-primary);
    font-weight: 500;
  }
  
  .budget-warning {
    color: #fca5a5;
    font-size: 0.8rem;
    font-weight: 500;
  }
  
  /* Objectifs styles */
  .objectives-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1rem;
  }
  
  .objective-summary-card {
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: var(--radius-md);
    padding: 1rem;
    transition: all var(--transition-fast);
  }
  
  .objective-summary-card:hover {
    background: rgba(255, 255, 255, 0.06);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }
  
  .objective-summary-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.5rem;
  }
  
  .objective-summary-icon {
    font-size: 1.5rem;
    flex-shrink: 0;
  }
  
  .objective-summary-info {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    flex: 1;
  }
  
  .objective-summary-name {
    color: var(--text-primary);
    font-weight: 600;
    font-size: 0.95rem;
  }
  
  .objective-summary-type {
    color: var(--text-secondary);
    font-size: 0.8rem;
  }
  
  .objective-summary-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .objective-summary-priority {
    color: var(--text-secondary);
    font-size: 0.85rem;
    padding: 0.25rem 0.5rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: var(--radius-sm);
  }
  
  .objective-summary-amount {
    color: var(--primary-color);
    font-weight: 600;
    font-size: 1rem;
  }
  
  /* Progression des objectifs */
  .objective-progress-section {
    width: 100%;
  }
  
  .objective-amounts {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.75rem;
  }
  
  .objective-progress {
    margin-top: 0.5rem;
  }
  
  .progress-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
    font-size: 0.85rem;
  }
  
  .progress-current {
    color: var(--text-primary);
    font-weight: 600;
  }
  
  .progress-percentage {
    color: var(--success-color);
    font-weight: 600;
  }
  
  .progress-bar-container {
    width: 100%;
    height: 6px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
    overflow: hidden;
  }
  
  .progress-bar-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.3s ease;
    position: relative;
  }
  
  /* Totaux */
  .summary-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    margin-top: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
  }
  
  .total-label {
    color: var(--text-secondary);
    font-weight: 600;
    font-size: 1rem;
  }
  
  .total-value {
    color: var(--text-primary);
    font-weight: 700;
    font-size: 1.1rem;
  }
  
  .summary-count {
    text-align: center;
    color: var(--text-secondary);
    font-size: 0.9rem;
    font-style: italic;
    margin-top: 0.5rem;
  }
  
  /* Prochaines étapes */
  .next-steps-card {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    border-color: rgba(102, 126, 234, 0.2);
  }
  
  .next-steps-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 1rem;
  }
  
  .next-step-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
    transition: all var(--transition-fast);
  }
  
  .next-step-item:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateY(-1px);
  }
  
  .step-icon {
    font-size: 1.5rem;
    flex-shrink: 0;
  }
  
  .step-content h4 {
    color: var(--text-primary);
    font-weight: 600;
    font-size: 0.9rem;
    margin-bottom: 0.25rem;
  }
  
  .step-content p {
    color: var(--text-secondary);
    font-size: 0.8rem;
    line-height: 1.3;
  }
  
  /* États vides */
  .empty-state-small {
    text-align: center;
    padding: 1.5rem;
    color: var(--text-muted);
    font-style: italic;
    font-size: 0.9rem;
  }
  
  /* Responsive */
  @media (max-width: 768px) {
    .banks-grid,
    .accounts-grid,
    .objectives-grid,
    .next-steps-grid,
    .budget-repartition-grid,
    .charges-grid {
      grid-template-columns: 1fr;
    }
    
    .summary-total,
    .charges-total-card {
      flex-direction: column;
      gap: 0.5rem;
      text-align: center;
    }
    
    .charge-summary-details {
      flex-direction: column;
      gap: 0.5rem;
      align-items: flex-start;
    }
  }
  </style>