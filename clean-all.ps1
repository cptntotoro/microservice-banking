helm uninstall microbank -n dev
kubectl delete namespace dev --force --grace-period=0
kubectl create namespace dev
kubectl delete pvc --all -n dev

Write-Host "Clean done"
