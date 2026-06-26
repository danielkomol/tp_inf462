// ==============================================
// MIDDLEWARE RATE LIMITING
// ==============================================
// Limite le nombre de requêtes par IP
// pour éviter les abus et les attaques DDoS

const rateLimit = require('express-rate-limit');

const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS) || 60000, // 1 minute
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS) || 100,     // 100 requêtes max
  message: {
    success: false,
    message: 'Trop de requêtes. Veuillez réessayer dans une minute.'
  },
  standardHeaders: true,  // Retourne les infos de limite dans les headers
  legacyHeaders: false
});

// Limite plus stricte pour les endpoints d'authentification
// (protection contre le brute-force sur le login)
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5,                    // 5 tentatives max
  message: {
    success: false,
    message: 'Trop de tentatives de connexion. Réessayez dans 15 minutes.'
  }
});

module.exports = { limiter, authLimiter };
