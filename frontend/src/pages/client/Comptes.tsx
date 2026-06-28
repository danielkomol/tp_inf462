import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { accountApi, operatorApi } from '../../api/services';

export default function Comptes() {
  const { user } = useAuth();
  const [comptes, setComptes] = useState<any[]>([]);
  const [operateurs, setOperateurs] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ accountType: 'COURANT', operatorId: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchComptes = () => {
    if (!user) return;
    accountApi.getAll(user.id)
      .then(r => setComptes(r.data?.data ?? r.data ?? []))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchComptes();
    operatorApi.getAll()
      .then(r => {
        const ops = r.data?.data ?? r.data ?? [];
        setOperateurs(ops);
        if (ops.length > 0) setForm(f => ({ ...f, operatorId: ops[0].id ?? ops[0].code }));
      })
      .catch(() => {});
  }, [user]);

  const handleOpen = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await accountApi.create({
        accountType: form.accountType,
        operatorId: String(form.operatorId),
        customerId: user?.id ?? '',
      });
      setMsg('Compte ouvert avec succès !');
      setShowForm(false);
      fetchComptes();
      setTimeout(() => setMsg(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de l'ouverture du compte");
    }
  };

  return (
    <Layout>
      <div className="page-title">Mes comptes</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      {loading ? <div>Chargement...</div> : (
        <>
          <div className="cards-row">
            {comptes.length === 0 ? <p style={{color:'#888'}}>Aucun compte. Ouvrez-en un !</p> :
              comptes.map((c: any) => (
                <div className="card" key={c.id}>
                  <div className="card-label">{c.accountType ?? c.type} — {c.operatorId ?? c.operateur}</div>
                  <div className="card-value green">{(c.balance ?? c.solde ?? 0).toLocaleString()} {c.currency ?? 'XAF'}</div>
                  <div style={{fontSize:'0.78rem', marginTop:6, color:'#888'}}>
                    N° {c.accountNumber ?? c.id} &nbsp;•&nbsp;
                    <span className={`badge ${c.status === 'ACTIVE' ? 'success' : 'warning'}`}>{c.status ?? c.statut}</span>
                  </div>
                </div>
              ))
            }
          </div>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>+ Ouvrir un compte</button>
          {showForm && (
            <div className="form-box" style={{marginTop:20}}>
              <h2>Ouvrir un compte</h2>
              <form onSubmit={handleOpen}>
                <div className="form-group"><label>Type de compte</label>
                  <select value={form.accountType} onChange={e => setForm({...form, accountType: e.target.value})}>
                    <option value="COURANT">Compte Courant</option>
                    <option value="EPARGNE">Compte Épargne</option>
                    <option value="MOBILE_MONEY">Mobile Money</option>
                  </select></div>
                <div className="form-group"><label>Opérateur</label>
                  <select value={form.operatorId} onChange={e => setForm({...form, operatorId: e.target.value})}>
                    {operateurs.map((op: any) => (
                      <option key={op.id ?? op.code} value={op.id ?? op.code}>{op.nom ?? op.name}</option>
                    ))}
                  </select></div>
                <button className="btn btn-success" type="submit">Confirmer</button>
              </form>
            </div>
          )}
        </>
      )}
    </Layout>
  );
}
