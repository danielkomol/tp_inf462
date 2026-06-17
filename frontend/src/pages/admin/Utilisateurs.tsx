import { useState } from 'react';
import Layout from '../../components/Layout';

const initial = [
  { id:'U001', nom:'Jean Dupont',   email:'jean@mail.com',  role:'CLIENT',   kyc:'Validé',     statut:'Actif' },
  { id:'U002', nom:'Alice Mballa',  email:'alice@mail.com', role:'CLIENT',   kyc:'En attente', statut:'Actif' },
  { id:'U003', nom:'Paul Nkomo',    email:'paul@mail.com',  role:'AGENT',    kyc:'Validé',     statut:'Actif' },
  { id:'U004', nom:'Admin Système', email:'admin@bank.com', role:'ADMIN',    kyc:'N/A',        statut:'Actif' },
  { id:'U005', nom:'Marc Essono',   email:'marc@mail.com',  role:'CLIENT',   kyc:'Rejeté',     statut:'Suspendu' },
];
const kycColor: Record<string,string> = { Validé:'success', 'En attente':'warning', Rejeté:'danger', 'N/A':'info' };

export default function Utilisateurs() {
  const [users, setUsers] = useState(initial);
  const [search, setSearch] = useState('');
  const toggle = (id: string) => setUsers(users.map(u => u.id===id ? {...u, statut:u.statut==='Actif'?'Suspendu':'Actif'} : u));
  const filtered = users.filter(u => u.nom.toLowerCase().includes(search.toLowerCase()) || u.email.toLowerCase().includes(search.toLowerCase()));

  return (
    <Layout>
      <div className="page-title">Gestion des utilisateurs</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Total</div><div className="card-value blue">{users.length}</div></div>
        <div className="card"><div className="card-label">Actifs</div><div className="card-value green">{users.filter(u=>u.statut==='Actif').length}</div></div>
        <div className="card"><div className="card-label">KYC validés</div><div className="card-value">{users.filter(u=>u.kyc==='Validé').length}</div></div>
        <div className="card"><div className="card-label">KYC en attente</div><div className="card-value" style={{color:'#f39c12'}}>{users.filter(u=>u.kyc==='En attente').length}</div></div>
      </div>
      <div className="table-box">
        <div style={{marginBottom:12}}>
          <input placeholder="Rechercher..." value={search} onChange={e => setSearch(e.target.value)}
            style={{padding:'8px 12px',border:'1px solid #ddd',borderRadius:6,width:300,fontSize:'0.88rem'}} />
        </div>
        <table>
          <thead><tr><th>ID</th><th>Nom</th><th>Email</th><th>Rôle</th><th>KYC</th><th>Statut</th><th>Action</th></tr></thead>
          <tbody>{filtered.map(u => (
            <tr key={u.id}>
              <td>{u.id}</td><td>{u.nom}</td><td>{u.email}</td>
              <td><span className="badge info">{u.role}</span></td>
              <td><span className={`badge ${kycColor[u.kyc]}`}>{u.kyc}</span></td>
              <td><span className={`badge ${u.statut==='Actif'?'success':'danger'}`}>{u.statut}</span></td>
              <td><button className={`btn btn-sm ${u.statut==='Actif'?'btn-danger':'btn-success'}`} onClick={() => toggle(u.id)}>
                {u.statut==='Actif'?'Suspendre':'Réactiver'}
              </button></td>
            </tr>
          ))}</tbody>
        </table>
      </div>
    </Layout>
  );
}
