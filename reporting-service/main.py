# ==============================================
# POINT D'ENTRÉE — Reporting Service
# ==============================================
# Service de génération de rapports et statistiques
# pour la plateforme bancaire. Lit les données depuis
# Elasticsearch (alimenté par l'audit-service).

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers.reporting_router import router as reporting_router

app = FastAPI(
    title="Reporting Service — Plateforme Bancaire INF462",
    description="Service de génération de rapports et statistiques (dashboard, exports Excel)",
    version="1.0.0",
    docs_url="/swagger-ui.html"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(reporting_router)


@app.get("/")
def racine():
    return {
        "service": "Reporting Service",
        "version": "1.0.0",
        "description": "Statistiques et rapports — Plateforme Bancaire INF462"
    }


@app.get("/health")
def health_check():
    return {"status": "UP", "service": "reporting-service"}
