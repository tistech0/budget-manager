<template>
    <div class="step-content">
        <div class="step-header">
            <h1 class="step-title">Vos charges fixes</h1>
            <p class="step-subtitle">
                Configurez vos dépenses récurrentes pour un suivi automatique de votre budget
            </p>
        </div>

        <div class="charges-fixes-section">
            <!-- Suggestions de charges communes (toujours visible) -->
            <div class="suggestions-section">
                <h4>Charges courantes à configurer :</h4>
                <div class="suggestions-grid">
                    <button v-for="suggestion in commonCharges" :key="suggestion.nom"
                        @click="applySuggestion(suggestion)" class="suggestion-card">
                        <span class="suggestion-icon">{{ suggestion.icon }}</span>
                        <span class="suggestion-name">{{ suggestion.nom }}</span>
                    </button>
                </div>
            </div>

            <!-- Aperçu budget charges fixes -->
            <div v-if="budgetChargesFixes > 0" class="budget-overview">
                <div class="budget-card">
                    <div class="budget-header">
                        <h4>Budget charges fixes</h4>
                        <span class="budget-percentage">{{ progressionBudget }}%</span>
                    </div>
                    <div class="budget-progress">
                        <div class="budget-bar">
                            <div class="budget-fill" :style="{
                                width: `${Math.min(progressionBudget, 100)}%`,
                                backgroundColor: progressionBudget > 100 ? '#f56565' : '#48bb78'
                            }"></div>
                        </div>
                        <div class="budget-amounts">
                            <span class="budget-used">{{ formatCurrency(totalMensuel) }}</span>
                            <span class="budget-separator">/</span>
                            <span class="budget-allocated">{{ formatCurrency(budgetChargesFixes) }}</span>
                        </div>
                    </div>
                    <div v-if="progressionBudget > 100" class="budget-warning">
                        ⚠️ Dépassement de {{ formatCurrency(totalMensuel - budgetChargesFixes) }}
                    </div>
                </div>
            </div>

            <!-- Liste des charges existantes -->
            <div v-if="chargesFixes.length > 0" class="charges-list">
                <h3>Charges configurées</h3>
                <div class="charges-grid">
                    <div v-for="charge in chargesFixes" :key="charge.id" class="charge-card">
                        <div class="charge-header">
                            <div class="charge-info">
                                <span class="charge-category">{{ getCategoryIcon(charge.categorie) }}</span>
                                <div class="charge-details">
                                    <h4 class="charge-name">{{ charge.nom }}</h4>
                                    <p class="charge-account">{{ charge.compte?.nom }}</p>
                                </div>
                            </div>
                            <div class="charge-actions">
                                <button @click="editCharge(charge)" class="btn-edit" title="Modifier">
                                    ✏️
                                </button>
                                <button @click="deleteCharge(charge)" class="btn-delete" title="Supprimer">
                                    🗑️
                                </button>
                            </div>
                        </div>

                        <div class="charge-summary">
                            <div class="charge-amount">{{ formatCurrency(charge.montant) }}</div>
                            <div class="charge-frequency">{{ getFrequencyLabel(charge.frequence) }}</div>
                            <div class="charge-date">{{ formatJourPrelevement(charge.jourPrelevement) }}</div>
                        </div>
                    </div>
                </div>

                <div class="charges-total">
                    <div class="total-card">
                        <span class="total-label">Total mensuel estimé :</span>
                        <span class="total-amount">{{ formatCurrency(totalMensuel) }}</span>
                    </div>
                </div>
            </div>

            <div v-else class="empty-charges">
                <div class="empty-illustration">📋</div>
                <h3>Aucune charge fixe configurée</h3>
                <p>Ajoutez vos premières charges récurrentes pour organiser votre budget</p>
            </div>

            <!-- Bouton d'ajout -->
            <button @click="toggleAddForm" class="btn btn-primary add-charge-btn">
                {{ showAddForm ? 'Masquer' : '+ Ajouter une charge fixe' }}
            </button>

            <!-- Formulaire d'ajout/modification -->
            <div v-if="showAddForm" ref="addFormRef" class="add-charge-form">
                <h4>{{ editingCharge ? 'Modifier' : 'Nouvelle' }} charge fixe</h4>

                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">Nom de la charge *</label>
                            <input v-model="formData.nom" type="text" class="form-input"
                                placeholder="Loyer, EDF, Assurance..." required maxlength="100">
                        </div>

                        <div class="form-group">
                            <label class="form-label">Montant *</label>
                            <div class="currency-input">
                                <input v-model.number="formData.montant" type="number" class="form-input currency-field"
                                    placeholder="650" step="0.01" min="0" required>
                                <span class="currency-symbol">€</span>
                            </div>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">Catégorie *</label>
                            <select v-model="formData.categorie" class="form-select" required>
                                <option value="">Choisissez...</option>
                                <option value="LOYER">🏠 Loyer</option>
                                <option value="ASSURANCE">🛡️ Assurance</option>
                                <option value="ABONNEMENT">📱 Abonnement</option>
                                <option value="CREDIT_IMMOBILIER">🏡 Crédit immobilier</option>
                                <option value="CREDIT_CONSO">💳 Crédit conso</option>
                                <option value="IMPOTS">🏛️ Impôts</option>
                                <option value="MUTUELLE">🏥 Mutuelle</option>
                                <option value="AUTRE">📦 Autre</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Compte de prélèvement *</label>
                            <select v-model="formData.compteId" class="form-select" required>
                                <option value="">Choisissez un compte...</option>
                                <option v-for="compte in userAccounts" :key="compte.tempId"
                                    :value="compte.tempId">
                                    {{ compte.nom }} ({{ compte.banque.nom }})
                                </option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">Fréquence *</label>
                            <select v-model="formData.frequence" class="form-select" required>
                                <option value="MENSUELLE">📅 Mensuelle</option>
                                <option value="BIMESTRIELLE">📆 Bimestrielle (2 mois)</option>
                                <option value="TRIMESTRIELLE">🗓️ Trimestrielle (3 mois)</option>
                                <option value="SEMESTRIELLE">📊 Semestrielle (6 mois)</option>
                                <option value="ANNUELLE">📋 Annuelle</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Jour de prélèvement *</label>
                            <select v-model="formData.jourPrelevement" class="form-select" required>
                                <option value="">Jour...</option>
                                <option v-for="day in 31" :key="day" :value="day">
                                    {{ day }}{{ day === 1 ? 'er' : '' }} du mois
                                </option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">Date de début *</label>
                            <input v-model="formData.dateDebut" type="date" class="form-input" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Date de fin (optionnelle)</label>
                            <input v-model="formData.dateFin" type="date" class="form-input">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Description (optionnelle)</label>
                        <textarea v-model="formData.description" class="form-textarea"
                            placeholder="Détails supplémentaires..." maxlength="500" rows="3"></textarea>
                    </div>

                    <div class="form-actions">
                        <button type="button" @click="resetForm" class="btn btn-secondary">
                            Annuler
                        </button>
                        <button type="submit" class="btn btn-primary">
                            {{ editingCharge ? 'Modifier' : 'Ajouter' }}
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
import { useOnboardingStore, type ChargeFixeFormData } from '@/stores/onboarding'
import { formatCurrency } from '@/utils/formatters'
import type { TypeTransaction, FrequenceCharge } from '@/types'

const onboardingStore = useOnboardingStore()
const { userAccounts, budgetConfig, userData, userChargesFixes } = storeToRefs(onboardingStore)
const { addChargeFixe, updateChargeFixe, removeChargeFixe } = onboardingStore

const chargesFixes = userChargesFixes

const showAddForm = ref(false)
const editingCharge = ref<ChargeFixeFormData | null>(null)
const addFormRef = ref<HTMLElement | null>(null)

const formData = ref({
    nom: '',
    montant: 0,
    categorie: '' as TypeTransaction,
    compteId: '',
    frequence: 'MENSUELLE' as FrequenceCharge,
    jourPrelevement: 1,
    dateDebut: new Date().toISOString().split('T')[0],
    dateFin: '',
    description: ''
})

const commonCharges = [
    { nom: 'Loyer', icon: '🏠', categorie: 'LOYER', montant: 800 },
    { nom: 'Électricité', icon: '⚡', categorie: 'ABONNEMENT', montant: 60 },
    { nom: 'Internet', icon: '📡', categorie: 'ABONNEMENT', montant: 30 },
    { nom: 'Assurance habitation', icon: '🛡️', categorie: 'ASSURANCE', montant: 25 },
    { nom: 'Téléphone', icon: '📱', categorie: 'ABONNEMENT', montant: 20 },
    { nom: 'Mutuelle', icon: '🏥', categorie: 'MUTUELLE', montant: 45 }
]

const totalMensuel = computed(() => {
    return chargesFixes.value.reduce((total, charge) => {
        const montantMensuel = getMontantMensuel(charge.montant, charge.frequence)
        return total + montantMensuel
    }, 0)
})

const budgetChargesFixes = computed(() => {
    if (!userData.value.salaireMensuelNet || !budgetConfig.value.pourcentageChargesFixes) {
        return 0
    }
    return (userData.value.salaireMensuelNet * budgetConfig.value.pourcentageChargesFixes) / 100
})

const progressionBudget = computed(() => {
    if (budgetChargesFixes.value === 0) return 0
    return Math.round((totalMensuel.value / budgetChargesFixes.value) * 100)
})

const getDefaultAccount = computed(() => {
    return userAccounts.value.find(compte =>
        compte.isMainForCharges || compte.type === 'COMPTE_COURANT'
    ) || userAccounts.value[0]
})

const getCategoryIcon = (categorie: TypeTransaction): string => {
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

const getFrequencyLabel = (frequence: FrequenceCharge): string => {
    const labels = {
        MENSUELLE: 'Mensuelle',
        BIMESTRIELLE: 'Bimestrielle',
        TRIMESTRIELLE: 'Trimestrielle',
        SEMESTRIELLE: 'Semestrielle',
        ANNUELLE: 'Annuelle'
    }
    return labels[frequence] || frequence
}

const getMontantMensuel = (montant: number, frequence: FrequenceCharge): number => {
    const coefficients = {
        MENSUELLE: 1,
        BIMESTRIELLE: 1 / 2,
        TRIMESTRIELLE: 1 / 3,
        SEMESTRIELLE: 1 / 6,
        ANNUELLE: 1 / 12
    }
    return montant * (coefficients[frequence] || 1)
}

const formatJourPrelevement = (jour: number): string => {
  if (jour === 1) return '1er du mois'
  return `Le ${jour} du mois`
}

const toggleAddForm = async () => {
    const wasVisible = showAddForm.value
    showAddForm.value = !showAddForm.value

    if (!wasVisible && showAddForm.value) {
        // Set default account when opening the form
        const defaultAccount = getDefaultAccount.value
        if (defaultAccount && defaultAccount.tempId && !formData.value.compteId) {
            formData.value.compteId = defaultAccount.tempId
        }

        await nextTick()

        if (addFormRef.value) {
            addFormRef.value.scrollIntoView({
                behavior: 'smooth',
                block: 'start',
                inline: 'nearest'
            })
        }
    }
}

const applySuggestion = async (suggestion: any) => {
    formData.value.nom = suggestion.nom
    formData.value.categorie = suggestion.categorie
    formData.value.montant = suggestion.montant

    const defaultAccount = getDefaultAccount.value
    if (defaultAccount && defaultAccount.tempId) {
        formData.value.compteId = defaultAccount.tempId
    }

    showAddForm.value = true

    await nextTick()

    if (addFormRef.value) {
        addFormRef.value.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
            inline: 'nearest'
        })
    }
}

const editCharge = async (charge: any) => {
    editingCharge.value = charge
    formData.value = {
        nom: charge.nom,
        montant: charge.montant,
        categorie: charge.categorie,
        compteId: charge.compte?.tempId || charge.compte?.id || '',
        frequence: charge.frequence,
        jourPrelevement: charge.jourPrelevement,
        dateDebut: charge.dateDebut,
        dateFin: charge.dateFin || '',
        description: charge.description || ''
    }
    showAddForm.value = true

    await nextTick()

    if (addFormRef.value) {
        addFormRef.value.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
            inline: 'nearest'
        })
    }
}

const deleteCharge = (charge: any) => {
    if (confirm(`Supprimer la charge "${charge.nom}" ?`)) {
        // ✅ UTILISER la fonction du store
        removeChargeFixe(charge.id)
    }
}

const handleSubmit = () => {
    if (editingCharge.value && editingCharge.value.id) {
        updateChargeFixe(editingCharge.value.id, formData.value)
    } else {
        addChargeFixe(formData.value)
    }

    resetForm()
}

const resetForm = () => {
    showAddForm.value = false
    editingCharge.value = null

    // Set default account when resetting form
    const defaultAccount = getDefaultAccount.value
    const defaultCompteId = (defaultAccount && defaultAccount.tempId) ? defaultAccount.tempId : ''

    formData.value = {
        nom: '',
        montant: 0,
        categorie: '' as TypeTransaction,
        compteId: defaultCompteId,
        frequence: 'MENSUELLE',
        jourPrelevement: 1,
        dateDebut: new Date().toISOString().split('T')[0],
        dateFin: '',
        description: ''
    }
}
</script>

<style scoped>
/* Animation d'apparition pour le formulaire */
.add-charge-form {
    padding: 2rem;
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-lg);
    animation: slideDown 0.3s ease-out;
    scroll-margin-top: 2rem;
    /* Espace au-dessus lors du scroll */
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

.charges-fixes-section {
    display: flex;
    flex-direction: column;
    gap: 2rem;
}

/* Suggestions de charges (maintenant toujours visibles) */
.suggestions-section {
    margin-bottom: 1rem;
}

.suggestions-section h4 {
    color: var(--text-primary);
    font-size: 1.1rem;
    font-weight: 600;
    margin-bottom: 1rem;
}

.suggestions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
    gap: 0.75rem;
}

.suggestion-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    padding: 1rem;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
    cursor: pointer;
    transition: all var(--transition-fast);
}

.suggestion-card:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: var(--primary-color);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.suggestion-icon {
    font-size: 1.5rem;
}

.suggestion-name {
    color: var(--text-primary);
    font-size: 0.9rem;
    font-weight: 500;
    text-align: center;
}

/* Aperçu budget charges fixes */
.budget-overview {
    margin-bottom: 1.5rem;
}

.budget-card {
    background: rgba(102, 126, 234, 0.1);
    border: 1px solid rgba(102, 126, 234, 0.3);
    border-radius: var(--radius-lg);
    padding: 1.5rem;
}

.budget-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.budget-header h4 {
    color: var(--text-primary);
    font-size: 1.1rem;
    font-weight: 600;
    margin: 0;
}

.budget-percentage {
    color: var(--primary-color);
    font-weight: 700;
    font-size: 1.2rem;
}

.budget-progress {
    margin-bottom: 0.75rem;
}

.budget-bar {
    width: 100%;
    height: 8px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 0.5rem;
}

.budget-fill {
    height: 100%;
    border-radius: 4px;
    transition: all var(--transition-normal);
}

.budget-amounts {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.9rem;
}

.budget-used {
    color: var(--text-primary);
    font-weight: 600;
}

.budget-separator {
    color: var(--text-secondary);
}

.budget-allocated {
    color: var(--text-secondary);
    font-weight: 500;
}

.budget-warning {
    color: #fca5a5;
    font-size: 0.9rem;
    text-align: center;
    margin-top: 0.5rem;
    font-weight: 500;
}

.charges-list h3 {
    color: var(--text-primary);
    font-size: 1.2rem;
    font-weight: 600;
    margin-bottom: 1rem;
}

.charges-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.charge-card {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-lg);
    padding: 1.5rem;
    transition: all var(--transition-fast);
}

.charge-card:hover {
    background: rgba(255, 255, 255, 0.08);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
}

.charge-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1rem;
}

.charge-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    flex: 1;
}

.charge-category {
    font-size: 1.5rem;
    flex-shrink: 0;
}

.charge-details {
    flex: 1;
}

.charge-name {
    color: var(--text-primary);
    font-weight: 600;
    font-size: 1rem;
    margin-bottom: 0.25rem;
}

.charge-account {
    color: var(--text-secondary);
    font-size: 0.9rem;
}

.charge-actions {
    display: flex;
    gap: 0.5rem;
}

.btn-edit,
.btn-delete {
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: var(--radius-md);
    cursor: pointer;
    transition: all var(--transition-fast);
}

.btn-edit:hover {
    background: rgba(102, 126, 234, 0.2);
    color: var(--primary-color);
}

.btn-delete:hover {
    background: rgba(245, 101, 101, 0.2);
    color: #fca5a5;
}

.charge-summary {
    display: grid;
    grid-template-columns: 1fr auto auto;
    gap: 1rem;
    align-items: center;
}

.charge-amount {
    color: var(--success-color);
    font-weight: 600;
    font-size: 1.1rem;
}

.charge-frequency,
.charge-date {
    color: var(--text-secondary);
    font-size: 0.9rem;
    text-align: center;
}

.charges-total {
    margin-top: 1rem;
}

.total-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1.5rem;
    background: rgba(102, 126, 234, 0.1);
    border: 1px solid rgba(102, 126, 234, 0.3);
    border-radius: var(--radius-lg);
}

.total-label {
    color: var(--text-primary);
    font-weight: 600;
    font-size: 1rem;
}

.total-amount {
    color: var(--primary-color);
    font-weight: 700;
    font-size: 1.2rem;
}

.empty-charges {
    text-align: center;
    padding: 3rem 2rem;
    color: var(--text-secondary);
}

.empty-illustration {
    font-size: 4rem;
    margin-bottom: 1rem;
}

.empty-charges h3 {
    color: var(--text-primary);
    font-size: 1.3rem;
    margin-bottom: 0.5rem;
}

.add-charge-btn {
    width: 100%;
    margin: 1rem 0;
    transition: all 0.2s ease;
}

.add-charge-btn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.add-charge-form h4 {
    color: var(--text-primary);
    font-size: 1.2rem;
    font-weight: 600;
    margin-bottom: 1.5rem;
}

.form-actions {
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
    margin-top: 2rem;
}

@media (max-width: 768px) {
    .charges-grid {
        grid-template-columns: 1fr;
    }

    .charge-summary {
        grid-template-columns: 1fr;
        gap: 0.5rem;
        text-align: center;
    }

    .suggestions-grid {
        grid-template-columns: repeat(2, 1fr);
    }

    .form-actions {
        flex-direction: column-reverse;
    }
}
</style>