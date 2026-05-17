/**
 * Axios 實例封裝
 *
 * 為什麼要封裝？
 * 1. 統一設定 baseURL（後端位址），不用每個 API 都寫完整網址
 * 2. 自動在每個請求的 Header 加上 JWT Token（登入後才有）
 * 3. 統一處理錯誤（如 Token 過期自動跳轉登入頁）
 */
import axios from 'axios'
import router from '../router'

// 建立 Axios 實例，baseURL 指向 Spring Boot 後端
const api = axios.create({
  baseURL: 'http://localhost:7687/api',
  timeout: 10000, // 請求超時 10 秒
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 請求攔截器（Request Interceptor）
 * 每次發送 API 請求前，自動從 localStorage 取出 Token 並加到 Header
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 回應攔截器（Response Interceptor）
 * 統一處理後端回傳的錯誤
 */
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // 401 = Token 過期或無效 → 清除 Token 並跳轉登入頁
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default api
