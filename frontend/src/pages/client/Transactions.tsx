import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { transactionApi, accountApi } from '../../api/services';

const typeColor: Record<string, string> = { DEPOT:'success', RETRAIT:'danger', TRANSFERT_INTRA:'info', TRANSFERT_INTER:'info' };

export default function Transactions() {
  const { user } = useAuth();
  const [tab, setTab] = useState('historique');
  const [transactions, setTransactions] = useState<any[]>([]);
  const [comptes, setComptes] = useState<any[]>([]);
  const [tousLesComptes, setTousLesComptes] = useState<any[]>([]);
  const [form, setForm] = useState({ type: 'DEPOT', montant: '', compteSource: '', compteDestinataire: '', description: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [destMode, setDestMode] = useState<'mes-comptes' | 'tous' | 'manuel'>('mes-comptes');

  const fetchTx = () => {
    if (!user) return;
    transactionApi.historique(user.id)
      .then(r => setTransactions(r.data?.data ?? r.data ?? []))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (!user) return;
    fetchTx();
    // Mes comptes (source)
    accountApi.getAll(user.id).then(r => {
      const data = r.data?.data ?? r.data ?? [];
      setComptes(data);
      if (data.length > 0) setForm(f => ({ ...f, compteSource: data[0].accountNumber ?? data[0].id }));
    }).catch(() => {});
    // Tous les comptes de la plateforme (destinataire)
    accountApi.getAllAccounts().then(r => {
      const data = r.data?.data ?? r.data ?? [];
      setTousLesComptes(data);
    }).catch(() => {});
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setError(''); setSubmitting(true);
    try {
      const compte = comptes.find((c: any) => (c.accountNumber ?? c.id) === form.compteSource);
      const operateurId = compte?.operatorId ?? '';
      const accountId = compte?.id;

      if (form.type === 'DEPOT') {
        await transactionApi.depot({ compteDestinataire: form.compteSource, montant: Number(form.montant), clientId: user.id, operateurId, description: form.description || 'Dépôt' });
        if (accountId) await accountApi.credit(accountId, Number(form.montant), form.description || 'Dépôt');
      } else if (form.type === 'RETRAIT') {
        await transactionApi.retrait({ compteSource: form.compteSource, montant: Number(form.montant), clientId: user.id, operateurId, description: form.description || 'Retrait' });
        if (accountId) await accountApi.debit(accountId, Number(form.montant), form.description || 'Retrait');
      } else {
        await transactionApi.transfert({ compteSource: form.compteSource, compteDestinataire: form.compteDestinataire, montant: Number(form.montant), clientId: user.id, operateurSourceId: operateurId, description: form.description || 'Transfert' });
        // Débiter le compte source
        if (accountId) await accountApi.debit(accountId, Number(form.montant), form.description || 'Transfert');
        // Créditer le compte destinataire
        const allComptes = [...comptes, ...tousLesComptes];
        const compteDest = allComptes.find((c: any) => (c.accountNumber ?? c.id) === form.compteDestinataire);
        if (compteDest?.id) {
          await accountApi.credit(compteDest.id, Number(form.montant), form.description || 'Transfert reçu');
        } else {
          // Saisie manuelle — récupérer le compte par son numéro
          try {
            const r = await accountApi.getByNumber(form.compteDestinataire);
            const dest = r.data?.data ?? r.data;
            if (dest?.id) await accountApi.credit(dest.id, Number(form.montant), form.description || 'Transfert reçu');
          } catch { /* compte non trouvé */ }
        }
      }
      setMsg('Opération effectuée avec succès !');
      setTab('historique');
      fetchTx();
      setForm(f => ({ ...f, montant: '', compteDestinataire: '', description: '' }));
      setTimeout(() => setMsg(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de l'opération");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout>
      <div className="page-title">Transactions</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      <div style={{display:'flex', gap:10, marginBottom:20}}>
        {['historique','nouvelle'].map(t => (
          <button key={t} className={`btn ${tab===t?'btn-primary':''}`}
            style={tab!==t?{background:'#fff',border:'1px solid #ddd',color:'#333'}:{}}
            onClick={() => setTab(t)}>
            {t==='historique' ? '📋 Historique' : '+ Nouvelle opération'}
          </button>
        ))}
      </div>

      {tab === 'historique' && (
        loading ? <div>Chargement...</div> : (
          <div className="table-box">
            {transactions.length === 0 ? <p style={{color:'#888'}}>Aucune transaction.</p> : (
              <table>
                <thead><tr><th>Référence</th><th>Type</th><th>Montant</th><th>Frais</th><th>Date</th><th>Statut</th></tr></thead>
                <tbody>
                  {transactions.map((tx: any) => (
                    <tr key={tx.id}>
                      <td>{tx.reference}</td>
                      <td><span className={`badge ${typeColor[tx.type] ?? 'info'}`}>{tx.type}</span></td>
                      <td><strong>{(tx.montant ?? 0).toLocaleString()} XAF</strong></td>
                      <td>{(tx.frais ?? 0).toLocaleString()} XAF</td>
                      <td>{tx.createdAt ? new Date(tx.createdAt).toLocaleString('fr-FR') : '-'}</td>
                      <td><span className={`badge ${tx.status === 'VALIDEE' ? 'success' : tx.status === 'ECHOUEE' ? 'danger' : 'warning'}`}>{tx.status}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )
      )}

      {tab === 'nouvelle' && (
        <div className="form-box">
          <h2>Nouvelle opération</h2>
          <form onSubmit={handleSubmit}>

            {/* Type d'opération */}
            <div className="form-group">
              <label>Type d'opération</label>
              <select value={form.type} onChange={e => setForm({...form, type: e.target.value, compteDestinataire: ''})}>
                <option value="DEPOT">💰 Dépôt</option>
                <option value="RETRAIT">💸 Retrait</option>
                <option value="TRANSFERT_INTRA">🔄 Transfert</option>
              </select>
            </div>

            {/* Compte source / destinataire pour dépôt/retrait */}
            <div className="form-group">
              <label>Compte {form.type === 'DEPOT' ? 'destinataire (le mien)' : 'source'}</label>
              <select value={form.compteSource} onChange={e => setForm({...form, compteSource: e.target.value})}>
                {comptes.map((c: any) => (
                  <option key={c.id} value={c.accountNumber ?? c.id}>
                    {c.accountType} — {c.accountNumber ?? c.id} ({(c.balance ?? 0).toLocaleString()} XAF)
                  </option>
                ))}
              </select>
            </div>

            {/* Section destinataire pour transfert */}
            {form.type === 'TRANSFERT_INTRA' && (
              <div className="form-group">
                <label>Compte destinataire</label>

                {/* Toggle mes comptes / autre utilisateur */}
                <div style={{display:'flex', gap:8, marginBottom:10}}>
                  <button type="button"
                    className={`btn btn-sm ${destMode === 'mes-comptes' ? 'btn-primary' : ''}`}
                    style={destMode !== 'mes-comptes' ? {background:'#f5f5f5', border:'1px solid #ddd', color:'#555'} : {}}
                    onClick={() => { setDestMode('mes-comptes'); setForm(f => ({...f, compteDestinataire:''})); }}>
                    👤 Mes comptes
                  </button>
                  <button type="button"
                    className={`btn btn-sm ${destMode === 'tous' ? 'btn-primary' : ''}`}
                    style={destMode !== 'tous' ? {background:'#f5f5f5', border:'1px solid #ddd', color:'#555'} : {}}
                    onClick={() => { setDestMode('tous'); setForm(f => ({...f, compteDestinataire:''})); }}>
                    🌐 Autre utilisateur
                  </button>
                  <button type="button"
                    className={`btn btn-sm ${destMode === 'manuel' ? 'btn-primary' : ''}`}
                    style={destMode !== 'manuel' ? {background:'#f5f5f5', border:'1px solid #ddd', color:'#555'} : {}}
                    onClick={() => { setDestMode('manuel'); setForm(f => ({...f, compteDestinataire:''})); }}>
                    ✏️ Saisie libre
                  </button>
                </div>

                {/* Mes comptes */}
                {destMode === 'mes-comptes' && (
                  <select required value={form.compteDestinataire}
                    onChange={e => setForm({...form, compteDestinataire: e.target.value})}>
                    <option value="">-- Choisir un de mes comptes --</option>
                    {comptes
                      .filter((c: any) => (c.accountNumber ?? c.id) !== form.compteSource)
                      .map((c: any) => (
                        <option key={c.id} value={c.accountNumber ?? c.id}>
                          {c.accountType} — {c.accountNumber ?? c.id} ({(c.balance ?? 0).toLocaleString()} XAF)
                        </option>
                      ))}
                  </select>
                )}

                {/* Tous les comptes de la plateforme */}
                {destMode === 'tous' && (
                  <select required value={form.compteDestinataire}
                    onChange={e => setForm({...form, compteDestinataire: e.target.value})}>
                    <option value="">-- Choisir un compte --</option>
                    {tousLesComptes
                      .filter((c: any) => (c.accountNumber ?? c.id) !== form.compteSource)
                      .map((c: any) => (
                        <option key={c.id} value={c.accountNumber ?? c.id}>
                          {c.accountNumber ?? c.id} — {c.accountType}
                        </option>
                      ))}
                    {tousLesComptes.length === 0 && (
                      <option disabled>Chargement des comptes...</option>
                    )}
                  </select>
                )}

                {/* Saisie manuelle */}
                {destMode === 'manuel' && (
                  <input required placeholder="Entrez le numéro de compte (ex: MM1234567890)"
                    value={form.compteDestinataire}
                    onChange={e => setForm({...form, compteDestinataire: e.target.value})}
                    style={{width:'100%', padding:'8px 12px', border:'1px solid #ddd', borderRadius:6}} />
                )}
              </div>
            )}

            {/* Montant */}
            <div className="form-group">
              <label>Montant (XAF)</label>
              <input type="number" min="1" required value={form.montant}
                onChange={e => setForm({...form, montant: e.target.value})} />
            </div>

            {/* Description */}
            <div className="form-group">
              <label>Description <span style={{color:'#aaa', fontSize:'0.8rem'}}>(optionnel)</span></label>
              <input placeholder="Ex: Loyer, remboursement..." value={form.description}
                onChange={e => setForm({...form, description: e.target.value})} />
            </div>

            <button className="btn btn-success" type="submit" disabled={submitting}>
              {submitting ? '⏳ Traitement...' : '✅ Confirmer'}
            </button>
          </form>
        </div>
      )}
    </Layout>
  );
}
