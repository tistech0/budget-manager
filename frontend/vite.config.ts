import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  plugins: [
    vue({
      // Enable script setup optimizations
      script: {
        defineModel: true,
        propsDestructure: true
      }
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 8000,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: mode === 'development',
    target: 'es2015',
    // Chunk size optimization
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        // Manual chunk splitting for better caching
        manualChunks: {
          // Vendor chunks
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Validation library
          'validation': ['zod'],
          // Chart components (if they exist)
          'charts': [
            './src/components/BudgetPieChart.vue',
            './src/components/ObjectifProgressChart.vue'
          ].filter(path => {
            try {
              require.resolve(path)
              return true
            } catch {
              return false
            }
          })
        },
        // Optimize chunk file names
        chunkFileNames: (chunkInfo) => {
          const facadeModuleId = chunkInfo.facadeModuleId
          if (facadeModuleId) {
            // Group by feature/directory
            if (facadeModuleId.includes('/views/')) {
              return 'assets/views/[name]-[hash].js'
            }
            if (facadeModuleId.includes('/components/')) {
              return 'assets/components/[name]-[hash].js'
            }
            if (facadeModuleId.includes('/stores/')) {
              return 'assets/stores/[name]-[hash].js'
            }
          }
          return 'assets/[name]-[hash].js'
        },
        // Optimize entry and asset file names
        entryFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    },
    // Enable minification
    minify: mode === 'production' ? 'esbuild' : false,
    // CSS code splitting
    cssCodeSplit: true,
    // Optimize dependencies
    commonjsOptions: {
      include: [/node_modules/],
      transformMixedEsModules: true
    }
  },
  esbuild: {
    drop: mode === 'production' ? ['console', 'debugger'] : [],
    // Pure annotations for better tree-shaking
    pure: mode === 'production' ? ['console.log', 'console.info', 'console.debug'] : [],
    // Optimize for modern browsers in production
    target: mode === 'production' ? 'es2020' : 'es2015'
  },
  // Optimize dependency pre-bundling
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'zod'
    ],
    exclude: []
  },
  // Performance optimizations
  preview: {
    port: 8000
  }
}))
