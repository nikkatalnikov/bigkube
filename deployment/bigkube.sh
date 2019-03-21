#!/bin/bash

function wait() {
    while [ $(kubectl get pods | awk '{print $3}' | tail -n +2 | grep -v Running | wc -l) != 0 ]; do
        sleep 1
    done
}

function serve_jar_directory () {
    docker rm nginx-jar-server -f
    local PARENT_DIR=$(cd ../ && pwd)
    docker run --name nginx-jar-server -v ${PARENT_DIR}/target/scala-2.11:/usr/share/nginx/html:ro -d -p 8080:80 nginx
    echo "don't forget to pass following ip into your Spark CRD declaration to mainApplicationFile field:"
    ifconfig en0 | awk '$1 == "inet" {print $2}'
}

function init_spark_operator() {
    kubectl create clusterrolebinding default --clusterrole=edit --serviceaccount=default:default --namespace=default
    helm init --wait &&
    helm install incubator/sparkoperator --namespace spark-operator --set enableWebhook=true
}

function drop_spark_operator() {
    kubectl delete -f spark-prometheus.yaml
    helm delete $(helm list --namespace=spark-operator --short)
}

function create() {
    kubectl create secret generic mssql-user --from-literal=user=sa
    kubectl create secret generic mssql-password --from-literal=password=YOUR_PASSWORD_123_abcd
    kubectl create -f mssql.yaml && wait
    local MSSQL_NODE_PORT=$(kubectl get svc mssql -o=jsonpath='{.spec.ports[?(@.port==1433)].nodePort}')
    sqlcmd -S $(minikube ip),MSSQL_NODE_PORT -U sa -P YOUR_PASSWORD_123_abcd -i create_db.sql
    kubectl create -f kafka.yaml && wait
    kubectl create -f schema-registry.yaml && wait
    kubectl create configmap hive-env --from-env-file hive.env --dry-run -o yaml | kubectl apply -f -
    kubectl create -f hdfs.yaml && wait
    kubectl create -f metastore.yaml && wait
    kubectl create -f presto.yaml && wait
}

function delete() {
    kubectl delete -f ./
}

cd "$(dirname "$0")"
while [ $# -gt 0 ]; do
    case "$1" in
        --serve-jar)
            serve_jar_directory
            ;;
        --spark-init)
            init_spark_operator
            ;;
        --spark-drop)
            drop_spark_operator
            ;;
        --create)
            create
            ;;
        --delete)
            delete
            ;;
        -*)
            # do not exit out, just note failure
            echo 1>&2 "unrecognized option: $1"
            ;;
        *)
            break;
            ;;
    esac
    shift
done
