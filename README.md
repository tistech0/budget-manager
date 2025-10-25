# Budget Manager v2.0

[![CI - Pull Request](https://github.com/tistech0/budget-manager-v2/actions/workflows/ci-pr.yml/badge.svg)](https://github.com/tistech0/budget-manager-v2/actions/workflows/ci-pr.yml)
[![CI/CD - Main Branch](https://github.com/tistech0/budget-manager-v2/actions/workflows/ci-main.yml/badge.svg)](https://github.com/tistech0/budget-manager-v2/actions/workflows/ci-main.yml)

A modern personal budget management application with multi-account savings objectives allocation, transaction tracking, and intelligent budget distribution.

## Features

### Core Functionality
- **User Profile Management** - Customize your financial profile, salary, budget distribution (50/30/20 rule)
- **Fixed Charges Management** - Configure recurring expenses (rent, subscriptions, utilities) with automatic tracking
- **Multi-Account Support** - Manage multiple bank accounts (checking, savings, PEA, etc.)
- **Savings Objectives** - Create goals with multi-account allocation tracking
- **Transaction Management** - Full CRUD operations with categorization and filtering
- **PDF Bank Statement Import** - Automatic parsing and transaction extraction
- **Dashboard Analytics** - Visual insights with charts and budget tracking
- **Month Validation System** - Lock and validate monthly budgets with rollback capability

### User Experience
- **7-Step Onboarding** - Guided setup for new users
- **Glass-morphism Design** - Modern, elegant UI with smooth animations
- **Real-time Budget Tracking** - Live updates of spending vs. budget
- **Intelligent Free Money Calculation** - Automatically calculates available funds
- **Responsive Design** - Works seamlessly on desktop and mobile

## Tech Stack

### Backend
- **Quarkus 3.26.4** - Supersonic Subatomic Java Framework
- **Java 21** - Latest LTS with modern language features
- **Hibernate ORM with Panache** - Simplified database operations
- **PostgreSQL** (production) / **H2** (development)
- **RESTEasy Reactive** - High-performance REST endpoints
- **SmallRye OpenAPI** - Automatic API documentation

### Frontend
- **Vue 3** - Progressive JavaScript Framework with Composition API
- **TypeScript** - Type-safe development
- **Pinia** - Intuitive state management
- **Vite** - Lightning-fast build tool
- **Axios** - HTTP client with interceptors
- **Chart.js** - Beautiful data visualizations

## Quick Start

### Prerequisites
- Docker & Docker Compose (recommended)
- OR Java 21 + Node.js 18+ (for local development)

### Option 1: Docker Compose (Recommended)

**Development with hot reload + PostgreSQL:**
```bash
# Start all services with persistent data
docker-compose -f docker-compose.dev.yml up

# Run in background
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Stop services
docker-compose -f docker-compose.dev.yml down
```

**Access:**
- Frontend: http://localhost:8000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger
- PostgreSQL: localhost:5432

**Benefits:**
- ✅ Data persists between restarts (PostgreSQL volume)
- ✅ Hot reload for both backend and frontend
- ✅ No local Java/Node.js installation required
- ✅ Production-like database environment

**Full Docker documentation:** See [docs/DOCKER.md](docs/DOCKER.md)

### Option 2: Local Development

**Backend:**
```bash
cd backend
./mvnw quarkus:dev
# Runs on http://localhost:8080
# API docs: http://localhost:8080/swagger
# H2 Console: http://localhost:8080/h2-console
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
# Runs on http://localhost:8000
```

**Note:** Local development uses H2 in-memory database which may reset on schema changes. Use Docker for persistent data.

## Project Structure

```
budget-manager-v2/
├── backend/                          # Quarkus Backend
│   └── src/main/java/com/budgetmanager/
│       ├── entity/                   # JPA Entities
│       │   ├── User.java
│       │   ├── Compte.java
│       │   ├── Objectif.java
│       │   ├── Transaction.java
│       │   └── ...
│       ├── resource/                 # REST Endpoints
│       │   ├── DashboardResource.java
│       │   ├── TransactionResource.java
│       │   ├── CompteResource.java
│       │   └── ...
│       ├── service/                  # Business Logic
│       │   ├── TransactionService.java
│       │   ├── CompteService.java
│       │   └── ...
│       └── dto/                      # Data Transfer Objects
│
├── frontend/                         # Vue 3 Frontend
│   └── src/
│       ├── views/                    # Page Components
│       │   ├── DashboardView.vue
│       │   ├── TransactionsView.vue
│       │   ├── ComptesView.vue
│       │   ├── ObjectifsView.vue
│       │   ├── UserProfileView.vue
│       │   └── ...
│       ├── components/               # Reusable Components
│       │   ├── modals/
│       │   ├── cards/
│       │   └── ...
│       ├── stores/                   # Pinia State Management
│       │   └── dashboard.ts
│       ├── services/                 # API Client
│       │   └── api.ts
│       └── types/                    # TypeScript Definitions
│           └── index.ts
│
├── docker-compose.dev.yml           # Development Docker setup
├── docker-compose.prod.yml          # Production Docker setup
└── DOCKER.md                        # Docker documentation
```

## API Documentation

### Dashboard
- `GET /api/dashboard/{month}` - Get complete dashboard data for a specific month
- `GET /api/dashboard/test` - Test backend connectivity
- `DELETE /api/dashboard/month/{month}` - Delete validated month with cascade cleanup

### User Management
- `GET /api/user/profile` - Get user profile
- `POST /api/user/profile` - Create user profile
- `PUT /api/user/profile` - Update user profile
- `GET /api/user/budget-config` - Get budget configuration
- `PUT /api/user/budget-config` - Update budget percentages

### Accounts (Comptes)
- `GET /api/comptes` - List all accounts
- `GET /api/comptes/{id}` - Get specific account
- `POST /api/comptes` - Create new account
- `PUT /api/comptes/{id}` - Update account
- `DELETE /api/comptes/{id}` - Delete account
- `GET /api/comptes/principal-charges-fixes` - Get main account for fixed charges

### Objectives (Objectifs)
- `GET /api/objectifs` - List all objectives
- `GET /api/objectifs/{id}` - Get specific objective
- `POST /api/objectifs` - Create new objective
- `PUT /api/objectifs/{id}` - Update objective
- `DELETE /api/objectifs/{id}` - Delete objective
- `POST /api/objectifs/{id}/versement` - Add payment to objective

### Transactions
- `GET /api/transactions` - List transactions with optional filters
- `POST /api/transactions` - Create new transaction
- `POST /api/transactions/salaire` - Validate monthly salary
- `POST /api/transactions/upload` - Upload and parse PDF bank statement
- `POST /api/transactions/bulk` - Create multiple transactions

### Transfers
- `POST /api/transferts/comptes` - Transfer between accounts
- `POST /api/transferts/objectifs` - Transfer between objectives

### Banks
- `GET /api/banques` - List all banks
- `POST /api/banques` - Create new bank
- `PUT /api/banques/{id}` - Update bank
- `DELETE /api/banques/{id}` - Delete bank
- `GET /api/banques/populate` - Populate default French banks

### Fixed Charges (Charges Fixes)
- `GET /api/charges-fixes` - List all fixed charges
- `GET /api/charges-fixes/{id}` - Get specific fixed charge
- `POST /api/charges-fixes` - Create new fixed charge
- `PUT /api/charges-fixes/{id}` - Update fixed charge
- `DELETE /api/charges-fixes/{id}` - Delete fixed charge

**Interactive API Documentation:** http://localhost:8080/swagger (when backend is running)

## Key Features Explained

### Fixed Charges Management
Configure recurring expenses with automatic tracking:
- **Create charges** with name, amount, category, and frequency
- **Frequency options**: Monthly, Quarterly, Annual
- **Automatic calculation**: View monthly equivalents for all frequencies
- **Account assignment**: Link charges to specific accounts
- **Day tracking**: Set the day of month for each charge
- **Categories**: Rent, insurance, subscriptions, utilities, transport, and more
- **Active/inactive toggle**: Temporarily disable charges without deletion

Fixed charges appear in the User Profile page with full CRUD operations and monthly total calculation.

### Multi-Account Objective Allocation
Objectives can be spread across multiple accounts with precise tracking:
- Create a savings goal (e.g., "Vacation Fund - €2000")
- Allocate portions to different accounts (e.g., 60% from Checking, 40% from Savings)
- Track progress with visual indicators
- See free money after objective allocations

### Free Money Calculation
```
Free Money = Account Total Balance - Sum(Allocated to Objectives)
```
This shows exactly how much money is truly available in each account.

### Budget Distribution (50/30/20 Rule)
Customize your budget allocation:
- **Fixed Charges** (e.g., 50%) - Rent, utilities, subscriptions
- **Variable Expenses** (e.g., 30%) - Food, entertainment, shopping
- **Savings** (e.g., 20%) - Emergency fund, investments, goals

Adjustable via sliders in User Profile page.

### Month Validation System
- Validate your salary at the start of each month
- Lock the month to prevent accidental changes
- Delete entire months with automatic rollback of:
  - Validated salary
  - All transactions for that month
  - Account balances (restored to pre-month state)
  - Objective allocations (restored)

### PDF Bank Statement Parsing
Upload PDF statements from major French banks:
- Automatic transaction extraction
- Date, description, and amount recognition
- Transaction categorization
- Bulk import with review before confirmation

## Development

### Backend Development

```bash
cd backend

# Start dev mode (hot reload)
./mvnw quarkus:dev

# Run tests
./mvnw test

# Run tests with coverage
./mvnw verify

# Build for production
./mvnw clean package
```

**Dev Mode Features:**
- Live reload on code changes
- Dev UI: http://localhost:8080/q/dev
- H2 Console: http://localhost:8080/h2-console
- OpenAPI/Swagger: http://localhost:8080/swagger

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev

# Type checking
npm run type-check

# Linting
npm run lint

# Build for production
npm run build

# Preview production build
npm run preview
```

### Database

**Development (Docker):**
- PostgreSQL with persistent volume
- Database: `budget`
- User: `budget_user`
- Password: `budget_pass`
- Port: `5432`

**Development (Local):**
- H2 file-based database
- Location: `./backend/data/budgetdev`
- Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/budgetdev`
- User: `sa`
- Password: (empty)

**Production:**
- PostgreSQL with Docker volume
- Automatic schema migration
- Persistent data storage

## Environment Variables

### Backend (.env or docker-compose)
```properties
# Database
QUARKUS_DATASOURCE_DB_KIND=postgresql
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/budget
QUARKUS_DATASOURCE_USERNAME=budget_user
QUARKUS_DATASOURCE_PASSWORD=budget_pass

# Hibernate
QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=update
```

### Frontend (.env)
```properties
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=10000
```

## Testing

### Backend Tests
```bash
cd backend
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests + coverage
```

### Frontend Tests
```bash
cd frontend
npm run test:unit              # Vitest unit tests
npm run test:e2e              # Playwright E2E tests
```

## CI/CD Pipeline

### Overview

Automated GitHub Actions workflows for continuous integration and deployment:

- **Pull Requests** - Automated validation before merge
- **Main Branch** - Automated Docker image builds and deployment to Docker Hub

### Pull Request Workflow

Triggered on every PR to `main` or `develop` branches.

**Frontend Checks:**
- ✅ ESLint linting
- ✅ TypeScript type checking
- ✅ Unit tests (Vitest)
- ✅ Production build validation

**Backend Checks:**
- ✅ Maven tests
- ✅ Maven verify
- ✅ Package build

All checks must pass before merge is allowed.

### Main Branch Workflow

Triggered on push to `main` branch (after PR merge).

**Steps:**
1. Run all validation checks (same as PR)
2. Build Docker images:
   - `tistech0/budget-manager-frontend`
   - `tistech0/budget-manager-backend`
3. Tag images with:
   - `latest` - Always points to latest main branch
   - `sha-<commit>` - Specific commit version (e.g., `sha-abc1234`)
4. Push images to Docker Hub

### Docker Images

Pull production images from Docker Hub:

```bash
# Frontend (Nginx + Vue production build)
docker pull tistech0/budget-manager-frontend:latest
docker pull tistech0/budget-manager-frontend:sha-abc1234

# Backend (OpenJDK 21 + Quarkus)
docker pull tistech0/budget-manager-backend:latest
docker pull tistech0/budget-manager-backend:sha-abc1234

# Run images
docker run -p 80:80 tistech0/budget-manager-frontend:latest
docker run -p 8080:8080 tistech0/budget-manager-backend:latest
```

### Setup CI/CD

**Required GitHub Secrets:**

Add these in **Settings → Secrets and variables → Actions**:

1. `DOCKERHUB_USERNAME` - Your Docker Hub username (tistech0)
2. `DOCKERHUB_TOKEN` - Docker Hub Personal Access Token
   - Create at: https://hub.docker.com/settings/security
   - Permissions: Read, Write, Delete

**Workflow Files:**
- `.github/workflows/ci-pr.yml` - Pull request validation
- `.github/workflows/ci-main.yml` - Main branch deployment

### Monitoring CI/CD

- **GitHub Actions Tab** - View workflow runs and logs
- **Pull Request Checks** - See status of all validations
- **Docker Hub** - Verify published images

## Production Deployment

### Option 1: Using Pre-built Docker Images (Recommended)

Pull and run the latest production images from Docker Hub:

```bash
# Pull latest images
docker pull tistech0/budget-manager-frontend:latest
docker pull tistech0/budget-manager-backend:latest

# Run with Docker Compose (create docker-compose.yml)
# Or run individually:
docker run -d -p 80:80 tistech0/budget-manager-frontend:latest
docker run -d -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://your-db:5432/budget \
  -e QUARKUS_DATASOURCE_USERNAME=budget_user \
  -e QUARKUS_DATASOURCE_PASSWORD=your_password \
  tistech0/budget-manager-backend:latest
```

### Option 2: Build from Source with Docker Compose

```bash
# Build and start production services
docker-compose -f docker-compose.prod.yml up --build -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Stop services
docker-compose -f docker-compose.prod.yml down
```

**Production Setup:**
- Frontend served via Nginx
- Backend on port 8080
- PostgreSQL with data volume
- Optimized builds
- Environment-based configuration

## Design System

### Colors
- **Primary Gradient:** `#667EEA → #764BA2` (Purple gradient)
- **Success:** `#4CAF50`, `#10B981` (Green)
- **Warning:** `#FF9800`, `#F59E0B` (Orange)
- **Danger:** `#F44336`, `#EF4444` (Red)
- **Background:** Dark gradient with glass-morphism effects

### Typography
- **Font:** System font stack for optimal performance
- **Headings:** 700 weight, gradient text
- **Body:** 400-600 weight, high contrast

### Components
- **Cards:** Glass-morphism with blur effect
- **Buttons:** Gradient backgrounds with shadows
- **Inputs:** Transparent with focus states
- **Modals:** Backdrop blur with slide-up animations

## Browser Support

- Chrome 90+ ✅
- Firefox 88+ ✅
- Safari 14+ ✅
- Edge 90+ ✅

## Contributing

This is a personal project, but contributions are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is private and for personal use.

## Documentation

- **[Frontend README](frontend/README.md)** - Vue 3 frontend documentation
- **[Backend README](backend/README.md)** - Quarkus backend documentation
- **[Docker Guide](docs/DOCKER.md)** - Complete Docker setup and usage
- **[Performance Guidelines](docs/PERFORMANCE_GUIDELINES.md)** - Frontend optimization best practices

## Support

For issues or questions:
- Check existing documentation in `docs/` folder
- Review API documentation at http://localhost:8080/swagger
- Check backend logs: `docker-compose -f docker-compose.dev.yml logs backend`
- Check frontend logs: `docker-compose -f docker-compose.dev.yml logs frontend`

---

**Built with ❤️ using Quarkus & Vue 3**

**Version:** 2.0.0
**Last Updated:** 2025-10-25
