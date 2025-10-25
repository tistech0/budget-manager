# Budget Manager - Homelab Deployment Guide

This guide will help you deploy the Budget Manager application on your homelab using Docker Compose.

## Prerequisites

- Docker and Docker Compose installed on your server
- At least 2GB of RAM available
- Ports 3000 and 8080 available (or customize in .env)

## Quick Start

### 1. Download the deployment files

You only need these files from the repository:
- `docker-compose.homelab.yml`
- `.env.homelab.example`

### 2. Configure your environment

```bash
# Copy the example environment file
cp .env.homelab.example .env

# Edit the .env file with your preferred settings
nano .env
```

**Important:** Change the `POSTGRES_PASSWORD` to a secure password!

### 3. Start the application

```bash
# Pull the latest images and start all services
docker compose -f docker-compose.homelab.yml up -d

# Or if using older Docker Compose:
docker-compose -f docker-compose.homelab.yml up -d
```

### 4. Verify the deployment

```bash
# Check all services are running
docker compose -f docker-compose.homelab.yml ps

# Check logs if needed
docker compose -f docker-compose.homelab.yml logs -f
```

### 5. Access the application

- **Frontend:** http://your-server-ip:3000
- **Backend API:** http://your-server-ip:8080
- **API Documentation:** http://your-server-ip:8080/q/swagger-ui

## Default Configuration

| Service  | Port | Container Name    | Purpose                    |
|----------|------|-------------------|----------------------------|
| Frontend | 3000 | budget-frontend   | Vue.js SPA (Nginx)         |
| Backend  | 8080 | budget-backend    | Quarkus API                |
| Database | 5432 | budget-db         | PostgreSQL 15              |

## Environment Variables

| Variable           | Default       | Description                          |
|--------------------|---------------|--------------------------------------|
| POSTGRES_DB        | budget        | Database name                        |
| POSTGRES_USER      | budget_user   | Database username                    |
| POSTGRES_PASSWORD  | budget_pass   | Database password (CHANGE THIS!)     |
| POSTGRES_PORT      | 5432          | PostgreSQL port                      |
| FRONTEND_PORT      | 3000          | Frontend application port            |
| BACKEND_PORT       | 8080          | Backend API port                     |

## Data Persistence

Database data is stored in a Docker volume named `postgres_data`. This ensures your data persists across container restarts.

### Backup the database

```bash
# Create a backup
docker exec budget-db pg_dump -U budget_user budget > backup_$(date +%Y%m%d_%H%M%S).sql

# Or use Docker volume backup
docker run --rm -v budget-manager-v2_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup-$(date +%Y%m%d).tar.gz -C /data .
```

### Restore the database

```bash
# Restore from SQL dump
cat your-backup.sql | docker exec -i budget-db psql -U budget_user budget

# Or restore from volume backup
docker run --rm -v budget-manager-v2_postgres_data:/data -v $(pwd):/backup alpine tar xzf /backup/your-backup.tar.gz -C /data
```

## Updating the Application

```bash
# Pull the latest images
docker compose -f docker-compose.homelab.yml pull

# Restart with new images
docker compose -f docker-compose.homelab.yml up -d

# Remove old images (optional)
docker image prune -f
```

## Using with Traefik (Reverse Proxy)

If you're using Traefik as a reverse proxy, uncomment the Traefik labels in `docker-compose.homelab.yml` and customize:

```yaml
labels:
  - "traefik.enable=true"
  - "traefik.http.routers.budget-frontend.rule=Host(`budget.yourdomain.com`)"
  - "traefik.http.routers.budget-frontend.entrypoints=websecure"
  - "traefik.http.routers.budget-frontend.tls.certresolver=letsencrypt"
  - "traefik.http.services.budget-frontend.loadbalancer.server.port=80"
```

Then remove the `ports` mapping from the frontend service and let Traefik handle the routing.

## Troubleshooting

### Check service health

```bash
# Check if all services are healthy
docker compose -f docker-compose.homelab.yml ps

# View logs
docker compose -f docker-compose.homelab.yml logs -f backend
docker compose -f docker-compose.homelab.yml logs -f frontend
docker compose -f docker-compose.homelab.yml logs -f database
```

### Backend can't connect to database

1. Check database is running: `docker logs budget-db`
2. Verify environment variables are correct in `docker-compose.homelab.yml`
3. Wait for database to fully start (check with `docker logs budget-db`)

### Frontend shows connection error

1. Verify backend is running: `curl http://localhost:8080/q/health`
2. Check nginx configuration is correct
3. Verify the frontend container can reach backend: `docker exec budget-frontend ping backend`

### Reset everything

```bash
# Stop and remove all containers and volumes
docker compose -f docker-compose.homelab.yml down -v

# Start fresh
docker compose -f docker-compose.homelab.yml up -d
```

## Monitoring

### Health checks

All services have health checks configured:

```bash
# Check health status
docker inspect budget-backend | grep -A 10 Health
docker inspect budget-frontend | grep -A 10 Health
docker inspect budget-db | grep -A 10 Health
```

### Resource usage

```bash
# Monitor resource usage
docker stats budget-frontend budget-backend budget-db
```

## Security Recommendations

1. **Change default passwords** in the `.env` file
2. **Use HTTPS** in production (via reverse proxy like Traefik/Nginx Proxy Manager)
3. **Restrict database port** - only expose if needed externally
4. **Keep images updated** - regularly pull and update
5. **Regular backups** - automate database backups
6. **Firewall rules** - only expose necessary ports to your network

## Support

For issues or questions:
- Check the logs: `docker compose -f docker-compose.homelab.yml logs`
- GitHub Issues: [Your Repository URL]
- Documentation: [Your Docs URL]

## License

[Your License]
