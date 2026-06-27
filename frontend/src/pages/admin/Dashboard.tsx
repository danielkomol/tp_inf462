import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { auditApi, customerApi, operatorApi } from '../../api/services';

export default function AdminDashboard() {
  const [logs, setLogs] = useState<any[]>([]);
  const [stats, setStats] = useState<any>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      auditApi.getLogs({ taille: 10 }).catch(() => ({ data: [] })),
      auditApi.getStats().catch(() => ({ data: {} })),
    ]).then(([logsRes, statsRes]) => {
      setLogs(logsRes.data?.data ?? logsRes.data ?? []);
      setStats(statsRes.data?.data ?? statsRes.data ?? {});
    }).finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="page-title">Tableau de bord — Administration</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Événements aujourd'hui</div><div className="card-value blue">{stats.totalAujourdHui ?? '—'}</div></div>
        <div className="card"><div className="card-label">Opérateurs actifs</div><div className="card-value">{stats.operateursActifs ?? '—'}</div></div>
        <div className="card"><div className="card-label">Échecs de connexion</div><div className="card-value red">{stats.echecConnexion ?? '—'}</div></div>
        <div className="card"><div className="card-label">Actions admin</div><div className="card-value">{stats.actionsAdmin ?? '—'}</div></div>
      </div>
      <div className="table-box">
        <h3>Activité récente</h3>
        {loading ? <div>Chargement...</div> : logs.length === 0 ? <p style={{color:'#888'}}>Aucun log disponible.</p> : (
          <table>
            <thead><tr><th>Action</th><th>Utilisateur</th><th>Service</th><th>Date</th></tr></thead>
            <tbody>{logs.map((a: any, i: number) => (
              <tr key={i}>
                <td><span className="badge info">{a.eventType ?? a.action}</span></td>
                <td>{a.userId ?? a.user}</td>
                <td style={{fontSize:'0.8rem',color:'#888'}}>{a.service}</td>
                <td style={{fontSize:'0.8rem'}}>{a.timestamp ? new Date(a.timestamp).toLocaleString('fr-FR') : a.date}</td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
