import { useState } from 'react';
import Layout from '../../components/Layout';

const mockAudit = [
  { id:'A001', action:'LOGIN',        user:'jean@mail.com',  service:'identity-service',    ip:'192.168.1.10', date:'12/06/2026 14:32', statut:'SUCCESS' },
  { id:'A002', action:'TRANSFER',     user:'jean@mail.com',  service:'transaction-service', ip:'192.168.1.10', date:'12/06/2026 14:15', statut:'SUCCESS' },
  { id:'A003', action:'REGISTER',     user:'alice@mail.com', service:'identity-service',    ip:'192.168.1.8',  date:'12/06/2026 13:50', statut:'SUCCESS' },
  { id:'A004', action:'LOAN_REQUEST', user:'jean@mail.com',  service:'loan-service',        ip:'192.168.1.10', date:'12/06/2026 13:20', statut:'SUCCESS' },
  { id:'A005', action:'LOGIN_FAILED', user:'inconnu@x.com',  service:'identity-service',    ip:'41.202.219.50',date:'12/06/2026 12:05', statut:'FAILED'  },
  { id:'A006', action:'KYC_VALIDATE', user:'admin@bank.com', service:'customer-service',    ip:'192.168.1.1',  date:'12/06/2026 11:30', statut:'SUCCESS' },
];
const actionColor: Record<string,string> = { LOGIN:'info', TRANSFER:'success', REGISTER:'info', LOAN_REQUEST:'warning', LOGIN_FAILED:'danger', KYC_VALIDATE:'success' };

export default function Audit() {
  const [filter, setFilter] = useState('');
  const filtered = mockAudit.filter(a => !filter || a.action.includes(filter) || a.user.includes(filter));

  return (
    <Layout>
      <div className="page-title">Journal d'audit</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Événements aujourd'hui</div><div className="card-value blue">342</div></div>
        <div className="card"><div className="card-label">Échecs de connexion</div><div className="card-value red">5</div></div>
        <div className="card"><div className="card-label">Actions admin</div><div className="card-value">18</div></div>
      </div>
      <div className="table-box">
        <div style={{marginBottom:12}}>
          <input placeholder="Filtrer par action ou utilisateur..." value={filter} onChange={e => setFilter(e.target.value)}
            style={{padding:'8px 12px',border:'1px solid #ddd',borderRadius:6,width:320,fontSize:'0.88rem'}} />
        </div>
        <table>
          <thead><tr><th>ID</th><th>Action</th><th>Utilisateur</th><th>Service</th><th>IP</th><th>Date</th><th>Statut</th></tr></thead>
          <tbody>{filtered.map(a => (
            <tr key={a.id}>
              <td>{a.id}</td>
              <td><span className={`badge ${actionColor[a.action]||'info'}`}>{a.action}</span></td>
              <td>{a.user}</td>
              <td style={{fontSize:'0.8rem',color:'#888'}}>{a.service}</td>
              <td>{a.ip}</td>
              <td style={{fontSize:'0.8rem'}}>{a.date}</td>
              <td><span className={`badge ${a.statut==='SUCCESS'?'success':'danger'}`}>{a.statut}</span></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </Layout>
  );
}
