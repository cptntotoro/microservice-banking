Запустите Consul:

bash
docker-compose -f docker-compose-consul.yml up -d consul-server
Загрузите конфиги в Consul:

bash
./load-consul-config.sh
Запустите остальные сервисы:

bash
docker-compose -f docker-compose-consul.yml up -d
Проверка работы:
Consul UI: http://localhost:8500 → перейдите в Key/Value → config/

Config Server: http://localhost:8888/actuator/health

Получить конфиг для account-service:

bash
curl http://localhost:8888/account-service/default