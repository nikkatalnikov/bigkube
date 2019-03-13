#!/usr/bin/env pwsh

param([String]$command = "Nothing") #Must be the first statement in your script
$ErrorActionPreference = "Stop"

function wait()
{
    while ((kubectl get pods | awk '{print $3}' | Select-String "!Running" | Measure-Object | Select-Object -expand Count) -ne 0)
    {
        Start-Sleep -s 1
    }
}

function serve_jar_directory()
{
    & minikube docker-env | Invoke-Expression
    docker rm nginx-jar-server -f
    $JAR_DIR="/$((Get-Item -Path ".").FullName.substring(0,1).ToLower())/" + "$((Get-Item -Path "./..").FullName)\target\scala-2.11".replace('\','/').replace(':','').substring(2)
    docker run --name nginx-jar-server -v $JAR_DIR":/usr/share/nginx/html:ro" -d -p 8080:80 nginx
    Write-Output "don't forget to pass following ip into your Spark CRD declaration to mainApplicationFile field:"
    minikube ip
}

function init_spark_operator() {
    kubectl create clusterrolebinding default --clusterrole=edit --serviceaccount=default:default --namespace=default
    helm init --wait
    helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator
    helm repo update
    if ($?) { helm install incubator/sparkoperator --namespace spark-operator --set enableWebhook=true }
}

function drop_spark_operator() {
    kubectl delete -f spark-prometheus.yaml
    helm delete $(helm list --namespace=spark-operator --short)
}

function create()
{
    kubectl create secret generic mssql-user --from-literal=user=sa
    kubectl create secret generic mssql-password --from-literal=password=YOUR_PASSWORD_123_abcd
    kubectl create configmap hive-env --from-env-file hive.env --dry-run -o yaml | kubectl apply -f -
    kubectl create -f mssql.yaml; if ($?) { wait }
    $MSSQL_NODE_PORT=$(kubectl get svc mssql -o=jsonpath='{.spec.ports[?(@.port==1433)].nodePort}')
    Invoke-Sqlcmd -ServerInstance "$(minikube ip),$MSSQL_NODE_PORT" -Username  sa -Password YOUR_PASSWORD_123_abcd -InputFile create_db.sql
    kubectl create -f kafka.yaml; if ($?) { wait }
    kubectl create -f hdfs.yaml; if ($?) { wait }
    kubectl create -f metastore.yaml; if ($?) { wait }
    kubectl create -f presto.yaml; if ($?) { wait }
}

function delete()
{
    kubectl delete -f ./
}

switch ( $command )
{
    serve-jar {
        serve_jar_directory;
    }
    spark-init {
        init_spark_operator;
    }
    spark-drop {
        drop_spark_operator;
    }
    create {
        create;
    }
    delete {
        delete;
    }
    default {
        # do not exit out, just note failure
        Write-Output "unrecognized option: $($command)"
    }
}
