from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.bash_operator import BashOperator


default_args = {
    'owner': 'viktor',
    'depends_on_past': False,
    'start_date': datetime(2019, 2, 1),
    'email': ['airflow@example.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 3,
    'retry_delay': timedelta(minutes=5)
}

dag = DAG('third_dag',
          default_args=default_args,
          schedule_interval=timedelta(minutes=3))

t1 = BashOperator(
    task_id='print_date',
    bash_command='date',
    dag=dag)

t2 = BashOperator(
    task_id='k8s_sync_print',
    bash_command="echo 'HELLO Airflow operator!'",
    dag=dag
)

t2.set_upstream(t1)
