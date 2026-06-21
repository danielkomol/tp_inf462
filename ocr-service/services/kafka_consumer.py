"""
CONSOMMATEUR KAFKA — OCR Service
==================================
Écoute le topic "document.submitted" publié par le
document-service. Pour chaque document soumis :
1. Lance l'extraction OCR
2. Publie le résultat sur "document.ocr.done"

Ceci complète la chaîne event-driven :
document-service --(document.submitted)--> ocr-service
ocr-service --(document.ocr.done)--> document-service, loan-service, audit-service
"""

import json
import os
import threading
from kafka import KafkaConsumer, KafkaProducer

from services.ocr_engine import extraire_texte, extraire_infos_cni, extraire_infos_bulletin_salaire

KAFKA_BROKERS = os.getenv("KAFKA_BROKERS", "kafka:9092")

# Producteur pour republier le résultat
producer = None


def get_producer():
    global producer
    if producer is None:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BROKERS.split(","),
            value_serializer=lambda v: json.dumps(v).encode("utf-8")
        )
    return producer


def traiter_document_soumis():
    """
    Boucle d'écoute du topic document.submitted
    """
    try:
        consumer = KafkaConsumer(
            "document.submitted",
            bootstrap_servers=KAFKA_BROKERS.split(","),
            group_id="ocr-service-group",
            value_deserializer=lambda v: json.loads(v.decode("utf-8")),
            auto_offset_reset="latest"
        )

        print("✅ Consommateur Kafka (ocr-service) démarré")

        for message in consumer:
            try:
                data = message.value
                document_id = data.get("documentId")
                client_id = data.get("clientId")
                type_document = data.get("typeDocument", "AUTRE")
                chemin_fichier = data.get("cheminFichier")

                print(f"📩 Traitement OCR du document {document_id} ({type_document})")

                # Lancer l'extraction OCR sur le fichier
                resultat = extraire_texte(chemin_fichier, langue="fra")

                # Structurer les données selon le type
                infos = {}
                if type_document == "CNI":
                    infos = extraire_infos_cni(resultat["texte_brut"])
                elif type_document == "BULLETIN_SALAIRE":
                    infos = extraire_infos_bulletin_salaire(resultat["texte_brut"])

                # Publier le résultat sur Kafka
                event = {
                    "eventType": "OCR_TERMINE",
                    "documentId": document_id,
                    "clientId": client_id,
                    "niveauConfiance": resultat["niveau_confiance"],
                    "donneesExtraites": infos,
                    "texteBrut": resultat["texte_brut"]
                }

                get_producer().send("document.ocr.done", value=event)
                get_producer().flush()
                print(f"✅ Résultat OCR publié pour le document {document_id}")

            except Exception as e:
                print(f"❌ Erreur traitement document {message.value} : {e}")

    except Exception as e:
        print(f"❌ Erreur connexion Kafka consumer (ocr-service) : {e}")


def demarrer_consumer_en_arriere_plan():
    """Lance le consommateur dans un thread séparé"""
    thread = threading.Thread(target=traiter_document_soumis, daemon=True)
    thread.start()
