#!/bin/bash

function wait() {
    while [ $(kubectl get pods | awk '{print $3}' | tail -n +2 | grep -v "Running\|Succeeded\|Completed" | wc -l) != 0 ]; do
        sleep 1
    done
}

function get_docker_network_interface_name() {
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        echo docker0
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo en0
    else
        echo docker0
    fi
}

function serve_jar_directory () {
    docker rm nginx-jar-server -f
    local PARENT_DIR=$(cd ../ && pwd)
    docker run --name nginx-jar-server -v ${PARENT_DIR}/target/scala-2.11:/usr/share/nginx/html:ro -d -p 8080:80 nginx
    echo "don't forget to pass following ip into your Spark CRD declaration to mainApplicationFile field:"
    ifconfig $(get_docker_network_interface_name) | awk '$1 == "inet" {print $2}'
}

function init_spark_operator() {
    kubectl create clusterrolebinding default --clusterrole=cluster-admin --serviceaccount=default:default --namespace=default
    helm init --wait &&
    helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator &&
    helm install incubator/sparkoperator --namespace spark-operator --set enableWebhook=true
}

function drop_spark_operator() {
    kubectl delete -f spark-prometheus.yaml
    helm delete $(helm list --namespace=spark-operator --short)
}

function create() {
    minikube ssh -- sudo ip link set docker0 promisc on
    kubectl create secret generic mssql-user --from-literal=user=sa
    kubectl create secret generic mssql-password --from-literal=password=YOUR_PASSWORD_123_abcd
    kubectl create -f mssql.yaml && wait
    kubectl create -f mssql-init-command.yaml && wait
    kubectl logs -f mssql-init-command
    kubectl delete -f mssql-init-command.yaml
    sed 's/$MINIKUBE_IP/'$(minikube ip)'/g' kafka.env > kafka.out.env
    kubectl create configmap kafka-env --from-env-file kafka.out.env
    kubectl create -f kafka.yaml && wait
    kubectl create -f schema-registry.yaml && wait
    kubectl create configmap hive-env --from-env-file hive.env --dry-run -o yaml | kubectl apply -f -
    kubectl create -f hdfs.yaml && wait
    local NAMENODE_POD=$(kubectl get pods -l hdfs=namenode -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}')
    kubectl exec ${NAMENODE_POD} /opt/hadoop-2.7.4/sbin/httpfs.sh start
    kubectl create -f metastore.yaml && wait
    minikube mount presto-etc:/presto-etc &
    kubectl create -f presto.yaml && wait
}

function delete() {
    kubectl delete -f ./
}

function build_airflow() {
    cd ../airflow
    docker build -f build/Dockerfile -t airflow-sandbox .
    docker tag airflow-sandbox localhost:5000/airflow-sandbox:0.1.0
    docker push localhost:5000/airflow-sandbox:0.1.0
    kubectl create -f ../deployment/postgres.yaml && wait
    kubectl create -f ../deployment/airflow.yaml && wait
}

cd "$(dirname "$0")"
while [[ $# -gt 0 ]]; do
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
