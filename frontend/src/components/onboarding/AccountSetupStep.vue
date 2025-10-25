<template>
    <div class="step-content">
      <div class="step-header">
        <h1 class="step-title">Configuration des comptes</h1>
        <p class="step-subtitle">
          Ajoutez vos comptes bancaires avec leurs soldes actuels
        </p>
      </div>
  
      <div class="accounts-section">
        <div v-if="userAccounts.length > 0" class="accounts-list">
          <div 
            v-for="(account, index) in userAccounts"
            :key="account.tempId"
            class="account-item"
          >
            <div class="account-info">
              <div 
                class="bank-color"
                :style="{ backgroundColor: account.banque.couleurTheme }"
              ></div>
              <div class="account-details">
                <h4>{{ account.nom }}</h4>
                <p>{{ account.banque.nom }} - {{ getTypeLabel(account.type) }}</p>
                <span class="account-solde">{{ formatCurrency(account.soldeTotal) }}</span>
                <div v-if="account.isMainForCharges" class="main-account-badge">
                  🏠 Compte principal charges fixes
                </div>
              </div>
            </div>
            <button @click="removeAccount(index)" class="btn-remove" title="Supprimer">
              🗑️
            </button>
          </div>
        </div>
  
        <div v-else class="empty-accounts">
          <p>Aucun compte ajouté pour le moment</p>
        </div>
  
        <button @click="toggleAddForm" class="btn btn-primary add-account-btn">
          {{ showAddForm ? 'Masquer' : '+ Ajouter un compte' }}
        </button>
  
        <!-- Formulaire d'ajout -->
        <div v-if="showAddForm" ref="addFormRef" class="add-account-form">
          <form @submit.prevent="handleAddAccount" class="form">
            <div class="form-group">
              <label class="form-label">Banque *</label>
              <select v-model="newAccount.banqueId" class="form-select" required @change="updateAccountNamePlaceholder">
                <option value="">Choisissez une banque</option>
                <option 
                  v-for="bank in selectedBanks" 
                  :key="bank.id" 
                  :value="bank.id"
                >
                  {{ bank.nom }}
                </option>
              </select>
            </div>
  
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Nom du compte (optionnel)</label>
                <input 
                  v-model="newAccount.nom"
                  type="text" 
                  class="form-input"
                  :placeholder="accountNamePlaceholder"
                  maxlength="100"
                >
              </div>
  
              <div class="form-group">
                <label class="form-label">Type de compte *</label>
                <select v-model="newAccount.type" class="form-select" required @change="updateAccountNamePlaceholder">
                  <option value="COMPTE_COURANT">Compte Courant</option>
                  <option value="LIVRET_A">Livret A</option>
                  <option value="LDDS">LDDS</option>
                  <option value="LIVRET_JEUNE">Livret Jeune</option>
                  <option value="LEP">LEP</option>
                  <option value="PEL">PEL</option>
                  <option value="CEL">CEL</option>
                  <option value="CSL">CSL</option>
                  <option value="PEA">PEA</option>
                  <option value="PEA_PME">PEA-PME</option>
                  <option value="ASSURANCE_VIE">Assurance Vie</option>
                  <option value="COMPTE_TITRE">Compte Titre</option>
                  <option value="CRYPTO">Crypto</option>
                  <option value="OR_METAUX">Or & Métaux</option>
                  <option value="AUTRE">Autre</option>
                </select>
              </div>
            </div>
  
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Solde actuel *</label>
                <div class="currency-input">
                  <input 
                    v-model.number="newAccount.soldeTotal"
                    type="number" 
                    class="form-input currency-field"
                    placeholder="1500"
                    step="0.01"
                    required
                  >
                  <span class="currency-symbol">€</span>
                </div>
              </div>
  
              <div class="form-group">
                <label class="form-label">Taux (optionnel)</label>
                <div class="percent-input">
                  <input
                    v-model.number="newAccount.taux"
                    type="number"
                    class="form-input percent-field"
                    placeholder="0"
                    step="0.1"
                    min="0"
                    max="20"
                  >
                  <span class="percent-symbol">%</span>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">Plafond (optionnel)</label>
                <div class="currency-input">
                  <input
                    v-model.number="newAccount.plafond"
                    type="number"
                    class="form-input currency-field"
                    placeholder="22950"
                    step="0.01"
                    min="0"
                  >
                  <span class="currency-symbol">€</span>
                </div>
              </div>
            </div>
  
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Options</label>
                <div class="checkbox-group">
                  <div class="custom-checkbox-wrapper">
                    <input 
                      v-model="newAccount.isMainForCharges"
                      type="checkbox" 
                      id="mainForCharges"
                      class="custom-checkbox-input"
                    >
                    <label for="mainForCharges" class="custom-checkbox-label">
                      <div class="checkbox-box">
                        <svg class="checkbox-icon" viewBox="0 0 20 20" fill="currentColor">
                          <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                        </svg>
                      </div>
                      <div class="checkbox-content">
                        <span class="checkbox-title">
                          🏠 Compte principal pour charges fixes
                        </span>
                        <span class="checkbox-description">
                          Ce compte sera présélectionné lors de l'ajout de charges fixes
                        </span>
                      </div>
                    </label>
                  </div>
                </div>
              </div>
              <div></div> <!-- Colonne vide pour équilibrer -->
            </div>
  
            <div class="form-actions">
              <button type="button" @click="resetForm" class="btn btn-secondary">
                Annuler
              </button>
              <button type="submit" class="btn btn-primary">
                Ajouter le compte
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
  import type { TypeCompte } from '@/types'
  
  const onboardingStore = useOnboardingStore()
  const { selectedBanks, userAccounts, availableBanks } = storeToRefs(onboardingStore)
  const { addAccount, removeAccount } = onboardingStore
  
  const showAddForm = ref(false)
  const addFormRef = ref<HTMLElement | null>(null)
  
  const newAccount = ref({
    banqueId: '',
    nom: '',
    type: 'COMPTE_COURANT' as TypeCompte,
    soldeTotal: 0,
    taux: 0,
    plafond: undefined as number | undefined,
    isMainForCharges: false
  })
  
  const getTypeLabel = (type: TypeCompte): string => {
    const labels: Partial<Record<TypeCompte, string>> = {
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
      AUTRE: 'Autre'
    }
    return labels[type] || type
  }
  
  // Calculer le placeholder dynamique basé sur la banque et le type sélectionnés
  const accountNamePlaceholder = computed(() => {
    const selectedBank = availableBanks.value.find(b => b.id === newAccount.value.banqueId)
    const bankName = selectedBank ? selectedBank.nom : 'Banque'
    const typeLabel = getTypeLabel(newAccount.value.type)
    return `${bankName} ${typeLabel}`
  })
  
  const updateAccountNamePlaceholder = () => {
    // Force la réactivité du placeholder
    // Cette méthode est appelée quand la banque ou le type change
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
  
  const handleAddAccount = () => {
    const selectedBank = availableBanks.value.find(b => b.id === newAccount.value.banqueId)
    if (!selectedBank) return
  
    // Si ce compte est marqué comme principal, démarquer les autres
    if (newAccount.value.isMainForCharges) {
      userAccounts.value.forEach(account => {
        account.isMainForCharges = false
      })
    }
  
    // Si le nom est vide, utiliser la valeur par défaut
    const accountName = newAccount.value.nom.trim() || accountNamePlaceholder.value
  
    const accountData = {
      banque: selectedBank,
      nom: accountName,
      type: newAccount.value.type,
      soldeTotal: newAccount.value.soldeTotal,
      taux: newAccount.value.taux || 0,
      ...(newAccount.value.plafond != null && { plafond: newAccount.value.plafond }),
      isMainForCharges: newAccount.value.isMainForCharges
    }

    addAccount(accountData)
    resetForm()
  }
  
  const resetForm = () => {
    showAddForm.value = false
    newAccount.value = {
      banqueId: '',
      nom: '',
      type: 'COMPTE_COURANT',
      soldeTotal: 0,
      taux: 0,
      plafond: undefined,
      isMainForCharges: false
    }
  }
  </script>
  
  <style scoped>
    /* Animation d'apparition pour le formulaire */
    .add-account-form {
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

    /* Supprime les flèches des inputs de type number */
    .currency-field::-webkit-outer-spin-button,
    .currency-field::-webkit-inner-spin-button,
    .percent-field::-webkit-outer-spin-button,
    .percent-field::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
    }

    .currency-field,
    .percent-field {
    -moz-appearance: textfield;
    }

    /* Améliorations pour les inputs avec devise/pourcentage */
    .currency-input,
    .percent-input {
    position: relative;
    display: flex;
    align-items: center;
    }

    .currency-input .form-input,
    .percent-input .form-input {
    padding-right: 35px;
    flex: 1;
    min-width: 0;
    }

    .currency-symbol,
    .percent-symbol {
    position: absolute;
    right: 12px;
    color: var(--text-secondary);
    font-weight: 600;
    pointer-events: none;
    font-size: 16px;
    }

    .add-account-btn {
    margin-top: 1rem;
    width: 100%;
    transition: all 0.2s ease;
    }
    
    .add-account-btn:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    .accounts-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 1rem;
    }

    .account-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
    transition: all var(--transition-fast);
    }

    .account-item:hover {
    background: rgba(255, 255, 255, 0.08);
    }

    .account-info {
    display: flex;
    align-items: center;
    gap: 1rem;
    }

    .bank-color {
    width: 4px;
    height: 40px;
    border-radius: var(--radius-sm);
    }

    .account-details h4 {
    color: var(--text-primary);
    font-weight: 600;
    margin-bottom: 0.25rem;
    }

    .account-details p {
    color: var(--text-secondary);
    font-size: 0.9rem;
    margin-bottom: 0.25rem;
    }

    .account-solde {
    color: var(--success-color);
    font-weight: 600;
    }

    /* Badge pour les comptes existants (dans la liste des comptes) */
    .account-details .main-account-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    background: rgba(16, 185, 129, 0.1);
    color: var(--success-color);
    padding: 0.125rem 0.5rem;
    border-radius: 8px;
    font-size: 0.75rem;
    font-weight: 500;
    margin-top: 0.5rem;
    border: 1px solid rgba(16, 185, 129, 0.2);
    /* Force le badge sur une nouvelle ligne */
    display: block;
    width: fit-content;
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

    .empty-accounts {
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

    /* === STYLES POUR LA CHECKBOX ÉLÉGANTE === */

    /* Checkbox personnalisée élégante */
    .custom-checkbox-wrapper {
    position: relative;
    }

    .custom-checkbox-input {
    position: absolute;
    opacity: 0;
    width: 0;
    height: 0;
    }

    .custom-checkbox-label {
    display: flex;
    align-items: flex-start;
    gap: 1rem;
    cursor: pointer;
    padding: 1rem;
    border-radius: var(--radius-lg);
    background: rgba(255, 255, 255, 0.02);
    border: 1px solid rgba(255, 255, 255, 0.05);
    transition: all 0.2s ease;
    position: relative;
    overflow: hidden;
    }

    /* Effet de fond gradient subtil */
    .custom-checkbox-label::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, var(--primary-color), var(--success-color));
    opacity: 0;
    transition: all 0.2s ease;
    z-index: 0;
    }

    .custom-checkbox-label:hover {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .custom-checkbox-input:checked + .custom-checkbox-label {
    background: rgba(59, 130, 246, 0.1);
    border-color: var(--primary-color);
    }

    .custom-checkbox-input:checked + .custom-checkbox-label::before {
    opacity: 0.05;
    }

    .checkbox-box {
    position: relative;
    width: 20px;
    height: 20px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-radius: 4px;
    background: transparent;
    transition: all 0.2s ease;
    flex-shrink: 0;
    margin-top: 2px;
    z-index: 1;
    }

    .custom-checkbox-input:checked + .custom-checkbox-label .checkbox-box {
    background: var(--primary-color);
    border-color: var(--primary-color);
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
    }

    .checkbox-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) scale(0);
    width: 12px;
    height: 12px;
    color: white;
    transition: transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    .custom-checkbox-input:checked + .custom-checkbox-label .checkbox-icon {
    transform: translate(-50%, -50%) scale(1);
    }

    .checkbox-content {
    flex: 1;
    z-index: 1;
    position: relative;
    }

    .checkbox-title {
    display: block;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 0.25rem;
    font-size: 1rem;
    transition: color 0.2s ease;
    }

    .checkbox-description {
    display: block;
    color: var(--text-secondary);
    font-size: 0.875rem;
    line-height: 1.4;
    }

    .custom-checkbox-input:checked + .custom-checkbox-label .checkbox-title {
    color: var(--primary-color);
    }

    /* Animation de pulse au focus */
    .custom-checkbox-input:focus + .custom-checkbox-label .checkbox-box {
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
    }

    /* Effet de brillance au survol */
    .custom-checkbox-label::after {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    right: -50%;
    bottom: -50%;
    background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.05), transparent);
    transform: translateX(-100%) translateY(-100%) rotate(45deg);
    transition: transform 0.6s ease;
    pointer-events: none;
    z-index: 1;
    }

    .custom-checkbox-label:hover::after {
    transform: translateX(100%) translateY(100%) rotate(45deg);
    }

    @media (max-width: 768px) {
    .form-actions {
        flex-direction: column-reverse;
    }
    }
  </style>