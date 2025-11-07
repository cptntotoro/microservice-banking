# microservice-banking

Микросервисное приложение «Банк» — это приложение с веб-интерфейсом, которое позволяет пользователю (клиенту банка):
- регистрироваться в системе по логину и паролю (заводить аккаунт);
- добавлять счета в различных валютах;
- класть виртуальные деньги на счёт и снимать их;
- переводить деньги между своими счетами с учётом конвертации в различные валюты;
- переводить деньги на другой счёт с учётом конвертации в различные валюты.

Приложение состоит из следующих микросервисов:
- фронта (Front UI);
- сервиса аккаунтов (Accounts);
- сервиса обналичивания денег (Cash);
- сервиса перевода денег между счетами одного или двух аккаунтов (Transfer);
- сервиса конвертации валют (Exchange);
- сервиса генерации курсов валют (Exchange Generator);
- сервиса блокировки подозрительных операций (Blocker);
- сервиса уведомлений (Notifications)

## Flow Diagram

![](flow-diagram.png)

# Как запускать

- Минимально необходимые для работы сервисы
    - account-service
    - serv-auth-service
    - user-auth-service
    - front-ui-service
    - чарт nginx-gateway



- Запуск Minikube
  - minikube start --memory=8192 --cpus=4 --driver=docker
  - minikube addons enable ingress

- Сборка и загрузка
  - mvn clean package -pl XXX
  - docker build -t XXX:latest -f XXX/Dockerfile .
  - minikube image load XXX:latest

- Повторить сборку для всех сервисов
  - account-service
  - serv-auth-service
  - user-auth-service
  - front-ui-service
  - blocker-service
  - cash-service
  - exchange-service
  - exchange-generator-service
  - notification-service
  - transfer-service

- Проверка загруженных образов
  - minikube image ls

- Очистка предыдущих установок (опционально, но рекомендуется)
  - helm uninstall microbank -n dev
  - kubectl delete namespace dev

- Обновление зависимостей umbrella-чарта
  - helm dependency update ./umbrella-chart

- В values umbrella-чарт можно включить отключенные по дефолту сервисы

- Установка приложения через umbrella-чарт
  - helm install microbank ./umbrella-chart -n dev --create-namespace --wait

- Проверка статуса развёртывания
  - helm list -n dev
  - kubectl get pods -n dev

- Проверка логов
  - kubectl logs microbank-postgres-0 -n dev

- Проброс порта фронтенда
  - kubectl port-forward svc/front-ui-service 8081:8081 -n dev
