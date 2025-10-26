#!/bin/bash

# AWS ECS Migration Runner Script
# Executes database migrations using ECS Fargate tasks

set -e

# Default values
ENVIRONMENT=${ENVIRONMENT:-hml}
ACTION=${ACTION:-migrate}
CLUSTER_NAME="financas-${ENVIRONMENT}-cluster"
TASK_DEFINITION="financas-migrations-task"
REGION=${AWS_REGION:-us-east-1}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 AWS ECS Database Migration Runner${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Action: ${ACTION}${NC}"
echo -e "${BLUE}Cluster: ${CLUSTER_NAME}${NC}"
echo -e "${BLUE}Region: ${REGION}${NC}"

# Validate required environment variables
if [ -z "$AWS_ACCESS_KEY_ID" ]; then
    echo -e "${RED}❌ Error: AWS_ACCESS_KEY_ID environment variable is required${NC}"
    exit 1
fi

if [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    echo -e "${RED}❌ Error: AWS_SECRET_ACCESS_KEY environment variable is required${NC}"
    exit 1
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

# Get ECS cluster information
echo -e "${YELLOW}🔍 Getting ECS cluster information...${NC}"
CLUSTER_ARN=$(aws ecs describe-clusters --clusters $CLUSTER_NAME --region $REGION --query 'clusters[0].clusterArn' --output text)

if [ "$CLUSTER_ARN" = "None" ] || [ -z "$CLUSTER_ARN" ]; then
    echo -e "${RED}❌ Error: ECS cluster '${CLUSTER_NAME}' not found${NC}"
    exit 1
fi

echo -e "${GREEN}✅ ECS cluster found: ${CLUSTER_ARN}${NC}"

# Get task definition information
echo -e "${YELLOW}🔍 Getting task definition information...${NC}"
TASK_DEF_ARN=$(aws ecs describe-task-definition --task-definition $TASK_DEFINITION --region $REGION --query 'taskDefinition.taskDefinitionArn' --output text)

if [ "$TASK_DEF_ARN" = "None" ] || [ -z "$TASK_DEF_ARN" ]; then
    echo -e "${RED}❌ Error: Task definition '${TASK_DEFINITION}' not found${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Task definition found: ${TASK_DEF_ARN}${NC}"

# Get subnet and security group information
echo -e "${YELLOW}🔍 Getting network configuration...${NC}"
SUBNET_ID=$(aws ec2 describe-subnets --filters "Name=tag:Environment,Values=${ENVIRONMENT}" "Name=tag:Type,Values=private" --region $REGION --query 'Subnets[0].SubnetId' --output text)
SECURITY_GROUP_ID=$(aws ec2 describe-security-groups --filters "Name=tag:Environment,Values=${ENVIRONMENT}" "Name=tag:Name,Values=*migrations*" --region $REGION --query 'SecurityGroups[0].GroupId' --output text)

if [ "$SUBNET_ID" = "None" ] || [ -z "$SUBNET_ID" ]; then
    echo -e "${RED}❌ Error: No suitable subnet found for environment ${ENVIRONMENT}${NC}"
    exit 1
fi

if [ "$SECURITY_GROUP_ID" = "None" ] || [ -z "$SECURITY_GROUP_ID" ]; then
    echo -e "${RED}❌ Error: No suitable security group found for environment ${ENVIRONMENT}${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Network configuration: Subnet=${SUBNET_ID}, SecurityGroup=${SECURITY_GROUP_ID}${NC}"

# Run ECS task
echo -e "${YELLOW}🚀 Starting ECS migration task...${NC}"
TASK_ARN=$(aws ecs run-task \
    --cluster $CLUSTER_NAME \
    --task-definition $TASK_DEFINITION \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[${SUBNET_ID}],securityGroups=[${SECURITY_GROUP_ID}],assignPublicIp=ENABLED}" \
    --overrides "{
        \"containerOverrides\": [{
            \"name\": \"migrations\",
            \"environment\": [
                {\"name\": \"FLYWAY_URL\", \"value\": \"jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}\"},
                {\"name\": \"FLYWAY_USER\", \"value\": \"${DB_USER}\"},
                {\"name\": \"FLYWAY_PASSWORD\", \"value\": \"${DB_PASSWORD}\"},
                {\"name\": \"ENVIRONMENT\", \"value\": \"${ENVIRONMENT}\"},
                {\"name\": \"APPLICATION\", \"value\": \"leitor-transacoes-ia\"}
            ],
            \"command\": [\"${ACTION}\"]
        }]
    }" \
    --region $REGION \
    --query 'tasks[0].taskArn' \
    --output text)

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error: Failed to start ECS task${NC}"
    exit 1
fi

echo -e "${GREEN}✅ ECS task started: ${TASK_ARN}${NC}"

# Wait for task completion
echo -e "${YELLOW}⏳ Waiting for migration task to complete...${NC}"
aws ecs wait tasks-stopped --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $REGION

# Check task exit code
echo -e "${YELLOW}🔍 Checking task execution results...${NC}"
TASK_DETAILS=$(aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $REGION)
EXIT_CODE=$(echo $TASK_DETAILS | jq -r '.tasks[0].containers[0].exitCode')
STOPPED_REASON=$(echo $TASK_DETAILS | jq -r '.tasks[0].stoppedReason')

echo -e "${BLUE}Task stopped reason: ${STOPPED_REASON}${NC}"

if [ "$EXIT_CODE" = "0" ]; then
    echo -e "${GREEN}✅ Migrations completed successfully!${NC}"
    exit 0
else
    echo -e "${RED}❌ Migrations failed with exit code: ${EXIT_CODE}${NC}"
    
    # Get task logs for debugging
    echo -e "${YELLOW}📋 Retrieving task logs for debugging...${NC}"
    LOG_GROUP="/ecs/${TASK_DEFINITION}"
    LOG_STREAM=$(aws logs describe-log-streams \
        --log-group-name $LOG_GROUP \
        --order-by LastEventTime \
        --descending \
        --max-items 1 \
        --region $REGION \
        --query 'logStreams[0].logStreamName' \
        --output text)
    
    if [ "$LOG_STREAM" != "None" ] && [ -n "$LOG_STREAM" ]; then
        echo -e "${BLUE}📄 Recent logs from ${LOG_GROUP}/${LOG_STREAM}:${NC}"
        aws logs get-log-events \
            --log-group-name $LOG_GROUP \
            --log-stream-name $LOG_STREAM \
            --region $REGION \
            --query 'events[*].message' \
            --output text
    fi
    
    exit 1
fi
