import { useState } from 'react';
import Layout from '../../components/Layout';

const initial = [
  { id:1, texte:'Votre dépôt de 150 000 F a été effectué avec succès.',  time:'Il y a 2h', lu:false },
  { id:2, texte:"Votre demande de prêt PR001 est en cours d'analyse.",   time:'Il y a 5h', lu:false },
  { id:3, texte:'Document CNI validé par l\'équipe KYC.',                time:'Hier',      lu:false },
  { id:4, texte:'Votre retrait de 30 000 F a été effectué.',             time:'Il y a 2j', lu:true  },
  { id:5, texte:'Bienvenue sur BankApp ! Votre compte a été créé.',      time:'Il y a 5j', lu:true  },
];

export default function Notifications() {
  const [notifs, setNotifs] = useState(initial);
  const unread = notifs.filter(n => !n.lu).length;

  return (
    <Layout>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:20}}>
        <div className="page-title">Notifications {unread > 0 && <span className="badge info">{unread} nouvelles</span>}</div>
        {unread > 0 && <button className="btn btn-sm" style={{background:'#fff',border:'1px solid #ddd'}} onClick={() => setNotifs(notifs.map(n => ({...n, lu:true})))}>Tout marquer comme lu</button>}
      </div>
      <div className="table-box">
        {notifs.map(n => (
          <div className="notif-item" key={n.id}>
            <div className={`notif-dot ${n.lu?'read':'unread'}`}></div>
            <div><div className="notif-text" style={{fontWeight:n.lu?400:600}}>{n.texte}</div>
              <div className="notif-time">{n.time}</div></div>
          </div>
        ))}
      </div>
    </Layout>
  );
}
