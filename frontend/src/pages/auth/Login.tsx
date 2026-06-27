import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const { login, error, setError } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    const role = await login(form.email, form.password);
    setSubmitting(false);
    if (role === 'CLIENT')    navigate('/client/dashboard');
    else if (role === 'ADMIN')     navigate('/admin/dashboard');
    else if (role === 'OPERATEUR') navigate('/operateur/dashboard');
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
              onChange={e => { setError(''); setForm({ ...form, email: e.target.value }); }} />
          </div>
          <div className="form-group">
            <label>Mot de passe</label>
            <input type="password" value={form.password} required
              onChange={e => setForm({ ...form, password: e.target.value })} />
          </div>
          <button className="btn btn-primary" style={{ width: '100%' }} type="submit" disabled={submitting}>
            {submitting ? 'Connexion...' : 'Se connecter'}
          </button>
        </form>
        <div className="auth-link">Pas encore de compte ? <Link to="/register">S'inscrire</Link></div>
      </div>
    </div>
  );
}
