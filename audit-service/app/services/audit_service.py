from datetime import datetime
from app.database import es_client, INDEX_AUDIT
import logging

logger = logging.getLogger(__name__)


def enregistrer_log(event_type: str, service: str, user_id: str = None,
                    description: str = "", donnees: dict = None,
                    ip_address: str = None):
    try:
        document = {
            "eventType": event_type,
            "service": service,
            "userId": user_id,
            "description": description,
            "donnees": donnees or {},
            "timestamp": datetime.utcnow().isoformat(),
            "ipAddress": ip_address
        }
        resultat = es_client.index(index=INDEX_AUDIT, document=document)
        return resultat["_id"]
    except Exception as e:
        logger.warning(f"Elasticsearch indisponible, log ignoré : {e}")
        return None


def rechercher_logs(query: str = None, service: str = None,
                    user_id: str = None, taille: int = 50):
    try:
        must_clauses = []
        if query:
            must_clauses.append({"match": {"description": query}})
        if service:
            must_clauses.append({"term": {"service": service}})
        if user_id:
            must_clauses.append({"term": {"userId": user_id}})

        corps_requete = {
            "query": {"bool": {"must": must_clauses}} if must_clauses else {"match_all": {}},
            "sort": [{"timestamp": {"order": "desc"}}],
            "size": taille
        }

        resultat = es_client.search(index=INDEX_AUDIT, body=corps_requete)
        logs = [
            {**hit["_source"], "id": hit["_id"]}
            for hit in resultat["hits"]["hits"]
        ]
        return {"total": resultat["hits"]["total"]["value"], "logs": logs}
    except Exception as e:
        logger.warning(f"Elasticsearch indisponible : {e}")
        return {"total": 0, "logs": []}


def get_logs_utilisateur(user_id: str, taille: int = 50):
    return rechercher_logs(user_id=user_id, taille=taille)


def get_statistiques():
    try:
        corps_requete = {
            "size": 0,
            "aggs": {
                "par_type": {"terms": {"field": "eventType", "size": 20}},
                "par_service": {"terms": {"field": "service", "size": 20}}
            }
        }
        resultat = es_client.search(index=INDEX_AUDIT, body=corps_requete)
        return {
            "total_logs": resultat["hits"]["total"]["value"],
            "par_type_evenement": [
                {"type": b["key"], "count": b["doc_count"]}
                for b in resultat["aggregations"]["par_type"]["buckets"]
            ],
            "par_service": [
                {"service": b["key"], "count": b["doc_count"]}
                for b in resultat["aggregations"]["par_service"]["buckets"]
            ]
        }
    except Exception as e:
        logger.warning(f"Elasticsearch indisponible : {e}")
        return {"total_logs": 0, "par_type_evenement": [], "par_service": []}
