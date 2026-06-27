import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { customerApi } from '../../api/services';

export default function OperateurClients() {
  const [clients, setClients] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    customerApi.getAll()
      .then(r => setClients(r.data?.data ?? r.data ?? []))
      .catch(() => setClients([]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = clients.filter((c: any) =>
    (c.nom + ' ' + c.prenom + ' ' + c.email).toLowerCase().includes(search.toLowerCase())
  );

  return (
    <Layout>
      <div className="page-title">Mes clients</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Total</div><div className="card-value blue">{clients.length}</div></div>
        <div className="card"><div className="card-label">KYC validés</div><div className="card-value green">{clients.filter((c:any) => c.statutVerification==='VERIFIE').length}</div></div>
        <div className="card"><div className="card-label">En attente</div><div className="card-value" style={{color:'#f39c12'}}>{clients.filter((c:any) => c.statutVerification==='EN_ATTENTE').length}</div></div>
      </div>
      <div className="table-box">
        <div style={{marginBottom:12}}>
          <input placeholder="Rechercher..." value={search} onChange={e => setSearch(e.target.value)}
            style={{padding:'8px 12px',border:'1px solid #ddd',borderRadius:6,width:280,fontSize:'0.88rem'}} />
        </div>
        {loading ? <div>Chargement...</div> : filtered.length === 0 ? <p style={{color:'#888'}}>Aucun client.</p> : (
          <table>
            <thead><tr><th>Nom</th><th>Email</th><th>Téléphone</th><th>Score crédit</th><th>KYC</th><th>Inscription</th></tr></thead>
            <tbody>{filtered.map((c: any) => (
              <tr key={c.id}>
                <td><strong>{c.nom} {c.prenom}</strong></td>
                <td>{c.email}</td>
                <td>{c.telephone}</td>
                <td>{c.scoreCredit}</td>
                <td><span className={`badge ${c.statutVerification==='VERIFIE'?'success':c.statutVerification==='REJETE'?'danger':'warning'}`}>{c.statutVerification}</span></td>
                <td>{c.createdAt ? new Date(c.createdAt).toLocaleDateString('fr-FR') : '—'}</td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
