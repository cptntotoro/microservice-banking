# Запуск Consul и Discovery Server

## Способ 1: С помощью Docker Compose

`docker-compose -f docker-compose-consul.yml up -d`

## Способ 2: Локальный запуск (для разработки)
Сначала запустите Consul:

`docker run -d --name consul -p 8500:8500 -p 8600:8600/udp consul:1.15 agent -server -ui -node=server-1 -bootstrap-expect=1 -client=0.0.0.0`

Затем запустите Discovery Server:

`cd api-gateway-server
mvn clean package
java -jar target/api-gateway-server.jar`

# Проверка работы
После запуска откройте в браузере:

Запущенные сервисы в Consul UI: http://localhost:8500/ui/dc1/services