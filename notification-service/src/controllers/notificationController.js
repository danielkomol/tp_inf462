// ==============================================
// CONTRÔLEUR NOTIFICATIONS — Endpoints REST
// ==============================================

const express = require('express');
const router = express.Router();
const { getNotificationsClient } = require('../services/notificationService');
const Notification = require('../models/Notification');

/**
 * GET /api/v1/notifications/client/:clientId
 * Récupérer toutes les notifications d'un client
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
