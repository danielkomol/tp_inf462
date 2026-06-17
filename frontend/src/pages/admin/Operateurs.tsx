import { useState } from 'react';
import Layout from '../../components/Layout';

const initial = [
  { id:'OP001', nom:'MTN Mobile Money', pays:'Cameroun', commission:'1.5%', plafond:'500 000 F', comptes:412 },
  { id:'OP002', nom:'Orange Money',     pays:'Cameroun', commission:'1.8%', plafond:'300 000 F', comptes:287 },
  { id:'OP003', nom:'Express Union',    pays:'Cameroun', commission:'2.0%', plafond:'200 000 F', comptes:98  },
];

export default function Operateurs() {
  const [ops, setOps] = useState(initial);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ nom:'', pays:'Cameroun', commission:'', plafond:'' });
  const [msg, setMsg] = useState('');

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault();
    setOps([...ops, { id:'OP00'+(ops.length+1), ...form, comptes:0 }]);
    setMsg('Opérateur ajouté !'); setShowForm(false);
    setTimeout(() => setMsg(''), 3000);
  };

  return (
    <Layout>
      <div className="page-title">Gestion des opérateurs</div>
      {msg && <div className="alert success">{msg}</div>}
      <div className="table-box" style={{marginBottom:20}}>
        <table>
          <thead><tr><th>ID</th><th>Nom</th><th>Pays</th><th>Commission</th><th>Plafond</th><th>Comptes</th><th>Statut</th></tr></thead>
          <tbody>{ops.map(o => (
            <tr key={o.id}><td>{o.id}</td><td><strong>{o.nom}</strong></td><td>{o.pays}</td><td>{o.commission}</td><td>{o.plafond}</td><td>{o.comptes}</td><td><span className="badge success">Actif</span></td></tr>
          ))}</tbody>
        </table>
      </div>
      <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>+ Ajouter un opérateur</button>
      {showForm && (
        <div className="form-box" style={{marginTop:20}}><h2>Nouvel opérateur</h2>
          <form onSubmit={handleAdd}>
            <div className="form-group"><label>Nom</label><input required value={form.nom} onChange={e => setForm({...form, nom:e.target.value})} /></div>
            <div className="form-group"><label>Pays</label><input value={form.pays} onChange={e => setForm({...form, pays:e.target.value})} /></div>
            <div className="form-group"><label>Commission (%)</label><input required value={form.commission} onChange={e => setForm({...form, commission:e.target.value})} /></div>
            <div className="form-group"><label>Plafond transaction</label><input required value={form.plafond} onChange={e => setForm({...form, plafond:e.target.value})} /></div>
            <button className="btn btn-success" type="submit">Enregistrer</button>
          </form>
        </div>
      )}
    </Layout>
  );
}
