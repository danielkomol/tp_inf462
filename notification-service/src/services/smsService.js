// SERVICE SMS — Twilio (optionnel)
// Si les credentials ne sont pas configurés, les SMS sont simulés en console.

let client = null;

if (process.env.TWILIO_ACCOUNT_SID && process.env.TWILIO_AUTH_TOKEN) {
  try {
    const twilio = require('twilio');
    client = twilio(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN);
    console.log('✅ Twilio initialisé');
  } catch (e) {
    console.warn('⚠️  Twilio non disponible :', e.message);
  }
} else {
  console.warn('⚠️  Twilio non configuré — SMS simulés en console uniquement');
}

const envoyerSMS = async (telephone, message) => {
  if (!client) {
    console.log(`[SMS SIMULÉ] → ${telephone} : ${message}`);
    return { success: true, simulated: true };
  }
  try {
    const result = await client.messages.create({
      body: message,
      from: process.env.TWILIO_PHONE_NUMBER,
      to: telephone
    });
    console.log(`✅ SMS envoyé à ${telephone} : ${result.sid}`);
    return { success: true, sid: result.sid };
  } catch (error) {
    console.error(`❌ Erreur SMS à ${telephone} :`, error.message);
    return { success: false, error: error.message };
  }
};

module.exports = { envoyerSMS };
