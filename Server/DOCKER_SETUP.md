# 🐳 Docker Setup for TodoList App

This comprehensive guide will help you run the TodoList application using Docker with MongoDB and RabbitMQ. The setup includes everything you need to get your full-stack application running in containers.

## 📋 Prerequisites

Before you begin, ensure you have:

- **Docker Desktop** installed and running ([Download here](https://www.docker.com/products/docker-desktop/))
- **Docker Compose** (included with Docker Desktop)
- **GitHub OAuth App** credentials (Client ID and Client Secret)
- **Terminal/Command Prompt** access
- **At least 4GB RAM** available for Docker

### 🔍 Verify Docker Installation

```bash
# Check Docker version
docker --version

# Check Docker Compose version
docker-compose --version

# Verify Docker is running
docker info
```

## 🚀 Quick Start (5 Minutes)

### Step 1: Set up Environment Variables

```bash
# Copy the environment template
cp docker.env.example .env

# Edit the file with your GitHub OAuth credentials
nano .env
# or use your preferred editor: vim, code, etc.
```

**Required Environment Variables:**
```bash
GITHUB_CLIENT_ID=your_actual_github_client_id
GITHUB_CLIENT_SECRET=your_actual_github_client_secret
```

> ⚠️ **Important**: Replace the placeholder values with your actual GitHub OAuth App credentials!

### Step 2: Build and Start Services

```bash
# Option 1: Start with logs visible (recommended for first run)
docker-compose up --build

# Option 2: Start in background (detached mode)
docker-compose up --build -d
```

### Step 3: Verify Everything is Working

```bash
# Check service status
docker-compose ps

# Test API health endpoint
curl http://localhost:8080/actuator/health

# Check logs if needed
docker-compose logs app
```

### Step 4: Access Your Services

- **🌐 API Server**: http://localhost:8080
- **🗄️ MongoDB**: localhost:27017
- **🐰 RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **❤️ Health Check**: http://localhost:8080/actuator/health

## 🏗️ Architecture Overview

The Docker setup includes three main services working together:

### 🗄️ MongoDB Database
- **Port**: 27017
- **Database**: todolist
- **Root User**: admin/password123
- **Features**: 
  - Automatic collection creation
  - Index optimization
  - Data validation
  - Persistent storage

### 🐰 RabbitMQ Message Queue
- **AMQP Port**: 5672
- **Management UI**: 15672
- **Credentials**: guest/guest
- **Features**:
  - Push notifications
  - Dead letter queues
  - Message TTL
  - Web management interface

### ☕ Spring Boot Application
- **Port**: 8080
- **Features**:
  - RESTful API
  - OAuth2 authentication
  - Health monitoring
  - Auto-reconnection to services

## 🛠️ Docker Commands Reference

### 📦 Basic Operations

```bash
# Start all services (with logs visible)
docker-compose up

# Start in background (detached mode)
docker-compose up -d

# Build and start (recommended for first run)
docker-compose up --build

# Stop all services
docker-compose down

# Stop and remove volumes (⚠️ WARNING: deletes all data)
docker-compose down -v

# Restart all services
docker-compose restart
```

### 📊 Monitoring & Logs

```bash
# View logs for all services
docker-compose logs

# View logs for specific service
docker-compose logs app
docker-compose logs mongodb
docker-compose logs rabbitmq

# Follow logs in real-time
docker-compose logs -f app

# Check service status
docker-compose ps

# View resource usage
docker stats

# Check health status
curl http://localhost:8080/actuator/health
```

### 🔧 Development Commands

```bash
# Rebuild only the app service
docker-compose up --build app

# Rebuild without cache (if having issues)
docker-compose build --no-cache

# Execute commands in running containers
docker-compose exec app bash
docker-compose exec mongodb mongosh
docker-compose exec rabbitmq rabbitmqctl status

# Access MongoDB shell
docker-compose exec mongodb mongosh todolist

# Check RabbitMQ queues
docker-compose exec rabbitmq rabbitmqctl list_queues
```

### 🚀 Using the Management Script

For convenience, use the provided management script:

```bash
# Make script executable
chmod +x docker-scripts.sh

# Start all services
./docker-scripts.sh start

# Check status
./docker-scripts.sh status

# View logs
./docker-scripts.sh logs

# View logs for specific service
./docker-scripts.sh logs app

# Stop services
./docker-scripts.sh stop

# Clean everything (⚠️ DESTRUCTIVE)
./docker-scripts.sh clean
```

## 💾 Data Persistence

Your data is safely stored in Docker volumes:

- **MongoDB data**: `mongodb_data` volume (persistent across restarts)
- **RabbitMQ data**: `rabbitmq_data` volume (persistent across restarts)
- **Data location**: Managed by Docker Desktop (usually in `~/Library/Containers/com.docker.docker/Data/vms/0/data/` on macOS)

### 📁 Volume Management

```bash
# List all volumes
docker volume ls

# Inspect volume details
docker volume inspect todolistapp_mongodb_data

# Backup MongoDB data
docker run --rm -v todolistapp_mongodb_data:/data -v $(pwd):/backup mongo:7.0 tar czf /backup/mongodb-backup.tar.gz /data

# Restore MongoDB data
docker run --rm -v todolistapp_mongodb_data:/data -v $(pwd):/backup mongo:7.0 tar xzf /backup/mongodb-backup.tar.gz -C /
```

## 🔧 Troubleshooting Guide

### 🚨 Common Issues & Solutions

#### 1. **Port Conflicts**
**Problem**: Services fail to start due to port conflicts
```bash
# Check what's using the ports
lsof -i :8080
lsof -i :27017
lsof -i :5672
lsof -i :15672

# Kill processes using ports (if needed)
sudo kill -9 <PID>
```

#### 2. **GitHub OAuth Issues**
**Problem**: OAuth login fails
- ✅ Ensure GitHub OAuth app callback URL is: `http://localhost:8080/login/oauth2/code/github`
- ✅ Check your `.env` file has correct `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET`
- ✅ Verify GitHub OAuth app is not expired

#### 3. **Memory Issues**
**Problem**: Docker runs out of memory
- ✅ Open Docker Desktop → Settings → Resources
- ✅ Increase Memory allocation to 4GB+ (recommended: 6GB)
- ✅ Restart Docker Desktop after changes

#### 4. **Service Connection Issues**
**Problem**: App can't connect to MongoDB/RabbitMQ
```bash
# Check if services are running
docker-compose ps

# Check service logs
docker-compose logs mongodb
docker-compose logs rabbitmq

# Test connectivity
docker-compose exec app ping mongodb
docker-compose exec app ping rabbitmq
```

#### 5. **Build Failures**
**Problem**: Docker build fails
```bash
# Clean build without cache
docker-compose build --no-cache

# Check Dockerfile syntax
docker build -t test-build .

# Verify Java/Maven setup
docker-compose exec app java -version
docker-compose exec app ./mvnw --version
```

### 🔄 Reset Everything

```bash
# Stop and remove everything (including data)
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Remove all volumes (⚠️ DESTRUCTIVE)
docker volume prune

# Start fresh
docker-compose up --build
```

### 📋 Diagnostic Commands

```bash
# Check Docker system info
docker system info

# Check disk usage
docker system df

# View all containers (including stopped)
docker ps -a

# View all images
docker images

# Check network configuration
docker network ls
docker network inspect todolistapp_todolist-network
```

## 🚀 Production Deployment

For production deployment, consider these enhancements:

### 🔐 Security
- **Secrets Management**: Use Docker secrets or external secret management
- **SSL/TLS**: Configure HTTPS with reverse proxy (nginx)
- **Network Security**: Use custom networks and firewall rules
- **Authentication**: Implement proper JWT token management

### 📊 Monitoring & Logging
- **Health Checks**: Enhanced health monitoring
- **Logging**: Centralized logging with ELK stack
- **Metrics**: Prometheus + Grafana for monitoring
- **Alerting**: Set up alerts for critical issues

### 💾 Backup & Recovery
- **Database Backups**: Automated MongoDB backups
- **Volume Snapshots**: Regular volume snapshots
- **Disaster Recovery**: Tested recovery procedures

### ⚡ Performance
- **Resource Limits**: Set memory and CPU limits
- **Scaling**: Horizontal scaling with Docker Swarm/Kubernetes
- **Caching**: Redis for session caching
- **CDN**: Content delivery network for static assets

## 📁 Project Structure

```
TodoListApp/
├── 🐳 Dockerfile                 # App container definition
├── 🐳 docker-compose.yml         # Multi-service orchestration
├── 🐳 .dockerignore             # Files to exclude from build
├── 🐳 docker.env.example        # Environment variables template
├── 🐳 docker-scripts.sh         # Management scripts
├── 📁 docker/
│   └── 📄 mongo-init.js         # MongoDB initialization script
├── 📄 DOCKER_SETUP.md           # This comprehensive guide
└── 📁 src/                      # Spring Boot source code
```

## 🎯 Next Steps

### For Development:
1. ✅ Set up your GitHub OAuth app
2. ✅ Configure environment variables in `.env`
3. ✅ Run `docker-compose up --build`
4. ✅ Test the API endpoints
5. ✅ Connect your iOS app to `http://localhost:8080`

### For Production:
1. 🔧 Set up proper secrets management
2. 🔧 Configure SSL/TLS certificates
3. 🔧 Set up monitoring and logging
4. 🔧 Implement backup strategies
5. 🔧 Configure CI/CD pipeline

## 🆘 Getting Help

If you encounter issues:

1. **Check the logs**: `docker-compose logs -f`
2. **Verify services**: `docker-compose ps`
3. **Test connectivity**: `curl http://localhost:8080/actuator/health`
4. **Review this guide**: Check the troubleshooting section above
5. **Reset if needed**: `docker-compose down -v && docker-compose up --build`

## 🎉 Success!

Once everything is running, you should see:
- ✅ API Server responding at http://localhost:8080
- ✅ MongoDB accepting connections on port 27017
- ✅ RabbitMQ management UI at http://localhost:15672
- ✅ Health check passing: `{"status":"UP"}`

**Your TodoList app is now fully containerized and ready for development!** 🚀