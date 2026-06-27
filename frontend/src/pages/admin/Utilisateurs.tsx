import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { customerApi } from '../../api/services';

const kycColor: Record<string, string> = { VERIFIE:'success', EN_ATTENTE:'warning', REJETE:'danger' };

export default function Utilisateurs() {
  const [users, setUsers] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    customerApi.getAll()
      .then(r => setUsers(r.data?.data ?? r.data ?? []))
      .catch(() => setUsers([]))
      .finally(() => setLoading(false));
  }, []);

  const updateKyc = async (id: string, statut: string) => {
    await customerApi.updateKyc(id, statut).catch(() => {});
    setUsers(users.map((u: any) => u.id === id ? { ...u, statutVerification: statut } : u));
  };

  const filtered = users.filter((u: any) =>
    (u.nom + ' ' + u.prenom + ' ' + u.email).toLowerCase().includes(search.toLowerCase())
  );

  return (
    <Layout>
      <div className="page-title">Gestion des utilisateurs</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Total</div><div className="card-value blue">{users.length}</div></div>
        <div className="card"><div className="card-label">KYC validés</div><div className="card-value green">{users.filter((u:any) => u.statutVerification==='VERIFIE').length}</div></div>
        <div className="card"><div className="card-label">KYC en attente</div><div className="card-value" style={{color:'#f39c12'}}>{users.filter((u:any) => u.statutVerification==='EN_ATTENTE').length}</div></div>
      </div>
      <div className="table-box">
        <div style={{marginBottom:12}}>
          <input placeholder="Rechercher..." value={search} onChange={e => setSearch(e.target.value)}
            style={{padding:'8px 12px',border:'1px solid #ddd',borderRadius:6,width:300,fontSize:'0.88rem'}} />
        </div>
        {loading ? <div>Chargement...</div> : filtered.length === 0 ? <p style={{color:'#888'}}>Aucun client.</p> : (
          <table>
            <thead><tr><th>Nom</th><th>Email</th><th>Téléphone</th><th>KYC</th><th>Score crédit</th><th>Action KYC</th></tr></thead>
            <tbody>{filtered.map((u: any) => (
              <tr key={u.id}>
                <td><strong>{u.nom} {u.prenom}</strong></td>
                <td>{u.email}</td>
                <td>{u.telephone}</td>
                <td><span className={`badge ${kycColor[u.statutVerification] ?? 'info'}`}>{u.statutVerification}</span></td>
                <td>{u.scoreCredit}</td>
                <td style={{display:'flex', gap:4}}>
                  {u.statutVerification !== 'VERIFIE' && (
                    <button className="btn btn-sm btn-success" onClick={() => updateKyc(u.id, 'VERIFIE')}>Valider</button>
                  )}
                  {u.statutVerification !== 'REJETE' && (
                    <button className="btn btn-sm btn-danger" onClick={() => updateKyc(u.id, 'REJETE')}>Rejeter</button>
                  )}
                </td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
