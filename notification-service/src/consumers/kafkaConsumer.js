// ==============================================
// CONSOMMATEUR KAFKA
// ==============================================
// Ce fichier écoute les événements publiés par
// les autres services sur Kafka et déclenche
// les notifications correspondantes.

const { Kafka } = require('kafkajs');
const { traiterEvenement } = require('../services/notificationService');

// Créer le client Kafka
const kafka = new Kafka({
  clientId: 'notification-service',
  brokers: (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  // Retry automatique si connexion perdue
  retry: {
    initialRetryTime: 3000,
    retries: 10
  }
});

// Créer le consommateur
const consumer = kafka.consumer({
  groupId: process.env.KAFKA_GROUP_ID || 'notification-service-group'
});

/**
 * DÉMARRER LE CONSOMMATEUR KAFKA
 * S'abonne à tous les topics qui nous intéressent
 */
const demarrerConsommateur = async () => {
  try {
    // Se connecter à Kafka
    await consumer.connect();
    console.log('✅ Consommateur Kafka connecté');

    // S'abonner aux topics
    // fromBeginning: false = seulement les nouveaux messages
    await consumer.subscribe({
      topics: [
        'transaction.validated',    // Transaction réussie
        'transaction.failed',       // Transaction échouée
        'account.created',          // Nouveau compte ouvert
        'loan.submitted',           // Demande de prêt soumise
        'loan.validated',           // Prêt approuvé
        'loan.rejected',            // Prêt refusé
        'repayment.due',            // Rappel échéance
        'user.created',             // Nouvel utilisateur inscrit
        'user.registered'           // Inscription confirmée
      ],
      fromBeginning: false
    });

    console.log('✅ Abonné aux topics Kafka');

    // Traiter chaque message reçu
    await consumer.run({
      eachMessage: async ({ topic, partition, message }) => {
        try {
          // Décoder le message JSON
          const eventData = JSON.parse(message.value.toString());
          
          // Normaliser le type d'événement :
          // topic "transaction.validated" → "TRANSACTION_VALIDATED"
          const topicNormalized = topic.toUpperCase().replace(/\./g, '_');
          const eventType = eventData.eventType || topicNormalized;
          
          console.log(`📩 Message reçu sur le topic "${topic}" : ${eventType}`);

          // Traiter l'événement et envoyer les notifications
          await traiterEvenement(eventType, eventData);

        } catch (error) {
          console.error(`❌ Erreur traitement message Kafka (topic: ${topic}) :`, error.message);
        }
      }
    });

  } catch (error) {
    console.error('❌ Erreur connexion Kafka :', error.message);
    // Réessayer après 5 secondes
    setTimeout(demarrerConsommateur, 5000);
  }
};

/**
 * ARRÊTER PROPREMENT LE CONSOMMATEUR
 */
const arreterConsommateur = async () => {
  await consumer.disconnect();
  console.log('🔌 Consommateur Kafka déconnecté');
};

module.exports = { demarrerConsommateur, arreterConsommateur };
