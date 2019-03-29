from flask import Response
from flask_admin import BaseView, expose

from prometheus_client import generate_latest

# Views for Flask App Builder
appbuilder_views = []
try:
    from flask_appbuilder import BaseView as FABBaseView, expose as FABexpose

    class RBACMetrics(FABBaseView):
        route_base = "/admin/metrics/"

        @FABexpose('/')
        def list(self):
            return Response(generate_latest(), mimetype='text')

    # Metrics View for Flask app builder used in airflow with rbac enabled
    RBACmetricsView = {
        "view": RBACMetrics(),
        "name": "metrics",
        "category": "Prometheus exporter"
    }
    appbuilder_views = [RBACmetricsView]

except ImportError:
    pass


class Metrics(BaseView):
    @expose('/')
    def index(self):
        return Response(generate_latest(), mimetype='text/plain')

ADMIN_VIEW = Metrics(category="Prometheus exporter", name="metrics")
