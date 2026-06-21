"""
POINT D'ENTRÉE — OCR Service
==============================
Service d'intelligence artificielle pour l'extraction
automatique d'informations depuis des documents scannés.

Démarrage : uvicorn main:app --host 0.0.0.0 --port 8000
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.openapi.utils import get_openapi

from routers.ocr_router import router as ocr_router
from services.kafka_consumer import demarrer_consumer_en_arriere_plan

# Créer l'application FastAPI
# FastAPI génère AUTOMATIQUEMENT la documentation Swagger !
app = FastAPI(
    title="OCR Service — Plateforme Bancaire INF462",
    description="Service de reconnaissance optique de caractères (OCR) pour l'extraction automatique d'informations depuis les documents clients",
    version="1.0.0",
    docs_url="/swagger-ui.html",  # URL personnalisée pour Swagger UI
    redoc_url="/redoc"
)

# Autoriser les requêtes cross-origin (depuis le frontend)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Inclure les routes OCR
app.include_router(ocr_router)


@app.on_event("startup")
def demarrer_consumer():
    """
    Démarre le consommateur Kafka au lancement du service.
    Il écoute "document.submitted" et traite chaque document
    automatiquement (event-driven OCR).
    """
    demarrer_consumer_en_arriere_plan()


@app.get("/")
async def racine():
    """Route racine — informations sur le service"""
    return {
        "service": "OCR Service",
        "version": "1.0.0",
        "description": "Extraction automatique d'informations par OCR",
        "documentation": "/swagger-ui.html"
    }


@app.get("/health")
async def health_check():
    """Health check pour Kubernetes"""
    return {"status": "UP", "service": "ocr-service"}
