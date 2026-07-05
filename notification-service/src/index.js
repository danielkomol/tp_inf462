// ==============================================
// POINT D'ENTRÉE — notification-service
// ==============================================
// C'est le fichier principal qui démarre tout :
// 1. Le serveur Express (API REST)
// 2. La connexion MongoDB
// 3. Le consommateur Kafka

require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');

// Importation des routes et du consommateur Kafka
const notificationRoutes = require('./controllers/notificationController');
const { demarrerConsommateur, arreterConsommateur } = require('./consumers/kafkaConsumer');

// Créer l'application Express
const app = express();
const PORT = process.env.PORT || 3000;

// ==============================================
// MIDDLEWARES
// ==============================================
app.use(helmet());
app.use(cors({
  origin: ['http://localhost:5173', 'http://localhost:8080'],
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(morgan('dev'));     // Logs des requêtes HTTP
app.use(express.json());    // Parse le body JSON automatiquement

// ==============================================
// ROUTES
// ==============================================
app.use('/api/v1/notifications', notificationRoutes);

// Route de santé (pour Kubernetes health checks)
app.get('/health', (req, res) => {
  res.json({
    status: 'UP',
    service: 'notification-service',
    timestamp: new Date().toISOString()
  });
});

// Route par défaut
app.get('/', (req, res) => {
  res.json({
    service: 'Notification Service',
    version: '1.0.0',
    description: 'Service de notifications multi-canal — Plateforme Bancaire INF462'
  });
});

// ==============================================
// CONNEXION MONGODB + DÉMARRAGE
// ==============================================
const demarrer = async () => {
  try {
    // 1. Connexion à MongoDB
    await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/notifications_db');
    console.log('✅ MongoDB connecté');

    // 2. Démarrer le consommateur Kafka
    await demarrerConsommateur();

    // 3. Démarrer le serveur Express
    app.listen(PORT, () => {
      console.log(`✅ Notification Service démarré sur le port ${PORT}`);
      console.log(`📖 Documentation : http://localhost:${PORT}/`);
      console.log(`❤️  Health check : http://localhost:${PORT}/health`);
    });

  } catch (error) {
    console.error('❌ Erreur démarrage :', error.message);
    process.exit(1);
  }
};

// Arrêt propre du service
process.on('SIGTERM', async () => {
  console.log('🔌 Arrêt du service...');
  await arreterConsommateur();
  await mongoose.disconnect();
  process.exit(0);
});

// Lancer le service
demarrer();
