import unittest

from airflow.models import DagBag


class TestDagIntegrity(unittest.TestCase):

    LOAD_SECOND_THRESHOLD = 2

    def setUp(self):
        self.dagbag = DagBag()

    def test_import_dags(self):
        self.assertFalse(
            len(self.dagbag.import_errors),
            'DAG import failures. Errors: {}'.format(
                self.dagbag.import_errors
            )
        )

    def test_alert_email_present(self):
        for dag_id, dag in self.dagbag.dags.items():
            emails = dag.default_args.get('email', [])
            msg = 'Alert email not set for DAG: {id}'.format(id=dag_id)
            self.assertTrue(emails, msg)

suite = unittest.TestLoader().loadTestsFromTestCase(TestDagIntegrity)
unittest.TextTestRunner(verbosity=2).run(suite)
