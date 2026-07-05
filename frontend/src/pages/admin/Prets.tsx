import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { loanApi } from '../../api/services';
import api from '../../api/axios';

export default function AdminPrets() {
  const [prets, setPrets] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('SOUMISE');

  const fetchPrets = () => {
    setLoading(true);
    // Récupère toutes les demandes en attente de tous les opérateurs
    api.get('/api/v1/loans/all')
      .then(r => setPrets(r.data?.data ?? r.data ?? []))
      .catch(() => {
        // Fallback: essayer avec un endpoint alternatif
        api.get('/api/v1/loans/pending/all')
          .then(r => setPrets(r.data?.data ?? r.data ?? []))
          .catch(() => setPrets([]))
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchPrets(); }, []);

  const handleValider = async (id: number, approuve: boolean) => {
    setError('');
    try {
      await loanApi.valider(id, { approuve });
      setMsg(approuve ? '✅ Prêt approuvé !' : '❌ Prêt rejeté.');
      fetchPrets();
      setTimeout(() => setMsg(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erreur lors de la validation');
    }
  };

  const filtered = prets.filter((p: any) =>
    filter === 'TOUS' || p.statut === filter
  );

  const stats = {
    total: prets.length,
    soumises: prets.filter((p: any) => p.statut === 'SOUMISE').length,
    enCours: prets.filter((p: any) => p.statut === 'EN_COURS').length,
    validees: prets.filter((p: any) => p.statut === 'VALIDEE' || p.statut === 'EN_COURS').length,
    rejetees: prets.filter((p: any) => p.statut === 'REJETEE').length,
  };

  return (
    <Layout>
      <div className="page-title">Gestion des Prêts</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}

      <div className="cards-row">
        <div className="card"><div className="card-label">Total demandes</div><div className="card-value blue">{stats.total}</div></div>
        <div className="card"><div className="card-label">En attente</div><div className="card-value" style={{color:'#f39c12'}}>{stats.soumises}</div></div>
        <div className="card"><div className="card-label">En cours</div><div className="card-value green">{stats.enCours}</div></div>
        <div className="card"><div className="card-label">Rejetées</div><div className="card-value" style={{color:'#e74c3c'}}>{stats.rejetees}</div></div>
      </div>

      {/* Filtres */}
      <div style={{display:'flex', gap:8, marginBottom:16}}>
        {['TOUS','SOUMISE','EN_COURS','VALIDEE','REJETEE','SOLDEE'].map(s => (
          <button key={s} className={`btn btn-sm ${filter===s?'btn-primary':''}`}
            style={filter!==s?{background:'#fff',border:'1px solid #ddd',color:'#555'}:{}}
            onClick={() => setFilter(s)}>{s}</button>
        ))}
      </div>

      <div className="table-box">
        {loading ? <div>Chargement...</div> : filtered.length === 0
          ? <p style={{color:'#888'}}>Aucune demande {filter !== 'TOUS' ? `avec le statut "${filter}"` : ''}.</p>
          : (
            <table>
              <thead>
                <tr>
                  <th>Référence</th>
                  <th>Client</th>
                  <th>Montant demandé</th>
                  <th>Durée</th>
                  <th>Motif</th>
                  <th>Statut</th>
                  <th>Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((p: any) => (
                  <tr key={p.id}>
                    <td><strong>{p.reference}</strong></td>
                    <td style={{fontSize:'0.8rem'}}>{p.clientId?.slice(0, 12)}...</td>
                    <td><strong>{(p.montantDemande ?? 0).toLocaleString()} XAF</strong></td>
                    <td>{p.duree} mois</td>
                    <td style={{maxWidth:150, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>{p.motif}</td>
                    <td>
                      <span className={`badge ${
                        p.statut === 'EN_COURS' || p.statut === 'VALIDEE' ? 'success' :
                        p.statut === 'REJETEE'  ? 'danger' :
                        p.statut === 'SOLDEE'   ? 'info' : 'warning'
                      }`}>{p.statut}</span>
                    </td>
                    <td style={{fontSize:'0.8rem'}}>{p.createdAt ? new Date(p.createdAt).toLocaleDateString('fr-FR') : '—'}</td>
                    <td>
                      {(p.statut === 'SOUMISE' || p.statut === 'EN_ANALYSE') && (
                        <div style={{display:'flex', gap:4}}>
                          <button className="btn btn-sm btn-success" onClick={() => handleValider(p.id, true)}>✓ Approuver</button>
                          <button className="btn btn-sm btn-danger"  onClick={() => handleValider(p.id, false)}>✗ Rejeter</button>
                        </div>
                      )}
                      {p.statut !== 'SOUMISE' && p.statut !== 'EN_ANALYSE' && (
                        <span style={{color:'#aaa', fontSize:'0.8rem'}}>—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )
        }
      </div>
    </Layout>
  );
}
