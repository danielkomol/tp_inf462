// ==============================================
// POINT D'ENTRÉE — API GATEWAY
// ==============================================
// C'est LE point d'entrée unique de toute la plateforme.
// Tout le trafic externe passe par ici avant
// d'être routé vers le bon microservice.

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');

const { setupRoutes } = require('./routes/proxyRoutes');
const { limiter } = require('./middlewares/rateLimiter');

const app = express();
const PORT = process.env.PORT || 8080;

// ==============================================
// MIDDLEWARES GLOBAUX
// ==============================================
app.use(helmet());           // Sécurité des headers HTTP
app.use(cors());             // Autorise les requêtes cross-origin (frontend)
app.use(morgan('dev'));      // Logs des requêtes HTTP
app.use(limiter);            // Rate limiting global

// ==============================================
// ROUTE DE SANTÉ (pour Kubernetes)
// ==============================================
app.get('/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'api-gateway',
    timestamp: new Date().toISOString()
  });
});

// Route racine — documentation des services disponibles
app.get('/', (req, res) => {
  res.json({
    service: 'API Gateway — Plateforme Bancaire INF462',
    version: '1.0.0',
    routes: {
      auth: '/api/v1/auth',
      accounts: '/api/v1/accounts',
      transactions: '/api/v1/transactions',
      loans: '/api/v1/loans',
      customers: '/api/v1/customers',
      operators: '/api/v1/operators',
      documents: '/api/v1/documents',
      notifications: '/api/v1/notifications',
      audit: '/api/v1/audit',
      reports: '/api/v1/reports'
    }
  });
});

// ==============================================
// CONFIGURER TOUTES LES ROUTES DE PROXY
// ==============================================
setupRoutes(app);

// ==============================================
// GESTION DES ROUTES INCONNUES (404)
// ==============================================
app.use((req, res) => {
  res.status(404).json({
    success: false,
    message: `Route non trouvée : ${req.method} ${req.originalUrl}`
  });
});

// ==============================================
// DÉMARRAGE DU SERVEUR
// ==============================================
app.listen(PORT, () => {
  console.log(`✅ API Gateway démarré sur le port ${PORT}`);
  console.log(`📖 Documentation : http://localhost:${PORT}/`);
  console.log(`❤️  Health check : http://localhost:${PORT}/health`);
});
