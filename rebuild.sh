#!/bin/bash

# Проверяем, передан ли параметр
if [ -z "$1" ]; then
  echo "Ошибка: укажите сервис, например: $0 front-ui-service"
  exit 1
fi

SERVICE=$1

# Выполняем команды
cd "./$SERVICE" || { echo "Ошибка: директория $SERVICE не найдена"; exit 1; }
mvn clean install
cd ..
docker-compose -f docker-compose-consul.yml up -d "$SERVICE"