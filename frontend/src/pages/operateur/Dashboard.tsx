import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { transactionApi, customerApi } from '../../api/services';

export default function OperateurDashboard() {
  const { user } = useAuth();
  const [clients, setClients] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    customerApi.getAll()
      .then(r => setClients(r.data?.data ?? r.data ?? []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="page-title">{user?.name} — Tableau de bord</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Clients</div><div className="card-value blue">{clients.length}</div></div>
        <div className="card"><div className="card-label">KYC validés</div><div className="card-value green">{clients.filter((c:any) => c.statutVerification==='VERIFIE').length}</div></div>
        <div className="card"><div className="card-label">KYC en attente</div><div className="card-value" style={{color:'#f39c12'}}>{clients.filter((c:any) => c.statutVerification==='EN_ATTENTE').length}</div></div>
      </div>
      <div className="table-box"><h3>Clients récents</h3>
        {loading ? <div>Chargement...</div> : clients.length === 0
          ? <p style={{color:'#888'}}>Aucun client.</p>
          : (
            <table>
              <thead><tr><th>Nom</th><th>Email</th><th>Score crédit</th><th>KYC</th></tr></thead>
              <tbody>{clients.slice(0, 10).map((c: any) => (
                <tr key={c.id}>
                  <td>{c.nom} {c.prenom}</td>
                  <td>{c.email}</td>
                  <td>{c.scoreCredit}</td>
                  <td><span className={`badge ${c.statutVerification==='VERIFIE'?'success':c.statutVerification==='REJETE'?'danger':'warning'}`}>{c.statutVerification}</span></td>
                </tr>
              ))}</tbody>
            </table>
          )
        }
      </div>
    </Layout>
  );
}
