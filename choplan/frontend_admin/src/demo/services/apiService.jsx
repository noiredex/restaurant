/**
 * 새로운 API 서비스
 * - Statistics (인기 식당, 인기 검색어)
 * - Favorites (찜)
 * - Notifications (알림)
 * - Restaurants (카테고리 검색)
 */
import axios from 'axios';
import { API_ENDPOINTS } from '../../constants/config/apiConfig';

const API_BASE = API_ENDPOINTS.RESTAURANTS.replace('/restaurants', '');

// 각 API 베이스 URL을 사용하도록 변경
const STATISTICS_BASE = API_ENDPOINTS.STATISTICS;
const FAVORITES_BASE = API_ENDPOINTS.FAVORITES;
const NOTIFICATIONS_BASE = API_ENDPOINTS.NOTIFICATIONS;
const RESTAURANTS_BASE = API_ENDPOINTS.RESTAURANTS;

// ============ Statistics API ============
export const statisticsAPI = {
  // 식당 클릭 기록
  recordClick: (restaurantId, userId = null) => {
    return axios.post(`${STATISTICS_BASE}/click`, {
      restaurantId,
      userId
    });
  },

  // 인기 식당 조회 (상위 N개)
  getPopularRestaurants: (limit = 10) => {
    return axios.get(`${STATISTICS_BASE}/popular-restaurants`, {
      params: { limit }
    });
  },

  // 인기 식당 조회 (클릭 수와 함께)
  getPopularRestaurantsWithCount: (limit = 10) => {
    return axios.get(`${STATISTICS_BASE}/popular-restaurants-with-count`, {
      params: { limit }
    });
  },

  // 최근 7일 인기 식당
  getRecentPopularRestaurants: (limit = 10) => {
    return axios.get(`${STATISTICS_BASE}/recent-popular-restaurants`, {
      params: { limit }
    });
  },

  // 오늘 인기 식당
  getTodayPopularRestaurants: (limit = 10) => {
    return axios.get(`${STATISTICS_BASE}/today-popular-restaurants`, {
      params: { limit }
    });
  },

  // 검색어 기록
  recordSearch: (keyword, userId = null) => {
    return axios.post(`${STATISTICS_BASE}/search`, {
      keyword
    });
  },

  // 인기 검색어 조회
  getPopularKeywords: (limit = 10) => {
    return axios.get(`${STATISTICS_BASE}/popular-keywords`, {
      params: { limit }
    });
  },
};

// ============ Favorites API ============
export const favoritesAPI = {
  // 찜 목록 조회
  getUserFavorites: (userId) => {
    return axios.get(`${FAVORITES_BASE}/${userId}`);
  },

  // 찜 추가
  addFavorite: (userId, restaurantId) => {
    return axios.post(`${FAVORITES_BASE}`, {
      userId,
      restaurantId
    });
  },

  // 찜 삭제
  removeFavorite: (userId, restaurantId) => {
    return axios.delete(`${FAVORITES_BASE}`, {
      params: { userId, restaurantId }
    });
  },

  // 찜 여부 확인
  isFavorite: (userId, restaurantId) => {
    return axios.get(`${FAVORITES_BASE}/check`, {
      params: { userId, restaurantId }
    });
  },

  // 찜 토글
  toggleFavorite: (userId, restaurantId) => {
    return axios.post(`${FAVORITES_BASE}/toggle`, {
      userId,
      restaurantId
    });
  },
};

// ============ Notifications API ============
export const notificationsAPI = {
  // 사용자 알림 조회 (모두)
  getUserNotifications: (userId) => {
    return axios.get(`${NOTIFICATIONS_BASE}/${userId}`);
  },

  // 읽지 않은 알림 조회
  getUnreadNotifications: (userId) => {
    return axios.get(`${NOTIFICATIONS_BASE}/${userId}/unread`);
  },

  // 읽지 않은 알림 개수
  getUnreadCount: (userId) => {
    return axios.get(`${NOTIFICATIONS_BASE}/${userId}/unread-count`);
  },

  // 알림 읽음 처리
  markAsRead: (notificationId) => {
    return axios.put(`${NOTIFICATIONS_BASE}/${notificationId}/read`);
  },

  // 모든 알림 읽음 처리
  markAllAsRead: (userId) => {
    return axios.put(`${NOTIFICATIONS_BASE}/${userId}/read-all`);
  },

  // 알림 삭제
  deleteNotification: (notificationId) => {
    return axios.delete(`${NOTIFICATIONS_BASE}/${notificationId}`);
  },
};

// ============ Restaurants API ============
export const restaurantsAPI = {
  // 모든 식당 조회
  getAll: () => {
    return axios.get(`${RESTAURANTS_BASE}/all`);
  },

  // 식당 상세 조회
  getById: (id) => {
    return axios.get(`${RESTAURANTS_BASE}/${id}`);
  },

  // 키워드 검색
  search: (keyword) => {
    return axios.get(`${RESTAURANTS_BASE}`, {
      params: { keyword }
    });
  },

  // 식당명으로 검색
  searchByName: (name) => {
    return axios.get(`${RESTAURANTS_BASE}/name`, {
      params: { name }
    });
  },

  // 지역 기반 검색
  searchByRegion: (latitude, longitude, radius = 5000) => {
    return axios.get(`${RESTAURANTS_BASE}/region`, {
      params: { latitude, longitude, radius }
    });
  },
};

export default {
  statisticsAPI,
  favoritesAPI,
  notificationsAPI,
  restaurantsAPI,
};

