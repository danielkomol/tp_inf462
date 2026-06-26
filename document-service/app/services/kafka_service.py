# ==============================================
# SERVICE KAFKA — Publication d'événements
# ==============================================

import json
import os
from kafka import KafkaProducer
from datetime import datetime

KAFKA_BROKERS = os.getenv("KAFKA_BROKERS", "kafka:9092")

# Créer le producteur Kafka
# value_serializer convertit automatiquement les dicts Python en JSON
producer = KafkaProducer(
    bootstrap_servers=KAFKA_BROKERS.split(","),
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)


def publier_evenement(topic: str, key: str, data: dict):
    """
    Publie un événement sur un topic Kafka

    Args:
        topic: nom du topic (ex: "document.submitted")
        key: clé de partitionnement (ex: client_id)
        data: données de l'événement
    """
    try:
        data["occurred_at"] = datetime.now().isoformat()
        producer.send(topic, key=key.encode("utf-8"), value=data)
        producer.flush()
        print(f"✅ Événement publié sur '{topic}' : {data.get('eventType')}")
    except Exception as e:
        print(f"❌ Erreur publication Kafka : {e}")
