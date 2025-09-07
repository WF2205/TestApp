#!/bin/bash

# Docker management scripts for TodoList App

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker Desktop."
        exit 1
    fi
}

# Setup environment file
setup_env() {
    if [ ! -f .env ]; then
        print_status "Creating .env file from template..."
        cp docker.env.example .env
        print_warning "Please edit .env file and add your GitHub OAuth credentials!"
        print_status "Required: GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET"
    else
        print_success ".env file already exists"
    fi
}

# Build and start services
start() {
    print_status "Starting TodoList App with Docker Compose..."
    check_docker
    setup_env
    
    print_status "Building and starting services..."
    docker-compose up --build -d
    
    print_success "Services started successfully!"
    print_status "Waiting for services to be ready..."
    
    # Wait for services to be healthy
    sleep 10
    
    print_status "Service URLs:"
    echo "  📱 API Server: http://localhost:8080"
    echo "  🗄️  MongoDB: localhost:27017"
    echo "  🐰 RabbitMQ Management: http://localhost:15672 (guest/guest)"
    echo "  ❤️  Health Check: http://localhost:8080/actuator/health"
}

# Stop services
stop() {
    print_status "Stopping TodoList App services..."
    docker-compose down
    print_success "Services stopped successfully!"
}

# Restart services
restart() {
    print_status "Restarting TodoList App services..."
    docker-compose restart
    print_success "Services restarted successfully!"
}

# View logs
logs() {
    local service=${1:-""}
    if [ -n "$service" ]; then
        print_status "Showing logs for $service..."
        docker-compose logs -f "$service"
    else
        print_status "Showing logs for all services..."
        docker-compose logs -f
    fi
}

# Check status
status() {
    print_status "Service status:"
    docker-compose ps
    
    echo ""
    print_status "Health check:"
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        print_success "API Server is healthy"
    else
        print_error "API Server is not responding"
    fi
}

# Clean up everything
clean() {
    print_warning "This will remove all containers, volumes, and data!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up..."
        docker-compose down -v --rmi all
        print_success "Cleanup completed!"
    else
        print_status "Cleanup cancelled"
    fi
}

# Show help
help() {
    echo "TodoList App Docker Management Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     Build and start all services"
    echo "  stop      Stop all services"
    echo "  restart   Restart all services"
    echo "  logs      Show logs (optionally specify service: app, mongodb, rabbitmq)"
    echo "  status    Show service status and health check"
    echo "  clean     Remove all containers, volumes, and images (DESTRUCTIVE)"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 start"
    echo "  $0 logs app"
    echo "  $0 status"
}

# Main script logic
case "${1:-help}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    logs)
        logs "$2"
        ;;
    status)
        status
        ;;
    clean)
        clean
        ;;
    help|--help|-h)
        help
        ;;
    *)
        print_error "Unknown command: $1"
        help
        exit 1
        ;;
esac
