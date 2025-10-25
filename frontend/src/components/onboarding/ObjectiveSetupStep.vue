<template>
    <div class="step-content">
      <div class="step-header">
        <h1 class="step-title">Vos objectifs d'épargne</h1>
        <p class="step-subtitle">
          Définissez vos premiers objectifs pour organiser votre épargne
        </p>
      </div>
  
      <div class="objectives-section">
        <div v-if="userObjectifs.length > 0" class="objectives-list">
          <div 
            v-for="(objectif, index) in userObjectifs"
            :key="index"
            class="objective-item"
          >
            <div class="objective-info">
              <span class="objective-icon" :style="{ color: objectif.couleur }">
                {{ objectif.icone || '🎯' }}
              </span>
              <div class="objective-details">
                <h4>{{ objectif.nom }}</h4>
                <p>{{ getTypeLabel(objectif.type) }} - {{ getPriorityLabel(objectif.priorite) }}</p>
                <span class="objective-target">{{ formatCurrency(objectif.montantCible) }}</span>
                <div v-if="objectif.repartitions && objectif.repartitions.length > 0" class="objective-repartitions">
                  <small>Montant initial : {{ formatCurrency(getTotalRepartitions(objectif.repartitions)) }}</small>
                </div>
              </div>
            </div>
            <button @click="removeObjectif(index)" class="btn-remove" title="Supprimer">
              🗑️
            </button>
          </div>
        </div>
  
        <div v-else class="empty-objectives">
          <p>Aucun objectif défini pour le moment</p>
        </div>
  
        <button @click="toggleAddForm" class="btn btn-primary add-objective-btn">
          {{ showAddForm ? 'Masquer' : '+ Ajouter un objectif' }}
        </button>
  
        <!-- Formulaire d'ajout -->
        <div v-if="showAddForm" ref="addFormRef" class="add-objective-form">
          <form @submit.prevent="handleAddObjectif" class="form">
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Nom de l'objectif *</label>
                <input 
                  v-model="newObjectif.nom"
                  type="text" 
                  class="form-input"
                  placeholder="Épargne de sécurité"
                  required
                  maxlength="100"
                >
              </div>
  
              <div class="form-group">
                <label class="form-label">Montant cible *</label>
                <div class="currency-input">
                  <input 
                    v-model.number="newObjectif.montantCible"
                    type="number" 
                    class="form-input currency-field"
                    placeholder="5000"
                    step="0.01"
                    min="1"
                    required
                  >
                  <span class="currency-symbol">€</span>
                </div>
              </div>
            </div>
  
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Type d'objectif *</label>
                <select v-model="newObjectif.type" class="form-select" required>
                  <option value="SECURITE">🛡️ Sécurité</option>
                  <option value="COURT_TERME">⚡ Court terme</option>
                  <option value="MOYEN_TERME">🎯 Moyen terme</option>
                  <option value="LONG_TERME">🚀 Long terme</option>
                  <option value="PLAISIR">🎉 Plaisir</option>
                  <option value="FAMILLE">👨‍👩‍👧‍👦 Famille</option>
                  <option value="FORMATION">📚 Formation</option>
                  <option value="INVESTISSEMENT">📈 Investissement</option>
                  <option value="PROJET_IMMOBILIER">🏠 Immobilier</option>
                  <option value="TRANSPORT">🚗 Transport</option>
                  <option value="SANTE">🏥 Santé</option>
                  <option value="TECHNOLOGIE">💻 Technologie</option>
                  <option value="OPPORTUNITE">💡 Opportunité</option>
                  <option value="DIVERS">📦 Divers</option>
                </select>
              </div>
  
              <div class="form-group">
                <label class="form-label">Priorité *</label>
                <select v-model="newObjectif.priorite" class="form-select" required>
                  <option value="CRITIQUE">🔴 Critique</option>
                  <option value="TRES_HAUTE">🟠 Très haute</option>
                  <option value="HAUTE">🟡 Haute</option>
                  <option value="NORMALE">🟢 Normale</option>
                  <option value="BASSE">🔵 Basse</option>
                </select>
              </div>
            </div>
  
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Icône</label>
                <div class="icon-selector">
                  <div class="selected-icon" @click="showIconPicker = !showIconPicker">
                    <span class="icon-display">{{ newObjectif.icone }}</span>
                    <span class="dropdown-arrow">▼</span>
                  </div>
                  <div v-if="showIconPicker" class="icon-picker">
                    <span 
                      v-for="icon in availableIcons" 
                      :key="icon"
                      class="icon-option"
                      :class="{ selected: newObjectif.icone === icon }"
                      @click="selectIcon(icon)"
                    >
                      {{ icon }}
                    </span>
                  </div>
                </div>
              </div>
  
              <div class="form-group">
                <label class="form-label">Couleur</label>
                <input 
                  v-model="newObjectif.couleur"
                  type="color" 
                  class="form-input color-input"
                >
              </div>
            </div>
  
            <!-- Section répartition initiale -->
            <div v-if="userAccounts.length > 0" class="initial-allocation-section">
              <h4 class="section-title">Répartition initiale (optionnelle)</h4>
              <p class="section-subtitle">Affectez des montants existants de vos comptes à cet objectif</p>
              
              <div class="allocation-list">
                <div 
                  v-for="account in userAccounts" 
                  :key="account.tempId"
                  class="allocation-item"
                >
                  <div class="account-info">
                    <div 
                      class="account-color"
                      :style="{ backgroundColor: account.banque.couleurTheme }"
                    ></div>
                    <div class="account-details">
                      <span class="account-name">{{ account.nom }}</span>
                      <span class="account-balance">{{ formatCurrency(account.soldeTotal) }} disponible</span>
                    </div>
                  </div>
                  <div class="allocation-input">
                    <div class="currency-input">
                      <input
                        v-model.number="allocations[account.tempId || '']"
                        type="number"
                        class="form-input currency-field small"
                        placeholder="0"
                        step="0.01"
                        min="0"
                        :max="account.soldeTotal"
                      >
                      <span class="currency-symbol">€</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-if="getTotalAllocations() > 0" class="allocation-summary">
                <strong>Total initial : {{ formatCurrency(getTotalAllocations()) }}</strong>
              </div>
            </div>
  
            <div class="form-group">
              <label class="form-label">Description (optionnelle)</label>
              <textarea 
                v-model="newObjectif.description"
                class="form-textarea"
                placeholder="Décrivez votre objectif..."
                maxlength="500"
                rows="3"
              ></textarea>
            </div>
  
            <div class="form-actions">
              <button type="button" @click="resetForm" class="btn btn-secondary">
                Annuler
              </button>
              <button type="submit" class="btn btn-primary">
                Ajouter l'objectif
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, computed, nextTick } from 'vue'
  import { storeToRefs } from 'pinia'
  import { useOnboardingStore } from '@/stores/onboarding'
  import { formatCurrency } from '@/utils/formatters'
  import type { TypeObjectif, PrioriteObjectif } from '@/types'
  
  const onboardingStore = useOnboardingStore()
  const { userObjectifs, userAccounts } = storeToRefs(onboardingStore)
  const { addObjectif, removeObjectif } = onboardingStore
  
  const showAddForm = ref(false)
  const showIconPicker = ref(false)
  const addFormRef = ref<HTMLElement | null>(null)
  const allocations = ref<Record<string, number>>({})
  
  const availableIcons = [
    '🎯', '🛡️', '🚗', '🏠', '💰', '📈', '🎉', '✈️', 
    '👨‍👩‍👧‍👦', '💻', '📚', '🏥', '🎵', '🍽️', '🏋️', '🎨',
    '💍', '🏖️', '🎁', '📱', '⌚', '🚀', '💡', '📦'
  ]
  
  const newObjectif = ref({
    nom: '',
    montantCible: 0,
    type: 'SECURITE' as TypeObjectif,
    priorite: 'NORMALE' as PrioriteObjectif,
    icone: '🎯',
    couleur: '#4CAF50',
    description: ''
  })
  
  const getTypeLabel = (type: TypeObjectif): string => {
    const labels: Partial<Record<TypeObjectif, string>> = {
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
    }
    return labels[type] || type
  }
  
  const getPriorityLabel = (priority: PrioriteObjectif): string => {
    const labels: Partial<Record<PrioriteObjectif, string>> = {
      CRITIQUE: 'Critique',
      TRES_HAUTE: 'Très haute',
      HAUTE: 'Haute',
      NORMALE: 'Normale',
      BASSE: 'Basse'
    }
    return labels[priority] || priority
  }
  
  // Nouvelle fonction pour gérer l'affichage du formulaire avec scroll
  const toggleAddForm = async () => {
    const wasVisible = showAddForm.value
    showAddForm.value = !showAddForm.value
    
    // Si le formulaire devient visible, scroll vers lui
    if (!wasVisible && showAddForm.value) {
      // Attendre que le DOM soit mis à jour
      await nextTick()
      
      // Scroll vers le formulaire avec une animation fluide
      if (addFormRef.value) {
        addFormRef.value.scrollIntoView({
          behavior: 'smooth',
          block: 'start',
          inline: 'nearest'
        })
      }
    }
  }
  
  const selectIcon = (icon: string) => {
    newObjectif.value.icone = icon
    showIconPicker.value = false
  }
  
  const getTotalAllocations = (): number => {
    return Object.values(allocations.value).reduce((total, amount) => total + (amount || 0), 0)
  }
  
  const getTotalRepartitions = (repartitions: any[]): number => {
    return repartitions.reduce((total, rep) => total + rep.montant, 0)
  }
  
  const handleAddObjectif = () => {
    // Créer les répartitions basées sur les allocations
    const repartitions = Object.entries(allocations.value)
      .filter(([, amount]) => amount && amount > 0)
      .map(([accountId, amount]) => ({
        accountId,
        montant: amount
      }))
  
    const objectifData = {
      nom: newObjectif.value.nom,
      montantCible: newObjectif.value.montantCible,
      type: newObjectif.value.type,
      priorite: newObjectif.value.priorite,
      icone: newObjectif.value.icone,
      couleur: newObjectif.value.couleur,
      ...(newObjectif.value.description && { description: newObjectif.value.description }),
      repartitions
    }
  
    addObjectif(objectifData)
    resetForm()
  }
  
  const resetForm = () => {
    showAddForm.value = false
    showIconPicker.value = false
    allocations.value = {}
    newObjectif.value = {
      nom: '',
      montantCible: 0,
      type: 'SECURITE',
      priorite: 'NORMALE',
      icone: '🎯',
      couleur: '#4CAF50',
      description: ''
    }
  }
  
  // Fermer le sélecteur d'icône en cliquant ailleurs
  document.addEventListener('click', (e) => {
    const target = e.target as Element
    if (target && !target.closest('.icon-selector')) {
      showIconPicker.value = false
    }
  })
  </script>
  
  <style scoped>
  /* Animation d'apparition pour le formulaire */
  .add-objective-form {
    margin-top: 1.5rem;
    padding: 1.5rem;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-lg);
    animation: slideDown 0.3s ease-out;
    scroll-margin-top: 2rem; /* Espace au-dessus lors du scroll */
  }
  
  /* Animation d'apparition */
  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  /* Supprime les flèches des inputs numériques */
  .currency-field::-webkit-outer-spin-button,
  .currency-field::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
  
  .currency-field {
    -moz-appearance: textfield;
  }
  
  /* Améliorations pour les inputs avec devise */
  .currency-input {
    position: relative;
    display: flex;
    align-items: center;
  }
  
  .currency-input .form-input {
    padding-right: 35px;
    flex: 1;
    min-width: 0;
  }
  
  .currency-symbol {
    position: absolute;
    right: 12px;
    color: var(--text-secondary);
    font-weight: 600;
    pointer-events: none;
    font-size: 16px;
  }
  
  .currency-input .form-input.small {
    max-width: 120px;
  }
  
  /* Sélecteur d'icône personnalisé */
  .icon-selector {
    position: relative;
  }
  
  .selected-icon {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--spacing-md);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
    background: rgba(255, 255, 255, 0.05);
    cursor: pointer;
    transition: all var(--transition-fast);
  }
  
  .selected-icon:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: var(--primary-color);
  }
  
  .icon-display {
    font-size: 1.5rem;
  }
  
  .dropdown-arrow {
    color: var(--text-secondary);
    font-size: 0.8rem;
  }
  
  .icon-picker {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    z-index: 1000;
    background: var(--bg-secondary);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: var(--radius-md);
    padding: var(--spacing-md);
    display: grid;
    grid-template-columns: repeat(8, 1fr);
    gap: var(--spacing-sm);
    max-height: 200px;
    overflow-y: auto;
    box-shadow: var(--shadow-xl);
  }
  
  .icon-option {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: var(--radius-md);
    cursor: pointer;
    font-size: 1.2rem;
    transition: all var(--transition-fast);
  }
  
  .icon-option:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  
  .icon-option.selected {
    background: var(--primary-color);
  }
  
  /* Section allocation initiale */
  .initial-allocation-section {
    margin: var(--spacing-xl) 0;
    padding: var(--spacing-xl);
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-lg);
  }
  
  .section-title {
    color: var(--text-primary);
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: var(--spacing-sm);
  }
  
  .section-subtitle {
    color: var(--text-secondary);
    font-size: 0.9rem;
    margin-bottom: var(--spacing-lg);
  }
  
  .allocation-list {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }
  
  .allocation-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--spacing-md);
  }
  
  .account-info {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
    flex: 1;
  }
  
  .account-color {
    width: 4px;
    height: 30px;
    border-radius: var(--radius-sm);
  }
  
  .account-details {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }
  
  .account-name {
    color: var(--text-primary);
    font-weight: 500;
    font-size: 0.9rem;
  }
  
  .account-balance {
    color: var(--text-secondary);
    font-size: 0.8rem;
  }
  
  .allocation-input {
    min-width: 140px;
  }
  
  .allocation-summary {
    margin-top: var(--spacing-lg);
    padding-top: var(--spacing-md);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    text-align: right;
    color: var(--success-color);
  }
  
  .add-objective-btn {
    margin-top: 1rem;
    width: 100%;
    transition: all 0.2s ease;
  }
  
  .add-objective-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  }
  
  .objectives-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 1rem;
  }
  
  .objective-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
    transition: all var(--transition-fast);
  }
  
  .objective-item:hover {
    background: rgba(255, 255, 255, 0.08);
  }
  
  .objective-info {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .objective-icon {
    font-size: 1.5rem;
  }
  
  .objective-details h4 {
    color: var(--text-primary);
    font-weight: 600;
    margin-bottom: 0.25rem;
  }
  
  .objective-details p {
    color: var(--text-secondary);
    font-size: 0.9rem;
    margin-bottom: 0.25rem;
  }
  
  .objective-target {
    color: var(--success-color);
    font-weight: 600;
  }
  
  .objective-repartitions {
    margin-top: 0.25rem;
  }
  
  .objective-repartitions small {
    color: var(--text-muted);
  }
  
  .btn-remove {
    padding: 0.5rem;
    background: rgba(245, 101, 101, 0.1);
    border: 1px solid rgba(245, 101, 101, 0.2);
    border-radius: var(--radius-md);
    color: #fca5a5;
    cursor: pointer;
    transition: all var(--transition-fast);
  }
  
  .btn-remove:hover {
    background: rgba(245, 101, 101, 0.2);
    transform: scale(1.05);
  }
  
  .empty-objectives {
    text-align: center;
    padding: 2rem;
    color: var(--text-secondary);
    font-style: italic;
  }
  
  .form-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
    margin-top: 1.5rem;
  }
  
  @media (max-width: 768px) {
    .form-actions {
      flex-direction: column-reverse;
    }
    
    .allocation-item {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .allocation-input {
      width: 100%;
      min-width: auto;
    }
  }
  </style>