// ==============================================
// MODÈLE NOTIFICATION — MongoDB
// ==============================================
// MongoDB est différent de PostgreSQL :
// - Pas de tables → des "collections"
// - Pas de lignes → des "documents" (JSON)
// - Pas de schéma fixe → mais on en définit un avec Mongoose

const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema({

  // ID du destinataire
  clientId: {
    type: String,
    required: true,
    index: true  // Index pour accélérer les recherches
  },

  // Type d'événement qui a déclenché la notification
  // Ex: TRANSACTION_VALIDATED, LOAN_APPROVED...
  eventType: {
    type: String,
    required: true
  },

  // Canal d'envoi
  canal: {
    type: String,
    enum: ['EMAIL', 'SMS', 'PUSH', 'IN_APP'],
    required: true
  },

  // Destinataire (email ou téléphone)
  destinataire: {
    type: String,
    required: true
  },

  // Contenu du message
  sujet: String,  // Pour les emails
  message: {
    type: String,
    required: true
  },

  // Statut d'envoi
  statut: {
    type: String,
    enum: ['EN_ATTENTE', 'ENVOYE', 'ECHEC'],
    default: 'EN_ATTENTE'
  },

  // Nombre de tentatives d'envoi
  tentatives: {
    type: Number,
    default: 0
  },

  // Erreur si envoi échoué
  erreur: String,

  // Lu ou non (pour le frontend)
  lu: {
    type: Boolean,
    default: false
  },

  // Données de l'événement source (pour traçabilité)
  donneesEvenement: mongoose.Schema.Types.Mixed,

}, {
  // Ajoute automatiquement createdAt et updatedAt
  timestamps: true
});

// Index pour améliorer les performances des requêtes fréquentes
NotificationSchema.index({ clientId: 1, createdAt: -1 });
NotificationSchema.index({ statut: 1 });

module.exports = mongoose.model('Notification', NotificationSchema);
