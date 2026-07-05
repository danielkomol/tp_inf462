import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { auditApi } from '../../api/services';

const actionColor: Record<string, string> = {
  LOGIN:'info', TRANSFER:'success', REGISTER:'info',
  LOAN_REQUEST:'warning', LOGIN_FAILED:'danger', KYC_VALIDATE:'success',
  USER_CREATED:'info', TRANSACTION_VALIDATED:'success',
};

export default function Audit() {
  const [logs, setLogs] = useState<any[]>([]);
  const [stats, setStats] = useState<any>({});
  const [filter, setFilter] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      auditApi.getLogs({ taille: 100 }).catch(() => ({ data: {} })),
      auditApi.getStats().catch(() => ({ data: {} })),
    ]).then(([logsRes, statsRes]) => {
      const logsData = logsRes.data?.data ?? logsRes.data ?? {};
      const logsList = Array.isArray(logsData) ? logsData : (logsData.logs ?? []);
      setLogs(logsList);
      setStats(statsRes.data?.data ?? statsRes.data ?? {});
    }).finally(() => setLoading(false));
  }, []);

  const filtered = logs.filter((a: any) =>
    !filter ||
    (a.eventType ?? a.action ?? '').toLowerCase().includes(filter.toLowerCase()) ||
    (a.userId ?? a.user ?? '').toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <Layout>
      <div className="page-title">Journal d'audit</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Total logs</div><div className="card-value blue">{logs.length}</div></div>
        <div className="card"><div className="card-label">Erreurs</div><div className="card-value" style={{color:'#e74c3c'}}>{logs.filter((l:any) => l.eventType?.includes('FAILED') || l.eventType?.includes('ERROR')).length}</div></div>
        <div className="card"><div className="card-label">Services actifs</div><div className="card-value">{[...new Set(logs.map((l:any) => l.service))].length}</div></div>
      </div>
      <div className="table-box">
        <div style={{marginBottom:12}}>
          <input placeholder="Filtrer par action ou utilisateur..." value={filter} onChange={e => setFilter(e.target.value)}
            style={{padding:'8px 12px',border:'1px solid #ddd',borderRadius:6,width:320,fontSize:'0.88rem'}} />
        </div>
        {loading ? <div>Chargement...</div> : filtered.length === 0 ? <p style={{color:'#888'}}>Aucun log.</p> : (
          <table>
            <thead><tr><th>Action</th><th>Utilisateur</th><th>Service</th><th>IP</th><th>Date</th></tr></thead>
            <tbody>{filtered.map((a: any, i: number) => (
              <tr key={i}>
                <td><span className={`badge ${actionColor[a.eventType ?? a.action] ?? 'info'}`}>{a.eventType ?? a.action}</span></td>
                <td>{a.description || a.userId || '—'}</td>
                <td style={{fontSize:'0.8rem',color:'#888'}}>{a.service}</td>
                <td>{a.ipAddress ?? a.ip ?? '—'}</td>
                <td style={{fontSize:'0.8rem'}}>{a.timestamp ? new Date(a.timestamp).toLocaleString('fr-FR') : a.date}</td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
