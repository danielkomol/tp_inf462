import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const { login, error, setError } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const role = login(form.email, form.password);
    if (role === 'CLIENT')    navigate('/client/dashboard');
    if (role === 'ADMIN')     navigate('/admin/dashboard');
    if (role === 'OPERATEUR') navigate('/operateur/dashboard');
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-box">
        <div className="auth-logo">🏦 BankApp</div>
        <div className="auth-title">Connexion</div>
        <div className="auth-sub">Accédez à votre espace personnel</div>
        {error && <div className="alert error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input type="email" value={form.email} required
              onChange={e => { setError(''); setForm({...form, email: e.target.value}); }} />
          </div>
          <div className="form-group">
            <label>Mot de passe</label>
            <input type="password" value={form.password} required
              onChange={e => setForm({...form, password: e.target.value})} />
          </div>
          <button className="btn btn-primary" style={{width:'100%'}} type="submit">Se connecter</button>
        </form>
        <div className="auth-link">Pas encore de compte ? <Link to="/register">S'inscrire</Link></div>
        <div style={{marginTop:20, fontSize:'0.78rem', color:'#aaa'}}>
          <strong>Comptes démo :</strong><br/>
          client@bank.com / 1234 &nbsp;|&nbsp; admin@bank.com / 1234 &nbsp;|&nbsp; operateur@bank.com / 1234
        </div>
      </div>
    </div>
  );
}
