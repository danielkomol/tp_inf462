import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { useAuth } from '../../context/AuthContext';
import { notificationApi } from '../../api/services';

export default function Notifications() {
  const { user } = useAuth();
  const [notifs, setNotifs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifs = () => {
    if (!user) return;
    notificationApi.getMyNotifs(user.id)
      .then(r => {
        const data = r.data?.data ?? r.data ?? [];
        const list = Array.isArray(data) ? data : (data.notifications ?? []);
        setNotifs(list);
      })
      .catch(() => setNotifs([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchNotifs(); }, [user]);

  const markAllRead = async () => {
    if (!user) return;
    await notificationApi.markAllRead(user.id).catch(() => {});
    setNotifs(notifs.map((n: any) => ({ ...n, lu: true, read: true })));
  };

  const unread = notifs.filter((n: any) => !n.lu && !n.read).length;

  return (
    <Layout>
      <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:20}}>
        <div className="page-title">
          Notifications {unread > 0 && <span className="badge info">{unread} nouvelles</span>}
        </div>
        {unread > 0 && (
          <button className="btn btn-sm" style={{background:'#fff',border:'1px solid #ddd'}} onClick={markAllRead}>
            Tout marquer comme lu
          </button>
        )}
      </div>
      <div className="table-box">
        {loading ? <div>Chargement...</div> : notifs.length === 0
          ? <p style={{color:'#888'}}>Aucune notification.</p>
          : notifs.map((n: any) => (
            <div className="notif-item" key={n.id ?? n._id}>
              <div className={`notif-dot ${(n.lu || n.read) ? 'read' : 'unread'}`}></div>
              <div>
                <div className="notif-text" style={{fontWeight:(n.lu || n.read) ? 400 : 600}}>
                  {n.texte ?? n.message ?? n.content}
                </div>
                <div className="notif-time">
                  {n.createdAt ? new Date(n.createdAt).toLocaleString('fr-FR') : n.time}
                </div>
              </div>
            </div>
          ))
        }
      </div>
    </Layout>
  );
}
