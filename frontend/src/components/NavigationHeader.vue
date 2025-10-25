<template>
  <header class="main-header">
    <div class="container">
      <div class="header-content">
        <div class="header-left">
          <div class="logo">
            <span class="logo-icon">💰</span>
            <span class="logo-text">Budget Manager</span>
          </div>
        </div>

        <nav class="main-nav">
          <RouterLink to="/dashboard" class="nav-item">
            <span class="nav-icon">📊</span>
            <span class="nav-text">Dashboard</span>
          </RouterLink>
          <RouterLink to="/patrimoine" class="nav-item">
            <span class="nav-icon">💎</span>
            <span class="nav-text">Patrimoine</span>
          </RouterLink>
          <RouterLink to="/transactions" class="nav-item">
            <span class="nav-icon">📝</span>
            <span class="nav-text">Transactions</span>
          </RouterLink>
        </nav>

        <div class="header-right">
          <div class="user-profile" @click="goToProfile">
            <div class="user-avatar">{{ userInitial }}</div>
            <span class="user-name">{{ userName }}</span>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useOnboardingStore } from '@/stores/onboarding'

const router = useRouter()
const onboardingStore = useOnboardingStore()

const userInitial = computed(() => {
  return onboardingStore.currentUser?.prenom?.charAt(0) || 'U'
})

const userName = computed(() => {
  return onboardingStore.currentUser?.prenom || 'User'
})

const goToProfile = () => {
  router.push('/profil')
}
</script>

<style scoped>
.main-header {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--glass-border);
  padding: var(--spacing-lg) 0;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--glass-shadow);
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
  gap: var(--spacing-xl);
}

.header-left {
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  cursor: pointer;
}

.logo-icon {
  font-size: 2rem;
  filter: drop-shadow(0 2px 8px rgba(102, 126, 234, 0.3));
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.main-nav {
  display: flex;
  gap: var(--spacing-sm);
  background: rgba(255, 255, 255, 0.05);
  padding: var(--spacing-xs);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-item {
  position: relative;
  color: var(--text-secondary);
  text-decoration: none;
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 500;
  font-size: 0.95rem;
}

.nav-icon {
  font-size: 1.2rem;
  transition: transform var(--transition-fast);
}

.nav-text {
  transition: color var(--transition-fast);
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
}

.nav-item.router-link-active {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.nav-item.router-link-active .nav-icon {
  transform: scale(1.15);
}

.header-right {
  flex-shrink: 0;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.1);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
  font-size: 1rem;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.95rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .main-nav {
    display: none;
  }

  .logo-text {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
