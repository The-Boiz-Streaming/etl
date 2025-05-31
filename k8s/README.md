# Deployment guide

This project is deployed on Kubernetes.

First create project namespace:

```
kubectl create namespace lab
```

To deploy clickhouse and metabase

```
kubectl apply -k k8s/
```

To deploy spark task first it's needed to deploy spark operator. You can do it by following this guide: https://github.com/apache/spark-kubernetes-operator;

Then run 
```
kubectl apply -f spark-application.yaml
```

