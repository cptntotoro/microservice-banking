$services = @(
    "serv-auth-service",
    "user-auth-service",
    "account-service",
    "blocker-service",
    "cash-service",
    "exchange-service",
    "exchange-generator-service",
    "notification-service",
    "transfer-service",
    "api-gateway-server",
    "front-ui-service"
)

Write-Host "=== DOCKER BUILD & MINIKUBE LOAD ===" -ForegroundColor Magenta

foreach ($service in $services) {
    if (Test-Path $service) {
        Write-Host "`nBUILDING & LOADING $service..." -ForegroundColor Green

        mvn clean package -pl $service
        docker build -t $service:latest ./$service
        minikube image load $service:latest
    }
}

Write-Host "`n=== ALL DONE! ===" -ForegroundColor Magenta
Write-Host "Verify: minikube image ls" -ForegroundColor Yellow