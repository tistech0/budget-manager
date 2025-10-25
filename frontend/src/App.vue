<template>
  <div id="app">
    <router-view />
    <ToastNotification />
  </div>
</template>

<script setup lang="ts">
import ToastNotification from '@/components/ToastNotification.vue'
</script>

<style>
/* Styles globaux si nécessaire */
#app {
  width: 100%;
  height: 100vh;
}
</style>