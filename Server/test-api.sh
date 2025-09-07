#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "🧪 TodoList API Test Script"
echo "=========================="

echo ""
echo "📊 Testing Health Check..."
curl -s "$BASE_URL/actuator/health" | jq '.' 2>/dev/null || curl -s "$BASE_URL/actuator/health"

echo ""
echo "🔐 Testing Auth Endpoints..."
echo "Login endpoint:"
curl -s "$BASE_URL/auth/login" | jq '.' 2>/dev/null || curl -s "$BASE_URL/auth/login"

echo ""
echo "📝 Testing Public Endpoints (without auth)..."
echo "Note: Most endpoints require authentication"

echo ""
echo "🔧 To test authenticated endpoints:"
echo "1. Set up GitHub OAuth credentials"
echo "2. Start the application: ./mvnw spring-boot:run"
echo "3. Visit: http://localhost:8080/api/auth/login"
echo "4. Complete GitHub OAuth flow"
echo "5. Use the returned session/token for API calls"

echo ""
echo "📋 Example authenticated API calls (after login):"
echo "GET  $BASE_URL/users/me"
echo "GET  $BASE_URL/todos"
echo "POST $BASE_URL/todos"
echo "GET  $BASE_URL/notifications"

echo ""
echo "🐰 RabbitMQ Management: http://localhost:15672 (guest/guest)"
echo "🍃 MongoDB: Use mongosh to connect"