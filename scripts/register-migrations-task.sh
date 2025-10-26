#!/bin/bash

# Script para registrar task definition de migrations no ECS
# Uso: ./register-migrations-task.sh

set -e

echo "🔧 Registrando task definition para migrations..."

# Registrar task definition
aws ecs register-task-definition \
  --cli-input-json file://task-definition-migrations.json \
  --region us-east-1

echo "✅ Task definition registrada com sucesso!"

# Criar log group se não existir
aws logs create-log-group \
  --log-group-name /ecs/financas-migrations \
  --region us-east-1 || echo "⚠️ Log group já existe"

echo "✅ Setup completo!"
