<template>
  <MainLayout>
    <div class="comptes-view">
      <!-- Page Title Header -->
    <header class="page-header">
      <div class="container">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">💳 Gestion des Comptes</h1>
            <p class="page-subtitle">Créez, modifiez et gérez vos comptes bancaires</p>
          </div>
          <button @click="showAddModal = true" class="btn btn-primary">
            ➕ Nouveau compte
          </button>
        </div>
      </div>
    </header>

    <main class="page-main">
      <div class="container">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Chargement de vos comptes...</p>
        </div>

        <div v-else-if="comptes.length === 0" class="empty-state glass-card">
          <span class="empty-icon">💳</span>
          <h3>Aucun compte configuré</h3>
          <p>Ajoutez votre premier compte bancaire pour commencer à gérer votre budget</p>
          <button @click="showAddModal = true" class="btn btn-primary btn-lg">
            ➕ Créer mon premier compte
          </button>
        </div>

        <div v-else class="comptes-grid">
          <div
            v-for="compte in comptes"
            :key="compte.id"
            class="compte-card glass-card"
          >
            <div class="compte-header">
              <div
                class="bank-indicator"
                :style="{ backgroundColor: compte.banque?.couleurTheme || '#667EEA' }"
              ></div>

              <div class="compte-main-info">
                <div class="compte-title-section">
                  <span class="compte-icon">{{ getCompteIcon(compte.type) }}</span>
                  <div class="compte-names">
                    <h3 class="compte-name">{{ compte.nom }}</h3>
                    <p class="compte-meta">
                      <span class="compte-type">{{ getTypeLabel(compte.type) }}</span>
                      <span class="separator">•</span>
                      <span class="compte-bank">{{ compte.banque?.nom }}</span>
                    </p>
                  </div>
                </div>

                <div class="compte-actions">
                  <button @click="editCompte(compte)" class="btn-icon" title="Modifier">
                    ✏️
                  </button>
                  <button @click="deleteCompte(compte)" class="btn-icon danger" title="Supprimer">
                    🗑️
                  </button>
                </div>
              </div>
            </div>

            <div class="compte-details">
              <div class="soldes-grid">
                <div class="solde-item">
                  <span class="solde-label">Solde total</span>
                  <span class="solde-value total">{{ formatCurrency(compte.soldeTotal) }}</span>
                </div>
                <div class="solde-item">
                  <span class="solde-label">Argent libre</span>
                  <span class="solde-value libre" :class="{ 'positive': (compte.argentLibre ?? 0) > 0 }">
                    {{ formatCurrency(compte.argentLibre ?? 0) }}
                  </span>
                </div>
              </div>

              <div class="compte-extra-info">
                <div class="info-item">
                  <span class="info-icon">📈</span>
                  <div class="info-content">
                    <span class="info-label">Taux d'intérêt</span>
                    <span class="info-value" :class="{ 'value-empty': !compte.taux }">
                      {{ compte.taux ? `${compte.taux}%` : '-' }}
                    </span>
                  </div>
                </div>
                <div class="info-item">
                  <span class="info-icon">🎯</span>
                  <div class="info-content">
                    <span class="info-label">Plafond</span>
                    <span class="info-value" :class="{ 'value-empty': !compte.plafond }">
                      {{ compte.plafond ? formatCurrency(compte.plafond) : '-' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Add/Edit Compte -->
    <div v-if="showAddModal || editingCompte" class="modal-overlay" @click="closeModal">
      <div class="modal-content glass-card" @click.stop>
        <div class="modal-header">
          <h3>{{ editingCompte ? '✏️ Modifier' : '➕ Nouveau' }} compte</h3>
          <button @click="closeModal" class="btn-close">✕</button>
        </div>

        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label class="form-label">Nom du compte</label>
            <input v-model="formData.nom" type="text" class="form-input" placeholder="Ex: Compte Courant SG" required>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Type de compte</label>
              <select v-model="formData.type" class="form-input">
                <option value="COMPTE_COURANT">Compte Courant</option>
                <option value="LIVRET_A">Livret A</option>
                <option value="LDDS">LDDS</option>
                <option value="LDD">Livret de Développement Durable</option>
                <option value="PEA">PEA</option>
                <option value="PEL">PEL</option>
                <option value="CEL">CEL</option>
                <option value="ASSURANCE_VIE">Assurance Vie</option>
                <option value="COMPTE_TITRES">Compte Titres</option>
                <option value="COMPTE_EPARGNE">Compte Épargne</option>
                <option value="LIVRET_JEUNE">Livret Jeune</option>
                <option value="CRYPTO">Crypto</option>
                <option value="COMPTE_INVEST">Compte Investissement</option>
                <option value="PEA_PME">PEA-PME</option>
                <option value="AUTRE">Autre</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Solde initial (€)</label>
              <input v-model.number="formData.soldeTotal" type="number" class="form-input" placeholder="0.00" step="0.01" required>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Taux d'intérêt (%) - Optionnel</label>
              <input v-model.number="formData.taux" type="number" class="form-input" placeholder="0.00" step="0.01" min="0">
            </div>

            <div class="form-group">
              <label class="form-label">Plafond (€) - Optionnel</label>
              <input v-model.number="formData.plafond" type="number" class="form-input" placeholder="0.00" step="0.01" min="0">
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Nom de la banque</label>
            <input v-model="formData.banqueNom" type="text" class="form-input" placeholder="Ex: Société Générale" required>
          </div>

          <div class="form-group">
            <label class="form-label">Couleur de la banque</label>
            <div class="color-picker-wrapper">
              <input v-model="formData.banqueCouleur" type="color" class="form-color-input">
              <input v-model="formData.banqueCouleur" type="text" class="form-input" placeholder="#667EEA">
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn btn-secondary">
              Annuler
            </button>
            <button type="submit" class="btn btn-primary">
              {{ editingCompte ? 'Enregistrer' : 'Créer le compte' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { useDashboardStore } from '@/stores/dashboard'
import MainLayout from '@/layouts/MainLayout.vue'
import { apiService } from '@/services/api'
import { formatCurrency } from '@/utils/formatters'
import { useToast } from '@/composables/useToast'
import { useValidation } from '@/composables/useValidation'
import { CreateCompteFormSchema, UpdateCompteFormSchema } from '@/utils/form-validation'
import type { Compte, TypeCompte } from '@/types'
import { logger } from '@/utils/logger'

const toast = useToast()
const validation = useValidation(CreateCompteFormSchema)

const route = useRoute()
const router = useRouter()

const dashboardStore = useDashboardStore()
const { user, comptes, loading } = storeToRefs(dashboardStore)
const { loadDashboard } = dashboardStore

// Check if patrimoine nav should be active (includes /comptes)
const isPatrimoineActive = computed(() => {
  return route.path === '/patrimoine' || route.path === '/comptes'
})

const navigateToPatrimoine = () => {
  router.push('/patrimoine')
}

const showAddModal = ref(false)
const editingCompte = ref<Compte | null>(null)
const formData = ref({
  nom: '',
  type: 'COMPTE_COURANT' as TypeCompte,
  soldeTotal: 0,
  taux: undefined as number | undefined,
  plafond: undefined as number | undefined,
  banqueNom: '',
  banqueCouleur: '#667EEA'
})

const getCompteIcon = (type: TypeCompte): string => {
  const icons: Record<string, string> = {
    'COMPTE_COURANT': '💳',
    'LIVRET_A': '📗',
    'LDDS': '📘',
    'PEA': '📊',
    'ASSURANCE_VIE': '🛡️',
    'PEL': '🏠',
    'CEL': '🏡',
    'COMPTE_TITRES': '💰',
    'CRYPTO': '₿',
    'COMPTE_EPARGNE': '💚'
  }
  return icons[type] || '💰'
}

const getTypeLabel = (type: TypeCompte): string => {
  const labels: Record<string, string> = {
    'COMPTE_COURANT': 'Compte Courant',
    'LIVRET_A': 'Livret A',
    'LDDS': 'LDDS',
    'LDD': 'Livret de Développement Durable',
    'PEA': 'PEA',
    'PEL': 'PEL',
    'CEL': 'CEL',
    'ASSURANCE_VIE': 'Assurance Vie',
    'COMPTE_TITRES': 'Compte Titres',
    'COMPTE_EPARGNE': 'Compte Épargne',
    'LIVRET_JEUNE': 'Livret Jeune',
    'CRYPTO': 'Crypto',
    'COMPTE_INVEST': 'Compte Investissement',
    'PEA_PME': 'PEA-PME',
    'AUTRE': 'Autre'
  }
  return labels[type] || type
}

const editCompte = (compte: Compte) => {
  editingCompte.value = compte
  formData.value.nom = compte.nom
  formData.value.type = compte.type
  formData.value.soldeTotal = compte.soldeTotal
  formData.value.taux = compte.taux
  formData.value.plafond = compte.plafond
  formData.value.banqueNom = compte.banque?.nom || ''
  formData.value.banqueCouleur = compte.banque?.couleurTheme || '#667EEA'
}

const deleteCompte = async (compte: Compte) => {
  if (confirm(`Supprimer le compte "${compte.nom}" ?\n\nCette action est irréversible.`)) {
    try {
      await apiService.deleteAccount(compte.id)
      await loadDashboard()
    } catch (error) {
      logger.error('Erreur suppression:', error)
    }
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingCompte.value = null
  formData.value = {
    nom: '',
    type: 'COMPTE_COURANT',
    soldeTotal: 0,
    taux: undefined,
    plafond: undefined,
    banqueNom: '',
    banqueCouleur: '#667EEA'
  }
}

const handleSubmit = async () => {
  try {
    loading.value = true

    // First, create or get the bank
    let banque = await apiService.getBanques().then(banques =>
      banques.find(b => b.nom === formData.value.banqueNom)
    )

    if (!banque) {
      // Create new bank if it doesn't exist
      banque = await apiService.createBanque({
        nom: formData.value.banqueNom,
        couleurTheme: formData.value.banqueCouleur,
        actif: true
      })
    }

    // Prepare data for validation
    const compteData = {
      banqueId: banque.id,
      nom: formData.value.nom,
      type: formData.value.type,
      soldeTotal: formData.value.soldeTotal,
      taux: formData.value.taux || undefined,
      plafond: formData.value.plafond || undefined,
      dateOuverture: new Date().toISOString().split('T')[0]
    }

    // Validate form data
    const result = await validation.validate(compteData)
    if (!result.success) {
      // Show first validation error
      toast.error(result.errors[0].message)
      logger.warn('Validation errors:', result.errors)
      return
    }

    // Create the compte with validated data
    await apiService.createCompte(result.data)

    logger.info('Compte created successfully')
    toast.success('Compte créé avec succès')

    // Refresh dashboard to show new compte
    await loadDashboard()

    // Close modal
    closeModal()
  } catch (error) {
    logger.error('Error creating compte:', error)
    toast.error('Erreur lors de la création du compte')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadDashboard()
})
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
  max-width: 1400px;
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
  padding: 2rem 0;
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

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
  opacity: 0.5;
}

.empty-state h3 {
  color: #E8EAF6;
  margin: 0 0 0.75rem 0;
  font-size: 1.5rem;
}

.empty-state p {
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 2rem 0;
  max-width: 500px;
}

/* Comptes Grid */
.comptes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.compte-card {
  padding: 0;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.compte-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.2);
}

.compte-header {
  position: relative;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  min-height: 120px;
  display: flex;
  align-items: center;
}

.bank-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #667EEA;
}

.compte-main-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  width: 100%;
}

.compte-title-section {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex: 1;
}

.compte-icon {
  font-size: 2.5rem;
}

.compte-names {
  flex: 1;
}

.compte-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #E8EAF6;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.compte-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.compte-type {
  font-weight: 500;
}

.separator {
  opacity: 0.5;
}

.compte-bank {
  opacity: 0.8;
}

.compte-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1.125rem;
}

.btn-icon:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.btn-icon.danger:hover {
  background: rgba(244, 67, 54, 0.2);
}

.compte-details {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.soldes-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.solde-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.solde-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.solde-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #E8EAF6;
}

.solde-value.libre {
  color: rgba(255, 255, 255, 0.5);
}

.solde-value.libre.positive {
  color: #4CAF50;
}

.compte-extra-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  padding-top: 1rem;
  margin-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.2s;
}

.info-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
}

.info-icon {
  font-size: 1.25rem;
  flex-shrink: 0;
  margin-top: 2px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.info-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 0.95rem;
  color: #E8EAF6;
  font-weight: 600;
}

.info-value.value-empty {
  color: rgba(255, 255, 255, 0.3);
  font-weight: 400;
}

/* Buttons */
.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  white-space: nowrap;
}

.btn-primary {
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
}

.btn-lg {
  padding: 1rem 2rem;
  font-size: 1rem;
}

/* Modal */
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
  width: 100%;
  max-width: 600px;
  padding: 2rem;
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
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.form-input,
select.form-input {
  padding: 0.875rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #E8EAF6;
  font-size: 0.95rem;
  transition: all 0.2s;
}

select.form-input {
  cursor: pointer;
}

select.form-input option {
  background: #1a1a2e;
  color: #E8EAF6;
}

.form-input:focus,
select.form-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.15);
  border-color: #667EEA;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
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

.color-picker-wrapper {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.form-color-input {
  width: 60px;
  height: 44px;
  padding: 0.25rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.form-color-input:hover {
  border-color: #667EEA;
}

.form-color-input::-webkit-color-swatch-wrapper {
  padding: 0;
}

.form-color-input::-webkit-color-swatch {
  border: none;
  border-radius: 6px;
}

.color-picker-wrapper .form-input {
  flex: 1;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 0.5rem;
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

  .comptes-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
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

  .header-content {
    flex-direction: column;
    align-items: stretch;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .comptes-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .compte-extra-info {
    grid-template-columns: 1fr;
  }

  .soldes-grid {
    grid-template-columns: 1fr;
  }
}
</style>
