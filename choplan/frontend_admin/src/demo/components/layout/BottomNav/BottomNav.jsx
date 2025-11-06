/**
 * 공통 하단 네비게이션 바
 */
import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import LoginRequiredModal from '../../../../components/modals/LoginRequiredModal';
import './BottomNav.css';

const BottomNav = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [showLoginModal, setShowLoginModal] = useState(false);

  // 홈페이지인지 확인
  const isHomePage = location.pathname === '/';

  // 홈페이지용 네비게이션
  const homeNavItems = [
    { path: '/', label: '홈', requiresAuth: false },
    { path: '/search', label: '검색', requiresAuth: false },
    { path: '/nearme', label: '위치로검색', requiresAuth: false },
    { path: user?.role === 'OWNER' ? '/owner-dashboard' : '/profile', label: '마이페이지', requiresAuth: true },
  ];

  // 로그인하지 않은 경우 기본 메뉴
  const defaultNavItems = [
    { path: '/', label: '홈', requiresAuth: false },
    { path: '/search', label: '검색', requiresAuth: false },
    { path: '/nearme', label: '위치로검색', requiresAuth: false },
    { path: '/login', label: '마이페이지', requiresAuth: false },
  ];

  // 로그인한 경우 전체 메뉴
  const loggedInNavItems = [
    { path: '/', label: '홈', requiresAuth: false },
    { path: '/search', label: '검색', requiresAuth: false },
    { path: '/nearme', label: '위치로검색', requiresAuth: false },
    { path: '/profile', label: '마이페이지', requiresAuth: true },
  ];

  // 네비게이션 아이템 선택
  let navItems;
  if (isHomePage) {
    navItems = homeNavItems;
  } else {
    navItems = user 
      ? (user.role === 'ADMIN' 
          ? [...loggedInNavItems, { path: '/demo/admin', label: '관리자', icon: '⚙️', requiresAuth: true }]
          : loggedInNavItems)
      : defaultNavItems;
  }

  const handleNavClick = (item) => {
    // 로그인이 필요한 페이지이고 로그인되지 않은 경우
    if (item.requiresAuth && !user) {
      setShowLoginModal(true);
      return;
    }
    navigate(item.path);
  };

  const handleLogin = () => {
    setShowLoginModal(false);
    navigate('/demo/login');
  };

  const handleCloseModal = () => {
    setShowLoginModal(false);
  };

  return (
    <>
      <nav className="bottom-nav">
        {navItems.map(item => (
          <button
            key={item.path}
            className={location.pathname === item.path ? 'active' : ''}
            onClick={() => handleNavClick(item)}
          >
            <span className="nav-label">{item.label}</span>
          </button>
        ))}
      </nav>
      
      <LoginRequiredModal
        isOpen={showLoginModal}
        onClose={handleCloseModal}
        onLogin={handleLogin}
      />
    </>
  );
};

export default BottomNav;
