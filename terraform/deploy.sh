#!/bin/bash

# Script para deploy da infraestrutura Terraform
# Uso: ./deploy.sh [environment] [backend_url]

set -e

ENVIRONMENT=${1:-dev}
BACKEND_URL=${2:-http://localhost:8080}

echo "🚀 Deploying infrastructure for environment: $ENVIRONMENT"
echo "📡 Backend URL: $BACKEND_URL"

# Verificar se Terraform está instalado
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform não está instalado. Instale primeiro."
    exit 1
fi

# Verificar se AWS CLI está configurado
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ AWS CLI não está configurado. Configure primeiro."
    exit 1
fi

# Inicializar Terraform se necessário
if [ ! -d ".terraform" ]; then
    echo "📦 Inicializando Terraform..."
    terraform init
fi

# Planejar mudanças
echo "📋 Planejando mudanças..."
terraform plan \
    -var="environment=$ENVIRONMENT" \
    -var="backend_service_url=$BACKEND_URL" \
    -out=tfplan

# Aplicar mudanças
echo "🔨 Aplicando mudanças..."
terraform apply tfplan

# Mostrar outputs
echo "📤 Outputs da infraestrutura:"
terraform output

echo "✅ Deploy concluído!"
echo ""
echo "📝 Próximos passos:"
echo "1. Configure as variáveis de ambiente no backend usando os outputs acima"
echo "2. Reinicie o serviço backend"
echo "3. Teste a autenticação"
