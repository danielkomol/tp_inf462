import { useState } from 'react';
import Layout from '../../components/Layout';

const initial = [
  { id:'CPT001', type:'Courant', operateur:'MTN MoMo',   solde:'1 240 000 F' },
  { id:'CPT002', type:'Épargne', operateur:'Orange Money',solde:'320 000 F' },
];

export default function Comptes() {
  const [comptes, setComptes] = useState(initial);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ type:'Courant', operateur:'MTN MoMo' });
  const [msg, setMsg] = useState('');

  const handleOpen = (e: React.FormEvent) => {
    e.preventDefault();
    setComptes([...comptes, { id:'CPT00'+(comptes.length+1), type:form.type, operateur:form.operateur, solde:'0 F' }]);
    setMsg('Compte ouvert avec succès !'); setShowForm(false);
    setTimeout(() => setMsg(''), 3000);
  };

  return (
    <Layout>
      <div className="page-title">Mes comptes</div>
      {msg && <div className="alert success">{msg}</div>}
      <div className="cards-row">
        {comptes.map(c => (
          <div className="card" key={c.id}>
            <div className="card-label">{c.type} — {c.operateur}</div>
            <div className="card-value green">{c.solde}</div>
            <div style={{fontSize:'0.78rem', marginTop:6, color:'#888'}}>N° {c.id} • <span className="badge success">Actif</span></div>
          </div>
        ))}
      </div>
      <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>+ Ouvrir un compte</button>
      {showForm && (
        <div className="form-box" style={{marginTop:20}}>
          <h2>Ouvrir un compte</h2>
          <form onSubmit={handleOpen}>
            <div className="form-group"><label>Type</label>
              <select value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
                <option>Courant</option><option>Épargne</option>
              </select></div>
            <div className="form-group"><label>Opérateur</label>
              <select value={form.operateur} onChange={e => setForm({...form, operateur: e.target.value})}>
                <option>MTN MoMo</option><option>Orange Money</option><option>Express Union</option>
              </select></div>
            <button className="btn btn-success" type="submit">Confirmer</button>
          </form>
        </div>
      )}
    </Layout>
  );
}
