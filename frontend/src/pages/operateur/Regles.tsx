import { useState } from 'react';
import Layout from '../../components/Layout';

export default function Regles() {
  const [regles, setRegles] = useState({ commission:'1.5', plafondTx:'500000', plafondJour:'2000000', plafondMois:'10000000', zones:'Cameroun', delai:'24' });
  const [msg, setMsg] = useState('');

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setMsg('Règles métier mises à jour avec succès.');
    setTimeout(() => setMsg(''), 3000);
  };

  return (
    <Layout>
      <div className="page-title">Règles métier</div>
      {msg && <div className="alert success">{msg}</div>}
      <div className="form-box" style={{maxWidth:560}}><h2>Configuration des règles</h2>
        <form onSubmit={handleSave}>
          <div className="form-group"><label>Taux de commission (%)</label>
            <input type="number" step="0.1" value={regles.commission} onChange={e => setRegles({...regles, commission:e.target.value})} /></div>
          <div className="form-group"><label>Plafond par transaction (FCFA)</label>
            <input type="number" value={regles.plafondTx} onChange={e => setRegles({...regles, plafondTx:e.target.value})} /></div>
          <div className="form-group"><label>Plafond journalier (FCFA)</label>
            <input type="number" value={regles.plafondJour} onChange={e => setRegles({...regles, plafondJour:e.target.value})} /></div>
          <div className="form-group"><label>Plafond mensuel (FCFA)</label>
            <input type="number" value={regles.plafondMois} onChange={e => setRegles({...regles, plafondMois:e.target.value})} /></div>
          <div className="form-group"><label>Zones géographiques</label>
            <input value={regles.zones} onChange={e => setRegles({...regles, zones:e.target.value})} /></div>
          <div className="form-group"><label>Délai validation prêt (heures)</label>
            <input type="number" value={regles.delai} onChange={e => setRegles({...regles, delai:e.target.value})} /></div>
          <button className="btn btn-success" type="submit">Enregistrer</button>
        </form>
      </div>
    </Layout>
  );
}
