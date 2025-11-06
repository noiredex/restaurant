/**
 * 테스트용 관리자 페이지 - 데모 종료 시 제거 예정
 */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { adminAPI, reviewAPI, reservationAPI } from '../../../services/api';
import { API_ENDPOINTS } from '../../../../constants/config/apiConfig';
import NotificationModal from '../../../../components/common/NotificationModal';
import ConfirmModal from '../../../../components/common/ConfirmModal';
import useConfirmModal from '../../../../hooks/useConfirmModal';
import axios from 'axios';
import './AdminPage.css';

const AdminPage = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [activeMenu, setActiveMenu] = useState('dashboard');
  const [loading, setLoading] = useState(false);
  
  // 팝업 모달 상태
  const { modalState, showConfirm, hideConfirm, handleConfirm } = useConfirmModal();
  const [notificationModal, setNotificationModal] = useState({
    isOpen: false,
    type: 'info',
    title: '',
    message: ''
  });
  
  // 알림 모달 표시 함수
  const showNotification = (type, title, message) => {
    setNotificationModal({
      isOpen: true,
      type,
      title,
      message
    });
  };
  
  // 알림 모달 닫기 함수
  const closeNotification = () => {
    setNotificationModal(prev => ({ ...prev, isOpen: false }));
  };
  
  // 데이터 상태
  const [users, setUsers] = useState([]);
  const [restaurants, setRestaurants] = useState([]);
  const [reportedReviews, setReportedReviews] = useState([]);
  const [allReservations, setAllReservations] = useState([]);
  
  // 통계 데이터
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalRestaurants: 0,
    totalReservations: 0,
    totalReviews: 0,
    todayReservations: 0,
    pendingReservations: 0,
    reportedReviews: 0,
    activeUsers: 0
  });

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      showNotification('error', '접근 권한 없음', '관리자만 접근할 수 있습니다.');
      return;
    }
    loadDashboardData();
  }, [user]);

  useEffect(() => {
    if (activeMenu !== 'dashboard') {
      loadMenuData();
    }
  }, [activeMenu]);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const [usersRes, restaurantsRes, reservationsRes] = await Promise.all([
        adminAPI.getAllUsers(),
        axios.get(`${API_ENDPOINTS.RESTAURANTS}/all`),
        reservationAPI.getAllReservations()
      ]);

      const users = usersRes.data;
      const restaurants = restaurantsRes.data;
      const reservations = reservationsRes.data;

      const today = new Date().toISOString().split('T')[0];
      const todayReservations = reservations.filter(r => 
        r.reservationDate?.startsWith(today)
      ).length;

      setStats({
        totalUsers: users.length,
        totalRestaurants: restaurants.length,
        totalReservations: reservations.length,
        totalReviews: 0, // TODO: 리뷰 API 추가 시
        todayReservations: todayReservations,
        pendingReservations: reservations.filter(r => r.status === 'PENDING').length,
        reportedReviews: 0,
        activeUsers: users.filter(u => u.status === 'ACTIVE').length
      });
    } catch (error) {
      console.error('대시보드 데이터 로딩 오류:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMenuData = async () => {
    setLoading(true);
    try {
      if (activeMenu === 'users') {
        const response = await adminAPI.getAllUsers();
        setUsers(response.data);
      } else if (activeMenu === 'restaurants') {
        const response = await axios.get(`${API_ENDPOINTS.RESTAURANTS}/all`);
        setRestaurants(response.data);
      } else if (activeMenu === 'reservations') {
        const response = await reservationAPI.getAllReservations();
        setAllReservations(response.data);
      } else if (activeMenu === 'reviews') {
        const response = await reviewAPI.getReported();
        setReportedReviews(response.data);
      }
    } catch (error) {
      console.error('데이터 로딩 오류:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUserStatusChange = async (userId, status) => {
    try {
      await adminAPI.updateUserStatus(userId, status);
      showNotification('success', '상태 변경 완료', '사용자 상태가 변경되었습니다.');
      loadMenuData();
    } catch (error) {
      showNotification('error', '오류 발생', '오류가 발생했습니다.');
    }
  };

  const handleDeleteUser = async (userId) => {
    showConfirm({
      title: '사용자 삭제',
      message: '정말 이 사용자를 삭제하시겠습니까?',
      confirmText: '삭제',
      cancelText: '취소',
      type: 'danger',
      onConfirm: async () => {
        try {
          await adminAPI.deleteUser(userId);
          showNotification('success', '삭제 완료', '사용자가 삭제되었습니다.');
          loadMenuData();
        } catch (error) {
          showNotification('error', '오류 발생', '오류가 발생했습니다.');
        }
      }
    });
  };

  const getRoleBadge = (role) => {
    const badges = {
      ADMIN: { text: '관리자', color: '#f44336' },
      OWNER: { text: '가게주인', color: '#ff9800' },
      USER: { text: '일반회원', color: '#2196f3' }
    };
    const badge = badges[role] || badges.USER;
    return (
      <span className="role-badge" style={{ backgroundColor: badge.color }}>
        {badge.text}
      </span>
    );
  };

  const getStatusBadge = (status) => {
    const badges = {
      ACTIVE: { text: '활동', color: '#4caf50' },
      SUSPENDED: { text: '정지', color: '#ff9800' },
      DELETED: { text: '탈퇴', color: '#999' }
    };
    const badge = badges[status] || badges.ACTIVE;
    return (
      <span className="status-badge" style={{ backgroundColor: badge.color }}>
        {badge.text}
      </span>
    );
  };

  const getReservationStatusBadge = (status) => {
    const badges = {
      PENDING: { text: '대기', color: '#ff9800' },
      APPROVED: { text: '승인', color: '#4caf50' },
      REJECTED: { text: '거절', color: '#f44336' },
      CANCELLED: { text: '취소', color: '#999' }
    };
    const badge = badges[status] || badges.PENDING;
    return (
      <span className="reservation-badge" style={{ backgroundColor: badge.color }}>
        {badge.text}
      </span>
    );
  };

  if (user?.role !== 'ADMIN') {
    return (
      <div className="admin-dashboard">
        <div className="access-denied">
          <h2>⛔ 접근 권한이 없습니다</h2>
          <p>관리자만 이 페이지에 접근할 수 있습니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      {/* 왼쪽 사이드바 */}
      <aside className="dashboard-sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-text">ChopRest Admin</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <div 
            className={`nav-item ${activeMenu === 'dashboard' ? 'active' : ''}`}
            onClick={() => setActiveMenu('dashboard')}
          >
            <span className="nav-text">Dashboard</span>
          </div>
          <div 
            className={`nav-item ${activeMenu === 'users' ? 'active' : ''}`}
            onClick={() => setActiveMenu('users')}
          >
            <span className="nav-text">회원 관리</span>
          </div>
          <div 
            className={`nav-item ${activeMenu === 'restaurants' ? 'active' : ''}`}
            onClick={() => setActiveMenu('restaurants')}
          >
            <span className="nav-text">식당 관리</span>
          </div>
          <div 
            className={`nav-item ${activeMenu === 'reservations' ? 'active' : ''}`}
            onClick={() => setActiveMenu('reservations')}
          >
            <span className="nav-text">예약 관리</span>
          </div>
          <div 
            className={`nav-item ${activeMenu === 'reviews' ? 'active' : ''}`}
            onClick={() => setActiveMenu('reviews')}
          >
            <span className="nav-text">리뷰 관리</span>
          </div>
          <div 
            className={`nav-item ${activeMenu === 'logs' ? 'active' : ''}`}
            onClick={() => setActiveMenu('logs')}
          >
            <span className="nav-text">시스템 로그</span>
          </div>
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <span className="user-name">{user?.name}</span>
          </div>
          <button className="logout-btn" onClick={() => {
            showConfirm({
              title: '로그아웃',
              message: '로그아웃 하시겠습니까?',
              confirmText: '로그아웃',
              cancelText: '취소',
              type: 'warning',
              onConfirm: () => {
                logout();
                navigate('/login');
              }
            });
          }}>
            로그아웃
          </button>
        </div>
      </aside>

      {/* 메인 컨텐츠 */}
      <div className="dashboard-main">
        {/* 상단 헤더 */}
        <header className="dashboard-top-header">
          <h1>
            {activeMenu === 'dashboard' && 'Dashboard'}
            {activeMenu === 'users' && '회원 관리'}
            {activeMenu === 'restaurants' && '식당 관리'}
            {activeMenu === 'reservations' && '예약 관리'}
            {activeMenu === 'reviews' && '리뷰 관리'}
            {activeMenu === 'logs' && '시스템 로그'}
          </h1>
          <div className="header-actions">
            <span className="current-time">
              {new Date().toLocaleDateString('ko-KR', { 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric',
                weekday: 'long'
              })}
            </span>
          </div>
        </header>

        {/* 대시보드 화면 */}
        {activeMenu === 'dashboard' && (
          <>
            {/* 통계 카드 */}
            <div className="stats-section">
              <div className="stat-card">
                <div className="stat-info">
                  <div className="stat-label">Total Users</div>
                  <div className="stat-value">{stats.totalUsers}</div>
                  <div className="stat-detail">Active: {stats.activeUsers}</div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-info">
                  <div className="stat-label">Restaurants</div>
                  <div className="stat-value">{stats.totalRestaurants}</div>
                  <div className="stat-detail">Running</div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-info">
                  <div className="stat-label">Reservations</div>
                  <div className="stat-value">{stats.totalReservations}</div>
                  <div className="stat-detail">Today: {stats.todayReservations}</div>
                </div>
              </div>

              <div className="stat-card">
                <div className="stat-info">
                  <div className="stat-label">Pending</div>
                  <div className="stat-value">{stats.pendingReservations}</div>
                  <div className="stat-detail">Waiting</div>
                </div>
              </div>
            </div>

            {/* 차트 & 시스템 상태 */}
            <div className="dashboard-content">
              <div className="chart-section">
                <h3>Monthly Statistics</h3>
                <div className="chart-placeholder">
                  <div className="chart-bars">
                    <div className="chart-bar" style={{ height: '55%' }}>
                      <div className="bar-fill" style={{ background: 'linear-gradient(to top, #1976d2, #42a5f5)' }}></div>
                      <span className="bar-label">1월</span>
                    </div>
                    <div className="chart-bar" style={{ height: '70%' }}>
                      <div className="bar-fill" style={{ background: 'linear-gradient(to top, #1976d2, #42a5f5)' }}></div>
                      <span className="bar-label">2월</span>
                    </div>
                    <div className="chart-bar" style={{ height: '82%' }}>
                      <div className="bar-fill" style={{ background: 'linear-gradient(to top, #1976d2, #42a5f5)' }}></div>
                      <span className="bar-label">3월</span>
                    </div>
                    <div className="chart-bar" style={{ height: '65%' }}>
                      <div className="bar-fill" style={{ background: 'linear-gradient(to top, #1976d2, #42a5f5)' }}></div>
                      <span className="bar-label">4월</span>
                    </div>
                    <div className="chart-bar" style={{ height: '78%' }}>
                      <div className="bar-fill" style={{ background: 'linear-gradient(to top, #1976d2, #42a5f5)' }}></div>
                      <span className="bar-label">5월</span>
                    </div>
                    <div className="chart-bar" style={{ height: '90%' }}>
                      <div className="bar-fill" style={{ background: 'linear-gradient(to top, #1976d2, #42a5f5)' }}></div>
                      <span className="bar-label">6월</span>
                    </div>
                  </div>
                  <p className="chart-legend">월별 예약 추이</p>
                </div>
              </div>

              <div className="activity-section">
                <h3>System Status</h3>
                <div style={{ padding: '10px 0' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                    <span style={{ fontSize: '13px', color: '#b0b0b0' }}>회원 관리</span>
                    <span style={{ fontSize: '14px', fontWeight: '600', color: '#4caf50' }}>Active</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                    <span style={{ fontSize: '13px', color: '#b0b0b0' }}>식당 관리</span>
                    <span style={{ fontSize: '14px', fontWeight: '600', color: '#4caf50' }}>Active</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                    <span style={{ fontSize: '13px', color: '#b0b0b0' }}>예약 시스템</span>
                    <span style={{ fontSize: '14px', fontWeight: '600', color: '#4caf50' }}>Active</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                    <span style={{ fontSize: '13px', color: '#b0b0b0' }}>리뷰 시스템</span>
                    <span style={{ fontSize: '14px', fontWeight: '600', color: '#4caf50' }}>Active</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                    <span style={{ fontSize: '13px', color: '#b0b0b0' }}>알림 시스템</span>
                    <span style={{ fontSize: '14px', fontWeight: '600', color: '#4caf50' }}>Active</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0' }}>
                    <span style={{ fontSize: '13px', color: '#b0b0b0' }}>데이터베이스</span>
                    <span style={{ fontSize: '14px', fontWeight: '600', color: '#4caf50' }}>Active</span>
                  </div>
                </div>
              </div>
            </div>
          </>
        )}

        {/* 회원 관리 화면 */}
        {activeMenu === 'users' && (
          <div className="content-section">
            {loading ? (
              <div className="loading-spinner">로딩 중...</div>
            ) : (
              <>
                <div className="section-header">
                  <h2>전체 회원 ({users.length}명)</h2>
                </div>
                <div className="data-table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>아이디</th>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>전화번호</th>
                        <th>역할</th>
                        <th>상태</th>
                        <th>가입일</th>
                        <th>관리</th>
                      </tr>
                    </thead>
                    <tbody>
                      {users.map(u => (
                        <tr key={u.id}>
                          <td>{u.id}</td>
                          <td>{u.username}</td>
                          <td>{u.name}</td>
                          <td>{u.email}</td>
                          <td>{u.phone}</td>
                          <td>{getRoleBadge(u.role)}</td>
                          <td>{getStatusBadge(u.status)}</td>
                          <td>{new Date(u.createdAt).toLocaleDateString('ko-KR')}</td>
                          <td>
                            <div className="action-buttons">
                              {u.status === 'ACTIVE' && (
                                <button 
                                  className="btn-suspend"
                                  onClick={() => handleUserStatusChange(u.id, 'SUSPENDED')}
                                >
                                  정지
                                </button>
                              )}
                              {u.status === 'SUSPENDED' && (
                                <button 
                                  className="btn-activate"
                                  onClick={() => handleUserStatusChange(u.id, 'ACTIVE')}
                                >
                                  활성화
                                </button>
                              )}
                              <button 
                                className="btn-delete"
                                onClick={() => handleDeleteUser(u.id)}
                              >
                                삭제
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </div>
        )}

        {/* 식당 관리 화면 */}
        {activeMenu === 'restaurants' && (
          <div className="content-section">
            {loading ? (
              <div className="loading-spinner">로딩 중...</div>
            ) : (
              <>
                <div className="section-header">
                  <h2>전체 식당 ({restaurants.length}개)</h2>
                </div>
                <div className="data-table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>식당명</th>
                        <th>지점명</th>
                        <th>카테고리</th>
                        <th>지역</th>
                        <th>전화번호</th>
                        <th>도로명주소</th>
                        <th>상태</th>
                      </tr>
                    </thead>
                    <tbody>
                      {restaurants.slice(0, 50).map(r => (
                        <tr key={r.id}>
                          <td>{r.id}</td>
                          <td>{r.restaurantName}</td>
                          <td>{r.branchName || '-'}</td>
                          <td>{r.category || '-'}</td>
                          <td>{r.regionName}</td>
                          <td>{r.phoneNumber || '-'}</td>
                          <td>{r.roadAddress}</td>
                          <td>
                            <span className="status-badge" style={{ backgroundColor: '#4caf50' }}>
                              {r.status || 'NORMAL'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </div>
        )}

        {/* 예약 관리 화면 */}
        {activeMenu === 'reservations' && (
          <div className="content-section">
            {loading ? (
              <div className="loading-spinner">로딩 중...</div>
            ) : (
              <>
                <div className="section-header">
                  <h2>전체 예약 ({allReservations.length}건)</h2>
                </div>
                <div className="data-table-container">
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>예약 ID</th>
                        <th>사용자</th>
                        <th>식당</th>
                        <th>예약일시</th>
                        <th>인원</th>
                        <th>상태</th>
                        <th>생성일</th>
                      </tr>
                    </thead>
                    <tbody>
                      {allReservations.map(r => (
                        <tr key={r.id}>
                          <td>{r.id}</td>
                          <td>{r.userName}</td>
                          <td>{r.restaurantName}</td>
                          <td>{r.reservationDate} {r.reservationTime}</td>
                          <td>{r.numberOfPeople}명</td>
                          <td>{getReservationStatusBadge(r.status)}</td>
                          <td>{new Date(r.createdAt).toLocaleString('ko-KR')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </>
            )}
          </div>
        )}

        {/* 리뷰 관리 화면 */}
        {activeMenu === 'reviews' && (
          <div className="content-section">
            {loading ? (
              <div className="loading-spinner">로딩 중...</div>
            ) : (
              <>
                <div className="section-header">
                  <h2>신고된 리뷰 ({reportedReviews.length}개)</h2>
                </div>
                {reportedReviews.length === 0 ? (
                  <div className="empty-state">
                    <span className="empty-icon">⭐</span>
                    <h3>신고된 리뷰가 없습니다</h3>
                    <p>현재 처리할 신고 내역이 없습니다.</p>
                  </div>
                ) : (
                  <div className="reviews-grid">
                    {reportedReviews.map(review => (
                      <div key={review.id} className="review-card">
                        <div className="review-header">
                          <span className="report-badge">🚨 신고됨</span>
                          <span>작성자: {review.userName}</span>
                        </div>
                        <div className="review-body">
                          <p><strong>식당:</strong> {review.restaurantName}</p>
                          <p><strong>평점:</strong> {'⭐'.repeat(review.rating)}</p>
                          <p><strong>내용:</strong> {review.content}</p>
                          <p><strong>작성일:</strong> {new Date(review.createdAt).toLocaleString('ko-KR')}</p>
                        </div>
                        <div className="review-actions">
                          <button className="btn-delete">삭제</button>
                          <button className="btn-ignore">무시</button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {/* 시스템 로그 화면 */}
        {activeMenu === 'logs' && (
          <div className="content-section">
            <div className="empty-state">
              <span className="empty-icon">📋</span>
              <h3>시스템 로그</h3>
              <p>시스템 로그 기능은 추후 구현 예정입니다.</p>
            </div>
          </div>
        )}
      </div>
      
      {/* 확인 팝업 */}
      <ConfirmModal
        isOpen={modalState.isOpen}
        onClose={hideConfirm}
        onConfirm={handleConfirm}
        title={modalState.title}
        message={modalState.message}
        confirmText={modalState.confirmText}
        cancelText={modalState.cancelText}
        type={modalState.type}
      />
      
      {/* 알림 팝업 */}
      <NotificationModal
        isOpen={notificationModal.isOpen}
        onClose={closeNotification}
        type={notificationModal.type}
        title={notificationModal.title}
        message={notificationModal.message}
        buttonText="확인"
        autoClose={false}
      />
    </div>
  );
};

export default AdminPage;

