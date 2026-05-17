/**
 * Cart Store（購物車狀態管理）
 *
 * 使用 Pinia 管理購物車狀態。
 *
 * 設計說明：
 * - 購物車資料以後端 DB 為主（server-side cart）
 * - 前端只快取 cartCount 用於顯示 Badge，避免每個頁面都打 API
 * - 重要操作（加入/刪除/結帳）都會同步後端後再更新前端狀態
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useCartStore = defineStore('cart', () => {
  // 購物車完整資料（CartResponse 結構）
  const cart = ref(null)
  // 是否正在載入
  const loading = ref(false)

  // 購物車商品種類數（用於 Header badge）
  const cartCount = computed(() => cart.value?.totalItems ?? 0)
  // 購物車總金額
  const totalAmount = computed(() => cart.value?.totalAmount ?? 0)
  // 購物車項目列表
  const items = computed(() => cart.value?.items ?? [])

  /** 從後端拉取最新購物車資料 */
  async function fetchCart() {
    loading.value = true
    try {
      const response = await api.get('/cart')
      cart.value = response.data
    } catch (error) {
      // 未登入時 401 由 Axios interceptor 處理，這裡不額外處理
      cart.value = null
    } finally {
      loading.value = false
    }
  }

  /** 加入商品（成功後自動更新本地狀態） */
  async function addItem(productId, quantity = 1) {
    const response = await api.post('/cart/items', { productId, quantity })
    cart.value = response.data
    return response.data
  }

  /** 更新數量 */
  async function updateItem(itemId, quantity) {
    const response = await api.put(`/cart/items/${itemId}`, { quantity })
    cart.value = response.data
    return response.data
  }

  /** 移除單項 */
  async function removeItem(itemId) {
    const response = await api.delete(`/cart/items/${itemId}`)
    cart.value = response.data
    return response.data
  }

  /** 清空購物車 */
  async function clearCart() {
    await api.delete('/cart')
    cart.value = { items: [], totalItems: 0, totalAmount: 0 }
  }

  /** 登出時重置購物車狀態 */
  function reset() {
    cart.value = null
  }

  return {
    cart,
    loading,
    cartCount,
    totalAmount,
    items,
    fetchCart,
    addItem,
    updateItem,
    removeItem,
    clearCart,
    reset
  }
})
