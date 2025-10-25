<template>
  <div class="main-layout">
    <NavigationHeader />
    <main class="main-content">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import NavigationHeader from '@/components/NavigationHeader.vue'
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(118, 75, 162, 0.1) 0%, transparent 50%),
    linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
}

.main-content {
  /* Content wrapper - views add their own container/padding */
}
</style>
