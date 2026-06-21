# ==============================================
# ROUTEUR AUDIT — Endpoints REST
# ==============================================

from fastapi import APIRouter, Query
from typing import Optional
from app.services.audit_service import rechercher_logs, get_logs_utilisateur, get_statistiques

router = APIRouter(prefix="/api/v1/audit", tags=["Audit"])


@router.get("/logs")
def chercher_logs(
    query: Optional[str] = Query(None, description="Recherche textuelle"),
    service: Optional[str] = Query(None, description="Filtrer par service"),
    user_id: Optional[str] = Query(None, description="Filtrer par utilisateur"),
    taille: int = Query(50, description="Nombre max de résultats")
):
    """Rechercher dans les logs d'audit avec filtres"""
    resultat = rechercher_logs(query=query, service=service, user_id=user_id, taille=taille)
    return {
        "success": True,
        "message": f"{resultat['total']} log(s) trouvé(s)",
        "data": resultat
    }


@router.get("/logs/user/{user_id}")
def logs_utilisateur(user_id: str):
    """Historique complet des actions d'un utilisateur (traçabilité)"""
    resultat = get_logs_utilisateur(user_id)
    return {
        "success": True,
        "message": f"{resultat['total']} action(s) trouvée(s) pour cet utilisateur",
        "data": resultat
    }


@router.get("/stats")
def statistiques():
    """Statistiques globales d'audit (pour le dashboard admin)"""
    stats = get_statistiques()
    return {"success": True, "data": stats}


@router.get("/health")
def health_check():
    return {"status": "UP", "service": "audit-service"}
