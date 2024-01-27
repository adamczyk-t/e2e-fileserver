#!/bin/bash

DB_CONTAINER_NAME="e2e-fileserver-db"
DB_IMAGE_NAME="postgres:latest"
DB_PASSWORD="test1"

if ! docker image inspect $DB_IMAGE_NAME &> /dev/null; then
  echo "No postgres:latest image. Downloading...";
  docker pull ${DB_IMAGE_NAME}
fi

if docker ps -a --format '{{.Names}}' | grep -q "^$DB_CONTAINER_NAME$"; then
  echo "Stopping and removing existing PostgreSQL container..."
  docker stop $DB_CONTAINER_NAME >/dev/null
  docker rm $DB_CONTAINER_NAME >/dev/null
fi

docker run --name $DB_CONTAINER_NAME -p 5432:5432 -e POSTGRES_PASSWORD=$DB_PASSWORD -d $DB_IMAGE_NAME >/dev/null \
  && sleep 1s && echo "Container started."