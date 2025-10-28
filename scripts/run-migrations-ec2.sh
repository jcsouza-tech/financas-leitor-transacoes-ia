#!/bin/bash

# Database Migration Runner for EC2
# Runs Flyway migrations directly on EC2 (better than ECS for your setup)

set -e

# Default values
ENVIRONMENT=${ENVIRONMENT:-prod}
ACTION=${ACTION:-migrate}
REGION=${AWS_REGION:-us-east-1}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Running Database Migrations via EC2${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Action: ${ACTION}${NC}"

# Check if Flyway is installed
if ! command -v flyway &> /dev/null; then
    echo -e "${YELLOW}📦 Flyway not found. Installing...${NC}"
    # Download Flyway
    wget -q -O /tmp/flyway.tar.gz https://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/9.22.0/flyway-commandline-9.22.0-linux-x64.tar.gz
    tar -xzf /tmp/flyway.tar.gz -C /tmp
    export PATH=$PATH:/tmp/flyway-9.22.0
    echo -e "${GREEN}✅ Flyway installed${NC}"
fi

# Get database credentials from AWS Secrets Manager
echo -e "${YELLOW}🔐 Retrieving database credentials from AWS Secrets Manager...${NC}"
DB_CREDS=$(aws secretsmanager get-secret-value \
    --secret-id "financas/leitor-transacoes-ia/${ENVIRONMENT}/database" \
    --region $REGION \
    --query SecretString --output text)

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error: Failed to retrieve database credentials${NC}"
    exit 1
fi

# Extract database connection details
DB_HOST=$(echo $DB_CREDS | jq -r '.host')
DB_PORT=$(echo $DB_CREDS | jq -r '.port')
DB_NAME=$(echo $DB_CREDS | jq -r '.database')
DB_USER=$(echo $DB_CREDS | jq -r '.username')
DB_PASSWORD=$(echo $DB_CREDS | jq -r '.password')

echo -e "${GREEN}✅ Database credentials retrieved successfully${NC}"
echo -e "${BLUE}Database: ${DB_HOST}:${DB_PORT}/${DB_NAME}${NC}"

# Configure Flyway
echo -e "${YELLOW}📝 Configuring Flyway...${NC}"
export FLYWAY_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
export FLYWAY_USER="${DB_USER}"
export FLYWAY_PASSWORD="${DB_PASSWORD}"
export FLYWAY_DRIVER="com.mysql.cj.jdbc.Driver"
export FLYWAY_LOCATIONS="filesystem:$(pwd)/src/main/resources/db/migration"
export FLYWAY_VALIDATE_ON_MIGRATE="true"
export FLYWAY_BASELINE_ON_MIGRATE="true"
export FLYWAY_BASELINE_VERSION="0"
export FLYWAY_CLEAN_DISABLED="true"

# Test database connection
echo -e "${YELLOW}🔍 Testing database connection...${NC}"
flyway info

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error: Cannot connect to database${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Database connection successful${NC}"

# Execute Flyway command
case "${ACTION}" in
    "migrate")
        echo -e "${YELLOW}🔄 Executing database migrations...${NC}"
        flyway migrate
        ;;
    "info")
        echo -e "${YELLOW}📊 Showing migration information...${NC}"
        flyway info
        ;;
    "validate")
        echo -e "${YELLOW}✅ Validating migrations...${NC}"
        flyway validate
        ;;
    "baseline")
        echo -e "${YELLOW}📌 Creating baseline...${NC}"
        flyway baseline
        ;;
    *)
        echo -e "${RED}❌ Unknown action: ${ACTION}${NC}"
        echo -e "${BLUE}Available actions: migrate, info, validate, baseline${NC}"
        exit 1
        ;;
esac

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Migrations completed successfully!${NC}"
    exit 0
else
    echo -e "${RED}❌ Migrations failed${NC}"
    exit 1
fi
