import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name:'', email:'', phone:'', password:'', role:'CLIENT' });
  const [success, setSuccess] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSuccess(true);
    setTimeout(() => navigate('/login'), 1800);
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-box">
        <div className="auth-logo">🏦 BankApp</div>
        <div className="auth-title">Inscription</div>
        <div className="auth-sub">Créez votre compte</div>
        {success && <div className="alert success">Compte créé ! Redirection...</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group"><label>Nom complet</label>
            <input value={form.name} required onChange={e => setForm({...form, name: e.target.value})} /></div>
          <div className="form-group"><label>Email</label>
            <input type="email" value={form.email} required onChange={e => setForm({...form, email: e.target.value})} /></div>
          <div className="form-group"><label>Téléphone</label>
            <input value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} /></div>
          <div className="form-group"><label>Rôle</label>
            <select value={form.role} onChange={e => setForm({...form, role: e.target.value})}>
              <option value="CLIENT">Client</option>
              <option value="AGENT">Agent</option>
            </select></div>
          <div className="form-group"><label>Mot de passe</label>
            <input type="password" value={form.password} required onChange={e => setForm({...form, password: e.target.value})} /></div>
          <button className="btn btn-primary" style={{width:'100%'}} type="submit">Créer mon compte</button>
        </form>
        <div className="auth-link">Déjà un compte ? <Link to="/login">Se connecter</Link></div>
      </div>
    </div>
  );
}
