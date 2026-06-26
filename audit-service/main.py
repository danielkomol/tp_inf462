# ==============================================
# POINT D'ENTRÉE — Audit Service
# ==============================================
# Service de traçabilité : écoute TOUS les événements
# de la plateforme et les enregistre de façon immuable
# dans Elasticsearch.

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.database import initialiser_index
from app.routers.audit_router import router as audit_router
from app.services.kafka_consumer import demarrer_consumer_en_arriere_plan

app = FastAPI(
    title="Audit Service — Plateforme Bancaire INF462",
    description="Service de traçabilité et journalisation immuable de tous les événements de la plateforme",
    version="1.0.0",
    docs_url="/swagger-ui.html"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(audit_router)


@app.on_event("startup")
def demarrage():
    """
    Au démarrage du service :
    1. Initialise l'index Elasticsearch
    2. Démarre l'écoute de TOUS les événements Kafka
    """
    initialiser_index()
    demarrer_consumer_en_arriere_plan()


@app.get("/")
def racine():
    return {
        "service": "Audit Service",
        "version": "1.0.0",
        "description": "Traçabilité complète de la plateforme bancaire"
    }


@app.get("/health")
def health_check():
    return {"status": "UP", "service": "audit-service"}
