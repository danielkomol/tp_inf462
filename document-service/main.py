# ==============================================
# POINT D'ENTRÉE — document-service (FastAPI)
# ==============================================
# FastAPI génère automatiquement la doc Swagger
# accessible sur http://localhost:8085/docs

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine, Base
from app.routers import document_router
from app.services.ocr_consumer import demarrer_consumer_en_arriere_plan

# Créer les tables en base de données automatiquement
Base.metadata.create_all(bind=engine)

# Créer l'application FastAPI
app = FastAPI(
    title="Document Service",
    description="Service de gestion documentaire — Plateforme Bancaire INF462",
    version="1.0.0"
)

# Autoriser les requêtes cross-origin (CORS)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Enregistrer les routes
app.include_router(document_router.router)


@app.on_event("startup")
def demarrer_consumer():
    """
    Démarre le consommateur Kafka au lancement.
    Écoute "document.ocr.done" pour mettre à jour
    les documents avec leur résultat OCR.
    """
    demarrer_consumer_en_arriere_plan()


@app.get("/health")
def health_check():
    """Health check pour Kubernetes"""
    return {"status": "UP", "service": "document-service"}


@app.get("/")
def root():
    return {
        "service": "Document Service",
        "version": "1.0.0",
        "docs": "/docs"
    }
