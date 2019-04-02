import os
import time

from datetime import datetime

import kubernetes.config as kube_config
from kubernetes.client import ApiClient, CustomObjectsApi, V1DeleteOptions

from airflow import DAG
from unittest import TestCase

from airflow.models import TaskInstance
from operators.spark_operator import SparkJobOperator


class TestSparkOperator(TestCase):

    def setUp(self):
        self.namespace = "default"
        self.yml_file = "{}/files/spark.yml".format(os.path.abspath('.'))
        self.group = 'sparkoperator.k8s.io'
        self.version = 'v1beta1'
        self.plural = 'sparkapplications'
        self.job_name = "{}-{}".format("spark-test-job", int(time.time()))
        config = kube_config.load_kube_config()
        self.api_instance = CustomObjectsApi(ApiClient(config))

    def tearDown(self):
        self.api_instance.delete_namespaced_custom_object(
           group=self.group,
           version=self.version,
           namespace=self.namespace,
           plural=self.plural,
           name=self.job_name,
           body=V1DeleteOptions()
        )

    def test_execute(self):
        dag = DAG(dag_id='test_spark_operator',
                  start_date=datetime.now())
        task = SparkJobOperator(dag=dag,
                                task_id='test_k8s_spark_task',
                                namespace=self.namespace,
                                job_name=self.job_name,
                                yml_file=self.yml_file,
                                timeout=60
                                )
        ti = TaskInstance(task=task, execution_date=datetime.now())
        task.execute(ti.get_template_context())
