<template>
    <div class="onboarding-view">
      <div class="onboarding-container">
        <!-- Progress Bar -->
        <div class="progress-container">
          <div class="progress-bar">
            <div 
              class="progress-fill"
              :style="{ width: `${progress}%` }"
            ></div>
          </div>
          <div class="step-counter">
            Étape {{ currentStep }} / {{ totalSteps }}
          </div>
        </div>
  
        <!-- Error Message -->
        <div v-if="error" class="error-message" @click="clearError">
          {{ error }}
          <span class="error-close">×</span>
        </div>
  
        <!-- Steps -->
        <Transition name="slide" mode="out-in">
          <component 
            :is="currentStepComponent"
            :key="currentStep"
          />
        </Transition>
  
        <!-- Navigation -->
        <div class="navigation">
          <button 
            v-if="currentStep > 1"
            @click="prevStep"
            class="btn btn-secondary"
            :disabled="loading"
          >
            ← Retour
          </button>
          <div v-else></div>
          
          <button 
            v-if="currentStep < totalSteps"
            @click="handleNext"
            class="btn btn-primary"
            :disabled="!canGoNext || loading"
          >
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>Continuer →</span>
          </button>
          
          <button 
            v-else
            @click="finishOnboarding"
            class="btn btn-primary"
            :disabled="loading"
          >
            <span v-if="loading" class="loading-spinner"></span>
            <span v-else>Terminer</span>
          </button>
        </div>
      </div>
    </div>
</template>
  
<script setup lang="ts">
  import { computed, onMounted, type Component } from 'vue'
  import { useRouter } from 'vue-router'
  import { storeToRefs } from 'pinia'
  import { useOnboardingStore } from '@/stores/onboarding'

  // Composants des étapes
  import PersonalInfoStep from '@/components/onboarding/PersonalInfoStep.vue'
  import BankSelectionStep from '@/components/onboarding/BankSelectionStep.vue' 
  import AccountSetupStep from '@/components/onboarding/AccountSetupStep.vue'
  import ObjectiveSetupStep from '@/components/onboarding/ObjectiveSetupStep.vue'
  import SummaryStep from '@/components/onboarding/SummaryStep.vue'
  import BudgetConfigStep from '@/components/onboarding/BudgetConfigStep.vue'
  import ChargesFixesStep from '@/components/onboarding/ChargesFixesStep.vue'
  
  const router = useRouter()
  const onboardingStore = useOnboardingStore()

  const { 
    currentStep, 
    totalSteps, 
    loading, 
    error, 
    progress, 
    canGoNext 
  } = storeToRefs(onboardingStore)
  
  const { nextStep, prevStep, submitOnboarding, loadBanks, clearError } = onboardingStore
  
  const stepComponents: Record<number, any> = {
    1: PersonalInfoStep,
    2: BankSelectionStep,
    3: AccountSetupStep,
    4: ObjectiveSetupStep,
    5: BudgetConfigStep,
    6: ChargesFixesStep,
    7: SummaryStep
    }

  const currentStepComponent = computed(() => stepComponents[currentStep.value])
  
  const handleNext = async (): Promise<void> => {
    if (currentStep.value === 2) {
      await loadBanks()
    }
    nextStep()
  }
  
  const finishOnboarding = async (): Promise<void> => {
    const success = await submitOnboarding()
    if (success) {
      // Onboarding completed, redirect to dashboard
      router.push('/dashboard')
    }
  }
  
  onMounted(() => {
    // Charger les banques dès le début pour être prêt
    loadBanks()
  })
</script>