#!/bin/sh

# Flyway Migrations Entrypoint Script
# Handles environment variable substitution and migration execution

set -e

echo "🚀 Starting Flyway Database Migrations..."
echo "Environment: ${ENVIRONMENT:-dev}"
echo "Application: ${APPLICATION:-leitor-transacoes-ia}"

# Generate flyway.conf from template
echo "📝 Generating flyway.conf from template..."
envsubst < /flyway/conf/flyway.conf.template > /flyway/conf/flyway.conf

echo "📋 Flyway Configuration:"
cat /flyway/conf/flyway.conf

# Validate required environment variables
if [ -z "$FLYWAY_URL" ]; then
    echo "❌ Error: FLYWAY_URL environment variable is required"
    exit 1
fi

if [ -z "$FLYWAY_USER" ]; then
    echo "❌ Error: FLYWAY_USER environment variable is required"
    exit 1
fi

if [ -z "$FLYWAY_PASSWORD" ]; then
    echo "❌ Error: FLYWAY_PASSWORD environment variable is required"
    exit 1
fi

# Test database connection
echo "🔍 Testing database connection..."
flyway info

if [ $? -ne 0 ]; then
    echo "❌ Error: Cannot connect to database"
    exit 1
fi

echo "✅ Database connection successful"

# Check if migrations are needed
echo "📊 Checking migration status..."
flyway info

# Execute migrations based on command
case "${1:-migrate}" in
    "migrate")
        echo "🔄 Executing database migrations..."
        flyway migrate
        ;;
    "info")
        echo "📊 Showing migration information..."
        flyway info
        ;;
    "validate")
        echo "✅ Validating migrations..."
        flyway validate
        ;;
    "baseline")
        echo "📌 Creating baseline..."
        flyway baseline
        ;;
    "repair")
        echo "🔧 Repairing migration history..."
        flyway repair
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo "Available commands: migrate, info, validate, baseline, repair"
        exit 1
        ;;
esac

echo "✅ Migration process completed successfully!"
