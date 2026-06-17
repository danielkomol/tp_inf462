import { useState } from 'react';
import Layout from '../../components/Layout';

const initial = [
  { id:1, nom:'CNI_recto.jpg',        type:"Carte Nationale d'Identité", date:'01/06/2026', statut:'Validé' },
  { id:2, nom:'bulletin_salaire.pdf', type:'Bulletin de salaire',         date:'05/06/2026', statut:'En attente' },
];

export default function Documents() {
  const [docs, setDocs] = useState(initial);
  const [form, setForm] = useState({ type:'CNI', fichier: null as File | null });
  const [msg, setMsg] = useState('');

  const handleUpload = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.fichier) return;
    setDocs([...docs, { id:docs.length+1, nom:form.fichier.name, type:form.type, date:new Date().toLocaleDateString('fr-FR'), statut:'En attente' }]);
    setMsg("Document soumis. En cours d'analyse OCR...");
    setForm({ type:'CNI', fichier:null });
    setTimeout(() => setMsg(''), 4000);
  };

  return (
    <Layout>
      <div className="page-title">Documents</div>
      {msg && <div className="alert success">{msg}</div>}
      <div className="table-box" style={{marginBottom:24}}>
        <h3>Mes documents soumis</h3>
        <table>
          <thead><tr><th>Fichier</th><th>Type</th><th>Date</th><th>Statut OCR</th></tr></thead>
          <tbody>{docs.map(d => (
            <tr key={d.id}><td>📎 {d.nom}</td><td>{d.type}</td><td>{d.date}</td>
              <td><span className={`badge ${d.statut==='Validé'?'success':'warning'}`}>{d.statut}</span></td></tr>
          ))}</tbody>
        </table>
      </div>
      <div className="form-box"><h2>Soumettre un document</h2>
        <form onSubmit={handleUpload}>
          <div className="form-group"><label>Type de document</label>
            <select value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
              <option value="CNI">Carte Nationale d'Identité</option>
              <option value="Passeport">Passeport</option>
              <option value="Domicile">Justificatif de domicile</option>
              <option value="Salaire">Bulletin de salaire</option>
              <option value="Releve">Relevé bancaire</option>
              <option value="Contrat">Contrat de travail</option>
              <option value="Autre">Document administratif</option>
            </select></div>
          <div className="form-group"><label>Fichier (PDF, JPG, PNG)</label>
            <input type="file" accept=".pdf,.jpg,.jpeg,.png" required
              onChange={e => setForm({...form, fichier: e.target.files?.[0] ?? null})} /></div>
          <button className="btn btn-primary" type="submit">Envoyer</button>
        </form>
      </div>
    </Layout>
  );
}
