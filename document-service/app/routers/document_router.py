# ==============================================
# ROUTEUR DOCUMENTS — Endpoints REST
# ==============================================
# Équivalent du @RestController en Java

from fastapi import APIRouter, UploadFile, File, Form, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.services import document_service
from app.schemas.document_schema import DocumentResponse, ApiResponse

router = APIRouter(prefix="/api/v1/documents", tags=["Documents"])


@router.post("/upload", response_model=ApiResponse)
async def upload_document(
    client_id: str = Form(...),
    type_document: str = Form(...),
    fichier: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    """
    Upload d'un document (CNI, passeport, bulletin de salaire...)
    Déclenche automatiquement le traitement OCR
    """
    try:
        # 1. Sauvegarder le fichier physique
        chemin = await document_service.sauvegarder_fichier(fichier, client_id, type_document)

        # 2. Créer l'enregistrement en base + publier événement Kafka
        document = document_service.creer_document(
            db, client_id, type_document, chemin, fichier.filename
        )

        return ApiResponse(
            success=True,
            message="Document uploadé avec succès, traitement OCR en cours",
            data={"documentId": document.id, "statut": document.statut}
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/{document_id}", response_model=DocumentResponse)
def get_document(document_id: int, db: Session = Depends(get_db)):
    """Consulter un document par son ID"""
    try:
        return document_service.get_document(db, document_id)
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.get("/client/{client_id}", response_model=list[DocumentResponse])
def get_documents_client(client_id: str, db: Session = Depends(get_db)):
    """Lister tous les documents d'un client"""
    return document_service.get_documents_client(db, client_id)
