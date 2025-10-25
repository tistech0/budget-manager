<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-overlay" @click="handleCancel">
      <div class="modal-content glass-card" @click.stop>
        <!-- Modal Header -->
        <div class="modal-header">
          <div class="header-left">
            <span class="warning-icon">⚠️</span>
            <h3 class="modal-title">Supprimer le mois de {{ monthLabel }}</h3>
          </div>
          <button @click="handleCancel" class="btn-close">✕</button>
        </div>

        <!-- Modal Body -->
        <div class="modal-body">
          <p class="warning-text">
            Êtes-vous sûr de vouloir supprimer le salaire validé pour <strong>{{ monthLabel }}</strong> ?
          </p>

          <div class="warning-box">
            <p class="warning-box-title">⚠️ Cette action est irréversible et supprimera :</p>
            <ul class="deletion-list">
              <li>❌ Le salaire validé pour ce mois</li>
              <li>❌ Toutes les transactions du mois</li>
              <li>❌ Les dépenses variables enregistrées</li>
              <li>❌ L'historique de ce mois</li>
            </ul>
          </div>

          <div class="info-box">
            <p class="info-text">
              💡 Les comptes et objectifs ne seront pas affectés
            </p>
          </div>

          <!-- Confirmation Input -->
          <div class="confirmation-section">
            <label class="confirmation-label">
              Pour confirmer, tapez <strong>"SUPPRIMER"</strong> :
            </label>
            <input
              v-model="confirmationText"
              type="text"
              class="confirmation-input"
              placeholder="Tapez SUPPRIMER pour confirmer"
              @keyup.enter="handleConfirm"
            />
            <p v-if="showError" class="error-message">
              Vous devez taper "SUPPRIMER" pour confirmer la suppression
            </p>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="modal-footer">
          <button @click="handleCancel" class="btn btn-secondary" :disabled="isDeleting">
            Annuler
          </button>
          <button
            @click="handleConfirm"
            class="btn btn-danger"
            :disabled="!canDelete || isDeleting"
          >
            <span v-if="isDeleting">Suppression...</span>
            <span v-else>Supprimer le mois</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

// Props
interface Props {
  modelValue: boolean
  monthLabel: string
  monthValue: string
  isDeleting?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isDeleting: false
})

// Emits
interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm', monthValue: string): void
  (e: 'cancel'): void
}

const emit = defineEmits<Emits>()

// State
const confirmationText = ref('')
const showError = ref(false)

// Computed
const canDelete = computed(() => {
  return confirmationText.value === 'SUPPRIMER'
})

// Methods
const handleConfirm = () => {
  if (!canDelete.value) {
    showError.value = true
    return
  }
  emit('confirm', props.monthValue)
}

const handleCancel = () => {
  emit('update:modelValue', false)
  emit('cancel')
  resetModal()
}

const resetModal = () => {
  confirmationText.value = ''
  showError.value = false
}

// Watch for modal close/open
watch(() => props.modelValue, (newValue) => {
  if (!newValue) {
    resetModal()
  }
})
</script>

<style scoped>
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
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-content {
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Modal Header */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 2rem 2rem 1rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.warning-icon {
  font-size: 2.5rem;
  filter: drop-shadow(0 2px 8px rgba(255, 152, 0, 0.3));
}

.modal-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #E8EAF6;
  line-height: 1.3;
}

.btn-close {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-size: 1.5rem;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

/* Modal Body */
.modal-body {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.warning-text {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  line-height: 1.6;
}

.warning-text strong {
  color: #FF9800;
  font-weight: 700;
}

/* Warning Box */
.warning-box {
  background: rgba(244, 67, 54, 0.1);
  border: 1px solid rgba(244, 67, 54, 0.3);
  border-radius: 12px;
  padding: 1.5rem;
}

.warning-box-title {
  color: #F44336;
  font-weight: 700;
  font-size: 0.95rem;
  margin: 0 0 1rem 0;
}

.deletion-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.deletion-list li {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
  padding-left: 0.5rem;
  line-height: 1.5;
}

/* Info Box */
.info-box {
  background: rgba(76, 175, 80, 0.1);
  border: 1px solid rgba(76, 175, 80, 0.3);
  border-radius: 12px;
  padding: 1rem;
}

.info-text {
  color: #4CAF50;
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.5;
}

/* Confirmation Section */
.confirmation-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.confirmation-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.confirmation-label strong {
  color: #FF9800;
  font-weight: 700;
}

.confirmation-input {
  padding: 0.875rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: #E8EAF6;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.2s;
}

.confirmation-input:focus {
  outline: none;
  background: rgba(255, 255, 255, 0.15);
  border-color: #FF9800;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.2);
}

.confirmation-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
  font-weight: 400;
  letter-spacing: normal;
}

.error-message {
  color: #F44336;
  font-size: 0.875rem;
  margin: 0;
  font-weight: 500;
}

/* Modal Footer */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1rem 2rem 2rem 2rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
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

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #E8EAF6;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
}

.btn-danger {
  background: linear-gradient(135deg, #F44336 0%, #D32F2F 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}

.btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(244, 67, 54, 0.4);
}

/* Responsive */
@media (max-width: 768px) {
  .modal-content {
    max-width: 100%;
    margin: 0;
  }

  .modal-header {
    padding: 1.5rem 1.5rem 1rem 1.5rem;
  }

  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .warning-icon {
    font-size: 2rem;
  }

  .modal-title {
    font-size: 1.25rem;
  }

  .modal-body {
    padding: 1.5rem;
  }

  .modal-footer {
    flex-direction: column-reverse;
    padding: 1rem 1.5rem 1.5rem 1.5rem;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
