// ==============================================
// CONFIGURATION DES ROUTES — PROXY
// ==============================================
// Ce fichier définit vers quel microservice
// chaque type de requête doit être redirigé.

const { createProxyMiddleware } = require('http-proxy-middleware');
const { verifyToken } = require('../middlewares/authMiddleware');
const { authLimiter } = require('../middlewares/rateLimiter');

/**
 * Configure toutes les routes de proxy sur l'application Express
 */
const setupRoutes = (app) => {

  // ============================================
  // ROUTES PUBLIQUES (pas de token requis)
  // ============================================

  // Authentification → identity-service
  app.use('/api/v1/auth',
    authLimiter, // Protection brute-force
    createProxyMiddleware({
      target: process.env.IDENTITY_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('identity-service')
    })
  );

  // ============================================
  // ROUTES PROTÉGÉES (token JWT requis)
  // ============================================

  // Comptes → account-service
  app.use('/api/v1/accounts',
    verifyToken,
    createProxyMiddleware({
      target: process.env.ACCOUNT_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('account-service')
    })
  );

  // Transactions → transaction-service
  app.use('/api/v1/transactions',
    verifyToken,
    createProxyMiddleware({
      target: process.env.TRANSACTION_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('transaction-service')
    })
  );

  // Prêts → loan-service
  app.use('/api/v1/loans',
    verifyToken,
    createProxyMiddleware({
      target: process.env.LOAN_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('loan-service')
    })
  );

  // Clients → customer-service
  app.use('/api/v1/customers',
    verifyToken,
    createProxyMiddleware({
      target: process.env.CUSTOMER_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('customer-service')
    })
  );

  // Opérateurs → operator-service
  app.use('/api/v1/operators',
    verifyToken,
    createProxyMiddleware({
      target: process.env.OPERATOR_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('operator-service')
    })
  );

  // Documents → document-service
  app.use('/api/v1/documents',
    verifyToken,
    createProxyMiddleware({
      target: process.env.DOCUMENT_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('document-service')
    })
  );

  // Notifications → notification-service
  app.use('/api/v1/notifications',
    verifyToken,
    createProxyMiddleware({
      target: process.env.NOTIFICATION_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('notification-service')
    })
  );

  // Audit & Rapports → audit-service
  app.use('/api/v1/audit',
    verifyToken,
    createProxyMiddleware({
      target: process.env.AUDIT_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('audit-service')
    })
  );

  // Reporting → reporting-service
  app.use('/api/v1/reports',
    verifyToken,
    createProxyMiddleware({
      target: process.env.REPORTING_SERVICE_URL,
      changeOrigin: true,
      onError: handleProxyError('reporting-service')
    })
  );
};

/**
 * GESTION DES ERREURS DE PROXY
 * Si un microservice est down, on renvoie une erreur propre
 * au lieu de planter l'api-gateway (pattern Circuit Breaker simplifié)
 */
const handleProxyError = (serviceName) => (err, req, res) => {
  console.error(`❌ Erreur de connexion vers ${serviceName} :`, err.message);
  res.status(503).json({
    success: false,
    message: `Le service ${serviceName} est actuellement indisponible. Réessayez plus tard.`
  });
};

module.exports = { setupRoutes };
