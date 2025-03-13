#!/bin/bash

if [ "$#" -lt 1 ]; then
  echo "Usage: $0 <migration_description>"
  exit 1
fi


DESCRIPTION=$(echo "$*" | tr ' ' '_')

./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=dev \
  -Dspring-boot.run.main-class=com.alex.MigrationGeneratorApp \
  -Dspring-boot.run.arguments="$DESCRIPTION"