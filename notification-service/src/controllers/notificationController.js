// ==============================================
// CONTRÔLEUR NOTIFICATIONS — Endpoints REST
// ==============================================

const express = require('express');
const router = express.Router();
const { getNotificationsClient } = require('../services/notificationService');
const Notification = require('../models/Notification');

/**
 * GET /api/v1/notifications/:clientId
 * Récupérer les notifications d'un client (route principale du frontend)
 */
router.get('/:clientId', async (req, res) => {
  try {
    const { clientId } = req.params;
    // Si c'est un ObjectId MongoDB, chercher par ID de notification
    // Sinon chercher par clientId
    if (clientId.match(/^[0-9a-fA-F]{24}$/)) {
      // C'est un ID MongoDB — chercher une notification spécifique
      const notification = await Notification.findById(clientId);
      if (!notification) {
        // Pas trouvé comme ID → essayer comme clientId
        const result = await getNotificationsClient(clientId);
        return res.json({ success: true, data: result.notifications });
      }
      return res.json({ success: true, data: notification });
    }
    // C'est un UUID de client
    const result = await getNotificationsClient(clientId);
    res.json({ success: true, data: result.notifications });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * PUT /api/v1/notifications/:clientId/read-all
 * Marquer toutes les notifications d'un client comme lues
 */
router.put('/:clientId/read-all', async (req, res) => {
  try {
    await Notification.updateMany(
      { clientId: req.params.clientId },
      { $set: { lu: true } }
    );
    res.json({ success: true, message: 'Toutes les notifications marquées comme lues' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/v1/notifications/client/:clientId
 * Récupérer toutes les notifications d'un client (route alternative)
 */
router.get('/client/:clientId', async (req, res) => {
  try {
    const { clientId } = req.params;
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 20;

    const result = await getNotificationsClient(clientId, page, limit);

    res.json({
      success: true,
      message: `${result.total} notification(s) trouvée(s)`,
      data: result
    });

  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/v1/notifications/:id
 * Récupérer une notification par ID
 */
router.get('/:id', async (req, res) => {
  try {
    const notification = await Notification.findById(req.params.id);
    if (!notification) {
      return res.status(404).json({ success: false, message: 'Notification introuvable' });
    }
    res.json({ success: true, data: notification });

  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

/**
 * GET /api/v1/notifications/stats/global
 * Statistiques des notifications (pour l'admin)
 */
router.get('/stats/global', async (req, res) => {
  try {
    const stats = await Notification.aggregate([
      {
        $group: {
          _id: '$statut',
          count: { $sum: 1 }
        }
      }
    ]);

    const total = await Notification.countDocuments();

    res.json({
      success: true,
      data: { total, parStatut: stats }
    });

  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

module.exports = router;
