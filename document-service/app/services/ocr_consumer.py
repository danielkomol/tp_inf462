# ==============================================
# CONSOMMATEUR KAFKA — Résultats OCR
# ==============================================
# Écoute le topic "document.ocr.done" publié par
# l'ocr-service une fois l'extraction terminée,
# et met à jour le document correspondant.

import json
import threading
from kafka import KafkaConsumer
from app.database import SessionLocal
from app.services.document_service import mettre_a_jour_resultat_ocr
import os

KAFKA_BROKERS = os.getenv("KAFKA_BROKERS", "kafka:9092")


def ecouter_resultats_ocr():
    """
    Démarre l'écoute du topic Kafka "document.ocr.done"
    Tourne en boucle infinie dans un thread séparé
    """
    try:
        consumer = KafkaConsumer(
            "document.ocr.done",
            bootstrap_servers=KAFKA_BROKERS.split(","),
            group_id="document-service-group",
            value_deserializer=lambda v: json.loads(v.decode("utf-8")),
            auto_offset_reset="latest"
        )

        print("✅ Consommateur Kafka (document-service) démarré")

        for message in consumer:
            try:
                data = message.value
                print(f"📩 Résultat OCR reçu : {data}")

                document_id = data.get("documentId")
                donnees_extraites = json.dumps(data.get("donneesExtraites", {}))
                confiance = data.get("niveauConfiance", 0)

                # Mettre à jour le document en base
                db = SessionLocal()
                try:
                    mettre_a_jour_resultat_ocr(db, document_id, donnees_extraites, confiance)
                    print(f"✅ Document {document_id} mis à jour avec le résultat OCR")
                finally:
                    db.close()

            except Exception as e:
                print(f"❌ Erreur traitement message OCR : {e}")

    except Exception as e:
        print(f"❌ Erreur connexion Kafka consumer : {e}")


def demarrer_consumer_en_arriere_plan():
    """
    Lance le consommateur Kafka dans un thread séparé
    pour ne pas bloquer le serveur FastAPI
    """
    thread = threading.Thread(target=ecouter_resultats_ocr, daemon=True)
    thread.start()
