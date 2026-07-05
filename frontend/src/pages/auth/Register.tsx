import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../../api/services';
import api from '../../api/axios';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', telephone: '', motDePasse: '' });
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const res = await authApi.register({ ...form, role: 'CLIENT' });
      // Créer automatiquement le profil client dans customer-service
      const userId = res.data?.userId ?? res.data?.data?.userId;
      if (userId) {
        try {
          await api.post('/api/v1/customers', {
            userId,
            nom: form.nom,
            prenom: form.prenom,
            email: form.email,
            telephone: form.telephone,
          });
        } catch {
          // Le profil sera créé plus tard — non bloquant
        }
      }
      setSuccess(true);
      setTimeout(() => navigate('/login'), 1800);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de l'inscription");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-box">
        <div className="auth-logo">🏦 BankApp</div>
        <div className="auth-title">Inscription</div>
        <div className="auth-sub">Créez votre compte client</div>
        {success && <div className="alert success">Compte créé ! Redirection...</div>}
        {error  && <div className="alert error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group"><label>Nom</label>
            <input required value={form.nom} onChange={e => setForm({ ...form, nom: e.target.value })} /></div>
          <div className="form-group"><label>Prénom</label>
            <input required value={form.prenom} onChange={e => setForm({ ...form, prenom: e.target.value })} /></div>
          <div className="form-group"><label>Email</label>
            <input type="email" required value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></div>
          <div className="form-group"><label>Téléphone</label>
            <input required value={form.telephone} onChange={e => setForm({ ...form, telephone: e.target.value })} /></div>
          <div className="form-group"><label>Mot de passe</label>
            <input type="password" required value={form.motDePasse} onChange={e => setForm({ ...form, motDePasse: e.target.value })} /></div>
          <button className="btn btn-primary" style={{ width: '100%' }} type="submit" disabled={submitting}>
            {submitting ? 'Création...' : 'Créer mon compte'}
          </button>
        </form>
        <div className="auth-link">Déjà un compte ? <Link to="/login">Se connecter</Link></div>
      </div>
    </div>
  );
}
