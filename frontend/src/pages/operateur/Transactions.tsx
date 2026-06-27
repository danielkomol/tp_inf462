import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { loanApi } from '../../api/services';

export default function OperateurTransactions() {
  const { user } = useAuth();
  const [prets, setPrets] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const fetchPrets = () => {
    if (!user) return;
    loanApi.getPending(user.id)
      .then(r => setPrets(r.data?.data ?? r.data ?? []))
      .catch(() => setPrets([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchPrets(); }, [user]);

  const handleValider = async (id: number, approuve: boolean) => {
    setError('');
    try {
      await loanApi.valider(id, { approuve });
      setMsg(approuve ? 'Prêt approuvé !' : 'Prêt rejeté.');
      fetchPrets();
      setTimeout(() => setMsg(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de la validation");
    }
  };

  return (
    <Layout>
      <div className="page-title">Dossiers de prêt en attente</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      <div className="table-box">
        {loading ? <div>Chargement...</div> : prets.length === 0
          ? <p style={{color:'#888'}}>Aucun dossier en attente.</p>
          : (
            <table>
              <thead><tr><th>Référence</th><th>Client</th><th>Montant</th><th>Durée</th><th>Motif</th><th>Date</th><th>Actions</th></tr></thead>
              <tbody>{prets.map((p: any) => (
                <tr key={p.id}>
                  <td>{p.reference}</td>
                  <td>{p.clientId}</td>
                  <td>{(p.montantDemande ?? 0).toLocaleString()} XAF</td>
                  <td>{p.duree} mois</td>
                  <td>{p.motif}</td>
                  <td>{p.createdAt ? new Date(p.createdAt).toLocaleDateString('fr-FR') : '—'}</td>
                  <td style={{display:'flex', gap:4}}>
                    <button className="btn btn-sm btn-success" onClick={() => handleValider(p.id, true)}>✓ Approuver</button>
                    <button className="btn btn-sm btn-danger"  onClick={() => handleValider(p.id, false)}>✗ Rejeter</button>
                  </td>
                </tr>
              ))}</tbody>
            </table>
          )
        }
      </div>
    </Layout>
  );
}
