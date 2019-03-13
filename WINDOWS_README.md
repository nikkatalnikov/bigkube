<p align="center">
    <img src="assets/bigkube.svg" width="200">
</p>

## Bigkube - effortless Spark applications deployment and testing in minikube.

----

Bigkube is about big data and Spark local development automation - automated deployments, well fitted components, integration testing with SBT right from IDE console.

----
    
####  Prerequisites
0. Run ``powershell`` as Administrator.
1. Install [virtualbox](https://docs.docker.com/install/)
1. Install [Docker for windows]()
1. Turn of Hyper-V. ``````
2. Install [choco](https://chocolatey.org/)
5. Install [Minikube](https://chocolatey.org/packages/Minikube)
6. Install [kubernetes-ctl](https://chocolatey.org/packages/kubernetes-cli)
7. Install [Helm](https://chocolatey.org/packages/kubernetes-helm)
8. Install [SBT](https://chocolatey.org/packages/sbt)
9. Install [JDK](https://stackoverflow.com/questions/52511778/how-to-install-openjdk-11-on-windows)
10. Install [SBT](https://chocolatey.org/packages/sbt)
11. Make sure SBT version is not less than 1.2.8 and there's Scala 2.11 sdk is set for the project

#### Before deployment 

1. Run ```sbt assembly``` repo's base dir.
2. Make sure ```minikube start --cpus=4 --memory=8192 --mount``` flags set.

#### Deployment steps

1. ```cd deployment``` - DON'T SKIP THIS
2. ```.\bigkube.ps1 serve-jar``` - read output instructions carefully, ensure jar serving host ip
is substituted according instructions
3. Modify `Kafka.yaml` , change external kafka listener to `minikube ip`
4. ```.\bigkube.ps1 create``` - creates all necessary infrastructure. Troubleshooting: don't worry if some pods are "red" right after deployment. All Hadoop deployments are smart enough to wait for each other to be stand by in appropriate sequence. Just give them some time.  
5. ```.\bigkube.ps1 spark-init``` - inits Helm (with Tiller) and Spark operator.

Note: `bigkube.ps1` resolves all service accounts issues, secrets, config maps and etc.

#### Run integration tests
0.Don't forget ``cd ..``. Come back to sbt folder.
1. Write your own integration test using ```SparkController```. Examples provided.
2. Change minkube ip and mssql, presto ports in ``it.conf``.
3. Simply run ```sbt it:test``` from repo's base dir - and that's it, your Spark app is deployed into minikube and tests are executed locally on your host machine.

#### GUI
1. ```minikube dashboard```
2. Run ```minikube service list```. You can go to ```namenode``` and ```presto``` UI with corresponding URLs.
3. One can use [Metabase](https://www.metabase.com/start/), an open source tool for rapid access data. Works with Presto and SQLServer as well. 

#### Delete deployments

Alongside with ```kubectl delete -f file.yaml``` you can use:
1. ```.\bigkube.ps1 delete``` - deletes all bigkube infrastructure
2. ```.\bigkube.ps1 spark-drop``` - deletes helmed spark operator 


#### Acknowledgments

Thanks to [Nik Katalnikov](https://github.com/nikkatalnikov) for linux & macos implementation.
Thanks to [Big Data Europe](https://github.com/big-data-europe) for Hadoop Docker images.
Thanks to [Valeira Katalnikova](mailto:lelia.katalnikova@icloud.com) for Bigkube logo.