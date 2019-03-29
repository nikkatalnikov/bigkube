import os
import time

from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.bash_operator import BashOperator

from operators.spark_operator import SparkJobOperator


default_args = {
    'owner': 'viktor',
    'depends_on_past': False,
    'start_date': datetime.today() - timedelta(days=1),
    'email': ['airflow@example.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 3,
    'retry_delay': timedelta(minutes=5)
}

dag = DAG(
    dag_id='spark_dag',
    default_args=default_args,
    schedule_interval=timedelta(minutes=720)
)

t1 = BashOperator(
    task_id='print_date',
    bash_command='date',
    dag=dag
)

test_job = SparkJobOperator(
    task_id='k8s_spark',
    namespace='default',
    job_name='spark-pi-{}'.format(int(time.time())),
    yml_file='{}/jobs/spark_example.yml'.format(os.path.abspath('.')),
    timeout=300,
    dag=dag
)

t1 << test_job
