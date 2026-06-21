// ==============================================
// MIDDLEWARE D'AUTHENTIFICATION JWT
// ==============================================
// Ce middleware vérifie le token JWT avant de
// laisser passer la requête vers les microservices.

const jwt = require('jsonwebtoken');

/**
 * VÉRIFIE LE TOKEN JWT
 * Si le token est valide, ajoute les infos utilisateur à req.user
 * Si invalide, rejette la requête avec 401
 */
const verifyToken = (req, res, next) => {
  // 1. Récupérer le header Authorization
  const authHeader = req.headers['authorization'];

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      success: false,
      message: 'Token manquant. Header Authorization requis.'
    });
  }

  // 2. Extraire le token (enlever "Bearer ")
  const token = authHeader.substring(7);

  try {
    // 3. Vérifier et décoder le token
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    // 4. Ajouter les infos utilisateur à la requête
    req.user = {
      email: decoded.sub,
      role: decoded.role
    };

    next(); // Passer au middleware suivant

  } catch (error) {
    console.error('❌ Token invalide :', error.message);
    return res.status(401).json({
      success: false,
      message: 'Token invalide ou expiré'
    });
  }
};

/**
 * VÉRIFIE LE RÔLE DE L'UTILISATEUR
 * Utilisé pour protéger certains endpoints (ex: admin uniquement)
 *
 * Usage : requireRole('ROLE_ADMIN')
 */
const requireRole = (roleRequis) => {
  return (req, res, next) => {
    if (!req.user || req.user.role !== roleRequis) {
      return res.status(403).json({
        success: false,
        message: `Accès refusé. Rôle requis : ${roleRequis}`
      });
    }
    next();
  };
};

module.exports = { verifyToken, requireRole };
