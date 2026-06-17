import Layout from '../../components/Layout';

const recentAudit = [
  { action:'LOGIN',    user:'client@bank.com',     ip:'192.168.1.10', time:'14:32' },
  { action:'TRANSFER', user:'jean.dupont@mail.com', ip:'192.168.1.12', time:'14:15' },
  { action:'REGISTER', user:'alice@mail.com',       ip:'192.168.1.8',  time:'13:50' },
  { action:'LOAN_REQ', user:'client@bank.com',      ip:'192.168.1.10', time:'13:20' },
];

export default function AdminDashboard() {
  return (
    <Layout>
      <div className="page-title">Tableau de bord — Administration</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Utilisateurs total</div><div className="card-value blue">1 248</div></div>
        <div className="card"><div className="card-label">Opérateurs actifs</div><div className="card-value">3</div></div>
        <div className="card"><div className="card-label">Transactions aujourd'hui</div><div className="card-value green">342</div></div>
        <div className="card"><div className="card-label">Prêts en attente</div><div className="card-value" style={{color:'#f39c12'}}>12</div></div>
      </div>
      <div className="table-box">
        <h3>Activité récente</h3>
        <table>
          <thead><tr><th>Action</th><th>Utilisateur</th><th>IP</th><th>Heure</th></tr></thead>
          <tbody>{recentAudit.map((a,i) => (
            <tr key={i}><td><span className="badge info">{a.action}</span></td><td>{a.user}</td><td>{a.ip}</td><td>{a.time}</td></tr>
          ))}</tbody>
        </table>
      </div>
    </Layout>
  );
}
