# ==============================================
# ROUTEUR REPORTING — Endpoints REST
# ==============================================

from fastapi import APIRouter, Query
from fastapi.responses import FileResponse
from typing import Optional
import os
import uuid

from app.services.reporting_service import (
    get_statistiques_transactions,
    get_statistiques_prets,
    get_statistiques_clients,
    get_dashboard_global,
    exporter_rapport_excel
)

router = APIRouter(prefix="/api/v1/reports", tags=["Reporting"])

DOSSIER_EXPORTS = "/tmp/rapports"
os.makedirs(DOSSIER_EXPORTS, exist_ok=True)


@router.get("/dashboard")
def dashboard():
    """Dashboard global — vue d'ensemble pour les administrateurs"""
    return {"success": True, "data": get_dashboard_global()}


@router.get("/transactions")
def stats_transactions(
    date_debut: Optional[str] = Query(None),
    date_fin: Optional[str] = Query(None)
):
    """Statistiques détaillées des transactions sur une période"""
    stats = get_statistiques_transactions(date_debut, date_fin)
    return {"success": True, "data": stats}


@router.get("/loans")
def stats_prets():
    """Statistiques des prêts — taux d'approbation, volumes"""
    return {"success": True, "data": get_statistiques_prets()}


@router.get("/customers")
def stats_clients():
    """Statistiques clients — acquisition, vérification KYC"""
    return {"success": True, "data": get_statistiques_clients()}


@router.get("/export/excel")
def exporter_excel():
    """
    EXPORTER LE DASHBOARD COMPLET EN EXCEL
    Génère un fichier .xlsx téléchargeable
    """
    donnees = get_dashboard_global()
    nom_fichier = f"rapport_{uuid.uuid4().hex[:8]}.xlsx"
    chemin = os.path.join(DOSSIER_EXPORTS, nom_fichier)

    exporter_rapport_excel(donnees, chemin)

    return FileResponse(
        path=chemin,
        filename="rapport_plateforme_bancaire.xlsx",
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )


@router.get("/health")
def health_check():
    return {"status": "UP", "service": "reporting-service"}
