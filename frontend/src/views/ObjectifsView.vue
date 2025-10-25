<template>
  <MainLayout>
    <div class="objectifs-view">
      <!-- Page Title Header -->
    <header class="page-header">
      <div class="container">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">🎯 Gestion des Objectifs</h1>
            <p class="page-subtitle">Créez, modifiez et suivez vos objectifs d'épargne</p>
          </div>
          <button @click="showAddModal = true" class="btn btn-primary">
            ➕ Nouvel objectif
          </button>
        </div>
      </div>
    </header>

    <main class="page-main">
      <div class="container">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Chargement de vos objectifs...</p>
        </div>

        <div v-else-if="objectifs.length === 0" class="empty-state glass-card">
          <span class="empty-icon">🎯</span>
          <h3>Aucun objectif défini</h3>
          <p>Créez votre premier objectif d'épargne pour commencer à suivre vos progrès</p>
          <button @click="showAddModal = true" class="btn btn-primary btn-lg">
            ➕ Créer mon premier objectif
          </button>
        </div>

        <div v-else class="objectifs-grid">
          <div
            v-for="objectif in objectifs"
            :key="objectif.id"
            class="objectif-card glass-card"
          >
            <div class="objectif-header">
              <div
                class="priority-indicator"
                :class="getPriorityClass(objectif.priorite)"
              ></div>

              <div class="objectif-main-info">
                <div class="objectif-title-section">
                  <span class="objectif-icon" :style="{ color: objectif.couleur || '#4CAF50' }">
                    {{ objectif.icone || '🎯' }}
                  </span>
                  <div class="objectif-names">
                    <h3 class="objectif-name">{{ objectif.nom }}</h3>
                    <p class="objectif-meta">
                      <span class="objectif-type">{{ getTypeLabel(objectif.type) }}</span>
                      <span class="separator">•</span>
                      <span class="objectif-priority">{{ getPriorityLabel(objectif.priorite) }}</span>
                    </p>
                  </div>
                </div>

                <div class="objectif-actions">
                  <button @click="editObjectif(objectif)" class="btn-icon" title="Modifier">
                    ✏️
                  </button>
                  <button @click="deleteObjectif(objectif)" class="btn-icon danger" title="Supprimer">
                    🗑️
                  </button>
                </div>
              </div>
            </div>

            <div class="objectif-details">
              <div class="objectif-progress-section">
                <div class="progress-info">
                  <div class="progress-amounts">
                    <span class="current-amount">{{ formatCurrency(objectif.montantActuel || 0) }}</span>
                    <span class="separator">/</span>
                    <span class="target-amount">{{ formatCurrency(objectif.montantCible) }}</span>
                  </div>
                  <span class="progress-percent">{{ Math.round(objectif.pourcentageProgression || 0) }}%</span>
                </div>

                <div class="progress-bar-container">
                  <div class="progress-bar">
                    <div
                      class="progress-fill"
                      :style="{
                        width: `${Math.min(objectif.pourcentageProgression || 0, 100)}%`,
                        background: getProgressGradient(objectif.couleur || '#4CAF50')
                      }"
                    ></div>
                  </div>
                </div>
              </div>

              <div v-if="objectif.description" class="objectif-description">
                <span class="description-icon">📝</span>
                <span class="description-text">{{ objectif.description }}</span>
              </div>

              <div class="objectif-footer">
                <div class="remaining-amount">
                  <span class="label">Reste à épargner :</span>
                  <span class="value">{{ formatCurrency(objectif.montantCible - (objectif.montantActuel || 0)) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Add/Edit Objectif -->
    <div v-if="showAddModal || editingObjectif" class="modal-overlay" @click="closeModal">
      <div class="modal-content glass-card" @click.stop>
        <div class="modal-header">
          <h3>{{ editingObjectif ? '✏️ Modifier' : '➕ Nouvel' }} objectif</h3>
          <button @click="closeModal" class="btn-close">✕</button>
        </div>

        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Nom de l'objectif</label>
              <input v-model="formData.nom" type="text" class="form-input" placeholder="Ex: Épargne de sécurité" required>
            </div>

            <div class="form-group">
              <label class="form-label">Icône</label>
              <input v-model="formData.icone" type="text" class="form-input" placeholder="🎯" maxlength="2">
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">Type</label>
              <select v-model="formData.type" class="form-input">
                <option value="SECURITE">Sécurité</option>
                <option value="COURT_TERME">Court terme</option>
                <option value="MOYEN_TERME">Moyen terme</option>
                <option value="LONG_TERME">Long terme</option>
                <option value="PLAISIR">Plaisir</option>
                <option value="PROJET">Projet</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Priorité</label>
              <select v-model="formData.priorite" class="form-input">
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
              <label class="form-label">Montant cible (€)</label>
              <input v-model.number="formData.montantCible" type="number" class="form-input" placeholder="0.00" step="0.01" min="0" required>
            </div>

            <div class="form-group">
              <label class="form-label">Couleur</label>
              <div class="color-picker-wrapper">
                <input v-model="formData.couleur" type="color" class="form-color-input">
                <input v-model="formData.couleur" type="text" class="form-input" placeholder="#4CAF50">
              </div>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Description (optionnelle)</label>
            <textarea v-model="formData.description" class="form-textarea" placeholder="Décrivez votre objectif..." rows="3"></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn btn-secondary">
              Annuler
            </button>
            <button type="submit" class="btn btn-primary">
              {{ editingObjectif ? 'Enregistrer' : 'Créer l\'objectif' }}
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
import { CreateObjectifFormSchema, UpdateObjectifFormSchema } from '@/utils/form-validation'
import type { Objectif, TypeObjectif, PrioriteObjectif } from '@/types'
import { logger } from '@/utils/logger'

const toast = useToast()
const validation = useValidation(CreateObjectifFormSchema)

const route = useRoute()
const router = useRouter()

const dashboardStore = useDashboardStore()
const { user, objectifs, loading } = storeToRefs(dashboardStore)
const { loadDashboard } = dashboardStore

// Check if patrimoine nav should be active (includes /objectifs)
const isPatrimoineActive = computed(() => {
  return route.path === '/patrimoine' || route.path === '/comptes' || route.path === '/objectifs'
})

const navigateToPatrimoine = () => {
  router.push('/patrimoine')
}

const showAddModal = ref(false)
const editingObjectif = ref<Objectif | null>(null)
const formData = ref({
  nom: '',
  type: 'COURT_TERME' as TypeObjectif,
  priorite: 'NORMALE' as PrioriteObjectif,
  montantCible: 0,
  description: '',
  couleur: '#4CAF50',
  icone: '🎯'
})

const getTypeLabel = (type: TypeObjectif): string => {
  const labels: Record<string, string> = {
    'SECURITE': 'Sécurité',
    'COURT_TERME': 'Court terme',
    'MOYEN_TERME': 'Moyen terme',
    'LONG_TERME': 'Long terme',
    'PLAISIR': 'Plaisir',
    'PROJET': 'Projet'
  }
  return labels[type] || type
}

const getPriorityLabel = (priority: PrioriteObjectif): string => {
  const labels: Record<string, string> = {
    'CRITIQUE': 'Critique',
    'TRES_HAUTE': 'Très haute',
    'HAUTE': 'Haute',
    'NORMALE': 'Normale',
    'BASSE': 'Basse'
  }
  return labels[priority] || priority
}

const getPriorityClass = (priority: PrioriteObjectif): string => {
  const classes: Record<string, string> = {
    'CRITIQUE': 'priority-critique',
    'TRES_HAUTE': 'priority-tres-haute',
    'HAUTE': 'priority-haute',
    'NORMALE': 'priority-normale',
    'BASSE': 'priority-basse'
  }
  return classes[priority] || 'priority-normale'
}

const getProgressGradient = (color: string): string => {
  return `linear-gradient(90deg, ${color}, ${color}dd)`
}

const editObjectif = (objectif: Objectif) => {
  editingObjectif.value = objectif
  formData.value.nom = objectif.nom
  formData.value.type = objectif.type
  formData.value.priorite = objectif.priorite
  formData.value.montantCible = objectif.montantCible
  formData.value.description = objectif.description || ''
  formData.value.couleur = objectif.couleur || '#4CAF50'
  formData.value.icone = objectif.icone || '🎯'
}

const deleteObjectif = async (objectif: Objectif) => {
  if (confirm(`Supprimer l'objectif "${objectif.nom}" ?\n\nCette action est irréversible.`)) {
    try {
      await apiService.deleteObjectif(objectif.id)
      await loadDashboard()
    } catch (error) {
      logger.error('Erreur suppression:', error)
    }
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingObjectif.value = null
  formData.value = {
    nom: '',
    type: 'COURT_TERME',
    priorite: 'NORMALE',
    montantCible: 0,
    description: '',
    couleur: '#4CAF50',
    icone: '🎯'
  }
}

const handleSubmit = async () => {
  try {
    loading.value = true

    // Prepare data for validation
    const objectifData = {
      nom: formData.value.nom,
      type: formData.value.type,
      priorite: formData.value.priorite,
      montantCible: formData.value.montantCible,
      description: formData.value.description || undefined,
      couleur: formData.value.couleur,
      icone: formData.value.icone || undefined
    }

    // Validate form data
    const result = await validation.validate(objectifData)
    if (!result.success) {
      // Show first validation error
      toast.error(result.errors[0].message)
      logger.warn('Validation errors:', result.errors)
      return
    }

    // Create or update the objectif with validated data
    if (editingObjectif.value) {
      await apiService.updateObjectif(editingObjectif.value.id, result.data)
      toast.success('Objectif mis à jour avec succès')
    } else {
      await apiService.createObjectif(result.data)
      toast.success('Objectif créé avec succès')
    }

    logger.info('Objectif operation successful')

    // Refresh dashboard to show changes
    await loadDashboard()

    // Close modal
    closeModal()
  } catch (error) {
    logger.error('Error with objectif:', error)
    toast.error(editingObjectif.value ? 'Erreur lors de la mise à jour' : 'Erreur lors de la création de l\'objectif')
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

/* Objectifs Grid */
.objectifs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.objectif-card {
  padding: 0;
  overflow: hidden;
  position: relative;
}

.objectif-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.2);
}

.objectif-header {
  position: relative;
  padding: 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.priority-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.priority-critique {
  background: #F44336;
}

.priority-tres-haute {
  background: #FF9800;
}

.priority-haute {
  background: #FFC107;
}

.priority-normale {
  background: #4CAF50;
}

.priority-basse {
  background: #2196F3;
}

.objectif-main-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.objectif-title-section {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex: 1;
}

.objectif-icon {
  font-size: 2.5rem;
}

.objectif-names {
  flex: 1;
}

.objectif-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: #E8EAF6;
  margin: 0 0 0.5rem 0;
}

.objectif-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.objectif-type {
  font-weight: 500;
}

.separator {
  opacity: 0.5;
}

.objectif-priority {
  opacity: 0.8;
}

.objectif-actions {
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

.objectif-details {
  padding: 1.5rem;
}

.objectif-progress-section {
  margin-bottom: 1rem;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.progress-amounts {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
}

.current-amount {
  color: #4CAF50;
}

.progress-amounts .separator {
  color: rgba(255, 255, 255, 0.4);
  font-size: 1rem;
}

.target-amount {
  color: rgba(255, 255, 255, 0.6);
}

.progress-percent {
  font-size: 1.25rem;
  font-weight: 700;
  color: #E8EAF6;
}

.progress-bar-container {
  width: 100%;
}

.progress-bar {
  height: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.5s ease;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.objectif-description {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
}

.description-icon {
  font-size: 1rem;
  flex-shrink: 0;
}

.description-text {
  flex: 1;
}

.objectif-footer {
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.remaining-amount {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remaining-amount .label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.remaining-amount .value {
  font-size: 1rem;
  font-weight: 700;
  color: #FF9800;
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

.form-textarea {
  padding: 0.875rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: #E8EAF6;
  font-size: 0.95rem;
  transition: all 0.2s;
  resize: vertical;
  font-family: inherit;
  min-height: 80px;
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

  .objectifs-grid {
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

  .objectifs-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
