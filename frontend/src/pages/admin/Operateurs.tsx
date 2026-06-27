import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { operatorApi } from '../../api/services';

export default function Operateurs() {
  const [ops, setOps] = useState<any[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ nom: '', type: 'BANQUE', email: '', telephone: '', code: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const fetchOps = () => {
    operatorApi.getAll()
      .then(r => setOps(r.data?.data ?? r.data ?? []))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchOps(); }, []);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      await operatorApi.create(form);
      setMsg('Opérateur ajouté !');
      setShowForm(false);
      fetchOps();
      setTimeout(() => setMsg(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de l'ajout");
    }
  };

  return (
    <Layout>
      <div className="page-title">Gestion des opérateurs</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      <div className="table-box" style={{marginBottom:20}}>
        {loading ? <div>Chargement...</div> : (
          <table>
            <thead><tr><th>Code</th><th>Nom</th><th>Type</th><th>Email</th><th>Commission</th><th>Plafond tx</th><th>Statut</th></tr></thead>
            <tbody>{ops.map((o: any) => (
              <tr key={o.id}>
                <td>{o.code}</td>
                <td><strong>{o.nom ?? o.name}</strong></td>
                <td>{o.type}</td>
                <td>{o.email}</td>
                <td>{o.tauxCommission ?? o.commission}%</td>
                <td>{(o.plafondTransaction ?? 0).toLocaleString()} XAF</td>
                <td><span className={`badge ${o.statut==='ACTIF'||o.status==='ACTIF'?'success':'danger'}`}>{o.statut ?? o.status}</span></td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
      <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>+ Ajouter un opérateur</button>
      {showForm && (
        <div className="form-box" style={{marginTop:20}}><h2>Nouvel opérateur</h2>
          <form onSubmit={handleAdd}>
            <div className="form-group"><label>Code unique</label><input required value={form.code} placeholder="ex: MTN-MOMO" onChange={e => setForm({...form, code:e.target.value})} /></div>
            <div className="form-group"><label>Nom</label><input required value={form.nom} onChange={e => setForm({...form, nom:e.target.value})} /></div>
            <div className="form-group"><label>Type</label>
              <select value={form.type} onChange={e => setForm({...form, type:e.target.value})}>
                <option value="BANQUE">Banque</option>
                <option value="MICROFINANCE">Microfinance</option>
                <option value="MOBILE_MONEY">Mobile Money</option>
              </select></div>
            <div className="form-group"><label>Email</label><input type="email" required value={form.email} onChange={e => setForm({...form, email:e.target.value})} /></div>
            <div className="form-group"><label>Téléphone</label><input required value={form.telephone} onChange={e => setForm({...form, telephone:e.target.value})} /></div>
            <button className="btn btn-success" type="submit">Enregistrer</button>
          </form>
        </div>
      )}
    </Layout>
  );
}
