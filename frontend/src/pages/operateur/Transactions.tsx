import { useState } from 'react';
import Layout from '../../components/Layout';

const mockTx = [
  { id:'TX341', client:'Jean Dupont',  type:'Dépôt',    montant:'50 000 F',  commission:'750 F',   date:'12/06/2026 14:28' },
  { id:'TX340', client:'Alice Mballa', type:'Retrait',  montant:'20 000 F',  commission:'300 F',   date:'12/06/2026 14:10' },
  { id:'TX339', client:'Paul Nkomo',   type:'Transfert',montant:'100 000 F', commission:'1 500 F', date:'12/06/2026 13:55' },
  { id:'TX338', client:'Sophie Biya',  type:'Dépôt',    montant:'200 000 F', commission:'3 000 F', date:'12/06/2026 11:20' },
];
const typeColor: Record<string,string> = { Dépôt:'success', Retrait:'danger', Transfert:'info' };
const totalCommission = mockTx.reduce((acc, t) => acc + parseInt(t.commission.replace(/[^0-9]/g,'')), 0);

export default function OperateurTransactions() {
  const [filter, setFilter] = useState('');
  const filtered = mockTx.filter(t => !filter || t.type === filter);

  return (
    <Layout>
      <div className="page-title">Transactions</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Total transactions</div><div className="card-value blue">{mockTx.length}</div></div>
        <div className="card"><div className="card-label">Commissions totales</div><div className="card-value green">{totalCommission.toLocaleString()} F</div></div>
      </div>
      <div className="table-box">
        <div style={{display:'flex', gap:8, marginBottom:12}}>
          {['','Dépôt','Retrait','Transfert'].map(f => (
            <button key={f} className={`btn btn-sm ${filter===f?'btn-primary':''}`}
              style={filter!==f?{background:'#fff',border:'1px solid #ddd',color:'#333'}:{}}
              onClick={() => setFilter(f)}>{f||'Tous'}</button>
          ))}
        </div>
        <table>
          <thead><tr><th>ID</th><th>Client</th><th>Type</th><th>Montant</th><th>Commission</th><th>Date</th><th>Statut</th></tr></thead>
          <tbody>{filtered.map(t => (
            <tr key={t.id}><td>{t.id}</td><td>{t.client}</td>
              <td><span className={`badge ${typeColor[t.type]}`}>{t.type}</span></td>
              <td>{t.montant}</td><td style={{color:'#27ae60'}}>{t.commission}</td>
              <td style={{fontSize:'0.8rem'}}>{t.date}</td>
              <td><span className="badge success">Réussi</span></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </Layout>
  );
}
