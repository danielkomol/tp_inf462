import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';

const recentTx = [
  { id:1, type:'Dépôt',    montant:'+150 000 F', date:'12/06/2026' },
  { id:2, type:'Retrait',  montant:'-30 000 F',  date:'11/06/2026' },
  { id:3, type:'Transfert',montant:'-50 000 F',  date:'10/06/2026' },
  { id:4, type:'Dépôt',    montant:'+200 000 F', date:'08/06/2026' },
];

export default function ClientDashboard() {
  const { user } = useAuth();
  return (
    <Layout>
      <div className="page-title">Bonjour, {user?.name} 👋</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Solde principal</div><div className="card-value green">1 240 000 F</div></div>
        <div className="card"><div className="card-label">Comptes actifs</div><div className="card-value blue">2</div></div>
        <div className="card"><div className="card-label">Prêts en cours</div><div className="card-value">1</div></div>
        <div className="card"><div className="card-label">Notifications</div><div className="card-value blue">3</div></div>
      </div>
      <div className="table-box">
        <h3>Dernières transactions</h3>
        <table>
          <thead><tr><th>Type</th><th>Montant</th><th>Date</th><th>Statut</th></tr></thead>
          <tbody>
            {recentTx.map(tx => (
              <tr key={tx.id}>
                <td>{tx.type}</td>
                <td><strong>{tx.montant}</strong></td>
                <td>{tx.date}</td>
                <td><span className="badge success">Réussi</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
