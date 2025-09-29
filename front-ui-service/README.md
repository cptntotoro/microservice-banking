# Сервис фронтенда (Front UI)

Front UI Service — это stateless реактивный микросервис, предназначенный для запроса и агрегации данных из других сервисов, отрисовки клиентских интерфейсом и отправки пользовательских данных для обработки в другие сервисы.

## Сборка и запуск

1. Убедитесь, что у вас запущен Docker Desktop
2. Соберите сервис (компиляция + тесты)

`mvn clean install -pl front-ui-service`

3. Запустите Consul:

`docker-compose -f docker-compose-consul.yml up -d consul-server`

4. Запустите сервис

`java -jar front-ui-service/target/front-ui-service.jar`