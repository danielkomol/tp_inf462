# ==============================================
# SERVICE DOCUMENT — Logique métier
# ==============================================

import os
import shutil
import uuid
from sqlalchemy.orm import Session
from app.models.document import Document
from app.services.kafka_service import publier_evenement

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)


async def sauvegarder_fichier(fichier, client_id: str, type_document: str) -> str:
    """
    Sauvegarde le fichier uploadé sur le disque
    Retourne le chemin du fichier sauvegardé
    """
    # Générer un nom unique pour éviter les collisions
    extension = fichier.filename.split(".")[-1]
    nom_fichier = f"{client_id}_{type_document}_{uuid.uuid4().hex[:8]}.{extension}"
    chemin = os.path.join(UPLOAD_DIR, nom_fichier)

    # Écrire le fichier sur le disque
    with open(chemin, "wb") as buffer:
        shutil.copyfileobj(fichier.file, buffer)

    return chemin


def creer_document(db: Session, client_id: str, type_document: str,
                    chemin_fichier: str, nom_original: str) -> Document:
    """
    Crée un enregistrement de document en base de données
    et publie un événement Kafka pour déclencher l'OCR
    """
    document = Document(
        client_id=client_id,
        type_document=type_document,
        chemin_fichier=chemin_fichier,
        nom_original=nom_original,
        statut="EN_ATTENTE"
    )

    db.add(document)
    db.commit()
    db.refresh(document)

    # Publier l'événement pour déclencher le traitement OCR
    publier_evenement(
        topic="document.submitted",
        key=client_id,
        data={
            "eventType": "DOCUMENT_SUBMITTED",
            "documentId": document.id,
            "clientId": client_id,
            "typeDocument": type_document,
            "cheminFichier": chemin_fichier
        }
    )

    return document


def get_document(db: Session, document_id: int) -> Document:
    """Récupère un document par son ID"""
    document = db.query(Document).filter(Document.id == document_id).first()
    if not document:
        raise ValueError(f"Document introuvable : {document_id}")
    return document


def get_documents_client(db: Session, client_id: str):
    """Récupère tous les documents d'un client"""
    return db.query(Document).filter(Document.client_id == client_id).all()


def mettre_a_jour_resultat_ocr(db: Session, document_id: int,
                                 donnees_extraites: str, confiance: float):
    """
    Met à jour un document avec le résultat de l'OCR
    Appelé quand on reçoit l'événement OCR_TERMINE depuis ocr-service
    """
    document = get_document(db, document_id)
    document.donnees_extraites = donnees_extraites
    document.niveau_confiance = confiance
    document.statut = "TRAITE" if confiance >= 70 else "INVALIDE"
    db.commit()
    db.refresh(document)
    return document
