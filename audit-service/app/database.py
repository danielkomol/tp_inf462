# ==============================================
# CONNEXION ELASTICSEARCH
# ==============================================
# Elasticsearch est utilisé pour stocker les logs
# d'audit car il est optimisé pour la RECHERCHE
# rapide dans de gros volumes de données.
# Contrairement à PostgreSQL, pas besoin de schéma
# fixe — chaque log peut avoir une structure différente.

import os
from elasticsearch import Elasticsearch

ELASTICSEARCH_URL = os.getenv("ELASTICSEARCH_URL", "http://elasticsearch:9200")

# Index = équivalent d'une "table" en Elasticsearch
INDEX_AUDIT = "audit-logs"

# Client Elasticsearch global
es_client = Elasticsearch(ELASTICSEARCH_URL)


def initialiser_index():
    """
    Crée l'index audit-logs s'il n'existe pas déjà.
    Définit le mapping (structure) des champs.
    """
    if not es_client.indices.exists(index=INDEX_AUDIT):
        es_client.indices.create(
            index=INDEX_AUDIT,
            mappings={
                "properties": {
                    "eventType": {"type": "keyword"},      # Type d'événement (exact match)
                    "service": {"type": "keyword"},          # Service source
                    "userId": {"type": "keyword"},
                    "description": {"type": "text"},         # Texte recherchable
                    "donnees": {"type": "object"},            # Données brutes (flexible)
                    "timestamp": {"type": "date"},
                    "ipAddress": {"type": "ip"}
                }
            }
        )
        print(f"✅ Index Elasticsearch '{INDEX_AUDIT}' créé")
    else:
        print(f"✅ Index Elasticsearch '{INDEX_AUDIT}' déjà existant")
