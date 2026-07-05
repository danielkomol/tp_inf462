import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const menus: Record<string, { to: string; label: string }[]> = {
  CLIENT: [
    { to:'/client/dashboard',     label:'🏠 Tableau de bord' },
    { to:'/client/comptes',       label:'💳 Mes comptes' },
    { to:'/client/transactions',  label:'💸 Transactions' },
    { to:'/client/prets',         label:'🏦 Prêts' },
    { to:'/client/documents',     label:'📄 Documents' },
    { to:'/client/notifications', label:'🔔 Notifications' },
  ],
  ADMIN: [
    { to:'/admin/dashboard',    label:'🏠 Tableau de bord' },
    { to:'/admin/utilisateurs', label:'👥 Utilisateurs' },
    { to:'/admin/operateurs',   label:'🏢 Opérateurs' },
    { to:'/admin/prets',        label:'🏦 Prêts' },
    { to:'/admin/audit',        label:'🔍 Audit' },
    { to:'/admin/rapports',     label:'📊 Rapports' },
  ],
  OPERATEUR: [
    { to:'/operateur/dashboard',   label:'🏠 Tableau de bord' },
    { to:'/operateur/regles',      label:'⚙️ Règles métier' },
    { to:'/operateur/clients',     label:'👥 Clients' },
    { to:'/operateur/transactions',label:'💸 Transactions' },
  ],
};

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="sidebar">
      <div className="logo">🏦 BankApp</div>
      <nav>
        {(menus[user?.role ?? ''] ?? []).map(item => (
          <NavLink key={item.to} to={item.to} className={({ isActive }) => isActive ? 'active' : ''}>
            {item.label}
          </NavLink>
        ))}
      </nav>
      <button className="logout-btn" onClick={handleLogout}>⬅ Déconnexion</button>
    </div>
  );
}
