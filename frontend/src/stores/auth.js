/**
 * Auth Store（認證狀態管理）
 *
 * 使用 Pinia 管理全局的登入狀態：
 * - token: JWT Token（登入後取得）
 * - user: 使用者資訊（名稱、角色等）
 *
 * 為什麼要用 Pinia？
 * 多個頁面/元件都需要知道「使用者是否登入」「是什麼角色」，
 * 用 Pinia 統一管理，任何元件都能存取。
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  // === State（狀態）===
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  // === Getters（計算屬性）===
  // 是否已登入
  const isLoggedIn = computed(() => !!token.value)
  // 是否為管理員
  const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN')
  // 使用者名稱
  const username = computed(() => user.value?.username || '')

  // === Actions（方法）===

  /**
   * 登入
   * @param {string} email - 電子郵件
   * @param {string} password - 密碼
   */
  async function login(email, password) {
    const response = await api.post('/auth/login', { email, password })
    const data = response.data

    // 儲存 Token 和使用者資訊到 localStorage（重新整理後不會消失）
    token.value = data.token
    user.value = { username: data.username, role: data.role, email: data.email }

    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  /**
   * 註冊
   */
  async function register(username, email, password) {
    await api.post('/auth/register', { username, email, password })
  }

  /**
   * 登出
   */
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    username,
    login,
    register,
    logout
  }
})
