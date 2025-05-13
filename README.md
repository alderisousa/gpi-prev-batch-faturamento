# Rodar em container

```
mvn clean install -DskipTests

cd docker\

docker build -t gpi-prev-batch-faturamento-app . 
docker run --rm \
  --memory="1g" \
  -e DB_URL="sqlserver://host.docker.internal:1433;databaseName=gpiprev;encrypt=true;trustServerCertificate=true" \
  -e DB_USERNAME="sa" \
  -e DB_PASSWORD="1q2w3e4r@#$" \
  gpi-prev-batch-faturamento-app
```