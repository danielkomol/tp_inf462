import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';

export default function OperateurDashboard() {
  const { user } = useAuth();
  return (
    <Layout>
      <div className="page-title">{user?.name} — Tableau de bord</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Clients actifs</div><div className="card-value blue">412</div></div>
        <div className="card"><div className="card-label">Transactions du jour</div><div className="card-value green">98</div></div>
        <div className="card"><div className="card-label">Volume du jour</div><div className="card-value green">4 820 000 F</div></div>
        <div className="card"><div className="card-label">Commissions du mois</div><div className="card-value">123 000 F</div></div>
      </div>
      <div className="table-box"><h3>Dernières transactions</h3>
        <table>
          <thead><tr><th>ID</th><th>Client</th><th>Type</th><th>Montant</th><th>Commission</th><th>Heure</th></tr></thead>
          <tbody>
            <tr><td>TX341</td><td>Jean Dupont</td><td><span className="badge success">Dépôt</span></td><td>50 000 F</td><td>750 F</td><td>14:28</td></tr>
            <tr><td>TX340</td><td>Alice Mballa</td><td><span className="badge danger">Retrait</span></td><td>20 000 F</td><td>300 F</td><td>14:10</td></tr>
            <tr><td>TX339</td><td>Paul Nkomo</td><td><span className="badge info">Transfert</span></td><td>100 000 F</td><td>1 500 F</td><td>13:55</td></tr>
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
