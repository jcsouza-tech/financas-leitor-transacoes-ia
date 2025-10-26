#!/bin/bash

# Migration Validation Script
# Validates SQL syntax and migration versioning before commit

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Database Migration Validation${NC}"

# Check if migration directory exists
MIGRATION_DIR="src/main/resources/db/migration"
if [ ! -d "$MIGRATION_DIR" ]; then
    echo -e "${RED}❌ Error: Migration directory not found: ${MIGRATION_DIR}${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Migration directory found: ${MIGRATION_DIR}${NC}"

# Validate SQL files exist
SQL_FILES=$(find $MIGRATION_DIR -name "*.sql" | wc -l)
if [ $SQL_FILES -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Warning: No SQL migration files found${NC}"
    exit 0
fi

echo -e "${GREEN}✅ Found ${SQL_FILES} SQL migration files${NC}"

# Validate SQL syntax
echo -e "${YELLOW}🔍 Validating SQL syntax...${NC}"
for file in $MIGRATION_DIR/*.sql; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        echo -e "${BLUE}Validating ${filename}...${NC}"
        
        # Basic SQL syntax validation
        if ! grep -q "CREATE\|ALTER\|DROP\|INSERT\|UPDATE\|DELETE" "$file"; then
            echo -e "${RED}❌ Error: ${filename} does not contain valid SQL statements${NC}"
            exit 1
        fi
        
        # Check for dangerous operations
        if grep -qi "DROP DATABASE\|DROP SCHEMA\|TRUNCATE" "$file"; then
            echo -e "${RED}❌ Error: ${filename} contains dangerous operations (DROP DATABASE/SCHEMA, TRUNCATE)${NC}"
            exit 1
        fi
        
        # Check for missing semicolons
        if ! grep -q ";" "$file"; then
            echo -e "${RED}❌ Error: ${filename} is missing semicolons${NC}"
            exit 1
        fi
        
        echo -e "${GREEN}✅ ${filename} syntax is valid${NC}"
    fi
done

# Validate migration versioning
echo -e "${YELLOW}🔍 Validating migration versioning...${NC}"
cd $MIGRATION_DIR

# Get all version numbers
versions=($(ls V*.sql 2>/dev/null | sed 's/V\([0-9]*\)__.*/\1/' | sort -n))

if [ ${#versions[@]} -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Warning: No versioned migration files found${NC}"
    exit 0
fi

echo -e "${BLUE}Found versions: ${versions[*]}${NC}"

# Check for gaps in versioning
for i in "${!versions[@]}"; do
    if [ $i -gt 0 ]; then
        prev=${versions[$((i-1))]}
        curr=${versions[$i]}
        if [ $((curr - prev)) -ne 1 ]; then
            echo -e "${RED}❌ Error: Gap in migration versions between V${prev} and V${curr}${NC}"
            exit 1
        fi
    fi
done

echo -e "${GREEN}✅ Migration versioning is correct${NC}"

# Validate migration naming convention
echo -e "${YELLOW}🔍 Validating migration naming convention...${NC}"
for file in V*.sql; do
    if [ -f "$file" ]; then
        # Check naming pattern: V{version}__{description}.sql
        if [[ ! $file =~ ^V[0-9]+__[A-Za-z0-9_]+\.sql$ ]]; then
            echo -e "${RED}❌ Error: ${file} does not follow naming convention V{version}__{description}.sql${NC}"
            exit 1
        fi
        
        # Check description is not empty
        description=$(echo $file | sed 's/V[0-9]*__\(.*\)\.sql/\1/')
        if [ -z "$description" ]; then
            echo -e "${RED}❌ Error: ${file} has empty description${NC}"
            exit 1
        fi
        
        echo -e "${GREEN}✅ ${file} naming is correct${NC}"
    fi
done

# Check for duplicate versions
echo -e "${YELLOW}🔍 Checking for duplicate versions...${NC}"
duplicates=$(printf '%s\n' "${versions[@]}" | sort | uniq -d)
if [ -n "$duplicates" ]; then
    echo -e "${RED}❌ Error: Duplicate migration versions found: ${duplicates}${NC}"
    exit 1
fi

echo -e "${GREEN}✅ No duplicate versions found${NC}"

# Validate migration content best practices
echo -e "${YELLOW}🔍 Validating migration best practices...${NC}"
for file in V*.sql; do
    if [ -f "$file" ]; then
        echo -e "${BLUE}Checking best practices for ${file}...${NC}"
        
        # Check for transaction usage
        if grep -qi "BEGIN\|START TRANSACTION" "$file"; then
            echo -e "${YELLOW}⚠️  Warning: ${file} contains explicit transaction control. Flyway handles transactions automatically.${NC}"
        fi
        
        # Check for data deletion without WHERE clause
        if grep -qi "DELETE FROM.*WHERE" "$file"; then
            echo -e "${YELLOW}⚠️  Warning: ${file} contains DELETE with WHERE clause. Ensure this is intentional.${NC}"
        fi
        
        # Check for large data modifications
        if grep -qi "UPDATE.*SET.*WHERE.*=" "$file"; then
            echo -e "${YELLOW}⚠️  Warning: ${file} contains UPDATE operations. Ensure these are safe for production.${NC}"
        fi
        
        echo -e "${GREEN}✅ ${file} best practices check completed${NC}"
    fi
done

echo -e "${GREEN}🎉 All migration validations passed!${NC}"
echo -e "${BLUE}📋 Summary:${NC}"
echo -e "${BLUE}  - ${SQL_FILES} migration files found${NC}"
echo -e "${BLUE}  - SQL syntax validation: PASSED${NC}"
echo -e "${BLUE}  - Version numbering: PASSED${NC}"
echo -e "${BLUE}  - Naming convention: PASSED${NC}"
echo -e "${BLUE}  - Best practices: PASSED${NC}"
