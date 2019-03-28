package it_tests.utils

import java.io.{File, FileInputStream}
import java.util.concurrent.TimeUnit

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.internal.KubernetesDeserializer

class SparkController(crdNamespace: String, resourceName: String) {
  val k8sClient = new DefaultKubernetesClient()
  KubernetesDeserializer.registerCustomKind("sparkoperator.k8s.io/v1beta1", "SparkApplication", classOf[CustomObject])

  import com.fasterxml.jackson.module.scala.DefaultScalaModule
  import io.fabric8.kubernetes.client.dsl.base.OperationSupport

  object ScalaSupportOperationHook extends OperationSupport {
    OperationSupport.JSON_MAPPER.registerModule(DefaultScalaModule)
    OperationSupport.YAML_MAPPER.registerModule(DefaultScalaModule)

    def log() = {
      pprint.pprintln(OperationSupport.YAML_MAPPER.readValue(convertYamlToJson(resourceName), classOf[CustomObject]).spec)
    }
  }


  ScalaSupportOperationHook.log()

  private val loaded = k8sClient.load(convertYamlToJson(resourceName)).get()

  println("LLLLLL", loaded)

  launchSparkTestDeployment()

  def launchSparkTestDeployment(): Unit = {
    val l = k8sClient.resourceList(loaded)
    l.createOrReplace()
    l.waitUntilReady(10, TimeUnit.SECONDS)
    println("LLLLLL", l)
    println("CRD is ready")
  }

  def cleanUpSparkTestDeployment(): Unit = {
//    loaded.delete()
    println("Spark CRD deleted")
  }

  private def convertYamlToJson(resourceName: String): FileInputStream = {
    new FileInputStream(new File(resourceName))
  }
}
