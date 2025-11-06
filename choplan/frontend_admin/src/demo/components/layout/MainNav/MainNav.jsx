/**
 * 메인 네비게이션 바 (가로 풀 사이즈)
 * 홈, 검색, 내주변, 예약, 마이
 */
import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './MainNav.css';

const MainNav = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { path: '/', label: '홈', icon: '🏠' },
    { path: '/search', label: '검색', icon: '🔍' },
    { path: '/nearme', label: '내주변', icon: '📍' },
    { path: '/profile', label: '마이', icon: '👤' },
  ];

  const isActive = (path) => {
    if (path === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(path);
  };

  return (
    <nav className="main-nav">
      <div className="main-nav-container">
        {navItems.map(item => (
          <button
            key={item.path}
            className={`nav-item ${isActive(item.path) ? 'active' : ''}`}
            onClick={() => navigate(item.path)}
          >
            <span className="nav-icon">{item.icon}</span>
            <span className="nav-label">{item.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
};

export default MainNav;

