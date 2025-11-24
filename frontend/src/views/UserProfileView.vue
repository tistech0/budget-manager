<template>
  <MainLayout>
    <div class="user-profile-view">
      <!-- Page Title Header -->
    <header class="page-header">
      <div class="container">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">👤 Mon Profil</h1>
            <p class="page-subtitle">Gérez vos informations personnelles et vos paramètres</p>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="page-main">
      <div class="container">
        <!-- Loading State -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Chargement de votre profil...</p>
        </div>

        <!-- Form Content -->
        <div v-else class="profile-form">
          <!-- Personal Information Section -->
          <section class="form-section glass-card">
            <div class="section-header">
              <h2 class="section-title">📋 Informations Personnelles</h2>
            </div>
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label">Prénom</label>
                <input
                  v-model="formData.prenom"
                  type="text"
                  class="form-input"
                  placeholder="Votre prénom"
                  @input="markAsChanged"
                />
              </div>
              <div class="form-group">
                <label class="form-label">Nom</label>
                <input
                  v-model="formData.nom"
                  type="text"
                  class="form-input"
                  placeholder="Votre nom"
                  @input="markAsChanged"
                />
              </div>
            </div>
          </section>

          <!-- Financial Information Section -->
          <section class="form-section glass-card">
            <div class="section-header">
              <h2 class="section-title">💰 Informations Financières</h2>
            </div>
            <div class="form-grid">
              <div class="form-group">
                <label class="form-label">Salaire mensuel net (€)</label>
                <input
                  v-model.number="formData.salaireMensuelNet"
                  type="number"
                  class="form-input"
                  placeholder="2043"
                  min="0"
                  step="0.01"
                  @input="markAsChanged"
                />
                <span v-if="errors.salaireMensuelNet" class="error-message">{{ errors.salaireMensuelNet }}</span>
              </div>
              <div class="form-group">
                <label class="form-label">Jour de paie</label>
                <input
                  v-model.number="formData.jourPaie"
                  type="number"
                  class="form-input"
                  placeholder="25"
                  min="1"
                  max="31"
                  @input="markAsChanged"
                />
                <span v-if="errors.jourPaie" class="error-message">{{ errors.jourPaie }}</span>
              </div>
              <div class="form-group">
                <label class="form-label">Découvert autorisé (€)</label>
                <input
                  v-model.number="formData.decouvertAutorise"
                  type="number"
                  class="form-input"
                  placeholder="100"
                  min="0"
                  step="0.01"
                  @input="markAsChanged"
                />
              </div>
              <div class="form-group">
                <label class="form-label">Objectif compte courant (€)</label>
                <input
                  v-model.number="formData.objectifCompteCourant"
                  type="number"
                  class="form-input"
                  placeholder="2000"
                  min="0"
                  step="0.01"
                  @input="markAsChanged"
                />
              </div>
            </div>
          </section>

          <!-- Budget Distribution Section -->
          <section class="form-section glass-card">
            <div class="section-header">
              <h2 class="section-title">📊 Répartition Budget</h2>
              <div class="budget-total" :class="{ 'valid': budgetTotalValid, 'invalid': !budgetTotalValid }">
                Total: {{ budgetTotal }}%
                <span v-if="budgetTotalValid" class="status-icon">✓</span>
                <span v-else class="status-icon">✗</span>
              </div>
            </div>
            <div class="sliders-container">
              <div class="slider-group">
                <div class="slider-header">
                  <label class="form-label">Charges fixes</label>
                  <span class="slider-value">{{ formData.pourcentageChargesFixes }}%</span>
                </div>
                <input
                  v-model.number="formData.pourcentageChargesFixes"
                  type="range"
                  class="form-slider"
                  min="0"
                  max="100"
                  step="1"
                  @input="markAsChanged"
                />
                <p class="calculated-amount">{{ formatCurrency(calculatedChargesFixes) }}</p>
              </div>
              <div class="slider-group">
                <div class="slider-header">
                  <label class="form-label">Dépenses variables</label>
                  <span class="slider-value">{{ formData.pourcentageDepensesVariables }}%</span>
                </div>
                <input
                  v-model.number="formData.pourcentageDepensesVariables"
                  type="range"
                  class="form-slider"
                  min="0"
                  max="100"
                  step="1"
                  @input="markAsChanged"
                />
                <p class="calculated-amount">{{ formatCurrency(calculatedDepensesVariables) }}</p>
              </div>
              <div class="slider-group">
                <div class="slider-header">
                  <label class="form-label">Épargne</label>
                  <span class="slider-value">{{ formData.pourcentageEpargne }}%</span>
                </div>
                <input
                  v-model.number="formData.pourcentageEpargne"
                  type="range"
                  class="form-slider"
                  min="0"
                  max="100"
                  step="1"
                  @input="markAsChanged"
                />
                <p class="calculated-amount">{{ formatCurrency(calculatedEpargne) }}</p>
              </div>
            </div>
            <div v-if="!budgetTotalValid" class="warning-message">
              ⚠️ Le total des pourcentages doit être égal à 100%
            </div>
          </section>

          <!-- Charges Fixes Section -->
          <section class="form-section glass-card">
            <div class="section-header">
              <h2 class="section-title">💳 Charges Fixes</h2>
              <button @click="showAddChargeModal = true" class="btn btn-primary btn-sm">
                ➕ Ajouter
              </button>
            </div>

            <!-- Empty state -->
            <div v-if="chargesFixes.length === 0" class="empty-charges">
              <span class="empty-icon">📋</span>
              <p>Aucune charge fixe configurée</p>
            </div>

            <!-- Charges list -->
            <div v-else class="charges-list">
              <div v-for="charge in chargesFixes" :key="charge.id" class="charge-item">
                <div class="charge-info">
                  <div class="charge-icon">{{ getCategoryIcon(charge.categorie) }}</div>
                  <div class="charge-details">
                    <h4 class="charge-name">{{ charge.nom }}</h4>
                    <p class="charge-meta">
                      {{ charge.compte?.nom }} • {{ getFrequencyLabel(charge.frequence) }} • {{ formatJourPrelevement(charge.jourPrelevement) }}
                    </p>
                  </div>
                </div>
                <div class="charge-amount-section">
                  <span class="charge-amount">{{ formatCurrency(charge.montant) }}</span>
                  <div class="charge-actions">
                    <button @click="editCharge(charge)" class="btn-icon-small" title="Modifier">✏️</button>
                    <button @click="deleteCharge(charge)" class="btn-icon-small danger" title="Supprimer">🗑️</button>
                  </div>
                </div>
              </div>

              <!-- Total -->
              <div class="charges-total">
                <span class="total-label">Total mensuel :</span>
                <span class="total-amount">{{ formatCurrency(totalChargesMensuelles) }}</span>
              </div>
            </div>
          </section>

          <!-- Month Snapshots Section -->
          <section class="form-section glass-card">
            <div class="section-header">
              <h2 class="section-title">📸 Snapshots Mensuels</h2>
            </div>

            <p class="section-description">
              Les snapshots figent l'état financier d'un mois. Ils sont automatiquement créés lors de la validation d'un nouveau salaire, mais vous pouvez aussi les créer manuellement.
            </p>

            <!-- Create Snapshot Form -->
            <div class="snapshot-create-form">
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">Mois du snapshot</label>
                  <input
                    v-model="selectedSnapshotMonth"
                    type="month"
                    class="form-input"
                    :max="getCurrentMonth()"
                  />
                </div>
                <div class="form-group" style="display: flex; align-items: flex-end;">
                  <button
                    @click="createManualSnapshot"
                    class="btn btn-primary"
                    :disabled="creatingSnapshot || !selectedSnapshotMonth"
                  >
                    <span v-if="creatingSnapshot">Création...</span>
                    <span v-else>📸 Créer Snapshot</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Existing Snapshots List -->
            <div v-if="loadingSnapshots" class="loading-container">
              <div class="loading-spinner-small"></div>
              <p>Chargement des snapshots...</p>
            </div>

            <div v-else-if="snapshots.length === 0" class="empty-charges">
              <span class="empty-icon">📸</span>
              <p>Aucun snapshot créé pour le moment</p>
            </div>

            <div v-else class="snapshots-list">
              <div v-for="snapshot in snapshots" :key="snapshot.id" class="snapshot-item">
                <div class="snapshot-info">
                  <div class="snapshot-icon">📅</div>
                  <div class="snapshot-details">
                    <h4 class="snapshot-month">{{ formatSnapshotMonth(snapshot.month) }}</h4>
                    <p class="snapshot-meta">
                      Créé le {{ formatDate(snapshot.createdAt) }} • {{ snapshot.nombreTransactions }} transactions
                    </p>
                  </div>
                </div>
                <div class="snapshot-stats">
                  <div class="stat-item">
                    <span class="stat-label">Revenus</span>
                    <span class="stat-value positive">{{ formatCurrency(snapshot.totalRevenus) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">Dépenses</span>
                    <span class="stat-value negative">{{ formatCurrency(snapshot.totalChargesFixes + snapshot.totalDepensesVariables) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">Épargne</span>
                    <span class="stat-value">{{ formatCurrency(snapshot.totalEpargne) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <!-- Form Actions -->
          <div class="form-actions">
            <button @click="handleCancel" class="btn btn-secondary" :disabled="saving">
              Annuler
            </button>
            <button @click="handleSave" class="btn btn-primary" :disabled="!canSave || saving">
              <span v-if="saving">Enregistrement...</span>
              <span v-else>Enregistrer</span>
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
  </MainLayout>

  <!-- Add/Edit Charge Modal -->
  <Teleport to="body">
    <div v-if="showAddChargeModal" class="modal-overlay" @click.self="closeChargeModal">
      <div class="modal-content glass-card charge-modal">
        <div class="modal-header">
          <h3>{{ editingCharge ? '✏️ Modifier' : '➕ Ajouter' }} une charge fixe</h3>
          <button @click="closeChargeModal" class="btn-close">✕</button>
        </div>

        <form @submit.prevent="saveCharge" class="modal-form">
          <div class="form-group">
            <label class="form-label">Nom de la charge</label>
            <input v-model="chargeFormData.nom" type="text" class="form-input" placeholder="Ex: Loyer" required>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Montant (€)</label>
              <input v-model.number="chargeFormData.montant" type="number" class="form-input" placeholder="0.00" step="0.01" min="0" required>
            </div>

            <div class="form-group">
              <label class="form-label">Compte</label>
              <select v-model="chargeFormData.compteId" class="form-input" required>
                <option value="">Sélectionner...</option>
                <option v-for="compte in comptes" :key="compte.id" :value="compte.id">
                  {{ compte.nom }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Catégorie</label>
              <select v-model="chargeFormData.categorie" class="form-input" required>
                <option value="LOYER">🏠 Loyer</option>
                <option value="ASSURANCE">🛡️ Assurance</option>
                <option value="ABONNEMENT">📱 Abonnement</option>
                <option value="TRANSPORT">🚗 Transport</option>
                <option value="AUTRE">💳 Autre</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Fréquence</label>
              <select v-model="chargeFormData.frequence" class="form-input" required>
                <option value="MENSUELLE">Mensuelle</option>
                <option value="TRIMESTRIELLE">Trimestrielle</option>
                <option value="ANNUELLE">Annuelle</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Jour de prélèvement</label>
            <input v-model.number="chargeFormData.jourPrelevement" type="number" class="form-input" placeholder="1-31" min="1" max="31" required>
          </div>

          <div class="form-group">
            <label class="form-label">Description (optionnelle)</label>
            <textarea v-model="chargeFormData.description" class="form-textarea" placeholder="Détails supplémentaires..." rows="2"></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeChargeModal" class="btn btn-secondary">
              Annuler
            </button>
            <button type="submit" class="btn btn-primary">
              {{ editingCharge ? 'Enregistrer' : 'Ajouter' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import MainLayout from '@/layouts/MainLayout.vue'
import { apiService } from '@/services/api'
import { logger } from '@/utils/logger'
import { useToast } from '@/composables/useToast'
import { useValidation } from '@/composables/useValidation'
import { UserProfileFormSchema } from '@/utils/form-validation'
import type { ChargeFixe, TypeTransaction, FrequenceCharge, CreateChargeFixeRequest, MonthSnapshot } from '@/types'

const router = useRouter()
const dashboardStore = useDashboardStore()
const { user, comptes, chargesFixes } = storeToRefs(dashboardStore)
const { loadDashboard } = dashboardStore
const toast = useToast()
const validation = useValidation(UserProfileFormSchema)

// Snapshots state
const snapshots = ref<MonthSnapshot[]>([])
const loadingSnapshots = ref(false)
const creatingSnapshot = ref(false)
const selectedSnapshotMonth = ref('')

// Form state
interface ProfileFormData {
  prenom: string
  nom: string
  salaireMensuelNet: number
  jourPaie: number
  decouvertAutorise: number
  objectifCompteCourant: number
  pourcentageChargesFixes: number
  pourcentageDepensesVariables: number
  pourcentageEpargne: number
}

const formData = ref<ProfileFormData>({
  prenom: '',
  nom: '',
  salaireMensuelNet: 0,
  jourPaie: 1,
  decouvertAutorise: 0,
  objectifCompteCourant: 0,
  pourcentageChargesFixes: 50,
  pourcentageDepensesVariables: 30,
  pourcentageEpargne: 20
})

const originalData = ref<ProfileFormData | null>(null)
const loading = ref(true)
const saving = ref(false)
const hasUnsavedChanges = ref(false)

const errors = ref<Record<string, string>>({})

// Computed - Budget validation
const budgetTotal = computed(() => {
  return (formData.value.pourcentageChargesFixes || 0) +
    (formData.value.pourcentageDepensesVariables || 0) +
    (formData.value.pourcentageEpargne || 0)
})

const budgetTotalValid = computed(() => {
  return budgetTotal.value === 100
})

// Computed - Calculated amounts
const calculatedChargesFixes = computed(() => {
  return (formData.value.salaireMensuelNet || 0) * (formData.value.pourcentageChargesFixes || 0) / 100
})

const calculatedDepensesVariables = computed(() => {
  return (formData.value.salaireMensuelNet || 0) * (formData.value.pourcentageDepensesVariables || 0) / 100
})

const calculatedEpargne = computed(() => {
  return (formData.value.salaireMensuelNet || 0) * (formData.value.pourcentageEpargne || 0) / 100
})

// Computed - Form validation
const canSave = computed(() => {
  return hasUnsavedChanges.value &&
    budgetTotalValid.value &&
    formData.value.salaireMensuelNet > 0 &&
    formData.value.jourPaie >= 1 &&
    formData.value.jourPaie <= 31 &&
    Object.keys(errors.value).length === 0
})

// Methods
const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(value)
}

const markAsChanged = () => {
  hasUnsavedChanges.value = true
  validateFormData()
}

const validateFormData = () => {
  // Use our validation composable for immediate feedback
  const result = validation.validateSync(formData.value)
  if (!result.success) {
    // Map validation errors to the errors object for display
    errors.value = {}
    result.errors.forEach(err => {
      errors.value[err.field] = err.message
    })
  } else {
    errors.value = {}
  }
}

const loadUserProfile = async () => {
  try {
    loading.value = true
    const userData = await apiService.getUserProfile()

    // Populate form with user data
    formData.value = {
      prenom: userData.prenom || '',
      nom: userData.nom || '',
      salaireMensuelNet: userData.salaireMensuelNet || 0,
      jourPaie: userData.jourPaie || 1,
      decouvertAutorise: userData.decouvertAutorise || 0,
      objectifCompteCourant: (userData as any).objectifCompteCourant || 0,
      pourcentageChargesFixes: userData.pourcentageChargesFixes || 50,
      pourcentageDepensesVariables: userData.pourcentageDepensesVariables || 30,
      pourcentageEpargne: userData.pourcentageEpargne || 20
    }

    // Store original data for comparison
    originalData.value = JSON.parse(JSON.stringify(formData.value))
    hasUnsavedChanges.value = false
  } catch (error) {
    logger.error('Failed to load user profile:', error)
    toast.error('Erreur lors du chargement du profil')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!canSave.value) return

  try {
    saving.value = true

    // Prepare update payload
    const updateData = {
      prenom: formData.value.prenom,
      nom: formData.value.nom,
      salaireMensuelNet: formData.value.salaireMensuelNet,
      jourPaie: formData.value.jourPaie,
      decouvertAutorise: formData.value.decouvertAutorise || undefined,
      objectifCompteCourant: formData.value.objectifCompteCourant || undefined,
      pourcentageChargesFixes: formData.value.pourcentageChargesFixes,
      pourcentageDepensesVariables: formData.value.pourcentageDepensesVariables,
      pourcentageEpargne: formData.value.pourcentageEpargne
    }

    // Validate form data
    const result = await validation.validate(updateData)
    if (!result.success) {
      // Show first validation error
      toast.error(result.errors[0].message)
      logger.warn('Validation errors:', result.errors)
      return
    }

    // Call API with validated data
    const updatedUser = await apiService.updateUserProfile(result.data)

    // Update store
    dashboardStore.dashboardData!.user = updatedUser

    // Reset form state
    originalData.value = JSON.parse(JSON.stringify(formData.value))
    hasUnsavedChanges.value = false

    toast.success('Profil mis à jour avec succès')
    logger.info('Profile updated successfully')

    // Navigate back to dashboard
    setTimeout(() => {
      router.push('/dashboard')
    }, 1500)
  } catch (error: any) {
    logger.error('Failed to update profile:', error)
    toast.error(error.response?.data?.message || 'Erreur lors de la mise à jour du profil')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  if (hasUnsavedChanges.value) {
    if (confirm('Vous avez des modifications non enregistrées. Voulez-vous vraiment annuler ?')) {
      router.push('/dashboard')
    }
  } else {
    router.push('/dashboard')
  }
}

// Navigation guard for unsaved changes
onBeforeRouteLeave((to, from, next) => {
  if (hasUnsavedChanges.value) {
    const answer = window.confirm(
      'Vous avez des modifications non enregistrées. Voulez-vous vraiment quitter cette page ?'
    )
    if (answer) {
      next()
    } else {
      next(false)
    }
  } else {
    next()
  }
})

// Lifecycle
onMounted(async () => {
  await loadUserProfile()
  await loadSnapshots()
})

// Warn on page unload if unsaved changes
const beforeUnloadHandler = (e: BeforeUnloadEvent) => {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

onMounted(() => {
  window.addEventListener('beforeunload', beforeUnloadHandler)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})

// ============================================
// Snapshots Management
// ============================================

const loadSnapshots = async () => {
  try {
    loadingSnapshots.value = true
    const snapshotsData = await apiService.getAllSnapshots()
    // Sort by month descending (most recent first)
    snapshots.value = snapshotsData.sort((a, b) => b.month.localeCompare(a.month))
    logger.info('Snapshots loaded:', snapshots.value.length)
  } catch (error) {
    logger.error('Failed to load snapshots:', error)
    toast.error('Erreur lors du chargement des snapshots')
  } finally {
    loadingSnapshots.value = false
  }
}

const createManualSnapshot = async () => {
  if (!selectedSnapshotMonth.value) {
    toast.error('Veuillez sélectionner un mois')
    return
  }

  try {
    creatingSnapshot.value = true
    const snapshot = await apiService.createSnapshot(selectedSnapshotMonth.value)
    toast.success(`Snapshot créé pour ${formatSnapshotMonth(selectedSnapshotMonth.value)}`)
    logger.info('Snapshot created:', snapshot)

    // Reload snapshots to show the new one
    await loadSnapshots()

    // Reset selection
    selectedSnapshotMonth.value = ''
  } catch (error: any) {
    logger.error('Failed to create snapshot:', error)
    toast.error(error.response?.data?.error || 'Erreur lors de la création du snapshot')
  } finally {
    creatingSnapshot.value = false
  }
}

const formatSnapshotMonth = (month: string): string => {
  const date = new Date(month + '-01')
  return date.toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long'
  })
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getCurrentMonth = (): string => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

// ============================================
// Charges Fixes Management
// ============================================

const showAddChargeModal = ref(false)
const editingCharge = ref<ChargeFixe | null>(null)
const chargeFormData = ref<CreateChargeFixeRequest>({
  compteId: '',
  nom: '',
  description: undefined,
  montant: 0,
  categorie: 'AUTRE' as TypeTransaction,
  frequence: 'MENSUELLE' as FrequenceCharge,
  jourPrelevement: 1,
  dateDebut: new Date().toISOString().split('T')[0],
  dateFin: undefined,
  actif: true
})

// Computed
const totalChargesMensuelles = computed(() => {
  return chargesFixes.value.reduce((total, charge) => {
    let montantMensuel = charge.montant
    if (charge.frequence === 'TRIMESTRIELLE') {
      montantMensuel = charge.montant / 3
    } else if (charge.frequence === 'ANNUELLE') {
      montantMensuel = charge.montant / 12
    }
    return total + montantMensuel
  }, 0)
})

// Helper functions
const getCategoryIcon = (categorie: TypeTransaction): string => {
  const icons: Record<string, string> = {
    'LOYER': '🏠',
    'ASSURANCE': '🛡️',
    'ABONNEMENT': '📱',
    'TRANSPORT': '🚗',
    'ALIMENTATION': '🍔',
    'SANTE': '🏥',
    'LOISIRS': '🎮',
    'AUTRE': '💳'
  }
  return icons[categorie] || '💳'
}

const getFrequencyLabel = (frequence: FrequenceCharge): string => {
  const labels: Record<string, string> = {
    'MENSUELLE': 'Mensuelle',
    'TRIMESTRIELLE': 'Trimestrielle',
    'ANNUELLE': 'Annuelle'
  }
  return labels[frequence] || frequence
}

const formatJourPrelevement = (jour: number): string => {
  if (jour === 1 || jour === 21 || jour === 31) {
    return `Le ${jour}er`
  }
  return `Le ${jour}`
}

// CRUD Operations
const editCharge = (charge: ChargeFixe) => {
  editingCharge.value = charge
  chargeFormData.value = {
    compteId: charge.compte.id,
    nom: charge.nom,
    description: charge.description || undefined,
    montant: charge.montant,
    categorie: charge.categorie,
    frequence: charge.frequence,
    jourPrelevement: charge.jourPrelevement,
    dateDebut: charge.dateDebut,
    dateFin: charge.dateFin || undefined,
    actif: charge.actif
  }
  showAddChargeModal.value = true
}

const deleteCharge = async (charge: ChargeFixe) => {
  if (!confirm(`Supprimer la charge "${charge.nom}" ?\n\nCette action est irréversible.`)) {
    return
  }

  try {
    await apiService.deleteChargeFixe(charge.id)
    toast.success('Charge supprimée avec succès')
    await loadDashboard()
  } catch (error) {
    logger.error('Failed to delete charge:', error)
    toast.error('Erreur lors de la suppression de la charge')
  }
}

const saveCharge = async () => {
  try {
    if (editingCharge.value) {
      await apiService.updateChargeFixe(editingCharge.value.id, chargeFormData.value)
      toast.success('Charge modifiée avec succès')
    } else {
      await apiService.createChargeFixe(chargeFormData.value)
      toast.success('Charge ajoutée avec succès')
    }

    closeChargeModal()
    await loadDashboard()
  } catch (error) {
    logger.error('Failed to save charge:', error)
    toast.error('Erreur lors de l\'enregistrement de la charge')
  }
}

const closeChargeModal = () => {
  showAddChargeModal.value = false
  editingCharge.value = null
  chargeFormData.value = {
    compteId: '',
    nom: '',
    description: undefined,
    montant: 0,
    categorie: 'AUTRE' as TypeTransaction,
    frequence: 'MENSUELLE' as FrequenceCharge,
    jourPrelevement: 1,
    dateDebut: new Date().toISOString().split('T')[0],
    dateFin: undefined,
    actif: true
  }
}
</script>

<style scoped>
/* Page Header */
.page-header {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding: 2rem 0;
  position: sticky;
  top: 73px;
  z-index: 100;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}

.header-text {
  flex: 1;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 0.5rem 0;
}

.page-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.95rem;
  margin: 0;
}

/* Main Content */
.page-main {
  padding: 2rem 0 4rem 0;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: rgba(255, 255, 255, 0.7);
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #667EEA;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Form */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.form-section {
  padding: 2rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.section-header.clickable {
  cursor: pointer;
  user-select: none;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #E8EAF6;
  margin: 0;
}

.expand-icon {
  color: rgba(255, 255, 255, 0.6);
  font-size: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-input {
  padding: 0.875rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: #E8EAF6;
  font-size: 0.95rem;
  transition: all 0.2s;
}

/* Remove number input arrows */
.form-input[type="number"]::-webkit-inner-spin-button,
.form-input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.form-input[type="number"] {
  -moz-appearance: textfield;
}

.form-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.15);
  border-color: #667EEA;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

/* Sliders Container */
.sliders-container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.slider-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.slider-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: #E8EAF6;
}

.form-slider {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  outline: none;
  cursor: pointer;
}

.form-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  transition: all 0.2s;
}

.form-slider::-webkit-slider-thumb:hover {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.6);
}

.form-slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  cursor: pointer;
  border: none;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
  transition: all 0.2s;
}

.form-slider::-moz-range-thumb:hover {
  transform: scale(1.2);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.6);
}

.error-message {
  color: #F44336;
  font-size: 0.75rem;
  margin-top: -0.25rem;
}

.warning-message {
  background: rgba(255, 152, 0, 0.1);
  border: 1px solid rgba(255, 152, 0, 0.3);
  border-radius: 8px;
  padding: 1rem;
  color: #FF9800;
  font-size: 0.875rem;
  margin-top: 1rem;
}

/* Budget Section */
.budget-total {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-weight: 700;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.budget-total.valid {
  background: rgba(76, 175, 80, 0.2);
  color: #4CAF50;
}

.budget-total.invalid {
  background: rgba(244, 67, 54, 0.2);
  color: #F44336;
}

.status-icon {
  font-size: 1.25rem;
}

.progress-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  margin-top: 0.5rem;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
  border-radius: 4px;
}

.calculated-amount {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0.5rem 0 0 0;
  font-weight: 600;
}

/* Objectives Section */
.objectives-info {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.objective-item {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.calculated-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #4CAF50;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.help-text {
  font-size: 0.875rem;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.5);
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
}

.btn {
  padding: 0.875rem 2rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.95rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #E8EAF6;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
}

/* Responsive */
@media (max-width: 1024px) {
  .header-wrapper {
    flex-wrap: wrap;
  }

  .main-nav {
    order: 3;
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .logo-text {
    display: none;
  }

  .main-nav {
    gap: 0.25rem;
    padding: 0.25rem;
  }

  .nav-item {
    flex: 1;
    justify-content: center;
    padding: 0.75rem 0.5rem;
  }

  .nav-text {
    display: none;
  }

  .nav-icon {
    font-size: 1.5rem;
  }

  .user-name {
    display: none;
  }

  .page-header {
    top: 66px;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}

/* Charges Fixes Section */
.empty-charges {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
}

.empty-icon {
  font-size: 3rem;
  opacity: 0.5;
  margin-bottom: 1rem;
}

.empty-charges p {
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.charges-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.charge-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.2s;
}

.charge-item:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateX(4px);
}

.charge-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.charge-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 8px;
}

.charge-details {
  flex: 1;
}

.charge-name {
  font-size: 1rem;
  font-weight: 600;
  color: #E8EAF6;
  margin: 0 0 0.25rem 0;
}

.charge-meta {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.charge-amount-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.charge-amount {
  font-size: 1.125rem;
  font-weight: 700;
  color: #F56565;
  min-width: 100px;
  text-align: right;
}

.charge-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon-small {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1rem;
}

.btn-icon-small:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.btn-icon-small.danger:hover {
  background: rgba(245, 101, 101, 0.2);
}

.charges-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  margin-top: 1rem;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 10px;
  border: 1px solid rgba(102, 126, 234, 0.3);
}

.total-label {
  font-size: 1rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.total-amount {
  font-size: 1.25rem;
  font-weight: 700;
  color: #667EEA;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

/* Charge Modal */
.charge-modal {
  max-width: 600px;
  width: 100%;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  padding: 2rem;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  color: #E8EAF6;
}

.btn-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-size: 1.25rem;
  transition: all 0.2s;
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.form-textarea {
  padding: 0.875rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: #E8EAF6;
  font-size: 0.95rem;
  transition: all 0.2s;
  font-family: inherit;
  resize: vertical;
}

.form-textarea:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.15);
  border-color: #667EEA;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.form-textarea::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

@media (max-width: 640px) {
  .charge-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .charge-amount-section {
    width: 100%;
    justify-content: space-between;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

/* Snapshots Section */
.section-description {
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.snapshot-create-form {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.snapshots-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.snapshot-item {
  padding: 1.5rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.2s;
}

.snapshot-item:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateX(4px);
}

.snapshot-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.snapshot-icon {
  font-size: 1.5rem;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.2);
  border-radius: 10px;
}

.snapshot-details {
  flex: 1;
}

.snapshot-month {
  font-size: 1.125rem;
  font-weight: 600;
  color: #E8EAF6;
  margin: 0 0 0.25rem 0;
}

.snapshot-meta {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.snapshot-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.stat-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 600;
}

.stat-value {
  font-size: 1rem;
  font-weight: 700;
  color: #E8EAF6;
}

.stat-value.positive {
  color: #4CAF50;
}

.stat-value.negative {
  color: #F56565;
}

.loading-spinner-small {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #667EEA;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@media (max-width: 640px) {
  .snapshot-stats {
    grid-template-columns: 1fr;
  }

  .snapshot-create-form .form-row {
    grid-template-columns: 1fr;
  }

  .snapshot-create-form .form-group[style] {
    align-items: stretch !important;
  }

  .snapshot-create-form .btn {
    width: 100%;
  }
}
</style>
