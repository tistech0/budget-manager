<template>
  <MainLayout>
    <div class="patrimoine-view">
      <!-- Page Title Header -->
    <header class="page-header fade-in">
      <div class="container">
        <div class="header-content">
          <div class="header-text">
            <h1 class="page-title">💎 Votre Patrimoine</h1>
            <p class="page-subtitle">Vue d'ensemble de vos comptes et objectifs d'épargne</p>
          </div>
          <div class="header-stats">
            <div class="stat-card">
              <span class="stat-label">Total patrimoine</span>
              <span class="stat-value">{{ formatCurrency(totalPatrimoine) }}</span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <main class="page-main">
      <div class="container">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Chargement de vos données...</p>
        </div>

        <div v-else class="patrimoine-content">
          <!-- Section Comptes -->
          <section class="comptes-section">
            <div class="section-header">
              <h2 class="section-title">📊 Soldes par compte</h2>
              <RouterLink to="/comptes" class="section-link">
                Gérer les comptes →
              </RouterLink>
            </div>

            <div v-if="comptes.length === 0" class="empty-state glass-card">
              <span class="empty-icon">💳</span>
              <h3>Aucun compte configuré</h3>
              <p>Ajoutez votre premier compte pour commencer</p>
              <RouterLink to="/comptes" class="btn btn-primary">
                Ajouter un compte
              </RouterLink>
            </div>

            <div v-else class="comptes-list">
              <div
                v-for="(compte, index) in comptes"
                :key="compte.id"
                class="compte-card glass-card slide-in"
                :style="{ animationDelay: `${index * 0.1}s` }"
                :class="{
                  'has-libre': argentLibre(compte.id) > 0,
                  'has-allocations': hasAllocations(compte.id),
                  'expanded': isExpanded(compte.id),
                  'clickable': hasAllocations(compte.id)
                }"
              >
                <!-- Main row -->
                <div
                  class="compte-main-row"
                  @click.stop="handleCompteClick(compte.id)"
                >
                  <div
                    class="bank-indicator"
                    :style="{ backgroundColor: compte.banque?.couleurTheme || '#667EEA' }"
                  ></div>

                  <span class="compte-icon">{{ getCompteIcon(compte.type) }}</span>

                  <div class="compte-info">
                    <h3 class="compte-name">{{ getTypeLabel(compte.type) }}</h3>
                    <p class="compte-bank">{{ compte.nom }} • {{ compte.banque?.nom }}</p>
                  </div>

                  <span v-if="hasAllocations(compte.id)" class="allocation-badge">
                    🎯 {{ getCompteAllocations(compte.id).length }}
                  </span>

                  <div class="compte-amounts">
                    <div class="amount-group">
                      <span class="amount-label">Total</span>
                      <span class="amount-value">{{ formatCurrency(compte.soldeTotal) }}</span>
                    </div>
                    <div class="amount-group" v-if="compte.soldeTotal > 0">
                      <span class="amount-label">Libre</span>
                      <span
                        class="amount-value"
                        :class="argentLibre(compte.id) > 0 ? 'success' : 'muted'"
                      >
                        {{ formatCurrency(argentLibre(compte.id)) }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Allocations Details (expandable) -->
                <transition name="expand">
                  <div v-if="isExpanded(compte.id)"
                       :key="`allocations-${compte.id}`"
                       class="compte-allocations"
                       @click.stop>
                    <div class="allocations-header">
                      <span class="allocations-title">💰 Argent alloué aux objectifs</span>
                    </div>
                    <div class="allocations-list">
                      <div
                        v-for="allocation in getCompteAllocations(compte.id)"
                        :key="`${compte.id}-${allocation.objectifId}`"
                        class="allocation-item"
                      >
                      <div class="allocation-objectif">
                        <span class="objectif-icon">{{ allocation.objectifIcone }}</span>
                        <span class="objectif-nom">{{ allocation.objectifNom }}</span>
                      </div>
                      <div class="allocation-montant">
                        {{ formatCurrency(allocation.montant) }}
                      </div>
                    </div>
                  </div>
                  </div>
                </transition>
              </div>
            </div>

            <!-- Total summary -->
            <div v-if="comptes.length > 0" class="total-summary glass-card">
              <span class="summary-label">Total patrimoine</span>
              <span class="summary-value">{{ formatCurrency(totalPatrimoine) }}</span>
            </div>
          </section>

          <!-- Section Objectifs -->
          <section class="objectifs-section">
            <div class="section-header">
              <h2 class="section-title">🎯 Vos Objectifs</h2>
              <RouterLink to="/objectifs" class="section-link">
                Gérer les objectifs →
              </RouterLink>
            </div>

            <div v-if="objectifs.length === 0" class="empty-state glass-card">
              <span class="empty-icon">🎯</span>
              <h3>Aucun objectif défini</h3>
              <p>Créez votre premier objectif d'épargne</p>
              <RouterLink to="/objectifs" class="btn btn-primary">
                Créer un objectif
              </RouterLink>
            </div>

            <div v-else class="objectifs-list">
              <div
                v-for="(objectif, index) in objectifs"
                :key="objectif.id"
                class="objectif-card glass-card slide-in"
                :style="{ animationDelay: `${(comptes.length + index) * 0.1}s` }"
              >
                <div class="objectif-header">
                  <div class="objectif-title">
                    <span class="objectif-icon">{{ objectif.icone || '📊' }}</span>
                    <div>
                      <h3>{{ objectif.nom }}</h3>
                      <span class="objectif-priority" :class="`priority-${getPriorityLevel(objectif.priorite)}`">
                        Priorité {{ getPriorityLevel(objectif.priorite) }}
                      </span>
                    </div>
                  </div>
                  <div class="objectif-amount">
                    <span class="current">{{ formatCurrency(getMontantActuel(objectif)) }}</span>
                    <span class="separator">/</span>
                    <span class="target">{{ formatCurrency(objectif.montantCible) }}</span>
                  </div>
                </div>

                <div class="objectif-progress">
                  <div class="progress-bar">
                    <div
                      class="progress-fill"
                      :style="{
                        width: `${Math.min(getProgression(objectif), 100)}%`,
                        backgroundColor: objectif.couleur || '#4CAF50'
                      }"
                    ></div>
                  </div>
                  <span class="progress-percent">{{ getProgression(objectif).toFixed(0) }}%</span>
                </div>

                <!-- Répartitions multi-comptes -->
                <div v-if="objectif.repartitions && objectif.repartitions.length > 0" class="repartitions">
                  <div class="repartitions-header">
                    <span>Réparti sur {{ objectif.repartitions.length }} compte(s)</span>
                  </div>
                  <div class="repartitions-list">
                    <div
                      v-for="repartition in objectif.repartitions"
                      :key="repartition.id"
                      class="repartition-item"
                    >
                      <span class="repartition-compte">
                        {{ getCompteNom(repartition.compte?.id) }}
                      </span>
                      <span class="repartition-montant">
                        {{ formatCurrency(repartition.montantActuel) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>
  </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useDashboardStore } from '@/stores/dashboard'
import MainLayout from '@/layouts/MainLayout.vue'
import { logger } from '@/utils/logger'
import { formatCurrency } from '@/utils/formatters'
import type { TypeCompte } from '@/types'

const dashboardStore = useDashboardStore()
const {
  loading,
  user,
  comptes,
  objectifs
} = storeToRefs(dashboardStore)

const { loadDashboard } = dashboardStore

// Expanded compte state
const expandedCompte = ref<string | null>(null)

// Helper functions

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

const getCompteNom = (compteId: string | undefined): string => {
  if (!compteId) return 'Compte inconnu'
  const compte = comptes.value.find(c => c.id === compteId)
  return compte ? compte.nom : 'Compte inconnu'
}

// Calculs de progression des objectifs
const getMontantActuel = (objectif: any): number => {
  if (!objectif.repartitions || objectif.repartitions.length === 0) return 0
  return objectif.repartitions.reduce((total: number, rep: any) => total + (rep.montantActuel || 0), 0)
}

const getProgression = (objectif: any): number => {
  const actuel = getMontantActuel(objectif)
  const cible = objectif.montantCible || 1
  return (actuel / cible) * 100
}

const getPriorityLevel = (priorite: any): number => {
  const levels: Record<string, number> = {
    'CRITIQUE': 1,
    'TRES_HAUTE': 2,
    'HAUTE': 3,
    'MOYENNE': 4,
    'BASSE': 5
  }
  return levels[priorite] || 3
}

// Argent libre par compte
const argentLibre = (compteId: string): number => {
  const compte = comptes.value.find(c => c.id === compteId)
  return compte?.argentLibre ?? 0
}

// Récupérer toutes les allocations pour un compte
const getCompteAllocations = (compteId: string) => {
  const allocations: Array<{
    objectifId: string
    objectifNom: string
    objectifIcone: string
    montant: number
  }> = []

  objectifs.value.forEach(objectif => {
    if (objectif.repartitions) {
      objectif.repartitions.forEach(repartition => {
        if (repartition.compte?.id === compteId) {
          allocations.push({
            objectifId: objectif.id,
            objectifNom: objectif.nom,
            objectifIcone: objectif.icone || '🎯',
            montant: repartition.montantActuel
          })
        }
      })
    }
  })

  return allocations
}

// Check if compte has allocations
const hasAllocations = (compteId: string): boolean => {
  return getCompteAllocations(compteId).length > 0
}

// Check if compte is expanded
const isExpanded = (compteId: string): boolean => {
  return expandedCompte.value === compteId
}

// Handle compte card click
const handleCompteClick = (compteId: string) => {
  if (hasAllocations(compteId)) {
    if (expandedCompte.value === compteId) {
      expandedCompte.value = null
    } else {
      expandedCompte.value = compteId
    }
  }
}

// Computed properties
const totalPatrimoine = computed(() => {
  return comptes.value.reduce((sum, c) => sum + (c.soldeTotal || 0), 0)
})

const comptesParType = computed(() => {
  const grouped: Record<string, any[]> = {}
  comptes.value.forEach(compte => {
    const type = compte.type || 'AUTRE'
    if (!grouped[type]) {
      grouped[type] = []
    }
    grouped[type].push(compte)
  })
  return grouped
})

onMounted(async () => {
  logger.log('🏦 PatrimoineView mounted, loading data...')
  await loadDashboard()
})
</script>

<style scoped>
/* Page Title Header */
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
  font-size: 1rem;
  margin: 0;
}

.header-stats {
  display: flex;
  gap: 1rem;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 1rem 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #fff;
}

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

.patrimoine-content {
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #E8EAF6;
  margin: 0;
}

.section-link {
  color: #667EEA;
  text-decoration: none;
  font-size: 0.875rem;
  transition: all 0.2s;
}

.section-link:hover {
  color: #764BA2;
  transform: translateX(4px);
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  color: #E8EAF6;
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
}

.empty-state p {
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 1.5rem 0;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  display: inline-block;
  transition: all 0.2s;
  cursor: pointer;
  border: none;
  font-size: 0.875rem;
}

.btn-primary {
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

/* Comptes Section */
.comptes-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.compte-card {
  position: relative;
  overflow: hidden;
}

.compte-card.expanded {
  border-color: #667EEA;
}

.bank-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #667EEA;
}

.compte-main-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.2s ease;
}

.compte-card.clickable .compte-main-row {
  cursor: pointer;
}

.compte-card.clickable .compte-main-row:hover {
  background: rgba(255, 255, 255, 0.03);
}

.compte-icon {
  font-size: 2rem;
  margin-left: 1rem;
}

.compte-info {
  flex: 1;
  min-width: 0;
}

.compte-name {
  font-size: 1rem;
  font-weight: 600;
  color: #E8EAF6;
  margin: 0 0 0.25rem 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.compte-bank {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.allocation-badge {
  background: rgba(102, 126, 234, 0.2);
  color: #667EEA;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}

.compte-amounts {
  display: flex;
  gap: 1rem;
}

.amount-group {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.amount-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 0.25rem;
}

.amount-value {
  font-size: 1rem;
  font-weight: 700;
  color: #E8EAF6;
}

.amount-value.success {
  color: #4CAF50;
}

.amount-value.muted {
  color: rgba(255, 255, 255, 0.4);
}

.compte-allocations {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.allocations-header {
  margin-bottom: 0.75rem;
}

.allocations-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.allocations-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.allocation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.allocation-objectif {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.objectif-icon {
  font-size: 1.25rem;
}

.objectif-nom {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
}

.allocation-montant {
  font-size: 0.875rem;
  font-weight: 600;
  color: #4CAF50;
}

.total-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  padding: 1.5rem;
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
}

.summary-label {
  font-size: 1.125rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.summary-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #667EEA;
}

/* Objectifs Section */
.objectifs-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.objectif-card {
  padding: 1.5rem;
}

.objectif-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.objectif-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.objectif-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.objectif-title .objectif-icon {
  font-size: 2rem;
}

.objectif-title h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: #E8EAF6;
  margin: 0 0 0.25rem 0;
}

.objectif-priority {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  font-weight: 600;
}

.priority-1 { background: rgba(244, 67, 54, 0.2); color: #EF5350; }
.priority-2 { background: rgba(255, 152, 0, 0.2); color: #FFA726; }
.priority-3 { background: rgba(255, 235, 59, 0.2); color: #FFEB3B; }

.objectif-amount {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
}

.objectif-amount .current {
  font-size: 1.25rem;
  font-weight: 700;
  color: #4CAF50;
}

.objectif-amount .separator {
  color: rgba(255, 255, 255, 0.4);
}

.objectif-amount .target {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.6);
}

.objectif-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.progress-bar {
  flex: 1;
  height: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #4CAF50;
  transition: width 0.5s ease;
}

.progress-percent {
  font-size: 0.875rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  min-width: 45px;
  text-align: right;
}

.repartitions {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.repartitions-header {
  margin-bottom: 0.75rem;
}

.repartitions-header span {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

.repartitions-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.repartition-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.repartition-compte {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
}

.repartition-montant {
  font-size: 0.875rem;
  font-weight: 600;
  color: #4CAF50;
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
    align-items: flex-start;
  }

  .header-stats {
    width: 100%;
  }

  .stat-card {
    flex: 1;
  }

  .comptes-list {
    grid-template-columns: 1fr;
  }

  .compte-main-row {
    flex-wrap: wrap;
  }

  .compte-amounts {
    width: 100%;
    justify-content: space-between;
    margin-top: 0.5rem;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}

@media (max-width: 640px) {
  .page-title {
    font-size: 1.5rem;
  }

  .stat-value {
    font-size: 1.25rem;
  }
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

.slide-in {
  animation: slideIn 0.5s ease-out backwards;
}

/* Expand transition for allocations */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 500px;
}

/* Enhanced transitions */
.compte-card,
.objectif-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.compte-allocations,
.repartitions {
  animation: slideIn 0.3s ease-out;
}

.progress-fill {
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
