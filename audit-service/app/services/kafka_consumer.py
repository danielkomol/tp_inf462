# ==============================================
# CONSOMMATEUR KAFKA — Audit Service
# ==============================================
# L'audit-service ÉCOUTE TOUS LES TOPICS de tous
# les autres services pour tout tracer.
# C'est le seul service qui s'abonne à TOUT.

import json
import os
import threading
from kafka import KafkaConsumer
from app.services.audit_service import enregistrer_log

KAFKA_BROKERS = os.getenv("KAFKA_BROKERS", "kafka:9092")

# TOUS les topics de la plateforme à tracer
TOPICS_A_ECOUTER = [
    "user.created",
    "user.authenticated",
    "account.created",
    "account.credited",
    "account.debited",
    "account.status.changed",
    "transaction.validated",
    "transaction.failed",
    "transaction.inter.initiated",
    "loan.submitted",
    "loan.validated",
    "loan.rejected",
    "repayment.due",
    "document.submitted",
    "document.ocr.done",
    "customer.created",
    "customer.kyc.updated"
]


def deduire_service(topic: str) -> str:
    """Déduit le nom du service source depuis le nom du topic"""
    mapping = {
        "user.": "identity-service",
        "account.": "account-service",
        "transaction.": "transaction-service",
        "loan.": "loan-service",
        "repayment.": "loan-service",
        "document.": "document-service",
        "customer.": "customer-service"
    }
    for prefix, service in mapping.items():
        if topic.startswith(prefix):
            return service
    return "unknown-service"


def ecouter_tous_les_evenements():
    """
    Démarre l'écoute de TOUS les topics Kafka de la plateforme.
    Chaque événement reçu est enregistré comme un log d'audit immuable.
    """
    try:
        consumer = KafkaConsumer(
            *TOPICS_A_ECOUTER,
            bootstrap_servers=KAFKA_BROKERS.split(","),
            group_id="audit-service-group",
            value_deserializer=lambda v: json.loads(v.decode("utf-8")),
            auto_offset_reset="latest"
        )

        print(f"✅ Audit-service écoute {len(TOPICS_A_ECOUTER)} topics Kafka")

        for message in consumer:
            try:
                data = message.value
                topic = message.topic
                service_source = deduire_service(topic)

                # Enregistrer le log d'audit immuable
                enregistrer_log(
                    event_type=data.get("eventType", topic.upper()),
                    service=service_source,
                    user_id=data.get("clientId") or data.get("userId") or data.get("customerId"),
                    description=f"Événement {topic} reçu depuis {service_source}",
                    donnees=data
                )

                print(f"📝 Log audit enregistré : {topic} ({service_source})")

            except Exception as e:
                print(f"❌ Erreur enregistrement log audit : {e}")

    except Exception as e:
        print(f"❌ Erreur connexion Kafka consumer (audit-service) : {e}")


def demarrer_consumer_en_arriere_plan():
    """Lance le consommateur dans un thread séparé"""
    thread = threading.Thread(target=ecouter_tous_les_evenements, daemon=True)
    thread.start()
