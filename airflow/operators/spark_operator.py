import kubernetes.config as kube_config
from kubernetes.client import ApiClient, CustomObjectsApi

from kubernetes.watch import Watch

from airflow.exceptions import AirflowException
from airflow.models import BaseOperator

from airflow.utils.decorators import apply_defaults

from utils.helpers import yaml_2_json


class SparkJobOperator(BaseOperator):

    template_fields = ('namespace', 'job_name')

    @apply_defaults
    def __init__(self, namespace, job_name, yml_file, timeout, *args, **kwargs):
        """
        :param yaml_file: str, file obj
        :param timeout: int as seconds
        """
        super(SparkJobOperator, self).__init__(*args, **kwargs)
        self.namespace = namespace
        self.job_name = job_name
        self.crd_file = yml_file
        self.timeout = timeout
        # TODO check later when new version will be available
        self.group = 'sparkoperator.k8s.io'
        self.version = 'v1beta1'
        self.plural = 'sparkapplications'

    def execute(self, context):
        # TODO check later with prod kube
        config = kube_config.load_kube_config()
        # create an instance of the API class
        api_instance = CustomObjectsApi(ApiClient(config))
        # params to create custom object
        params = [self.group, self.version, self.namespace, self.plural]
        crd_created = self.create_custom_definition(api_instance, *params)
        if crd_created:
            w = Watch()
            for event in w.stream(api_instance.list_namespaced_custom_object, *params, timeout_seconds=self.timeout):
                job_name = event.get('object', {}).get('metadata', {}).get('name')
                job_state = event.get('object', {}).get('status', {}).get('applicationState', {}).get('state')
                if job_name == self.job_name and job_state == "COMPLETED":
                    break

    def create_custom_definition(self, kube_api_instance, *params):
        crd_body = yaml_2_json(self.crd_file)
        crd_body['metadata']['name'] = self.job_name
        try:
            success = True
            kube_api_instance.create_namespaced_custom_object(*params, crd_body, pretty=False)
        except AirflowException as e:
            success = False
            print("Exception when calling CustomObjectsApi -> create_namespaced_custom_object: %s\n" % e)
        return success
