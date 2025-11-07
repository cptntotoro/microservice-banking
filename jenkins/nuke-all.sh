#!/bin/bash
set -e

# Загрузка переменных из .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Проверка переменной
if [ -z "$DOCKER_REGISTRY" ]; then
  echo "DOCKER_REGISTRY не задан в .env"
  exit 1
fi

echo "Using DOCKER_REGISTRY: $DOCKER_REGISTRY"

echo "Uninstalling Helm releases..."
for ns in test prod; do
  helm uninstall serv-auth-service -n "$ns" || true
  helm uninstall user-auth-service -n "$ns" || true
  helm uninstall blocker-service -n "$ns" || true
  helm uninstall cash-service -n "$ns" || true
  helm uninstall exchange-service -n "$ns" || true
  helm uninstall exchange-generator-service -n "$ns" || true
  helm uninstall notification-service -n "$ns" || true
  helm uninstall transfer-service -n "$ns" || true
  helm uninstall api-gateway-server -n "$ns" || true
  helm uninstall front-ui-service -n "$ns" || true
  helm uninstall postgres -n "$ns" || true
done

echo "Deleting secrets..."
for ns in test prod; do
  kubectl delete secret postgres -n "$ns" --ignore-not-found
done

echo "Deleting namespaces..."
kubectl delete ns test --ignore-not-found
kubectl delete ns prod --ignore-not-found

echo "Shutting down Jenkins..."
docker compose down -v || true
docker stop jenkins && docker rm jenkins || true
docker volume rm jenkins_home || true

echo "Removing images..."
docker image rm serv-auth-service:latest || true
docker image rm user-auth-service:latest || true
docker image rm blocker-service:latest || true
docker image rm cash-service:latest || true
docker image rm exchange-service:latest || true
docker image rm exchange-generator-service:latest || true
docker image rm notification-service:latest || true
docker image rm transfer-service:latest || true
docker image rm api-gateway-server:latest || true
docker image rm front-ui-service:latest || true
docker image rm jenkins-jenkins:latest || true

echo "Pruning system..."

echo "Done! All clean."