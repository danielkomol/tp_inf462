# ==============================================
# CONNEXION ELASTICSEARCH — Reporting Service
# ==============================================
# Le reporting-service LIT les mêmes données que
# l'audit-service (logs Elasticsearch) mais les
# transforme en statistiques et rapports exploitables.
# Principe CQRS : audit-service écrit, reporting-service lit/analyse.

import os
from elasticsearch import Elasticsearch

ELASTICSEARCH_URL = os.getenv("ELASTICSEARCH_URL", "http://elasticsearch:9200")
INDEX_AUDIT = "audit-logs"

es_client = Elasticsearch(ELASTICSEARCH_URL)
