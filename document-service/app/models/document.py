# ==============================================
# MODÈLE DOCUMENT — SQLAlchemy
# ==============================================
# Équivalent de @Entity en Java

from sqlalchemy import Column, Integer, String, DateTime, Text, Float
from sqlalchemy.sql import func
from app.database import Base


class Document(Base):
    """
    Représente un document soumis par un client
    (CNI, passeport, bulletin de salaire, etc.)
    """
    __tablename__ = "documents"

    id = Column(Integer, primary_key=True, index=True)

    # ID du client propriétaire du document
    client_id = Column(String, nullable=False, index=True)

    # Type de document
    # CNI, PASSEPORT, JUSTIFICATIF_DOMICILE, BULLETIN_SALAIRE, etc.
    type_document = Column(String, nullable=False)

    # Chemin du fichier stocké
    chemin_fichier = Column(String, nullable=False)

    # Nom original du fichier
    nom_original = Column(String, nullable=False)

    # Statut du traitement
    # EN_ATTENTE, EN_COURS_OCR, TRAITE, INVALIDE
    statut = Column(String, default="EN_ATTENTE", nullable=False)

    # Données extraites par l'OCR (stockées en JSON texte)
    donnees_extraites = Column(Text, nullable=True)

    # Niveau de confiance de l'extraction OCR (0-100)
    niveau_confiance = Column(Float, nullable=True)

    # Message d'erreur si le traitement échoue
    erreur = Column(Text, nullable=True)

    # Dates automatiques
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
