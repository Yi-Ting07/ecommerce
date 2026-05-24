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

  // 初始化時檢查 token 是否已過期，若過期則立即清除
  if (token.value) {
    try {
      const payload = JSON.parse(atob(token.value.split('.')[1]))
      if (payload.exp && Date.now() / 1000 > payload.exp) {
        token.value = ''
        user.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    } catch {
      // token 格式異常，也清除
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }

  // 2FA 待驗狀態（登入但尚未完成 2FA 驗證）
  const pendingTwoFactor = ref(null) // { tempToken, method }

  // === Getters（計算屬性）===
  // 是否已登入
  const isLoggedIn = computed(() => !!token.value)
  // 是否為管理員
  const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN')
  // 使用者名稱
  const username = computed(() => user.value?.username || '')

  // === Actions（方法）===

  /**
   * 登入（第一步）
   * 若後端回傳 requiresTwoFactor，則不完成登入，改存 pendingTwoFactor
   * @returns {object} { requiresTwoFactor, method } 或 null（直接登入成功）
   */
  async function login(email, password, captchaId, captchaCode) {
    const response = await api.post('/auth/login', { email, password, captchaId, captchaCode })
    const data = response.data

    if (data.requiresTwoFactor) {
      // 需要 2FA：暫存 pending 狀態，等前端引導使用者完成驗證
      pendingTwoFactor.value = {
        tempToken: data.tempToken,
        method: data.twoFactorMethod,
        username: data.username,
        email: data.email,
        role: data.role
      }
      return { requiresTwoFactor: true, method: data.twoFactorMethod }
    }

    // 直接登入成功
    _saveSession(data)
    return { requiresTwoFactor: false }
  }

  /**
   * 完成 2FA 驗證（第二步）
   * @param {string} code - 使用者輸入的 6 位驗證碼
   */
  async function verifyTwoFactor(code) {
    if (!pendingTwoFactor.value) throw new Error('沒有待驗的 2FA 狀態')
    const response = await api.post('/auth/2fa/verify', {
      tempToken: pendingTwoFactor.value.tempToken,
      code
    })
    pendingTwoFactor.value = null
    _saveSession(response.data)
  }

  /**
   * 重新寄送 Email OTP
   */
  async function resendEmailOtp() {
    if (!pendingTwoFactor.value) throw new Error('沒有待驗的 2FA 狀態')
    await api.post('/auth/2fa/resend-email', { tempToken: pendingTwoFactor.value.tempToken })
  }

  /**
   * 清除 2FA pending 狀態（使用者取消）
   */
  function cancelTwoFactor() {
    pendingTwoFactor.value = null
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
    pendingTwoFactor.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  /** 私有：儲存 session */
  function _saveSession(data) {
    token.value = data.token
    user.value = { username: data.username, role: data.role, email: data.email }
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return {
    token,
    user,
    pendingTwoFactor,
    isLoggedIn,
    isAdmin,
    username,
    login,
    verifyTwoFactor,
    resendEmailOtp,
    cancelTwoFactor,
    register,
    logout
  }
})

