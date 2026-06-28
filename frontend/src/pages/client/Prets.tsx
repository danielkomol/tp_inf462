import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { loanApi, accountApi, operatorApi } from '../../api/services';

export default function Prets() {
  const { user } = useAuth();
  const [tab, setTab] = useState('liste');
  const [prets, setPrets] = useState<any[]>([]);
  const [comptes, setComptes] = useState<any[]>([]);
  const [operateurs, setOperateurs] = useState<any[]>([]);
  const [form, setForm] = useState({ montantDemande: '', duree: '12', motif: '', compteVersement: '', operateurId: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const fetchPrets = () => {
    if (!user) return;
    loanApi.getMyLoans(user.id)
      .then(r => setPrets(r.data?.data ?? r.data ?? []))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchPrets();
    accountApi.getAll(user.id).then(r => {
      const d = r.data?.data ?? r.data ?? [];
      setComptes(d);
      if (d.length > 0) setForm(f => ({ ...f, compteVersement: d[0].accountNumber ?? d[0].id }));
    }).catch(() => {});
    operatorApi.getAll().then(r => {
      const ops = r.data?.data ?? r.data ?? [];
      setOperateurs(ops);
      if (ops.length > 0) setForm(f => ({ ...f, operateurId: String(ops[0].id ?? ops[0].code) }));
    }).catch(() => {});
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setError(''); setSubmitting(true);
    try {
      await loanApi.soumettre({
        clientId: user.id,
        operateurId: form.operateurId,
        montantDemande: Number(form.montantDemande),
        duree: Number(form.duree),
        motif: form.motif,
        compteVersement: form.compteVersement,
      });
      setMsg('Demande soumise. En attente de validation.');
      setTab('liste');
      fetchPrets();
      setTimeout(() => setMsg(''), 4000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de la soumission");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout>
      <div className="page-title">Prêts</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      <div style={{display:'flex', gap:10, marginBottom:20}}>
        {['liste','demande','echeancier'].map(t => (
          <button key={t} className={`btn ${tab===t?'btn-primary':''}`}
            style={tab!==t?{background:'#fff',border:'1px solid #ddd',color:'#333'}:{}}
            onClick={() => setTab(t)}>
            {t==='liste'?'📋 Mes prêts': t==='demande'?'+ Nouvelle demande':'📅 Échéancier'}
          </button>
        ))}
      </div>

      {tab === 'liste' && (
        loading ? <div>Chargement...</div> : (
          <div className="table-box">
            {prets.length === 0 ? <p style={{color:'#888'}}>Aucun prêt en cours.</p> : (
              <table>
                <thead><tr><th>Référence</th><th>Montant demandé</th><th>Accordé</th><th>Durée</th><th>Taux</th><th>Statut</th></tr></thead>
                <tbody>{prets.map((p: any) => (
                  <tr key={p.id}>
                    <td>{p.reference}</td>
                    <td>{(p.montantDemande ?? 0).toLocaleString()} XAF</td>
                    <td>{p.montantAccorde ? p.montantAccorde.toLocaleString() + ' XAF' : '—'}</td>
                    <td>{p.duree} mois</td>
                    <td>{p.tauxInteret}%</td>
                    <td><span className={`badge ${p.statut==='EN_COURS'?'success': p.statut==='REJETEE'?'danger':'warning'}`}>{p.statut}</span></td>
                  </tr>
                ))}</tbody>
              </table>
            )}
          </div>
        )
      )}

      {tab === 'demande' && (
        <div className="form-box"><h2>Demande de prêt</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group"><label>Montant (XAF)</label>
              <input type="number" min="10000" required value={form.montantDemande}
                onChange={e => setForm({...form, montantDemande: e.target.value})} /></div>
            <div className="form-group"><label>Durée (mois)</label>
              <select value={form.duree} onChange={e => setForm({...form, duree: e.target.value})}>
                {[6,12,18,24,36,48,60].map(d => <option key={d} value={d}>{d}</option>)}
              </select></div>
            <div className="form-group"><label>Compte de versement</label>
              <select value={form.compteVersement} onChange={e => setForm({...form, compteVersement: e.target.value})}>
                {comptes.map((c: any) => (
                  <option key={c.id} value={c.accountNumber ?? c.id}>{c.accountType} — {c.accountNumber ?? c.id}</option>
                ))}
              </select></div>
            <div className="form-group"><label>Opérateur</label>
              <select value={form.operateurId} onChange={e => setForm({...form, operateurId: e.target.value})}>
                {operateurs.map((op: any) => (
                  <option key={op.id} value={String(op.id ?? op.code)}>{op.nom ?? op.name}</option>
                ))}
              </select></div>
            <div className="form-group"><label>Motif du prêt</label>
              <textarea rows={3} required value={form.motif} placeholder="Objet du prêt..."
                onChange={e => setForm({...form, motif: e.target.value})} /></div>
            <button className="btn btn-success" type="submit" disabled={submitting}>
              {submitting ? 'Envoi...' : 'Soumettre'}
            </button>
          </form>
        </div>
      )}

      {tab === 'echeancier' && (
        <div className="table-box">
          {prets.filter((p: any) => p.echeances?.length > 0).length === 0
            ? <p style={{color:'#888'}}>Pas d'échéancier disponible (prêt non encore validé).</p>
            : prets.filter((p: any) => p.echeances?.length > 0).map((p: any) => (
              <div key={p.id}>
                <h3>Échéancier — {p.reference}</h3>
                <table>
                  <thead><tr><th>#</th><th>Date</th><th>Capital</th><th>Intérêts</th><th>Total</th><th>Statut</th></tr></thead>
                  <tbody>{p.echeances.map((e: any) => (
                    <tr key={e.id}>
                      <td>{e.numero}</td>
                      <td>{e.dateEcheance}</td>
                      <td>{(e.partCapital ?? 0).toLocaleString()} XAF</td>
                      <td>{(e.partInteret ?? 0).toLocaleString()} XAF</td>
                      <td><strong>{(e.montantTotal ?? 0).toLocaleString()} XAF</strong></td>
                      <td><span className={`badge ${e.statut==='PAYEE'?'success': e.statut==='EN_RETARD'?'danger':'warning'}`}>{e.statut}</span></td>
                    </tr>
                  ))}</tbody>
                </table>
              </div>
            ))
          }
        </div>
      )}
    </Layout>
  );
}
