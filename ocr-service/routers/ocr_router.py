"""
ROUTEUR OCR — Endpoints REST
==============================
Expose les fonctionnalités OCR via une API REST FastAPI.
"""

from fastapi import APIRouter, UploadFile, File, HTTPException, Form
from typing import Optional
import shutil
import os
import uuid

from services.ocr_engine import extraire_texte, extraire_infos_cni, extraire_infos_bulletin_salaire

router = APIRouter(prefix="/api/v1/ocr", tags=["OCR"])

# Dossier temporaire pour stocker les images pendant le traitement
DOSSIER_TEMP = "/tmp/ocr_uploads"
os.makedirs(DOSSIER_TEMP, exist_ok=True)


@router.post("/extract")
async def extraire_document(
    file: UploadFile = File(..., description="Image du document à analyser"),
    type_document: Optional[str] = Form("AUTRE", description="Type: CNI, BULLETIN_SALAIRE, AUTRE"),
    langue: Optional[str] = Form("fra", description="Langue: fra ou eng")
):
    """
    ANALYSER UN DOCUMENT PAR OCR

    Reçoit une image, extrait le texte, et structure les
    informations selon le type de document.
    """
    # Vérifier que c'est bien une image
    if not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Le fichier doit être une image")

    # 1. Sauvegarder temporairement le fichier
    extension = file.filename.split(".")[-1]
    nom_fichier = f"{uuid.uuid4()}.{extension}"
    chemin_fichier = os.path.join(DOSSIER_TEMP, nom_fichier)

    try:
        with open(chemin_fichier, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)

        # 2. Extraire le texte brut avec Tesseract
        resultat_ocr = extraire_texte(chemin_fichier, langue)

        # 3. Structurer les données selon le type de document
        infos_structurees = {}
        if type_document == "CNI":
            infos_structurees = extraire_infos_cni(resultat_ocr["texte_brut"])
        elif type_document == "BULLETIN_SALAIRE":
            infos_structurees = extraire_infos_bulletin_salaire(resultat_ocr["texte_brut"])

        return {
            "success": True,
            "message": "Document analysé avec succès",
            "data": {
                "type_document": type_document,
                "texte_brut": resultat_ocr["texte_brut"],
                "niveau_confiance": resultat_ocr["niveau_confiance"],
                "nombre_mots_detectes": resultat_ocr["nombre_mots_detectes"],
                "informations_extraites": infos_structurees
            }
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur lors de l'analyse OCR : {str(e)}")

    finally:
        # 4. Nettoyer le fichier temporaire
        if os.path.exists(chemin_fichier):
            os.remove(chemin_fichier)


@router.get("/health")
async def health_check():
    """Vérification de l'état du service OCR"""
    return {"status": "UP", "service": "ocr-service"}
