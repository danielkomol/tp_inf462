import { useState } from 'react';
import Layout from '../../components/Layout';

const txParMois = [
  { mois:'Janvier', depot:1200000, retrait:800000,  transfert:400000 },
  { mois:'Février', depot:1450000, retrait:950000,  transfert:520000 },
  { mois:'Mars',    depot:1100000, retrait:700000,  transfert:380000 },
  { mois:'Avril',   depot:1650000, retrait:1100000, transfert:620000 },
  { mois:'Mai',     depot:1800000, retrait:1200000, transfert:700000 },
  { mois:'Juin',    depot:980000,  retrait:620000,  transfert:310000 },
];
const pretsStats = [
  { operateur:'MTN MoMo',     total:45, actifs:30, rembourses:12, defauts:3 },
  { operateur:'Orange Money', total:28, actifs:20, rembourses:7,  defauts:1 },
  { operateur:'Express Union',total:15, actifs:10, rembourses:4,  defauts:1 },
];

export default function Rapports() {
  const [tab, setTab] = useState('transactions');

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
      {tab === 'transactions' && (
        <>
          <div className="cards-row">
            <div className="card"><div className="card-label">Volume total (juin)</div><div className="card-value green">1 910 000 F</div></div>
            <div className="card"><div className="card-label">Transactions du mois</div><div className="card-value blue">1 842</div></div>
            <div className="card"><div className="card-label">Moyenne par jour</div><div className="card-value">61</div></div>
          </div>
          <div className="table-box"><h3>Volume par mois (FCFA)</h3>
            <table>
              <thead><tr><th>Mois</th><th>Dépôts</th><th>Retraits</th><th>Transferts</th><th>Total</th></tr></thead>
              <tbody>{txParMois.map(r => (
                <tr key={r.mois}><td>{r.mois}</td>
                  <td style={{color:'#27ae60'}}>{r.depot.toLocaleString()} F</td>
                  <td style={{color:'#e74c3c'}}>{r.retrait.toLocaleString()} F</td>
                  <td style={{color:'#2980b9'}}>{r.transfert.toLocaleString()} F</td>
                  <td><strong>{(r.depot+r.retrait+r.transfert).toLocaleString()} F</strong></td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        </>
      )}
      {tab === 'prets' && (
        <div className="table-box"><h3>Statistiques prêts par opérateur</h3>
          <table>
            <thead><tr><th>Opérateur</th><th>Total</th><th>Actifs</th><th>Remboursés</th><th>Défauts</th><th>Taux défaut</th></tr></thead>
            <tbody>{pretsStats.map(p => (
              <tr key={p.operateur}><td>{p.operateur}</td><td>{p.total}</td>
                <td><span className="badge info">{p.actifs}</span></td>
                <td><span className="badge success">{p.rembourses}</span></td>
                <td><span className="badge danger">{p.defauts}</span></td>
                <td>{((p.defauts/p.total)*100).toFixed(1)}%</td>
              </tr>
            ))}</tbody>
          </table>
        </div>
      )}
      {tab === 'operateurs' && (
        <div className="table-box"><h3>Revenus par opérateur</h3>
          <table>
            <thead><tr><th>Opérateur</th><th>Transactions</th><th>Volume</th><th>Commission</th><th>Revenus</th></tr></thead>
            <tbody>
              <tr><td>MTN MoMo</td><td>982</td><td>8 200 000 F</td><td>1.5%</td><td><strong>123 000 F</strong></td></tr>
              <tr><td>Orange Money</td><td>615</td><td>4 800 000 F</td><td>1.8%</td><td><strong>86 400 F</strong></td></tr>
              <tr><td>Express Union</td><td>245</td><td>1 900 000 F</td><td>2.0%</td><td><strong>38 000 F</strong></td></tr>
            </tbody>
          </table>
        </div>
      )}
    </Layout>
  );
}
