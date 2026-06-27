import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { documentApi } from '../../api/services';

export default function Documents() {
  const { user } = useAuth();
  const [docs, setDocs] = useState<any[]>([]);
  const [form, setForm] = useState({ type: 'CNI', fichier: null as File | null });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const fetchDocs = () => {
    if (!user) return;
    documentApi.getMyDocs(user.id)
      .then(r => setDocs(r.data?.data ?? r.data ?? []))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchDocs(); }, [user]);

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.fichier || !user) return;
    setError(''); setSubmitting(true);
    try {
      await documentApi.upload(user.id, form.type, form.fichier);
      setMsg("Document soumis. En cours d'analyse OCR...");
      setForm({ type: 'CNI', fichier: null });
      fetchDocs();
      setTimeout(() => setMsg(''), 4000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Erreur lors de l'upload");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout>
      <div className="page-title">Documents</div>
      {msg   && <div className="alert success">{msg}</div>}
      {error && <div className="alert error">{error}</div>}
      <div className="table-box" style={{marginBottom:24}}>
        <h3>Mes documents soumis</h3>
        {loading ? <div>Chargement...</div> : docs.length === 0 ? <p style={{color:'#888'}}>Aucun document soumis.</p> : (
          <table>
            <thead><tr><th>Fichier</th><th>Type</th><th>Date</th><th>Statut OCR</th></tr></thead>
            <tbody>{docs.map((d: any) => (
              <tr key={d.id}>
                <td>📎 {d.nomOriginal ?? d.nom_original ?? d.fichier}</td>
                <td>{d.typeDocument ?? d.type_document}</td>
                <td>{d.createdAt ? new Date(d.createdAt).toLocaleDateString('fr-FR') : d.created_at}</td>
                <td><span className={`badge ${d.statut==='TRAITE'?'success': d.statut==='INVALIDE'?'danger':'warning'}`}>{d.statut}</span></td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
      <div className="form-box"><h2>Soumettre un document</h2>
        <form onSubmit={handleUpload}>
          <div className="form-group"><label>Type de document</label>
            <select value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
              <option value="CNI">Carte Nationale d'Identité</option>
              <option value="PASSEPORT">Passeport</option>
              <option value="JUSTIFICATIF_DOMICILE">Justificatif de domicile</option>
              <option value="BULLETIN_SALAIRE">Bulletin de salaire</option>
              <option value="RELEVE_BANCAIRE">Relevé bancaire</option>
              <option value="CONTRAT_TRAVAIL">Contrat de travail</option>
              <option value="AUTRE">Document administratif</option>
            </select></div>
          <div className="form-group"><label>Fichier (PDF, JPG, PNG)</label>
            <input type="file" accept=".pdf,.jpg,.jpeg,.png" required
              onChange={e => setForm({...form, fichier: e.target.files?.[0] ?? null})} /></div>
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Envoi...' : 'Envoyer'}
          </button>
        </form>
      </div>
    </Layout>
  );
}
