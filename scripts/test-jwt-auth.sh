#!/bin/bash

# Script para testar autenticação JWT com Cognito
# Uso: ./test-jwt-auth.sh <USER_POOL_ID> <CLIENT_ID> <API_GATEWAY_URL>

set -e

USER_POOL_ID=${1:-""}
CLIENT_ID=${2:-""}
API_GATEWAY_URL=${3:-""}

if [ -z "$USER_POOL_ID" ] || [ -z "$CLIENT_ID" ] || [ -z "$API_GATEWAY_URL" ]; then
    echo "❌ Uso: $0 <USER_POOL_ID> <CLIENT_ID> <API_GATEWAY_URL>"
    echo "💡 Obtenha os valores com: terraform output"
    exit 1
fi

echo "🔐 Testando autenticação JWT..."
echo "📋 User Pool ID: $USER_POOL_ID"
echo "📋 Client ID: $CLIENT_ID"
echo "📋 API Gateway URL: $API_GATEWAY_URL"
echo ""

# Obter token JWT
echo "🔑 Obtendo token JWT..."
AUTH_RESPONSE=$(aws cognito-idp admin-initiate-auth \
  --user-pool-id "$USER_POOL_ID" \
  --client-id "$CLIENT_ID" \
  --auth-flow ADMIN_NO_SRP_AUTH \
  --auth-parameters USERNAME=admin@financas.com,PASSWORD=AdminPass123!)

JWT_TOKEN=$(echo "$AUTH_RESPONSE" | jq -r '.AuthenticationResult.IdToken')

if [ "$JWT_TOKEN" = "null" ] || [ -z "$JWT_TOKEN" ]; then
    echo "❌ Erro ao obter token JWT"
    echo "💡 Verifique se os usuários de teste foram criados"
    exit 1
fi

echo "✅ Token JWT obtido com sucesso!"
echo ""

# Testar endpoint protegido
echo "🧪 Testando endpoint protegido..."
echo "📡 URL: $API_GATEWAY_URL/api/v1/leitor/transacoes"
echo ""

RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  "$API_GATEWAY_URL/api/v1/leitor/transacoes")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n -1)

echo "📊 Status HTTP: $HTTP_CODE"
echo "📄 Resposta:"
echo "$BODY" | jq . 2>/dev/null || echo "$BODY"

if [ "$HTTP_CODE" = "200" ]; then
    echo ""
    echo "✅ Teste de autenticação bem-sucedido!"
    echo "🎉 A API está funcionando com JWT!"
else
    echo ""
    echo "❌ Teste falhou com status: $HTTP_CODE"
    echo "💡 Verifique se:"
    echo "   - O backend está rodando"
    echo "   - As variáveis de ambiente estão configuradas"
    echo "   - O API Gateway está configurado corretamente"
fi
