import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { accountApi, transactionApi } from '../../api/services';

export default function ClientDashboard() {
  const { user } = useAuth();
  const [comptes, setComptes] = useState<any[]>([]);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    Promise.all([
      accountApi.getAll().catch(() => ({ data: [] })),
      transactionApi.historique(user.id).catch(() => ({ data: [] })),
    ]).then(([accRes, txRes]) => {
      setComptes(accRes.data?.data ?? accRes.data ?? []);
      const txData = txRes.data?.data ?? txRes.data ?? [];
      setTransactions(Array.isArray(txData) ? txData.slice(0, 5) : []);
    }).finally(() => setLoading(false));
  }, [user]);

  const soldeTotal = comptes.reduce((s: number, c: any) => s + (c.balance ?? c.solde ?? 0), 0);

  return (
    <Layout>
      <div className="page-title">Bonjour, {user?.name} 👋</div>
      {loading ? <div>Chargement...</div> : (
        <>
          <div className="cards-row">
            <div className="card">
              <div className="card-label">Solde total</div>
              <div className="card-value green">{soldeTotal.toLocaleString()} XAF</div>
            </div>
            <div className="card">
              <div className="card-label">Comptes actifs</div>
              <div className="card-value blue">{comptes.filter((c: any) => c.status === 'ACTIVE' || c.statut === 'ACTIF').length || comptes.length}</div>
            </div>
            <div className="card">
              <div className="card-label">Transactions</div>
              <div className="card-value">{transactions.length}</div>
            </div>
          </div>
          <div className="table-box">
            <h3>Dernières transactions</h3>
            {transactions.length === 0 ? <p style={{color:'#888'}}>Aucune transaction pour le moment.</p> : (
              <table>
                <thead><tr><th>Référence</th><th>Type</th><th>Montant</th><th>Date</th><th>Statut</th></tr></thead>
                <tbody>
                  {transactions.map((tx: any) => (
                    <tr key={tx.id ?? tx.reference}>
                      <td>{tx.reference}</td>
                      <td>{tx.type}</td>
                      <td><strong>{(tx.montant ?? tx.amount ?? 0).toLocaleString()} XAF</strong></td>
                      <td>{tx.createdAt ? new Date(tx.createdAt).toLocaleDateString('fr-FR') : '-'}</td>
                      <td><span className={`badge ${tx.status === 'VALIDEE' || tx.status === 'SUCCESS' ? 'success' : 'warning'}`}>{tx.status ?? tx.statut}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </Layout>
  );
}
