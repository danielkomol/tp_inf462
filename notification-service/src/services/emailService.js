// ==============================================
// SERVICE EMAIL — Nodemailer
// ==============================================
// Nodemailer permet d'envoyer des emails depuis Node.js
// On utilise Gmail comme serveur SMTP

const nodemailer = require('nodemailer');

// Créer le transporteur email (connexion au serveur SMTP)
const transporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST,
  port: process.env.EMAIL_PORT,
  secure: false,  // false pour port 587 (TLS), true pour port 465 (SSL)
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASSWORD
  }
});

/**
 * ENVOYER UN EMAIL
 * @param {string} destinataire - Adresse email du destinataire
 * @param {string} sujet - Sujet de l'email
 * @param {string} message - Corps du message (texte ou HTML)
 */
const envoyerEmail = async (destinataire, sujet, message) => {
  try {
    const info = await transporter.sendMail({
      from: process.env.EMAIL_FROM,
      to: destinataire,
      subject: sujet,
      // Version texte (fallback)
      text: message,
      // Version HTML (si le message contient du HTML)
      html: `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
          <div style="background-color: #1F4E79; padding: 20px; text-align: center;">
            <h1 style="color: white; margin: 0;">🏦 Banque Platform</h1>
          </div>
          <div style="padding: 30px; background-color: #f9f9f9;">
            <p style="font-size: 16px; color: #333;">${message}</p>
          </div>
          <div style="padding: 10px; text-align: center; color: #888; font-size: 12px;">
            <p>Université de Yaoundé I — INF462</p>
          </div>
        </div>
      `
    });

    console.log(`✅ Email envoyé à ${destinataire} : ${info.messageId}`);
    return { success: true, messageId: info.messageId };

  } catch (error) {
    console.error(`❌ Erreur envoi email à ${destinataire} :`, error.message);
    throw error;
  }
};

module.exports = { envoyerEmail };
