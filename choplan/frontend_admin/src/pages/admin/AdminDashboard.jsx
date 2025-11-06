import React, { useState, useEffect } from 'react';
import { useAuth } from "../../demo/context/AuthContext.jsx";
import { useNavigate } from 'react-router-dom';
import { userAPI, restaurantAPI, statisticsAPI, reportAPI } from '../../demo/services/api';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [activeMenu, setActiveMenu] = useState('overview');
  const [users, setUsers] = useState([]);
  const [restaurants, setRestaurants] = useState([]);
  const [reports, setReports] = useState([]);
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalRestaurants: 0,
    totalReservations: 0,
    totalReviews: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/home');
      return;
    }
    loadDashboardData();
  }, [user]);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [usersRes, restaurantsRes, statsRes, reportsRes] = await Promise.all([
        userAPI.getAll(),
        restaurantAPI.getAll(),
        statisticsAPI.getAllSummary(),
        reportAPI.getAllReports(),
      ]);
      setUsers(usersRes.data || []);
      setRestaurants(restaurantsRes.data || []);
      setReports(reportsRes.data || []);
      setStats(statsRes.data || {});
    } catch (err) {
      console.error('관리자 대시보드 데이터 로드 실패:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="admin-dashboard loading">
        <p>데이터를 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      {/* 왼쪽 사이드바 */}
      <aside className="dashboard-sidebar">
        <div className="sidebar-header">
          <h2>Admin Panel</h2>
        </div>
        <nav className="sidebar-nav">
          <div
            className={`nav-item ${activeMenu === 'overview' ? 'active' : ''}`}
            onClick={() => setActiveMenu('overview')}
          >
            개요
          </div>
          <div
            className={`nav-item ${activeMenu === 'users' ? 'active' : ''}`}
            onClick={() => setActiveMenu('users')}
          >
            사용자 관리
          </div>
          <div
            className={`nav-item ${activeMenu === 'restaurants' ? 'active' : ''}`}
            onClick={() => setActiveMenu('restaurants')}
          >
            매장 관리
          </div>
          <div
            className={`nav-item ${activeMenu === 'reports' ? 'active' : ''}`}
            onClick={() => setActiveMenu('reports')}
          >
            신고 / 리뷰
          </div>
        </nav>
      </aside>

      {/* 메인 콘텐츠 */}
      <main className="dashboard-main">
        <header className="dashboard-header">
          <h1>
            {activeMenu === 'overview' && '관리자 대시보드 개요'}
            {activeMenu === 'users' && '사용자 관리'}
            {activeMenu === 'restaurants' && '매장 관리'}
            {activeMenu === 'reports' && '신고 / 리뷰 관리'}
          </h1>
        </header>

        <div className="dashboard-content">
          {activeMenu === 'overview' && (
            <div className="stats-grid">
              <div className="stat-card">
                <h3>총 사용자</h3>
                <p>{stats.totalUsers}</p>
              </div>
              <div className="stat-card">
                <h3>총 매장</h3>
                <p>{stats.totalRestaurants}</p>
              </div>
              <div className="stat-card">
                <h3>총 예약</h3>
                <p>{stats.totalReservations}</p>
              </div>
              <div className="stat-card">
                <h3>총 리뷰</h3>
                <p>{stats.totalReviews}</p>
              </div>
            </div>
          )}

          {activeMenu === 'users' && (
            <div className="table-section">
              <h2>사용자 목록</h2>
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>이름</th>
                    <th>이메일</th>
                    <th>역할</th>
                    <th>가입일</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td>{u.id}</td>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{u.role}</td>
                      <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {activeMenu === 'restaurants' && (
            <div className="table-section">
              <h2>매장 목록</h2>
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>매장명</th>
                    <th>카테고리</th>
                    <th>주소</th>
                    <th>등록일</th>
                  </tr>
                </thead>
                <tbody>
                  {restaurants.map((r) => (
                    <tr key={r.id}>
                      <td>{r.id}</td>
                      <td>{r.restaurantName}</td>
                      <td>{r.category || '-'}</td>
                      <td>{r.address || '-'}</td>
                      <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {activeMenu === 'reports' && (
            <div className="table-section">
              <h2>신고 내역</h2>
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>유형</th>
                    <th>대상</th>
                    <th>신고자</th>
                    <th>내용</th>
                    <th>처리 상태</th>
                  </tr>
                </thead>
                <tbody>
                  {reports.map((r) => (
                    <tr key={r.id}>
                      <td>{r.id}</td>
                      <td>{r.type}</td>
                      <td>{r.targetName}</td>
                      <td>{r.reporterName}</td>
                      <td>{r.reason}</td>
                      <td>{r.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;
