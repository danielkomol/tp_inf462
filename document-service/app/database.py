# ==============================================
# CONFIGURATION BASE DE DONNÉES
# ==============================================
# SQLAlchemy = ORM Python (équivalent JPA en Java)

import os
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# URL de connexion PostgreSQL
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:postgres@db-documents:5432/documents_db"
)

# Créer le moteur de connexion
engine = create_engine(DATABASE_URL)

# Session locale pour chaque requête
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Classe de base pour tous les modèles
Base = declarative_base()


def get_db():
    """
    Fournit une session de base de données.
    Utilisé comme dépendance FastAPI (Depends(get_db))
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
