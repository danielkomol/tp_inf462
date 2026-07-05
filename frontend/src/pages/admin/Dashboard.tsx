import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { auditApi, customerApi, operatorApi, reportingApi } from '../../api/services';

export default function AdminDashboard() {
  const [logs, setLogs] = useState<any[]>([]);
  const [customers, setCustomers] = useState<any[]>([]);
  const [operators, setOperators] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      auditApi.getLogs({ taille: 10 }).catch(() => ({ data: {} })),
      customerApi.getAll().catch(() => ({ data: [] })),
      operatorApi.getAll().catch(() => ({ data: [] })),
    ]).then(([logsRes, custRes, opRes]) => {
      const logsData = logsRes.data?.data ?? logsRes.data ?? {};
      setLogs(Array.isArray(logsData) ? logsData : (logsData.logs ?? []));
      setCustomers(custRes.data?.data ?? custRes.data ?? []);
      setOperators(opRes.data?.data ?? opRes.data ?? []);
    }).finally(() => setLoading(false));
  }, []);

  const activeOps = operators.filter((o: any) => o.statut === 'ACTIF' || o.status === 'ACTIF').length;
  const pendingKyc = customers.filter((c: any) => c.statutVerification === 'EN_ATTENTE').length;

  return (
    <Layout>
      <div className="page-title">Tableau de bord — Administration</div>
      {loading ? <div>Chargement...</div> : (
        <>
          <div className="cards-row">
            <div className="card">
              <div className="card-label">Total clients</div>
              <div className="card-value blue">{customers.length}</div>
            </div>
            <div className="card">
              <div className="card-label">Opérateurs actifs</div>
              <div className="card-value green">{activeOps}</div>
            </div>
            <div className="card">
              <div className="card-label">KYC en attente</div>
              <div className="card-value" style={{color:'#f39c12'}}>{pendingKyc}</div>
            </div>
            <div className="card">
              <div className="card-label">Logs d'audit</div>
              <div className="card-value">{logs.length}</div>
            </div>
          </div>

          <div className="table-box">
            <h3>Activité récente</h3>
            {logs.length === 0
              ? <p style={{color:'#888'}}>Aucune activité enregistrée pour le moment.</p>
              : (
                <table>
                  <thead><tr><th>Action</th><th>Utilisateur</th><th>Service</th><th>Date</th></tr></thead>
                  <tbody>{logs.map((a: any, i: number) => (
                    <tr key={i}>
                      <td><span className="badge info">{a.eventType ?? a.action}</span></td>
                      <td>{a.description ?? a.userId ?? '—'}</td>
                      <td style={{fontSize:'0.8rem',color:'#888'}}>{a.service}</td>
                      <td style={{fontSize:'0.8rem'}}>{a.timestamp ? new Date(a.timestamp).toLocaleString('fr-FR') : '—'}</td>
                    </tr>
                  ))}</tbody>
                </table>
              )
            }
          </div>

          {customers.length > 0 && (
            <div className="table-box" style={{marginTop:20}}>
              <h3>Derniers clients inscrits</h3>
              <table>
                <thead><tr><th>Nom</th><th>Email</th><th>KYC</th><th>Score</th></tr></thead>
                <tbody>{customers.slice(0, 5).map((c: any) => (
                  <tr key={c.id}>
                    <td><strong>{c.nom} {c.prenom}</strong></td>
                    <td>{c.email}</td>
                    <td><span className={`badge ${c.statutVerification==='VERIFIE'?'success':c.statutVerification==='REJETE'?'danger':'warning'}`}>{c.statutVerification}</span></td>
                    <td>{c.scoreCredit}</td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
          )}
        </>
      )}
    </Layout>
  );
}
