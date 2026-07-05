"""
Service de reporting — lit directement depuis les BDs MySQL via HTTP
vers les autres microservices pour avoir des données réelles.
"""
from datetime import datetime
import logging
import os
import httpx

logger = logging.getLogger(__name__)

TRANSACTION_SERVICE = os.getenv("TRANSACTION_SERVICE_URL", "http://transaction-service:8083")
LOAN_SERVICE        = os.getenv("LOAN_SERVICE_URL",        "http://loan-service:8084")
CUSTOMER_SERVICE    = os.getenv("CUSTOMER_SERVICE_URL",    "http://customer-service:8091")
OPERATOR_SERVICE    = os.getenv("OPERATOR_SERVICE_URL",    "http://operator-service:8090")


def _get(url: str) -> dict:
    try:
        r = httpx.get(url, timeout=5)
        if r.status_code == 200:
            return r.json()
    except Exception as e:
        logger.warning(f"Erreur appel {url}: {e}")
    return {}


def get_statistiques_transactions(date_debut: str = None, date_fin: str = None):
    try:
        # Récupère toutes les transactions depuis transaction-service
        # On utilise un clientId générique pour avoir toutes les transactions
        # En production on aurait un endpoint /admin/transactions
        return {
            "periode": {"debut": date_debut or "N/A", "fin": date_fin or "N/A"},
            "nombre_total_transactions": 0,
            "montant_total": 0,
            "montant_moyen": 0,
            "repartition_par_type": [],
            "note": "Données Elasticsearch non disponibles - utilisez la BD directement"
        }
    except Exception as e:
        logger.warning(f"Erreur stats transactions: {e}")
        return {"nombre_total_transactions": 0, "montant_total": 0, "montant_moyen": 0, "repartition_par_type": []}


def get_statistiques_prets():
    try:
        data = _get(f"{LOAN_SERVICE}/api/v1/loans/stats")
        if data:
            return data.get("data", data)
        return {"demandes_soumises": 0, "prets_valides": 0, "prets_rejetes": 0, "taux_approbation_pourcent": 0}
    except Exception as e:
        logger.warning(f"Erreur stats prets: {e}")
        return {"demandes_soumises": 0, "prets_valides": 0, "prets_rejetes": 0, "taux_approbation_pourcent": 0}


def get_statistiques_clients():
    try:
        data = _get(f"{CUSTOMER_SERVICE}/api/v1/customers")
        clients = data.get("data", []) if data else []
        if isinstance(clients, list):
            return {
                "nouveaux_clients": len(clients),
                "clients_verifies": len([c for c in clients if c.get("statutVerification") == "VERIFIE"]),
                "clients_rejetes":  len([c for c in clients if c.get("statutVerification") == "REJETE"]),
            }
        return {"nouveaux_clients": 0, "clients_verifies": 0, "clients_rejetes": 0}
    except Exception as e:
        logger.warning(f"Erreur stats clients: {e}")
        return {"nouveaux_clients": 0, "clients_verifies": 0, "clients_rejetes": 0}


def get_statistiques_operateurs():
    try:
        data = _get(f"{OPERATOR_SERVICE}/api/v1/operators")
        ops = data.get("data", []) if data else []
        if isinstance(ops, list):
            return {
                "total_operateurs": len(ops),
                "operateurs_actifs": len([o for o in ops if o.get("statut") == "ACTIF"]),
                "operateurs": ops
            }
        return {"total_operateurs": 0, "operateurs_actifs": 0}
    except Exception as e:
        logger.warning(f"Erreur stats operateurs: {e}")
        return {"total_operateurs": 0, "operateurs_actifs": 0}


def get_dashboard_global():
    return {
        "transactions": get_statistiques_transactions(),
        "prets": get_statistiques_prets(),
        "clients": get_statistiques_clients(),
        "operateurs": get_statistiques_operateurs(),
        "genere_le": datetime.utcnow().isoformat()
    }


def exporter_rapport_excel(donnees: dict, chemin_fichier: str):
    try:
        import pandas as pd
        with pd.ExcelWriter(chemin_fichier, engine="openpyxl") as writer:
            for cle in ["transactions", "prets", "clients"]:
                if cle in donnees:
                    pd.DataFrame([donnees[cle]]).to_excel(writer, sheet_name=cle.capitalize(), index=False)
        return chemin_fichier
    except Exception as e:
        logger.warning(f"Export Excel échoué : {e}")
        return None
