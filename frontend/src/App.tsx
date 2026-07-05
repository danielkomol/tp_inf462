import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

import Login    from './pages/auth/Login';
import Register from './pages/auth/Register';

import ClientDashboard  from './pages/client/Dashboard';
import Comptes          from './pages/client/Comptes';
import Transactions     from './pages/client/Transactions';
import Prets            from './pages/client/Prets';
import Documents        from './pages/client/Documents';
import Notifications    from './pages/client/Notifications';

import AdminDashboard from './pages/admin/Dashboard';
import Utilisateurs   from './pages/admin/Utilisateurs';
import Operateurs     from './pages/admin/Operateurs';
import Audit          from './pages/admin/Audit';
import Rapports       from './pages/admin/Rapports';
import AdminPrets     from './pages/admin/Prets';

import OperateurDashboard    from './pages/operateur/Dashboard';
import Regles                from './pages/operateur/Regles';
import OperateurClients      from './pages/operateur/Clients';
import OperateurTransactions from './pages/operateur/Transactions';

function PrivateRoute({ children, roles }: { children: JSX.Element; roles: string[] }) {
  const { user, loading } = useAuth();
  if (loading) return <div style={{padding:40}}>Chargement...</div>;
  if (!user) return <Navigate to="/login" />;
  if (!roles.includes(user.role)) return <Navigate to="/login" />;
  return children;
}

function AppRoutes() {
  const { user, loading } = useAuth();
  if (loading) return <div style={{padding:40}}>Chargement...</div>;

  return (
    <Routes>
      <Route path="/" element={
        !user ? <Navigate to="/login" /> :
        user.role === 'CLIENT'    ? <Navigate to="/client/dashboard" /> :
        user.role === 'ADMIN'     ? <Navigate to="/admin/dashboard" /> :
        <Navigate to="/operateur/dashboard" />
      } />

      <Route path="/login"    element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route path="/client/dashboard"     element={<PrivateRoute roles={['CLIENT']}><ClientDashboard /></PrivateRoute>} />
      <Route path="/client/comptes"       element={<PrivateRoute roles={['CLIENT']}><Comptes /></PrivateRoute>} />
      <Route path="/client/transactions"  element={<PrivateRoute roles={['CLIENT']}><Transactions /></PrivateRoute>} />
      <Route path="/client/prets"         element={<PrivateRoute roles={['CLIENT']}><Prets /></PrivateRoute>} />
      <Route path="/client/documents"     element={<PrivateRoute roles={['CLIENT']}><Documents /></PrivateRoute>} />
      <Route path="/client/notifications" element={<PrivateRoute roles={['CLIENT']}><Notifications /></PrivateRoute>} />

      <Route path="/admin/dashboard"    element={<PrivateRoute roles={['ADMIN']}><AdminDashboard /></PrivateRoute>} />
      <Route path="/admin/utilisateurs" element={<PrivateRoute roles={['ADMIN']}><Utilisateurs /></PrivateRoute>} />
      <Route path="/admin/operateurs"   element={<PrivateRoute roles={['ADMIN']}><Operateurs /></PrivateRoute>} />
      <Route path="/admin/prets"        element={<PrivateRoute roles={['ADMIN']}><AdminPrets /></PrivateRoute>} />
      <Route path="/admin/audit"        element={<PrivateRoute roles={['ADMIN']}><Audit /></PrivateRoute>} />
      <Route path="/admin/rapports"     element={<PrivateRoute roles={['ADMIN']}><Rapports /></PrivateRoute>} />

      <Route path="/operateur/dashboard"    element={<PrivateRoute roles={['OPERATEUR']}><OperateurDashboard /></PrivateRoute>} />
      <Route path="/operateur/regles"       element={<PrivateRoute roles={['OPERATEUR']}><Regles /></PrivateRoute>} />
      <Route path="/operateur/clients"      element={<PrivateRoute roles={['OPERATEUR']}><OperateurClients /></PrivateRoute>} />
      <Route path="/operateur/transactions" element={<PrivateRoute roles={['OPERATEUR']}><OperateurTransactions /></PrivateRoute>} />

      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
