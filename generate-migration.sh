#!/bin/bash

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 <migration_description> [mode]"
  echo "  mode: 'update' (default) for incremental changes, 'create' for full schema creation"
  exit 1
fi


DESCRIPTION=$(echo "$1" | tr ' ' '_')
MODE=${2:-update}  

echo "Generating migration script: $DESCRIPTION (Mode: $MODE)"



./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.main-class=com.alex.flyway_migrations.tools.migration.MigrationGeneratorApp \
  -Dspring-boot.run.arguments="$DESCRIPTION $MODE"