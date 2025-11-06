/**
 * 테스트용 레이아웃 - 데모 종료 시 제거 예정
 */
import React from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import BottomNav from '../BottomNav/BottomNav';
import './DemoLayout.css';

const DemoLayout = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isOwner = user?.role === 'OWNER';

  return (
    <div className="demo-layout">
      <header className="demo-header">
        <div className="demo-header-content">
          <div className="demo-header-left">
            <h1 onClick={() => navigate('/')}>🍽️ ChopRest</h1>
            <span className="demo-badge">DEMO</span>
          </div>
          <div className="demo-header-right">
            <span className="demo-user-info">
              {user?.name} ({user?.role})
            </span>
            <button className="demo-logout-btn" onClick={handleLogout}>
              로그아웃
            </button>
          </div>
        </div>
      </header>

      <main className={`demo-main ${isOwner ? 'owner-mode' : ''}`}>
        <Outlet />
      </main>

      {/* OWNER는 독립된 네비게이션을 가지므로 BottomNav 숨김 */}
      {!isOwner && <BottomNav />}
    </div>
  );
};

export default DemoLayout;

