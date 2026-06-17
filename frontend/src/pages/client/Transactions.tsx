import { useState } from 'react';
import Layout from '../../components/Layout';

const initial = [
  { id:'TX001', type:'Dépôt',    montant:'+150 000 F', date:'12/06/2026', compte:'CPT001' },
  { id:'TX002', type:'Retrait',  montant:'-30 000 F',  date:'11/06/2026', compte:'CPT001' },
  { id:'TX003', type:'Transfert',montant:'-50 000 F',  date:'10/06/2026', compte:'CPT001' },
  { id:'TX004', type:'Dépôt',    montant:'+200 000 F', date:'08/06/2026', compte:'CPT002' },
];
const typeColor: Record<string,string> = { Dépôt:'success', Retrait:'danger', Transfert:'info' };

export default function Transactions() {
  const [tab, setTab] = useState('historique');
  const [txList, setTxList] = useState(initial);
  const [form, setForm] = useState({ type:'Dépôt', montant:'', compte:'CPT001', destinataire:'' });
  const [msg, setMsg] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const sign = form.type === 'Dépôt' ? '+' : '-';
    setTxList([{ id:'TX00'+(txList.length+1), type:form.type, montant:sign+parseInt(form.montant).toLocaleString()+' F', date:new Date().toLocaleDateString('fr-FR'), compte:form.compte }, ...txList]);
    setMsg('Opération effectuée !'); setTab('historique');
    setForm({ type:'Dépôt', montant:'', compte:'CPT001', destinataire:'' });
    setTimeout(() => setMsg(''), 3000);
  };

  return (
    <Layout>
      <div className="page-title">Transactions</div>
      {msg && <div className="alert success">{msg}</div>}
      <div style={{display:'flex', gap:10, marginBottom:20}}>
        {['historique','nouvelle'].map(t => (
          <button key={t} className={`btn ${tab===t?'btn-primary':''}`}
            style={tab!==t?{background:'#fff',border:'1px solid #ddd',color:'#333'}:{}}
            onClick={() => setTab(t)}>
            {t==='historique'?'📋 Historique':'+ Nouvelle opération'}
          </button>
        ))}
      </div>
      {tab === 'historique' && (
        <div className="table-box">
          <table>
            <thead><tr><th>ID</th><th>Type</th><th>Montant</th><th>Compte</th><th>Date</th><th>Statut</th></tr></thead>
            <tbody>
              {txList.map(tx => (
                <tr key={tx.id}>
                  <td>{tx.id}</td>
                  <td><span className={`badge ${typeColor[tx.type]||'info'}`}>{tx.type}</span></td>
                  <td><strong>{tx.montant}</strong></td>
                  <td>{tx.compte}</td>
                  <td>{tx.date}</td>
                  <td><span className="badge success">Réussi</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {tab === 'nouvelle' && (
        <div className="form-box">
          <h2>Nouvelle opération</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group"><label>Type</label>
              <select value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
                <option>Dépôt</option><option>Retrait</option><option>Transfert</option>
              </select></div>
            <div className="form-group"><label>Compte source</label>
              <select value={form.compte} onChange={e => setForm({...form, compte: e.target.value})}>
                <option>CPT001</option><option>CPT002</option>
              </select></div>
            {form.type === 'Transfert' && (
              <div className="form-group"><label>Compte destinataire</label>
                <input placeholder="N° compte" value={form.destinataire} onChange={e => setForm({...form, destinataire: e.target.value})} /></div>
            )}
            <div className="form-group"><label>Montant (FCFA)</label>
              <input type="number" min="1" required value={form.montant} onChange={e => setForm({...form, montant: e.target.value})} /></div>
            <button className="btn btn-success" type="submit">Confirmer</button>
          </form>
        </div>
      )}
    </Layout>
  );
}
