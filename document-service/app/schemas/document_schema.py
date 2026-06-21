# ==============================================
# SCHÉMAS PYDANTIC — Validation des données
# ==============================================
# Pydantic = équivalent des DTOs en Java
# Valide automatiquement les données entrantes/sortantes

from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class DocumentResponse(BaseModel):
    """
    Schéma de réponse pour un document
    """
    id: int
    client_id: str
    type_document: str
    nom_original: str
    statut: str
    donnees_extraites: Optional[str] = None
    niveau_confiance: Optional[float] = None
    erreur: Optional[str] = None
    created_at: datetime

    class Config:
        # Permet de créer ce schéma depuis un objet SQLAlchemy
        from_attributes = True


class ApiResponse(BaseModel):
    """
    Réponse API générique
    """
    success: bool
    message: str
    data: Optional[dict] = None
