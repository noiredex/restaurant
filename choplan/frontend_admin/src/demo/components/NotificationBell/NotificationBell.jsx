/**
 * 알림 아이콘 + 배지
 */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../demo/AuthContext';
import { API_ENDPOINTS } from '../../../constants/config/apiConfig';
import axios from 'axios';
import './NotificationBell.css';

const NotificationBell = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  // 읽지 않은 알림 개수 조회
  useEffect(() => {
    if (user) {
      fetchUnreadCount();
      // 30초마다 알림 개수 갱신
      const interval = setInterval(fetchUnreadCount, 30000);
      return () => clearInterval(interval);
    }
  }, [user]);

  const fetchUnreadCount = async () => {
    try {
      const response = await axios.get(
        `${API_ENDPOINTS.NOTIFICATIONS}/${user.userId}/unread-count`
      );
      setUnreadCount(response.data.count);
    } catch (error) {
      console.error('알림 개수 조회 오류:', error);
    }
  };

  // 알림 목록 조회 (드롭다운 열 때)
  const fetchNotifications = async () => {
    try {
      const response = await axios.get(
        `${API_ENDPOINTS.NOTIFICATIONS}/${user.userId}/unread`
      );
      setNotifications(response.data.slice(0, 5)); // 최근 5개만
    } catch (error) {
      console.error('알림 조회 오류:', error);
    }
  };

  const handleBellClick = () => {
    if (!showDropdown) {
      fetchNotifications();
    }
    setShowDropdown(!showDropdown);
  };

  const handleNotificationClick = async (notification) => {
    // 읽음 처리
    try {
      await axios.put(
        `${API_ENDPOINTS.NOTIFICATIONS}/${notification.id}/read`
      );
      fetchUnreadCount();
      
      // 관련 페이지로 이동
      if (notification.type === 'RESERVATION_APPROVED' || 
          notification.type === 'REVIEW_REMINDER') {
        navigate('/reservation');
      }
      
      setShowDropdown(false);
    } catch (error) {
      console.error('알림 읽음 처리 오류:', error);
    }
  };

  const handleViewAll = () => {
    navigate('/notifications');
    setShowDropdown(false);
  };

  if (!user) return null;

  return (
    <div className="notification-bell">
      <button className="bell-btn" onClick={handleBellClick}>
        <span className="bell-icon">🔔</span>
        {unreadCount > 0 && (
          <span className="notification-badge">{unreadCount > 9 ? '9+' : unreadCount}</span>
        )}
      </button>

      {showDropdown && (
        <div className="notification-dropdown">
          <div className="notification-header">
            <h3>알림</h3>
            {unreadCount > 0 && (
              <span className="unread-count">{unreadCount}개</span>
            )}
          </div>

          <div className="notification-list">
            {notifications.length === 0 ? (
              <div className="no-notifications">
                <span className="empty-icon">📭</span>
                <p>새로운 알림이 없습니다</p>
              </div>
            ) : (
              notifications.map(notification => (
                <div
                  key={notification.id}
                  className="notification-item"
                  onClick={() => handleNotificationClick(notification)}
                >
                  <div className="notification-content">
                    <h4>{notification.title}</h4>
                    <p>{notification.message}</p>
                    <span className="notification-time">
                      {getTimeAgo(notification.createdAt)}
                    </span>
                  </div>
                  {!notification.isRead && <span className="unread-dot"></span>}
                </div>
              ))
            )}
          </div>

          {notifications.length > 0 && (
            <div className="notification-footer">
              <button onClick={handleViewAll}>모든 알림 보기</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

// 시간 차이 계산
const getTimeAgo = (dateString) => {
  const now = new Date();
  const past = new Date(dateString);
  const diffMs = now - past;
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return '방금 전';
  if (diffMins < 60) return `${diffMins}분 전`;
  if (diffHours < 24) return `${diffHours}시간 전`;
  if (diffDays < 7) return `${diffDays}일 전`;
  return past.toLocaleDateString('ko-KR');
};

export default NotificationBell;

