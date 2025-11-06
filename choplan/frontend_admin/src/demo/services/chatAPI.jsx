/**
 * 채팅 API 서비스
 */
import axios from 'axios';
import { API_ENDPOINTS } from '../../constants/config/apiConfig';

const API_BASE_URL = API_ENDPOINTS.CHAT;

export const chatAPI = {
  // 채팅방 생성 또는 조회
  createOrGetChatRoom: (userId, restaurantId) => {
    return axios.post(`${API_BASE_URL}/room`, null, {
      params: { userId, restaurantId }
    });
  },

  // 회원의 채팅방 목록 조회
  getUserChatRooms: (userId) => {
    return axios.get(`${API_BASE_URL}/rooms/user/${userId}`);
  },

  // 가게 주인의 채팅방 목록 조회
  getOwnerChatRooms: (ownerId) => {
    return axios.get(`${API_BASE_URL}/rooms/owner/${ownerId}`);
  },

  // 채팅방의 메시지 목록 조회
  getChatMessages: (chatRoomId, userId) => {
    return axios.get(`${API_BASE_URL}/room/${chatRoomId}/messages`, {
      params: { userId }
    });
  },

  // 메시지 전송
  sendMessage: (messageRequest) => {
    return axios.post(`${API_BASE_URL}/message`, messageRequest);
  },

  // 메시지를 읽음으로 표시
  markMessagesAsRead: (chatRoomId, userId) => {
    return axios.post(`${API_BASE_URL}/room/${chatRoomId}/read`, null, {
      params: { userId }
    });
  },

  // 읽지 않은 채팅방 수 조회
  getUnreadChatRoomCount: (userId) => {
    return axios.get(`${API_BASE_URL}/unread-count/${userId}`);
  },
};


