// ==============================================
// SERVICE NOTIFICATION — Logique principale
// ==============================================

const Notification = require('../models/Notification');
const { envoyerEmail } = require('./emailService');
const { envoyerSMS } = require('./smsService');

/**
 * TRAITER UN ÉVÉNEMENT ET ENVOYER LES NOTIFICATIONS
 *
 * @param {string} eventType - Type d'événement Kafka reçu
 * @param {object} eventData - Données de l'événement
 */
const traiterEvenement = async (eventType, eventData) => {
  console.log(`📨 Traitement de l'événement : ${eventType}`);

  // Construire le message selon le type d'événement
  const { sujet, message, clientId, email, telephone } = construireMessage(eventType, eventData);

  if (!message) {
    console.log(`⚠️ Aucun message configuré pour l'événement : ${eventType}`);
    return;
  }

  // Envoyer par EMAIL si email disponible
  if (email) {
    await envoyerEtSauvegarder({
      clientId,
      eventType,
      canal: 'EMAIL',
      destinataire: email,
      sujet,
      message,
      donneesEvenement: eventData
    });
  }

  // Envoyer par SMS si téléphone disponible
  if (telephone) {
    await envoyerEtSauvegarder({
      clientId,
      eventType,
      canal: 'SMS',
      destinataire: telephone,
      sujet: null,
      message: message.substring(0, 160), // SMS limité à 160 caractères
      donneesEvenement: eventData
    });
  }
};

/**
 * ENVOYER ET SAUVEGARDER EN BASE
 */
const envoyerEtSauvegarder = async ({ clientId, eventType, canal, destinataire, sujet, message, donneesEvenement }) => {

  // 1. Créer la notification en base (statut EN_ATTENTE)
  const notification = new Notification({
    clientId,
    eventType,
    canal,
    destinataire,
    sujet,
    message,
    statut: 'EN_ATTENTE',
    donneesEvenement
  });
  await notification.save();

  // 2. Essayer d'envoyer (max 3 tentatives)
  let tentatives = 0;
  const MAX_TENTATIVES = 3;

  while (tentatives < MAX_TENTATIVES) {
    try {
      tentatives++;
      notification.tentatives = tentatives;

      if (canal === 'EMAIL') {
        await envoyerEmail(destinataire, sujet, message);
      } else if (canal === 'SMS') {
        await envoyerSMS(destinataire, message);
      }

      // Succès → mettre à jour le statut
      notification.statut = 'ENVOYE';
      await notification.save();
      console.log(`✅ Notification ${canal} envoyée à ${destinataire}`);
      return;

    } catch (error) {
      console.error(`❌ Tentative ${tentatives}/${MAX_TENTATIVES} échouée :`, error.message);
      notification.erreur = error.message;

      if (tentatives === MAX_TENTATIVES) {
        notification.statut = 'ECHEC';
        await notification.save();
        console.error(`❌ Notification définitivement échouée après ${MAX_TENTATIVES} tentatives`);
      }
    }
  }
};

/**
 * CONSTRUIRE LE MESSAGE SELON L'ÉVÉNEMENT
 * Retourne le sujet, le message et les coordonnées du destinataire
 */
const construireMessage = (eventType, data) => {
  switch (eventType) {

    case 'TRANSACTION_VALIDATED':
      return {
        clientId: data.clientSourceId || data.clientDestId,
        email: data.emailClient,
        telephone: data.telephoneClient,
        sujet: '✅ Transaction confirmée — Banque Platform',
        message: `Votre transaction a été confirmée.\n\nRéférence : ${data.reference}\nMontant : ${data.montant} XAF\nFrais : ${data.frais} XAF\nType : ${data.type}\n\nMerci de votre confiance.`
      };

    case 'TRANSACTION_FAILED':
      return {
        clientId: data.clientSourceId,
        email: data.emailClient,
        telephone: data.telephoneClient,
        sujet: '❌ Transaction échouée — Banque Platform',
        message: `Votre transaction a échoué.\n\nRéférence : ${data.reference}\nMontant : ${data.montant} XAF\nMotif : ${data.motifEchec}\n\nContactez votre opérateur pour plus d'informations.`
      };

    case 'ACCOUNT_CREATED':
      return {
        clientId: data.customerId,
        email: data.emailClient,
        telephone: data.telephoneClient,
        sujet: '🏦 Compte ouvert — Banque Platform',
        message: `Votre compte a été ouvert avec succès.\n\nNuméro de compte : ${data.accountNumber}\nType : ${data.accountType}\nDevise : ${data.currency}\n\nBienvenue sur la plateforme !`
      };

    case 'LOAN_VALIDATED':
      return {
        clientId: data.clientId,
        email: data.emailClient,
        telephone: data.telephoneClient,
        sujet: '✅ Prêt approuvé — Banque Platform',
        message: `Votre demande de prêt a été approuvée !\n\nMontant accordé : ${data.montantAccorde} XAF\nDurée : ${data.duree} mois\nTaux : ${data.tauxInteret}%\n\nConsultez votre échéancier dans l'application.`
      };

    case 'LOAN_REJECTED':
      return {
        clientId: data.clientId,
        email: data.emailClient,
        telephone: data.telephoneClient,
        sujet: '❌ Demande de prêt refusée — Banque Platform',
        message: `Votre demande de prêt a été refusée.\n\nMotif : ${data.motifRejet}\n\nVous pouvez soumettre une nouvelle demande après 3 mois.`
      };

    case 'REPAYMENT_DUE':
      return {
        clientId: data.clientId,
        email: data.emailClient,
        telephone: data.telephoneClient,
        sujet: '⏰ Rappel échéance — Banque Platform',
        message: `Rappel : Votre échéance de remboursement arrive à terme.\n\nMontant dû : ${data.montant} XAF\nDate limite : ${data.dateEcheance}\n\nAssurez-vous d'avoir le solde suffisant.`
      };

    default:
      return { clientId: null, email: null, telephone: null, sujet: null, message: null };
  }
};

/**
 * RÉCUPÉRER LES NOTIFICATIONS D'UN CLIENT
 */
const getNotificationsClient = async (clientId, page = 1, limit = 20) => {
  const skip = (page - 1) * limit;
  const notifications = await Notification
    .find({ clientId })
    .sort({ createdAt: -1 })
    .skip(skip)
    .limit(limit);

  const total = await Notification.countDocuments({ clientId });
  return { notifications, total, page, totalPages: Math.ceil(total / limit) };
};

module.exports = { traiterEvenement, getNotificationsClient };
