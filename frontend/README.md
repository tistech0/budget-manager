# Budget Manager v2 - Frontend

Modern Vue 3 frontend for the Budget Manager application with TypeScript, Pinia, and Vite.

## Tech Stack

- **Vue 3.5.13** - Composition API with `<script setup>`
- **TypeScript 5.6.3** - Full type safety
- **Pinia 2.3.0** - State management
- **Vite 6.0.3** - Build tool with HMR
- **Vue Router 4.4.5** - Client-side routing
- **Chart.js 4.4.7** - Data visualizations
- **Axios 1.7.9** - HTTP client
- **Zod 3.24.1** - Runtime validation

## Quick Start

### Development

```bash
# Install dependencies
npm install

# Start dev server (http://localhost:8000)
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint

# Fix linting issues
npm run lint:fix
```

### Testing

```bash
# Run unit tests
npm run test:unit

# Run tests in watch mode
npm run test:unit:watch

# Run tests with coverage
npm run test:unit:coverage

# Run E2E tests
npm run test:e2e

# Run E2E tests in UI mode
npm run test:e2e:ui
```

### Production Build

```bash
# Build for production
npm run build

# Preview production build
npm run preview
```

## Project Structure

```
frontend/
├── public/                      # Static assets
├── src/
│   ├── assets/                  # Images, fonts, styles
│   ├── components/              # Reusable Vue components
│   │   ├── cards/              # Card components
│   │   ├── charts/             # Chart.js wrapper components
│   │   ├── layout/             # Layout components (MainLayout, etc.)
│   │   └── modals/             # Modal dialogs
│   ├── composables/             # Vue composables
│   │   ├── useToast.ts         # Toast notifications
│   │   └── useMonthNavigation.ts
│   ├── router/                  # Vue Router configuration
│   │   └── index.ts
│   ├── services/                # API and external services
│   │   └── api.ts              # Axios API client
│   ├── stores/                  # Pinia stores
│   │   ├── dashboard.ts        # Main dashboard store
│   │   └── onboarding.ts       # Onboarding flow store
│   ├── types/                   # TypeScript type definitions
│   │   └── index.ts
│   ├── utils/                   # Utility functions
│   │   ├── currency.ts
│   │   ├── date.ts
│   │   └── logger.ts
│   ├── views/                   # Page components
│   │   ├── __tests__/          # View tests
│   │   ├── DashboardView.vue
│   │   ├── TransactionsView.vue
│   │   ├── ComptesView.vue
│   │   ├── ObjectifsView.vue
│   │   ├── UserProfileView.vue
│   │   ├── PatrimoineView.vue
│   │   └── OnboardingView.vue
│   ├── App.vue                 # Root component
│   └── main.ts                 # Application entry point
├── .env                        # Environment variables
├── index.html                  # HTML template
├── vite.config.ts              # Vite configuration
├── tsconfig.json               # TypeScript configuration
├── vitest.config.ts            # Vitest configuration
└── playwright.config.ts        # Playwright E2E configuration
```

## Key Features

### Views

- **DashboardView** - Overview with charts, budget tracking, quick stats
- **TransactionsView** - Full transaction list with filtering, sorting, search
- **ComptesView** - Account management with balance tracking
- **ObjectifsView** - Savings objectives with multi-account allocation
- **UserProfileView** - User profile, budget config, fixed charges management
- **PatrimoineView** - Wealth overview with account and objective breakdowns
- **OnboardingView** - 7-step guided onboarding for new users

### State Management

**Dashboard Store** (`stores/dashboard.ts`):
- Centralized state for dashboard data
- Computed refs for transactions, accounts, objectives, user
- Actions for loading, validating, and updating data
- Month navigation and validation system

**Onboarding Store** (`stores/onboarding.ts`):
- Multi-step onboarding flow
- User data collection
- Account and objective setup
- Validation and submission

### API Integration

**API Service** (`services/api.ts`):
- Axios-based HTTP client
- Automatic base URL configuration
- Request/response interceptors
- Error handling with toast notifications
- Typed API methods for all endpoints

### Design System

**Colors:**
- Primary: `#667EEA → #764BA2` (Purple gradient)
- Success: `#10B981` (Green)
- Warning: `#F59E0B` (Orange)
- Danger: `#EF4444` (Red)

**UI Pattern:**
- Glass-morphism cards with backdrop blur
- Gradient buttons and accents
- Smooth animations and transitions
- Responsive mobile-first design

## Environment Variables

Create a `.env` file:

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=10000

# Optional: Environment flag
VITE_APP_ENV=development
```

## Build Optimization

### Code Splitting

The build uses intelligent code splitting:
- **Vendor chunks**: Vue, Router, Pinia separated
- **View chunks**: Each view lazy-loaded separately
- **Component chunks**: Grouped by type (modals, cards, charts)

### Performance Features

- Tree-shaking for minimal bundle size
- CSS code splitting per component
- Lazy-loaded routes
- Minification with ESBuild
- Gzip compression (67% reduction)

### Build Stats (Production)

```
Total Bundle Size: 768 KB
Gzipped First Load: 66 KB

JavaScript:
  - Vue vendor: 145 KB → 47 KB gzipped
  - Charts: 197 KB → 59 KB gzipped
  - Views: 8-202 KB each (lazy-loaded)

CSS: 82 KB → 15 KB gzipped
```

## Testing

### Unit Tests (Vitest)

- 535+ tests with 99.4% pass rate
- Component tests for all views
- Store tests for state management
- Utility function tests
- Mock API responses

**Coverage:**
- Statements: >90%
- Branches: >85%
- Functions: >90%
- Lines: >90%

### E2E Tests (Playwright)

- Full user flows
- Cross-browser testing (Chrome, Firefox, Safari)
- Mobile viewport testing
- Screenshot comparison

## Browser Support

- Chrome 90+ ✅
- Firefox 88+ ✅
- Safari 14+ ✅
- Edge 90+ ✅

## IDE Setup

### Recommended

- **VS Code** with extensions:
  - Vue - Official (Volar)
  - TypeScript Vue Plugin (Volar)
  - ESLint
  - Prettier

### Configuration

TypeScript support for `.vue` files is enabled via `vue-tsc`. The Volar extension provides IntelliSense, type checking, and auto-completion.

## Development Tips

### Hot Module Replacement

Vite HMR updates the page instantly on file changes without losing state.

### Type Safety

All components, stores, and services are fully typed. Use TypeScript's IntelliSense for autocomplete and error checking.

### Debugging

Use Vue DevTools browser extension for:
- Component inspection
- Pinia store state
- Router navigation
- Performance profiling

### Performance

- Lazy-load heavy components
- Use `v-once` for static content
- Debounce user input handlers
- Use computed refs for derived state
- Avoid watchers when possible (use computed)

## Common Tasks

### Adding a New View

1. Create component in `src/views/YourView.vue`
2. Add route in `src/router/index.ts`
3. Add navigation link in `MainLayout.vue`
4. Create tests in `src/views/__tests__/YourView.spec.ts`

### Adding a New API Endpoint

1. Add method to `src/services/api.ts`
2. Define TypeScript types in `src/types/index.ts`
3. Call from component or store
4. Handle errors with try/catch

### Adding a New Store

1. Create file in `src/stores/yourStore.ts`
2. Define state, getters, actions
3. Import and use with `useYourStore()`

## Troubleshooting

### Type Errors

```bash
# Run type checking
npm run type-check

# Common issues:
# - Missing type definitions: Install @types/* packages
# - Vue component types: Ensure Volar is installed
```

### Build Errors

```bash
# Clear cache and rebuild
rm -rf node_modules .vite
npm install
npm run build
```

### Test Failures

```bash
# Run specific test file
npm run test:unit -- YourComponent.spec.ts

# Debug mode
npm run test:unit -- --inspect-brk
```

## Documentation

- [Main README](../README.md) - Full project documentation
- [Docker Setup](../docs/DOCKER.md) - Docker Compose guide
- [Performance Guidelines](../docs/PERFORMANCE_GUIDELINES.md) - Optimization best practices

## License

Private project for personal use.

---

**Built with ❤️ using Vue 3 & TypeScript**
