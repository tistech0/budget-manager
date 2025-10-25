# Docker Setup - Budget Manager v2.0

This document explains how to run the Budget Manager application using Docker Compose.

## Overview

We provide two Docker Compose configurations:

1. **`docker-compose.dev.yml`** - Development environment with hot reload and persistent PostgreSQL
2. **`docker-compose.prod.yml`** - Production environment with optimized builds

## Development Setup (Recommended)

### Features

- ✅ **PostgreSQL database** - Persistent data (no more resets!)
- ✅ **Hot reload** for both backend and frontend
- ✅ **Volume mounts** - Edit code locally, see changes immediately
- ✅ **Debug port** exposed (5005) for backend debugging
- ✅ **Separate network** for service communication

### Quick Start

```bash
# Start all services (database + backend + frontend)
docker-compose -f docker-compose.dev.yml up

# Or run in background
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Stop services
docker-compose -f docker-compose.dev.yml down

# Stop and remove volumes (fresh start)
docker-compose -f docker-compose.dev.yml down -v
```

### Access Points

- **Frontend**: http://localhost:8000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger
- **PostgreSQL**: localhost:5432
  - Database: `budget`
  - User: `budget_user`
  - Password: `budget_pass`

### How Hot Reload Works

#### Backend (Quarkus)
- Source files are mounted from `./backend/src`
- Quarkus dev mode automatically detects changes
- No rebuild needed - just save your Java files!

#### Frontend (Vue + Vite)
- Source files are mounted from `./frontend/src`
- Vite HMR (Hot Module Replacement) detects changes
- Browser updates automatically - no refresh needed!

### Database Persistence

Your PostgreSQL data is stored in a Docker volume named `postgres_dev_data`:

```bash
# View volumes
docker volume ls

# Inspect volume
docker volume inspect budget-manager-v2_postgres_dev_data

# Remove volume (DANGER: deletes all data!)
docker volume rm budget-manager-v2_postgres_dev_data
```

**Important**: Unless you explicitly run `docker-compose down -v`, your data persists between restarts!

### Debugging Backend

The backend debug port (5005) is exposed. To connect from your IDE:

**IntelliJ IDEA / VS Code:**
1. Create a "Remote JVM Debug" configuration
2. Host: `localhost`
3. Port: `5005`
4. Set breakpoints and attach

### Rebuilding After Dependency Changes

If you add new dependencies to `pom.xml` or `package.json`:

```bash
# Rebuild specific service
docker-compose -f docker-compose.dev.yml up --build backend
docker-compose -f docker-compose.dev.yml up --build frontend

# Rebuild all services
docker-compose -f docker-compose.dev.yml up --build
```

### Common Development Tasks

```bash
# Restart a single service
docker-compose -f docker-compose.dev.yml restart backend

# View backend logs
docker-compose -f docker-compose.dev.yml logs -f backend

# View frontend logs
docker-compose -f docker-compose.dev.yml logs -f frontend

# Execute commands in running container
docker-compose -f docker-compose.dev.yml exec backend bash
docker-compose -f docker-compose.dev.yml exec database psql -U budget_user -d budget

# Check service status
docker-compose -f docker-compose.dev.yml ps
```

### Database Migrations

To reset the database schema while keeping the volume:

```bash
# Option 1: Drop and recreate database
docker-compose -f docker-compose.dev.yml exec database psql -U budget_user -d postgres -c "DROP DATABASE budget; CREATE DATABASE budget;"
docker-compose -f docker-compose.dev.yml restart backend

# Option 2: Fresh start (deletes all data!)
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up
```

### Troubleshooting

#### Port conflicts

If ports 8080, 8000, or 5432 are already in use:

```bash
# Check what's using the port
lsof -ti:8080
lsof -ti:8000
lsof -ti:5432

# Kill the process
kill $(lsof -ti:8080)
```

#### Backend won't start

```bash
# Check logs
docker-compose -f docker-compose.dev.yml logs backend

# Common issues:
# - Database not ready: Wait for database health check
# - Compilation errors: Check Java files for syntax errors
# - Maven cache issues: Rebuild with --no-cache
docker-compose -f docker-compose.dev.yml build --no-cache backend
```

#### Frontend won't start

```bash
# Check logs
docker-compose -f docker-compose.dev.yml logs frontend

# Common issues:
# - Node modules missing: Rebuild container
docker-compose -f docker-compose.dev.yml build --no-cache frontend
```

#### Database connection refused

```bash
# Check database is running
docker-compose -f docker-compose.dev.yml ps database

# Check health
docker-compose -f docker-compose.dev.yml exec database pg_isready -U budget_user

# Restart database
docker-compose -f docker-compose.dev.yml restart database
```

---

## Production Setup

### Features

- ✅ **Optimized builds** - Multi-stage Docker builds
- ✅ **Nginx** for serving frontend static files
- ✅ **Production profile** for backend
- ✅ **Persistent PostgreSQL**

### Quick Start

```bash
# Build and start all services
docker-compose -f docker-compose.prod.yml up --build

# Run in background
docker-compose -f docker-compose.prod.yml up -d

# Stop services
docker-compose -f docker-compose.prod.yml down
```

### Access Points

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080 (or through Nginx proxy at /api)
- **PostgreSQL**: localhost:5432

### Building for Production

```bash
# Build without starting
docker-compose -f docker-compose.prod.yml build

# Build with no cache (clean build)
docker-compose -f docker-compose.prod.yml build --no-cache

# Build specific service
docker-compose -f docker-compose.prod.yml build backend
```

### Deployment

For actual production deployment, you may want to:

1. **Use environment variables** for sensitive data:
   ```bash
   export POSTGRES_PASSWORD=your-secure-password
   docker-compose -f docker-compose.prod.yml up -d
   ```

2. **Use Docker secrets** (Docker Swarm) or **Kubernetes secrets**

3. **Add SSL/TLS** with a reverse proxy (Traefik, Nginx, etc.)

4. **Configure backups** for PostgreSQL:
   ```bash
   docker-compose -f docker-compose.prod.yml exec database \
     pg_dump -U budget_user budget > backup.sql
   ```

---

## Switching Between Dev and Prod

You can run both environments simultaneously by changing ports:

```bash
# Edit docker-compose.prod.yml to use different ports
# For example: 5433 for database, 8081 for backend, 3001 for frontend

# Then run both:
docker-compose -f docker-compose.dev.yml up -d
docker-compose -f docker-compose.prod.yml up -d
```

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Development                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐      ┌──────────────┐      ┌───────────────┐  │
│  │  Frontend   │─────▶│   Backend    │─────▶│  PostgreSQL   │  │
│  │   (Vite)    │      │  (Quarkus)   │      │               │  │
│  │  :8000      │      │  :8080       │      │  :5432        │  │
│  │             │      │  Debug: 5005 │      │               │  │
│  │  Hot Reload │      │  Hot Reload  │      │  Volume: data │  │
│  └─────────────┘      └──────────────┘      └───────────────┘  │
│        ▲                     ▲                                   │
│        │                     │                                   │
│        └─────────────────────┘                                   │
│          Volume Mounts                                           │
│       (./src directories)                                        │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         Production                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐      ┌──────────────┐      ┌───────────────┐  │
│  │  Frontend   │─────▶│   Backend    │─────▶│  PostgreSQL   │  │
│  │  (Nginx)    │      │  (Quarkus)   │      │               │  │
│  │  :3000      │      │  :8080       │      │  :5432        │  │
│  │             │      │              │      │               │  │
│  │  Static     │      │  Prod Build  │      │  Volume: data │  │
│  └─────────────┘      └──────────────┘      └───────────────┘  │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Best Practices

### Development Workflow

1. **Always use `docker-compose.dev.yml` for development**
   - Data persists between restarts
   - Hot reload speeds up development
   - No need to rebuild after code changes

2. **Commit often**
   - Your code is mounted, not copied
   - Docker won't save your changes

3. **Use `.dockerignore`**
   - Already configured to exclude `node_modules`, `target`, etc.
   - Speeds up builds

4. **Monitor resource usage**
   ```bash
   docker stats
   ```

### Production Checklist

- [ ] Use environment variables for secrets
- [ ] Configure SSL/TLS
- [ ] Set up database backups
- [ ] Configure logging (e.g., ELK stack)
- [ ] Set up monitoring (e.g., Prometheus)
- [ ] Use specific image tags (not `latest`)
- [ ] Configure resource limits
- [ ] Set up health checks
- [ ] Use read-only file systems where possible
- [ ] Scan images for vulnerabilities

---

## File Structure

```
.
├── docker-compose.dev.yml          # Development configuration
├── docker-compose.prod.yml         # Production configuration
├── backend/
│   ├── Dockerfile                  # Production backend build
│   ├── Dockerfile.dev              # Development backend build
│   ├── .dockerignore              # Files to exclude from build
│   └── src/main/resources/
│       ├── application.properties           # Base config (H2 for local)
│       └── application-dev.properties       # Dev profile (PostgreSQL)
└── frontend/
    ├── Dockerfile                  # Production frontend build (Nginx)
    ├── Dockerfile.dev              # Development frontend build (Vite)
    ├── nginx.conf                  # Nginx configuration
    └── .dockerignore              # Files to exclude from build
```

---

## FAQ

### Why PostgreSQL instead of H2?

- **Persistent data**: H2 file database resets on schema changes
- **Production parity**: Same database in dev and prod
- **Better performance**: PostgreSQL is optimized for concurrent access
- **Features**: Full PostgreSQL feature set

### Can I still use H2 for local development?

Yes! Just run without Docker:

```bash
cd backend && ./mvnw quarkus:dev
cd frontend && npm run dev
```

The default `application.properties` uses H2.

### How do I backup my PostgreSQL data?

```bash
# Create backup
docker-compose -f docker-compose.dev.yml exec database \
  pg_dump -U budget_user budget > backup_$(date +%Y%m%d).sql

# Restore backup
cat backup_20231015.sql | docker-compose -f docker-compose.dev.yml exec -T database \
  psql -U budget_user budget
```

### How do I connect to the database from my IDE?

**DataGrip / DBeaver / pgAdmin:**
- Host: `localhost`
- Port: `5432`
- Database: `budget`
- User: `budget_user`
- Password: `budget_pass`

### What if I need to add environment variables?

Create a `.env` file:

```bash
# .env
POSTGRES_PASSWORD=my-secure-password
BACKEND_LOG_LEVEL=DEBUG
```

Then reference in `docker-compose.dev.yml`:

```yaml
environment:
  - LOG_LEVEL=${BACKEND_LOG_LEVEL}
```

---

## Support

For issues or questions:
1. Check logs: `docker-compose -f docker-compose.dev.yml logs`
2. Check service status: `docker-compose -f docker-compose.dev.yml ps`
3. Review this documentation
4. Check application logs in containers

Happy coding! 🚀
