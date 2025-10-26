#!/bin/bash

# Script para criar usuários de teste no AWS Cognito
# Uso: ./create-test-users.sh <USER_POOL_ID> <CLIENT_ID>

set -e

USER_POOL_ID=${1:-""}
CLIENT_ID=${2:-""}

if [ -z "$USER_POOL_ID" ] || [ -z "$CLIENT_ID" ]; then
    echo "❌ Uso: $0 <USER_POOL_ID> <CLIENT_ID>"
    echo "💡 Obtenha os valores com: terraform output"
    exit 1
fi

echo "👤 Criando usuários de teste no Cognito..."
echo "📋 User Pool ID: $USER_POOL_ID"
echo "📋 Client ID: $CLIENT_ID"
echo ""

# Criar usuário admin
echo "🔧 Criando usuário admin..."
aws cognito-idp admin-create-user \
  --user-pool-id "$USER_POOL_ID" \
  --username "admin@financas.com" \
  --temporary-password "AdminPass123!" \
  --message-action SUPPRESS \
  --user-attributes Name=email,Value=admin@financas.com Name=name,Value="Admin User"

# Adicionar usuário admin ao grupo admin
aws cognito-idp admin-add-user-to-group \
  --user-pool-id "$USER_POOL_ID" \
  --username "admin@financas.com" \
  --group-name "admin"

# Criar usuário regular
echo "👤 Criando usuário regular..."
aws cognito-idp admin-create-user \
  --user-pool-id "$USER_POOL_ID" \
  --username "user@financas.com" \
  --temporary-password "UserPass123!" \
  --message-action SUPPRESS \
  --user-attributes Name=email,Value=user@financas.com Name=name,Value="Regular User"

# Adicionar usuário regular ao grupo user
aws cognito-idp admin-add-user-to-group \
  --user-pool-id "$USER_POOL_ID" \
  --username "user@financas.com" \
  --group-name "user"

echo ""
echo "✅ Usuários criados com sucesso!"
echo ""
echo "📋 Credenciais de teste:"
echo "👑 Admin: admin@financas.com / AdminPass123!"
echo "👤 User:  user@financas.com / UserPass123!"
echo ""
echo "🔐 Para obter tokens JWT, use:"
echo "aws cognito-idp admin-initiate-auth \\"
echo "  --user-pool-id $USER_POOL_ID \\"
echo "  --client-id $CLIENT_ID \\"
echo "  --auth-flow ADMIN_NO_SRP_AUTH \\"
echo "  --auth-parameters USERNAME=admin@financas.com,PASSWORD=AdminPass123!"
