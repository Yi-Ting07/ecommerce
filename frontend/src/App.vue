<!--
  App.vue — 應用程式根元件

  <router-view /> 會根據當前的 URL 自動替換成對應的頁面元件。
  例如：URL 是 /login → 顯示 LoginPage.vue
       URL 是 /products → 顯示 ProductList.vue
-->
<template>
  <router-view />

  <!-- 懸浮購物車按鈕：已登入且非管理後台頁面才顯示 -->
  <div
    v-if="authStore.isLoggedIn && !isAdminPage && !isCartPage"
    class="fab-wrap"
  >
    <!-- 購物車預覽面板 -->
    <Transition name="cart-panel">
      <div v-if="cartOpen" class="cart-preview" @click.stop>
        <div class="cp-header">
          <span class="cp-title">🛒 購物車預覽</span>
          <button class="cp-close" @click="cartOpen = false" aria-label="關閉">✕</button>
        </div>

        <!-- 載入中 -->
        <div v-if="cartStore.loading" class="cp-loading">載入中...</div>

        <!-- 空購物車 -->
        <div v-else-if="cartStore.items.length === 0" class="cp-empty">
          <span>購物車是空的</span>
          <router-link to="/products" class="cp-shop-link" @click="cartOpen = false">去逛逛 →</router-link>
        </div>

        <!-- 商品列表 -->
        <ul v-else class="cp-list">
          <li v-for="item in cartStore.items" :key="item.id" class="cp-item">
            <img
              class="cp-img"
              :src="item.productImageUrl || fallbackImage"
              :alt="item.productName"
            />
            <div class="cp-info">
              <p class="cp-name">{{ item.productName }}</p>
              <p class="cp-qty">x{{ item.quantity }}</p>
            </div>
            <p class="cp-price">{{ formatCurrency(item.subtotal) }}</p>
          </li>
        </ul>

        <!-- 合計 + 結帳按鈕 -->
        <div v-if="cartStore.items.length > 0" class="cp-footer">
          <div class="cp-total">
            <span>合計</span>
            <span class="cp-total-price">{{ formatCurrency(cartStore.totalAmount) }}</span>
          </div>
          <router-link to="/cart" class="cp-checkout-btn" @click="cartOpen = false">
            開始結帳
          </router-link>
        </div>
      </div>
    </Transition>

    <!-- FAB 按鈕 -->
    <button
      class="fab-cart"
      :class="{ active: cartOpen }"
      aria-label="預覽購物車"
      @click.stop="toggleCart"
    >
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="26" height="26">
        <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4H6zM3.5 6h17M16 10a4 4 0 01-8 0"/>
        <path fill-rule="evenodd" d="M7.5 6h9L19 2H5L7.5 6z" clip-rule="evenodd"/>
        <path d="M9 10a3 3 0 006 0" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round"/>
      </svg>
      <span v-if="cartStore.cartCount > 0" class="fab-badge">
        {{ cartStore.cartCount > 99 ? '99+' : cartStore.cartCount }}
      </span>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const isAdminPage = computed(() => route.path.startsWith('/admin'))
const isCartPage = computed(() => route.path === '/cart')

// ===== 購物車預覽面板 =====
const cartOpen = ref(false)

const fallbackImage =
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=100&q=60'

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})
function formatCurrency(value) {
  return formatter.format(Number(value || 0))
}

function toggleCart() {
  cartOpen.value = !cartOpen.value
  if (cartOpen.value && authStore.isLoggedIn) {
    cartStore.fetchCart()
  }
}

function closeCartOnOutside() {
  if (cartOpen.value) cartOpen.value = false
}

// ===== 閒置自動登出（30 分鐘）=====
const IDLE_TIMEOUT_MS = 30 * 60 * 1000
let idleTimer = null

function resetIdleTimer() {
  if (!authStore.isLoggedIn) return
  clearTimeout(idleTimer)
  idleTimer = setTimeout(handleIdleLogout, IDLE_TIMEOUT_MS)
}

async function handleIdleLogout() {
  if (!authStore.isLoggedIn) return
  authStore.logout()
  cartStore.reset()
  cartOpen.value = false
  await ElMessageBox.alert('您已閒置超過 30 分鐘，請重新登入。', '登入逾時', {
    type: 'warning',
    confirmButtonText: '確定',
    showClose: false
  })
  router.push('/login')
}

const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart', 'click']

// ===== Token 失效事件 =====
function handleAuthExpired() {
  authStore.logout()
  cartStore.reset()
}

onMounted(() => {
  window.addEventListener('auth:expired', handleAuthExpired)
  window.addEventListener('click', closeCartOnOutside)

  // 若已登入，啟動閒置計時器
  ACTIVITY_EVENTS.forEach(e => window.addEventListener(e, resetIdleTimer, { passive: true }))
  if (authStore.isLoggedIn) resetIdleTimer()
})

onUnmounted(() => {
  window.removeEventListener('auth:expired', handleAuthExpired)
  window.removeEventListener('click', closeCartOnOutside)
  ACTIVITY_EVENTS.forEach(e => window.removeEventListener(e, resetIdleTimer))
  clearTimeout(idleTimer)
})
</script>

<style scoped>
/* ===== FAB 包裹容器 ===== */
.fab-wrap {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

/* ===== FAB 按鈕 ===== */
.fab-cart {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #1976d2;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(25, 118, 210, 0.45);
  transition: background 0.2s, transform 0.15s, box-shadow 0.2s;
  position: relative;
  flex-shrink: 0;
}

.fab-cart:hover,
.fab-cart.active {
  background: #1565c0;
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(25, 118, 210, 0.55);
}

.fab-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #e53935;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  padding: 0 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  border: 2px solid #fff;
}

/* ===== 購物車預覽面板 ===== */
.cart-preview {
  width: 320px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

.cp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 10px;
  border-bottom: 1px solid #f0f0f0;
}

.cp-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
}

.cp-close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #909399;
  line-height: 1;
  padding: 2px 4px;
  border-radius: 4px;
  transition: color 0.15s, background 0.15s;
}
.cp-close:hover {
  color: #303133;
  background: #f5f5f5;
}

.cp-loading,
.cp-empty {
  padding: 24px 16px;
  text-align: center;
  color: #909399;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.cp-shop-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
}
.cp-shop-link:hover { text-decoration: underline; }

/* 商品列表 */
.cp-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 260px;
  overflow-y: auto;
}

.cp-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-bottom: 1px solid #f5f7fa;
}
.cp-item:last-child { border-bottom: none; }

.cp-img {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.cp-info {
  flex: 1;
  min-width: 0;
}

.cp-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cp-qty {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.cp-price {
  font-size: 13px;
  font-weight: 600;
  color: #e1251b;
  margin: 0;
  white-space: nowrap;
}

/* 底部合計＋結帳 */
.cp-footer {
  padding: 12px 16px 14px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.cp-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.cp-total-price {
  font-size: 16px;
  font-weight: 700;
  color: #e1251b;
}

.cp-checkout-btn {
  display: block;
  width: 100%;
  padding: 10px 0;
  background: #409eff;
  color: #fff;
  text-align: center;
  border-radius: 8px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: background 0.2s;
}
.cp-checkout-btn:hover { background: #337ecc; }

/* ===== 面板動畫 ===== */
.cart-panel-enter-active,
.cart-panel-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.cart-panel-enter-from,
.cart-panel-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.97);
}
</style>

