import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { reportingApi } from '../../api/services';

export default function Rapports() {
  const [tab, setTab] = useState('transactions');
  const [txStats, setTxStats] = useState<any>(null);
  const [loanStats, setLoanStats] = useState<any>(null);
  const [opStats, setOpStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      reportingApi.getTransactionStats().catch(() => ({ data: null })),
      reportingApi.getLoanStats().catch(() => ({ data: null })),
      reportingApi.getOperatorStats().catch(() => ({ data: null })),
    ]).then(([tx, loan, op]) => {
      setTxStats(tx.data?.data ?? tx.data);
      setLoanStats(loan.data?.data ?? loan.data);
      setOpStats(op.data?.data ?? op.data);
    }).finally(() => setLoading(false));
  }, []);

  return (
    <Layout>
      <div className="page-title">Rapports & Statistiques</div>
      <div style={{display:'flex', gap:10, marginBottom:20}}>
        {['transactions','prets','operateurs'].map(t => (
          <button key={t} className={`btn ${tab===t?'btn-primary':''}`}
            style={tab!==t?{background:'#fff',border:'1px solid #ddd',color:'#333'}:{}}
            onClick={() => setTab(t)}>
            {t==='transactions'?'💸 Transactions': t==='prets'?'🏦 Prêts':'🏢 Opérateurs'}
          </button>
        ))}
      </div>
      {loading ? <div>Chargement des statistiques...</div> : (
        <>
          {tab === 'transactions' && txStats && (
            <>
              <div className="cards-row">
                <div className="card"><div className="card-label">Total transactions</div><div className="card-value blue">{txStats.nombre_total_transactions ?? txStats.total ?? '—'}</div></div>
                <div className="card"><div className="card-label">Volume total</div><div className="card-value green">{(txStats.montant_total ?? 0).toLocaleString()} XAF</div></div>
                <div className="card"><div className="card-label">Montant moyen</div><div className="card-value">{(txStats.montant_moyen ?? 0).toLocaleString()} XAF</div></div>
              </div>
              {txStats.repartition_par_type && (
                <div className="table-box"><h3>Répartition par type</h3>
                  <table>
                    <thead><tr><th>Type</th><th>Nombre</th></tr></thead>
                    <tbody>{txStats.repartition_par_type.map((r: any) => (
                      <tr key={r.key ?? r.type}><td>{r.key ?? r.type}</td><td>{r.doc_count ?? r.count}</td></tr>
                    ))}</tbody>
                  </table>
                </div>
              )}
            </>
          )}
          {tab === 'prets' && loanStats && (
            <div className="table-box"><h3>Statistiques prêts</h3>
              <p>Total : <strong>{loanStats.total ?? '—'}</strong> &nbsp;|&nbsp;
                Actifs : <strong>{loanStats.actifs ?? '—'}</strong> &nbsp;|&nbsp;
                Défauts : <strong style={{color:'#e74c3c'}}>{loanStats.defauts ?? '—'}</strong>
              </p>
            </div>
          )}
          {tab === 'operateurs' && opStats && (
            <div className="table-box"><h3>Revenus par opérateur</h3>
              <pre style={{fontSize:'0.85rem'}}>{JSON.stringify(opStats, null, 2)}</pre>
            </div>
          )}
          {(tab === 'transactions' && !txStats) || (tab === 'prets' && !loanStats) || (tab === 'operateurs' && !opStats)
            ? <p style={{color:'#888'}}>Aucune donnée disponible pour le moment.</p>
            : null
          }
        </>
      )}
    </Layout>
  );
}
