import { useState } from 'react';
import Layout from '../../components/Layout';

const mockPrets = [
  { id:'PR001', montant:'500 000 F', duree:'12 mois', taux:'5%', reste:'350 000 F', prochaine:'15/07/2026' },
];
const echeancier = [
  { num:1, date:'15/01/2026', capital:'35 000 F', interet:'2 500 F', total:'37 500 F', statut:'Payé' },
  { num:2, date:'15/02/2026', capital:'35 000 F', interet:'2 325 F', total:'37 325 F', statut:'Payé' },
  { num:3, date:'15/03/2026', capital:'35 000 F', interet:'2 150 F', total:'37 150 F', statut:'Payé' },
  { num:4, date:'15/07/2026', capital:'35 000 F', interet:'1 975 F', total:'36 975 F', statut:'À venir' },
];

export default function Prets() {
  const [tab, setTab] = useState('liste');
  const [form, setForm] = useState({ montant:'', duree:'12', motif:'' });
  const [msg, setMsg] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setMsg('Demande soumise. En attente de validation.');
    setTab('liste'); setTimeout(() => setMsg(''), 4000);
  };

  return (
    <Layout>
      <div className="page-title">Prêts</div>
      {msg && <div className="alert success">{msg}</div>}
      <div style={{display:'flex', gap:10, marginBottom:20}}>
        {['liste','demande','echeancier'].map(t => (
          <button key={t} className={`btn ${tab===t?'btn-primary':''}`}
            style={tab!==t?{background:'#fff',border:'1px solid #ddd',color:'#333'}:{}}
            onClick={() => setTab(t)}>
            {t==='liste'?'📋 Mes prêts': t==='demande'?'+ Nouvelle demande':'📅 Échéancier'}
          </button>
        ))}
      </div>
      {tab === 'liste' && (
        <div className="table-box">
          <table>
            <thead><tr><th>ID</th><th>Montant</th><th>Durée</th><th>Taux</th><th>Reste</th><th>Prochaine échéance</th><th>Statut</th></tr></thead>
            <tbody>{mockPrets.map(p => (
              <tr key={p.id}><td>{p.id}</td><td>{p.montant}</td><td>{p.duree}</td><td>{p.taux}</td><td>{p.reste}</td><td>{p.prochaine}</td><td><span className="badge info">En cours</span></td></tr>
            ))}</tbody>
          </table>
        </div>
      )}
      {tab === 'demande' && (
        <div className="form-box"><h2>Demande de prêt</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group"><label>Montant (FCFA)</label>
              <input type="number" min="10000" required value={form.montant} onChange={e => setForm({...form, montant: e.target.value})} /></div>
            <div className="form-group"><label>Durée (mois)</label>
              <select value={form.duree} onChange={e => setForm({...form, duree: e.target.value})}>
                {[6,12,18,24,36].map(d => <option key={d}>{d}</option>)}
              </select></div>
            <div className="form-group"><label>Motif</label>
              <textarea rows={3} value={form.motif} onChange={e => setForm({...form, motif: e.target.value})} placeholder="Objet du prêt..." /></div>
            <button className="btn btn-success" type="submit">Soumettre</button>
          </form>
        </div>
      )}
      {tab === 'echeancier' && (
        <div className="table-box"><h3>Échéancier — PR001</h3>
          <table>
            <thead><tr><th>#</th><th>Date</th><th>Capital</th><th>Intérêts</th><th>Total</th><th>Statut</th></tr></thead>
            <tbody>{echeancier.map(e => (
              <tr key={e.num}><td>{e.num}</td><td>{e.date}</td><td>{e.capital}</td><td>{e.interet}</td><td><strong>{e.total}</strong></td>
                <td><span className={`badge ${e.statut==='Payé'?'success':'warning'}`}>{e.statut}</span></td></tr>
            ))}</tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
