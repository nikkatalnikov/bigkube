import logging
import pprint

from datetime import timedelta

from airflow import DAG

from airflow.operators.bash_operator import BashOperator
from airflow.operators.python_operator import PythonOperator

from airflow.utils.dates import days_ago


default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': days_ago(1),
    'email': ['test.alert@gmail.com'],
    'email_on_failure': 'viktor.pecheniuk@gmail.com',
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG('my_second_dag', default_args=default_args,  schedule_interval=timedelta(minutes=2))


def print_context(ds, **kwargs):
    logging.info("Here first arg >>> {}".format(ds))
    logging.info(pprint.pprint(kwargs))
    return "Done"

with dag:
    t1 = BashOperator(
        task_id='bash_example',
        bash_command='echo 1',
        dag=dag)

    t2 = PythonOperator(
        task_id='print_the_context',
        provide_context=True,
        python_callable=print_context,
        dag=dag)

    t3 = BashOperator(
    task_id='also_run_this',
    bash_command='echo "run_id={{ run_id }} | dag_run={{ dag_run }}"',
    dag=dag)

t2 >> t3 >> t1
