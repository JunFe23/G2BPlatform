#!/bin/bash
# set -e

cd ../
git pull
cd backend
./gradlew bootJar
docker-compose down
docker-compose up --build -d

