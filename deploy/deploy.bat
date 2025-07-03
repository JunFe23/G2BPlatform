cd ..
git pull
cd backend
gradlew.bat bootJar
docker-compose down
docker-compose up --build -d
