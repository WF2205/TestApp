#!/bin/bash

echo "🚀 TodoList App - Quick Test"
echo "============================"

echo ""
echo "📊 Testing Services..."

# Test Spring Boot App
echo "✅ Spring Boot App:"
curl -s http://localhost:8080/api/actuator/health | jq '.' 2>/dev/null || curl -s http://localhost:8080/api/actuator/health

# Test MongoDB
echo ""
echo "✅ MongoDB:"
mongosh --eval "db.runCommand('ping')" --quiet 2>/dev/null && echo "Connected" || echo "Not accessible"

# Test RabbitMQ
echo ""
echo "✅ RabbitMQ:"
curl -s http://localhost:15672/api/overview -u guest:guest >/dev/null 2>&1 && echo "Management UI accessible" || echo "Not accessible"

echo ""
echo "🔐 Testing Auth Flow..."
echo "Visit: http://localhost:8080/api/auth/login"
echo "Expected: Redirect to GitHub OAuth"

echo ""
echo "📋 Testing Protected Endpoints..."
echo "GET /api/users/me - Status: $(curl -s http://localhost:8080/api/users/me -w "%{http_code}" -o /dev/null)"
echo "GET /api/todos - Status: $(curl -s http://localhost:8080/api/todos -w "%{http_code}" -o /dev/null)"
echo "GET /api/notifications - Status: $(curl -s http://localhost:8080/api/notifications -w "%{http_code}" -o /dev/null)"

echo ""
echo "🎯 Management UIs:"
echo "RabbitMQ: http://localhost:15672 (guest/guest)"
echo "MongoDB: mongosh"

echo ""
echo "📖 Next Steps:"
echo "1. Set up GitHub OAuth credentials"
echo "2. Test OAuth flow in browser"
echo "3. Create todos and test notifications"
echo "4. Check RabbitMQ queues for message flow"

echo ""
echo "✨ Application is ready for testing!"