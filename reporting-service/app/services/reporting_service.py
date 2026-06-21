# ==============================================
# SERVICE REPORTING — Logique métier
# ==============================================
# Génère des statistiques et rapports à partir
# des logs d'événements stockés dans Elasticsearch.

from datetime import datetime, timedelta
from app.database import es_client, INDEX_AUDIT
import pandas as pd


def get_statistiques_transactions(date_debut: str = None, date_fin: str = None):
    """
    STATISTIQUES DES TRANSACTIONS
    Volume, montants, répartition par type sur une période.
    """
    date_debut = date_debut or (datetime.utcnow() - timedelta(days=30)).isoformat()
    date_fin = date_fin or datetime.utcnow().isoformat()

    corps_requete = {
        "size": 0,
        "query": {
            "bool": {
                "must": [
                    {"term": {"eventType": "TRANSACTION_VALIDATED"}},
                    {"range": {"timestamp": {"gte": date_debut, "lte": date_fin}}}
                ]
            }
        },
        "aggs": {
            "par_type": {
                "terms": {"field": "donnees.type.keyword", "size": 10}
            },
            "montant_total": {
                "sum": {"field": "donnees.montant"}
            },
            "montant_moyen": {
                "avg": {"field": "donnees.montant"}
            }
        }
    }

    resultat = es_client.search(index=INDEX_AUDIT, body=corps_requete)

    return {
        "periode": {"debut": date_debut, "fin": date_fin},
        "nombre_total_transactions": resultat["hits"]["total"]["value"],
        "montant_total": resultat["aggregations"]["montant_total"]["value"] or 0,
        "montant_moyen": round(resultat["aggregations"]["montant_moyen"]["value"] or 0, 2),
        "repartition_par_type": [
            {"type": b["key"], "count": b["doc_count"]}
            for b in resultat["aggregations"]["par_type"]["buckets"]
        ]
    }


def get_statistiques_prets():
    """
    STATISTIQUES DES PRÊTS
    Taux d'approbation, montants accordés, répartition par statut.
    """
    corps_requete = {
        "size": 0,
        "query": {
            "terms": {"eventType": ["LOAN_SUBMITTED", "LOAN_VALIDATED", "LOAN_REJECTED"]}
        },
        "aggs": {
            "par_statut": {
                "terms": {"field": "eventType", "size": 10}
            }
        }
    }

    resultat = es_client.search(index=INDEX_AUDIT, body=corps_requete)

    buckets = {b["key"]: b["doc_count"] for b in resultat["aggregations"]["par_statut"]["buckets"]}

    soumis = buckets.get("LOAN_SUBMITTED", 0)
    valides = buckets.get("LOAN_VALIDATED", 0)
    rejetes = buckets.get("LOAN_REJECTED", 0)

    taux_approbation = round((valides / soumis * 100), 2) if soumis > 0 else 0

    return {
        "demandes_soumises": soumis,
        "prets_valides": valides,
        "prets_rejetes": rejetes,
        "taux_approbation_pourcent": taux_approbation
    }


def get_statistiques_clients():
    """
    STATISTIQUES CLIENTS
    Nouveaux clients, taux de vérification KYC.
    """
    corps_requete = {
        "size": 0,
        "query": {
            "terms": {"eventType": ["CUSTOMER_CREATED", "CUSTOMER_VERIFIED", "CUSTOMER_REJECTED"]}
        },
        "aggs": {
            "par_statut": {
                "terms": {"field": "eventType", "size": 10}
            }
        }
    }

    resultat = es_client.search(index=INDEX_AUDIT, body=corps_requete)
    buckets = {b["key"]: b["doc_count"] for b in resultat["aggregations"]["par_statut"]["buckets"]}

    return {
        "nouveaux_clients": buckets.get("CUSTOMER_CREATED", 0),
        "clients_verifies": buckets.get("CUSTOMER_VERIFIED", 0),
        "clients_rejetes": buckets.get("CUSTOMER_REJECTED", 0)
    }


def get_dashboard_global():
    """
    DASHBOARD GLOBAL — Vue d'ensemble pour les administrateurs
    Combine toutes les statistiques en un seul appel.
    """
    return {
        "transactions": get_statistiques_transactions(),
        "prets": get_statistiques_prets(),
        "clients": get_statistiques_clients(),
        "genere_le": datetime.utcnow().isoformat()
    }


def exporter_rapport_excel(donnees: dict, chemin_fichier: str):
    """
    EXPORTER UN RAPPORT EN EXCEL
    Utilise pandas pour transformer les données en fichier .xlsx
    """
    with pd.ExcelWriter(chemin_fichier, engine="openpyxl") as writer:
        # Feuille Transactions
        if "transactions" in donnees:
            df_transactions = pd.DataFrame([donnees["transactions"]])
            df_transactions.to_excel(writer, sheet_name="Transactions", index=False)

        # Feuille Prêts
        if "prets" in donnees:
            df_prets = pd.DataFrame([donnees["prets"]])
            df_prets.to_excel(writer, sheet_name="Prets", index=False)

        # Feuille Clients
        if "clients" in donnees:
            df_clients = pd.DataFrame([donnees["clients"]])
            df_clients.to_excel(writer, sheet_name="Clients", index=False)

    return chemin_fichier
