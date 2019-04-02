import unittest

from airflow.models import DagBag


class TestMySecondDAG(unittest.TestCase):
    """Check DAG expectation"""

    def setUp(self):
        self.dagbag = DagBag()

    def test_task_count(self):
        """Check task count of my_second_dag dag"""
        dag_id = 'my_second_dag'
        dag = self.dagbag.get_dag(dag_id)
        self.assertEqual(len(dag.tasks), 3)

    def test_contain_tasks(self):
        """Check task contains in my_second_dag dag"""
        dag_id = 'my_second_dag'
        dag = self.dagbag.get_dag(dag_id)
        tasks = dag.tasks
        task_ids = list(map(lambda task: task.task_id, tasks))
        self.assertListEqual(task_ids, ['bash_example', 'print_the_context', 'also_run_this'])

    def test_dependencies_of_dummy_task(self):
        """Check the task dependencies of `bash_example` task in `my_second_dag` dag"""
        dag_id = 'my_second_dag'
        dag = self.dagbag.get_dag(dag_id)
        bash_task = dag.get_task('bash_example')

        upstream_task_ids = list(map(lambda task: task.task_id, bash_task.upstream_list))
        self.assertListEqual(upstream_task_ids, ['also_run_this'])
        downstream_task_ids = list(map(lambda task: task.task_id, bash_task.downstream_list))
        self.assertListEqual(downstream_task_ids, [])

    def test_dependencies_of_hello_task(self):
        """Check the task dependencies of `also_run_this` task in `my_second_dag` dag"""
        dag_id = 'my_second_dag'
        dag = self.dagbag.get_dag(dag_id)
        also_run_this_task = dag.get_task('also_run_this')

        upstream_task_ids = list(map(lambda task: task.task_id, also_run_this_task.upstream_list))
        self.assertListEqual(upstream_task_ids, ['print_the_context'])
        downstream_task_ids = list(map(lambda task: task.task_id, also_run_this_task.downstream_list))
        self.assertListEqual(downstream_task_ids, ['bash_example'])

suite = unittest.TestLoader().loadTestsFromTestCase(TestMySecondDAG)
unittest.TextTestRunner(verbosity=2).run(suite)
