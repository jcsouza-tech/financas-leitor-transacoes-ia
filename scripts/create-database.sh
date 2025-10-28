#!/bin/bash

# Create Database Script for RDS
# This script creates the database if it doesn't exist
# Run this BEFORE running Flyway migrations

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🗄️ Creating database if not exists...${NC}"

# Get credentials from environment or AWS Secrets Manager
if [ -z "$DB_HOST" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
    echo -e "${YELLOW}📥 Retrieving credentials from AWS Secrets Manager...${NC}"
    
    DB_CREDS=$(aws secretsmanager get-secret-value \
        --secret-id "financas/leitor-transacoes-ia/prod/database" \
        --region us-east-1 \
        --query SecretString --output text)
    
    DB_HOST=$(echo $DB_CREDS | jq -r '.host')
    DB_USER=$(echo $DB_CREDS | jq -r '.username')
    DB_PASSWORD=$(echo $DB_CREDS | jq -r '.password')
    DB_NAME=$(echo $DB_CREDS | jq -r '.database')
else
    DB_NAME="financas_prd_mysql"
fi

echo -e "${BLUE}Connecting to: ${DB_USER}@${DB_HOST}${NC}"

# Create database if not exists
mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" mysql << EOF
CREATE DATABASE IF NOT EXISTS ${DB_NAME} 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
  
SHOW DATABASES LIKE '${DB_NAME}';
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Database created/exists: ${DB_NAME}${NC}"
else
    echo -e "${RED}❌ Error creating database${NC}"
    exit 1
fi
