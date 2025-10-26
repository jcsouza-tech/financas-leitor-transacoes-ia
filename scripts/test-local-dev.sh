#!/bin/bash

# Script para testar aplicação em modo desenvolvimento local
# Uso: ./test-local-dev.sh

set -e

echo "🧪 Testando aplicação em modo desenvolvimento local..."
echo "🔧 Modo: SECURITY_ENABLED=false (sem autenticação)"
echo ""

# Verificar se a aplicação está rodando
echo "⏳ Verificando se a aplicação está rodando..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ Aplicação não está rodando em http://localhost:8080"
    echo "💡 Execute: mvn spring-boot:run"
    exit 1
fi

echo "✅ Aplicação está rodando!"
echo ""

# Testar endpoints públicos
echo "🔍 Testando endpoints públicos..."

echo "📊 Health Check:"
curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
echo ""

echo "📚 Swagger UI:"
curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8080/swagger-ui/index.html
echo ""

# Testar endpoints da API (sem autenticação)
echo "🔍 Testando endpoints da API (sem autenticação)..."

echo "📋 Listar transações:"
TRANSACOES_RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8080/api/v1/leitor/transacoes)
HTTP_CODE=$(echo "$TRANSACOES_RESPONSE" | tail -n1)
BODY=$(echo "$TRANSACOES_RESPONSE" | head -n -1)

echo "Status: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Endpoint funcionando (modo desenvolvimento)"
    echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
else
    echo "❌ Endpoint retornou status: $HTTP_CODE"
fi
echo ""

echo "📊 Listar processamentos:"
PROCESSAMENTOS_RESPONSE=$(curl -s -w "\n%{http_code}" http://localhost:8080/api/v1/leitor/processamentos)
HTTP_CODE=$(echo "$PROCESSAMENTOS_RESPONSE" | tail -n1)
BODY=$(echo "$PROCESSAMENTOS_RESPONSE" | head -n -1)

echo "Status: $HTTP_CODE"
if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Endpoint funcionando (modo desenvolvimento)"
    echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
else
    echo "❌ Endpoint retornou status: $HTTP_CODE"
fi
echo ""

# Verificar logs de UserContext
echo "🔍 Verificando logs de UserContext..."
echo "💡 Procure por 'local-user' nos logs da aplicação"
echo ""

echo "✅ Teste de desenvolvimento local concluído!"
echo ""
echo "📝 Resumo:"
echo "   - Aplicação rodando em modo desenvolvimento"
echo "   - Autenticação desabilitada (SECURITY_ENABLED=false)"
echo "   - Usuário mock: 'local-user'"
echo "   - Endpoints acessíveis sem token JWT"
echo ""
echo "🚀 Próximos passos:"
echo "   1. Configure AWS Cognito para produção"
echo "   2. Execute: terraform apply"
echo "   3. Configure variáveis de ambiente"
echo "   4. Teste com autenticação real"
