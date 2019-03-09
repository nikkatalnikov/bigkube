<p align="center">
    <img src="assets/bigkube.svg" width="200">
</p>

## Bigkube - effortless Spark applications deployment and testing in minikube.

----

Bigkube is about big data and Spark local development automation - automated deployments, well fitted components, integration testing with SBT right from IDE console.

----

####  Prerequisites

1. Install [Docker](https://docs.docker.com/install/)
2. Install [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)
3. Get [Helm](https://github.com/helm/helm#install)
4. Get [Scala](https://www.scala-lang.org/download/) and [SBT](https://www.scala-sbt.org/1.0/docs/Setup.html)
5. Make sure SBT version is not less tan 1.2.8 and there's Scala 2.11 sdk is set for the project

#### Before deployment 

1. Make sure minikube has --cpus=4 --memory=8192 flags set. Make sure the appropriate vm-driver is set (use ```minikube config set```).
2. Run ```sbt assmbly``` repo's base dir. 

#### Deployment steps

1. ```cd deployment``` - DON'T SKIP THIS
2. ```./bigkube.sh --serve-jar``` - read output instructions carefully, ensure jar serving host ip
is substituted according instructions
3. ```./bigkube.sh --create``` - creates all necessary infrastructure. Troubleshooting: don't worry if some pods are "red" right after deployment. All Hadoop deployments are smart enough to wait for each other to be stand by in appropriate sequence. Just give them some time.  
4. ```./bigkube.sh --spark-init``` - inits Helm (with Tiller) and Spark operator.

Note: bigkube.sh resolves all service accounts issues, secrets, config maps and etc.

#### Run integration tests

1. Write your own integration test using ```SparkController```. Examples provided.
2. Simply run ```sbt it:test``` from repo's base dir - and that's it, your Spark app is deployed into minikube and tests are executed locally on your host machine.

#### GUI
1. ```minikube dashboard```
2. Run ```minikube services list```. You can go to ```namenode``` and ```presto``` UI with corresponding URLs.
3. One can use [Metabase](https://www.metabase.com/start/), an open source tool for rapid access data. Works with Presto and SQLServer as well. 

#### Delete deployments

Alongside with ```kubectl delete -f file.yaml``` you can use:
1. ```./bigkube.sh --delete``` - deletes all bigkube infrastructure
2. ```./bigkube.sh --spark-drop``` - deletes helmed spark operator 


#### Thanks

Thanks to [Nick Grigoriev](https://github.com/GrigorievNick) for the idea and help.
Thanks to [Big Data Europe](https://github.com/big-data-europe) for Hadoop Docker images.