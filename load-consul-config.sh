##!/bin/bash
#
#CONSUL_HOST="localhost"
#CONSUL_PORT="8500"
#
#echo "=== Loading configurations to Consul KV Store ==="
#
## Функция для загрузки конфига в Consul
#load_config() {
#    local config_name=$1
#    local config_file=$2
#
#    if [ ! -f "$config_file" ]; then
#        echo "Error: File $config_file not found!"
#        return 1
#    fi
#
#    echo "Loading $config_name from $config_file"
#
#    response=$(curl --request PUT \
#         --data-binary @"$config_file" \
#         "http://$CONSUL_HOST:$CONSUL_PORT/v1/kv/config/$config_name/data" \
#         -H "Content-Type: text/yaml" \
#         --silent --write-out "%{http_code}" --output /dev/null)
#
#    if [ "$response" -eq 200 ]; then
#        echo "✓ Successfully loaded $config_name"
#    else
#        echo "✗ Failed to load $config_name (HTTP $response)"
#    fi
#}
#
## Проверяем, доступен ли Consul
#echo "Checking Consul connection..."
#if ! curl --silent --head "http://$CONSUL_HOST:$CONSUL_PORT/v1/status/leader" > /dev/null; then
#    echo "Error: Consul is not available at http://$CONSUL_HOST:$CONSUL_PORT"
#    echo "Please start Consul first: docker-compose -f docker-compose-consul.yml up -d consul-server"
#    exit 1
#fi
#
## Загружаем конфиги
#load_config "application" "consul-config/application.yml"
#load_config "account-service" "consul-config/account-service.yml"
#load_config "cash-service" "consul-config/cash-service.yml"
#load_config "transfer-service" "consul-config/transfer-service.yml"
#load_config "exchange-service" "consul-config/exchange-service.yml"
#load_config "api-gateway" "consul-config/api-gateway.yml"
#
#echo ""
#echo "=== Configuration loading completed ==="
#echo "Check Consul UI: http://localhost:8500/ui/dc1/kv/config/"
#echo "Check Config Server: http://localhost:8888/actuator/env"