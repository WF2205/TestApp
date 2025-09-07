#!/bin/bash

echo "🚀 TodoList App Setup Script"
echo "=========================="

echo ""
echo "📋 Prerequisites Check:"
echo "✅ MongoDB: $(brew services list | grep mongodb-community | awk '{print $2}')"
echo "✅ RabbitMQ: $(brew services list | grep rabbitmq | awk '{print $2}')"

echo ""
echo "🔧 GitHub OAuth Setup Required:"
echo "1. Go to: https://github.com/settings/developers"
echo "2. Click 'New OAuth App'"
echo "3. Fill in:"
echo "   - Application name: TodoList App"
echo "   - Homepage URL: http://localhost:8080"
echo "   - Authorization callback URL: http://localhost:8080/api/login/oauth2/code/github"
echo "4. Copy the Client ID and Client Secret"

echo ""
echo "🔑 Set Environment Variables:"
echo "export GITHUB_CLIENT_ID=your-client-id-here"
echo "export GITHUB_CLIENT_SECRET=your-client-secret-here"

echo ""
echo "🚀 To run the application:"
echo "./mvnw spring-boot:run"

echo ""
echo "📊 Management UIs:"
echo "MongoDB: mongosh (command line)"
echo "RabbitMQ: http://localhost:15672 (guest/guest)"

echo ""
echo "🧪 Test Endpoints:"
echo "Health Check: http://localhost:8080/api/actuator/health"
echo "Login: http://localhost:8080/api/auth/login"