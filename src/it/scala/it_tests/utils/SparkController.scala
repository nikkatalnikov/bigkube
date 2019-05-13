package it_tests.utils

import java.io.{File, FileInputStream}

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import io.fabric8.kubernetes.client.{DefaultKubernetesClient, KubernetesClient}

class SparkController(crdNamespace: String, resourceName: String) {
  val client: KubernetesClient = new DefaultKubernetesClient()
  private val crdInstanceName = "spark-pi"
  private val crdGroup = "sparkoperator.k8s.io"
  private val crdVersion = "v1beta1"
  private val crdPlural = "sparkapplications"
  private val crdScope = "Namespaced"
  private val crdPodName = "spark-pi-driver"

  private val resource = new FileInputStream(new File(resourceName))
  val crdContext: CustomResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
    .withGroup(crdGroup)
    .withName(crdInstanceName)
    .withScope(crdScope)
    .withPlural(crdPlural)
    .withVersion(crdVersion)
    .build()

  def launchSparkTestDeployment(): Unit = {
    val sparkCustomResource = client.customResource(crdContext)

    sparkCustomResource.create(crdNamespace, resource)
    Thread.sleep(30000)

    client.pods()
      .inNamespace(crdNamespace)
      .withName(crdPodName)
      .watchLog(System.out)
  }

  def cleanUpSparkTestDeployment(): Unit = {
    client.customResource(crdContext).delete(crdNamespace, crdInstanceName)
  }
}
