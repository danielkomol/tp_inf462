import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { operatorApi } from '../../api/services';

export default function Regles() {
  const { user } = useAuth();
  const [operateur, setOperateur] = useState<any>(null);
  const [regles, setRegles] = useState({
    tauxCommission: '', plafondTransaction: '', plafondSolde: '',
    tauxInteretDefaut: '', plafondPret: '',
  });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    // Chercher l'opérateur correspondant à l'utilisateur connecté
    operatorApi.getAll().then(r => {
      const ops = r.data?.data ?? r.data ?? [];
      const op = ops[0]; // L'opérateur connecté gère son propre service
      if (op) {
        setOperateur(op);
        setRegles({
          tauxCommission: String(op.tauxCommission ?? ''),
          plafondTransaction: String(op.plafondTransaction ?? ''),
          plafondSolde: String(op.plafondSolde ?? ''),
          tauxInteretDefaut: String(op.tauxInteretDefaut ?? ''),
          plafondPret: String(op.plafondPret ?? ''),
        });
      }
    }).catch(() => {}).finally(() => setLoading(false));
  }, [user]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!operateur) return;
    setError('');
    try {
      await operatorApi.updateRegles(operateur.id, {
        tauxCommission: Number(regles.tauxCommission),
        plafondTransaction: Number(regles.plafondTransaction),
        plafondSolde: Number(regles.plafondSolde),
        tauxInteretDefaut: Number(regles.tauxInteretDefaut),
        plafondPret: Number(regles.plafondPret),
      });
      setMsg('Règles métier mises à jour avec succès.');
      setTimeout(() => setMsg(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de la mise à jour");
    }
  };

  return (
    <Layout>
      <div className="page-title">Règles métier</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      {loading ? <div>Chargement...</div> : !operateur ? <p style={{color:'#888'}}>Aucun opérateur trouvé.</p> : (
        <div className="form-box" style={{maxWidth:560}}>
          <h2>Configuration — {operateur.nom}</h2>
          <form onSubmit={handleSave}>
            <div className="form-group"><label>Taux de commission (%)</label>
              <input type="number" step="0.1" value={regles.tauxCommission} onChange={e => setRegles({...regles, tauxCommission:e.target.value})} /></div>
            <div className="form-group"><label>Plafond par transaction (XAF)</label>
              <input type="number" value={regles.plafondTransaction} onChange={e => setRegles({...regles, plafondTransaction:e.target.value})} /></div>
            <div className="form-group"><label>Plafond de solde (XAF)</label>
              <input type="number" value={regles.plafondSolde} onChange={e => setRegles({...regles, plafondSolde:e.target.value})} /></div>
            <div className="form-group"><label>Taux d'intérêt prêt (%)</label>
              <input type="number" step="0.1" value={regles.tauxInteretDefaut} onChange={e => setRegles({...regles, tauxInteretDefaut:e.target.value})} /></div>
            <div className="form-group"><label>Plafond prêt (XAF)</label>
              <input type="number" value={regles.plafondPret} onChange={e => setRegles({...regles, plafondPret:e.target.value})} /></div>
            <button className="btn btn-success" type="submit">Enregistrer</button>
          </form>
        </div>
      )}
    </Layout>
  );
}
