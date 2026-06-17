import { useState } from 'react';
import Layout from '../../components/Layout';

const mockClients = [
  { id:'C001', nom:'Jean Dupont',  email:'jean@mail.com',  comptes:2, solde:'1 560 000 F', kyc:'Validé',     depuis:'Jan 2026' },
  { id:'C002', nom:'Alice Mballa', email:'alice@mail.com', comptes:1, solde:'320 000 F',   kyc:'En attente', depuis:'Mar 2026' },
  { id:'C003', nom:'Paul Nkomo',   email:'paul@mail.com',  comptes:1, solde:'780 000 F',   kyc:'Validé',     depuis:'Fév 2026' },
  { id:'C004', nom:'Sophie Biya',  email:'sophie@mail.com',comptes:2, solde:'2 100 000 F', kyc:'Validé',     depuis:'Jan 2026' },
];

export default function OperateurClients() {
  const [search, setSearch] = useState('');
  const filtered = mockClients.filter(c => c.nom.toLowerCase().includes(search.toLowerCase()));

  return (
    <Layout>
      <div className="page-title">Mes clients</div>
      <div className="cards-row">
        <div className="card"><div className="card-label">Total</div><div className="card-value blue">{mockClients.length}</div></div>
        <div className="card"><div className="card-label">KYC validés</div><div className="card-value green">{mockClients.filter(c=>c.kyc==='Validé').length}</div></div>
        <div className="card"><div className="card-label">En attente</div><div className="card-value" style={{color:'#f39c12'}}>{mockClients.filter(c=>c.kyc==='En attente').length}</div></div>
      </div>
      <div className="table-box">
        <div style={{marginBottom:12}}>
          <input placeholder="Rechercher..." value={search} onChange={e => setSearch(e.target.value)}
            style={{padding:'8px 12px',border:'1px solid #ddd',borderRadius:6,width:280,fontSize:'0.88rem'}} />
        </div>
        <table>
          <thead><tr><th>ID</th><th>Nom</th><th>Email</th><th>Comptes</th><th>Solde total</th><th>KYC</th><th>Depuis</th></tr></thead>
          <tbody>{filtered.map(c => (
            <tr key={c.id}><td>{c.id}</td><td><strong>{c.nom}</strong></td><td>{c.email}</td><td>{c.comptes}</td><td>{c.solde}</td>
              <td><span className={`badge ${c.kyc==='Validé'?'success':'warning'}`}>{c.kyc}</span></td><td>{c.depuis}</td></tr>
          ))}</tbody>
        </table>
      </div>
    </Layout>
  );
}
