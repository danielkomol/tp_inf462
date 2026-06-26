// ==============================================
// SERVICE SMS — Twilio
// ==============================================
// Twilio est un service d'envoi de SMS en ligne

const twilio = require('twilio');

// Initialiser le client Twilio avec les credentials
const client = twilio(
  process.env.TWILIO_ACCOUNT_SID,
  process.env.TWILIO_AUTH_TOKEN
);

/**
 * ENVOYER UN SMS
 * @param {string} telephone - Numéro de téléphone (format international : +237...)
 * @param {string} message - Texte du SMS (max 160 caractères)
 */
const envoyerSMS = async (telephone, message) => {
  try {
    const result = await client.messages.create({
      body: message,
      from: process.env.TWILIO_PHONE_NUMBER,
      to: telephone
    });

    console.log(`✅ SMS envoyé à ${telephone} : ${result.sid}`);
    return { success: true, sid: result.sid };

  } catch (error) {
    console.error(`❌ Erreur envoi SMS à ${telephone} :`, error.message);
    throw error;
  }
};

module.exports = { envoyerSMS };
