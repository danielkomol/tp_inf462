import api from './axios';

// ============================================================
// AUTH — identity-service via /api/auth/**
// ============================================================
export const authApi = {
  login: (email: string, password: string) =>
    api.post('/api/auth/login', { email, password }),

  register: (data: {
    nom: string; prenom: string; email: string;
    telephone: string; motDePasse: string; role?: string;
  }) => api.post('/api/auth/register', {
    email: data.email,
    password: data.motDePasse,
    phoneNumber: data.telephone,
    role: data.role ?? 'CLIENT',
  }),

  me: () => api.get('/api/auth/me'),
};

// ============================================================
// COMPTES — account-service via /api/v1/accounts/**
// ============================================================
export const accountApi = {
  getAll: (customerId: string) => api.get(`/api/v1/accounts/customer/${customerId}`),
  getAllAccounts: () => api.get('/api/v1/accounts'),
  getById: (id: number) => api.get(`/api/v1/accounts/${id}`),
  getByNumber: (accountNumber: string) => api.get(`/api/v1/accounts/number/${accountNumber}`),
  create: (data: { accountType: string; operatorId: string; customerId: string; currency?: string }) =>
    api.post('/api/v1/accounts', data),
  credit: (id: number, montant: number, desc: string) =>
    api.post(`/api/v1/accounts/${id}/credit`, { amount: montant, description: desc }),
  debit: (id: number, montant: number, desc: string) =>
    api.post(`/api/v1/accounts/${id}/debit`, { amount: montant, description: desc }),
};

// ============================================================
// TRANSACTIONS — transaction-service via /api/v1/transactions/**
// ============================================================
export const transactionApi = {
  depot: (data: { compteDestinataire: string; montant: number; clientId: string; operateurId: string; description: string }) =>
    api.post('/api/v1/transactions/depot', {
      compteDestinataire: data.compteDestinataire,
      montant: data.montant,
      description: data.description || 'Dépôt',
      clientId: data.clientId,
      operateurId: data.operateurId,
    }),

  retrait: (data: { compteSource: string; montant: number; clientId: string; operateurId: string; description: string }) =>
    api.post('/api/v1/transactions/retrait', {
      compteSource: data.compteSource,
      montant: data.montant,
      description: data.description || 'Retrait',
      clientId: data.clientId,
      operateurId: data.operateurId,
    }),

  transfert: (data: { compteSource: string; compteDestinataire: string; montant: number; clientId: string; operateurSourceId: string; description: string }) =>
    api.post('/api/v1/transactions/transfert/intra', {
      compteSource: data.compteSource,
      compteDestinataire: data.compteDestinataire,
      montant: data.montant,
      description: data.description || 'Transfert',
      clientSourceId: data.clientId,
      operateurSourceId: data.operateurSourceId,
    }),

  historique: (clientId: string) =>
    api.get(`/api/v1/transactions/client/${clientId}`),
};

// ============================================================
// PRÊTS — loan-service via /api/loans/**
// ============================================================
export const loanApi = {
  getMyLoans: (clientId: string) =>
    api.get(`/api/v1/loans/client/${clientId}`),

  soumettre: (data: {
    clientId: string; operateurId: string;
    montantDemande: number; duree: number;
    motif: string; compteVersement: string;
  }) => api.post('/api/v1/loans', data),

  getPending: (operateurId: string) =>
    api.get(`/api/v1/loans/pending/${operateurId}`),

  valider: (id: number, data: { approuve: boolean; montantAccorde?: number; tauxInteret?: number; motifRejet?: string }) =>
    api.put(`/api/v1/loans/${id}/validate`, data),
};

// ============================================================
// CLIENTS — customer-service via /api/v1/customers/**
// ============================================================
export const customerApi = {
  getAll: () => api.get('/api/v1/customers'),
  getById: (id: string) => api.get(`/api/v1/customers/${id}`),
  updateKyc: (id: string, statut: string) =>
    api.put(`/api/v1/customers/${id}/kyc`, { statut }),
};

// ============================================================
// DOCUMENTS — document-service via /api/documents/**
// ============================================================
export const documentApi = {
  getMyDocs: (clientId: string) =>
    api.get(`/api/v1/documents/client/${clientId}`),

  upload: (clientId: string, typeDoc: string, fichier: File) => {
    const form = new FormData();
    form.append('clientId', clientId);
    form.append('typeDocument', typeDoc);
    form.append('fichier', fichier);
    return api.post('/api/v1/documents/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

// ============================================================
// NOTIFICATIONS — notification-service via /api/notifications/**
// ============================================================
export const notificationApi = {
  getMyNotifs: (clientId: string) =>
    api.get(`/api/v1/notifications/${clientId}`),
  markAllRead: (clientId: string) =>
    api.put(`/api/v1/notifications/${clientId}/read-all`),
};

// ============================================================
// OPÉRATEURS — operator-service via /api/operators/**
// ============================================================
export const operatorApi = {
  getAll: () => api.get('/api/v1/operators'),
  getById: (id: string) => api.get(`/api/v1/operators/${id}`),
  create: (data: object) => api.post('/api/v1/operators', data),
  updateRegles: (id: string, data: object) =>
    api.put(`/api/v1/operators/${id}/rules`, data),
};

// ============================================================
// AUDIT — audit-service via /api/audit/**
// ============================================================
export const auditApi = {
  getLogs: (params?: { service?: string; userId?: string; query?: string; taille?: number }) =>
    api.get('/api/v1/audit/logs', { params }),
  getStats: () => api.get('/api/v1/audit/stats'),
};

// ============================================================
// REPORTING — reporting-service via /api/v1/reports/**
// ============================================================
export const reportingApi = {
  getTransactionStats: (debut?: string, fin?: string) =>
    api.get('/api/v1/reports/transactions', { params: { date_debut: debut, date_fin: fin } }),
  getLoanStats: () => api.get('/api/v1/reports/loans'),
  getOperatorStats: () => api.get('/api/v1/reports/customers'),
  getDashboard: () => api.get('/api/v1/reports/dashboard'),
};
