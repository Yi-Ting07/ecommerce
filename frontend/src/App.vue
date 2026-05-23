<!--
  App.vue — 應用程式根元件

  <router-view /> 會根據當前的 URL 自動替換成對應的頁面元件。
  例如：URL 是 /login → 顯示 LoginPage.vue
       URL 是 /products → 顯示 ProductList.vue
-->
<template>
  <router-view />

  <!-- 懸浮購物車按鈕：已登入且非管理後台頁面才顯示 -->
  <router-link
    v-if="authStore.isLoggedIn && !isAdminPage && !isCartPage"
    to="/cart"
    class="fab-cart"
    aria-label="前往購物車"
  >
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="26" height="26">
      <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4H6zM3.5 6h17M16 10a4 4 0 01-8 0"/>
      <path fill-rule="evenodd" d="M7.5 6h9L19 2H5L7.5 6z" clip-rule="evenodd"/>
      <path d="M9 10a3 3 0 006 0" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round"/>
    </svg>
    <span v-if="cartStore.cartCount > 0" class="fab-badge">
      {{ cartStore.cartCount > 99 ? '99+' : cartStore.cartCount }}
    </span>
  </router-link>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'

const route = useRoute()
const authStore = useAuthStore()
const cartStore = useCartStore()

const isAdminPage = computed(() => route.path.startsWith('/admin'))
const isCartPage = computed(() => route.path === '/cart')

// 監聽 Token 失效事件，清除 store reactive 狀態
function handleAuthExpired() {
  authStore.logout()
}

onMounted(() => window.addEventListener('auth:expired', handleAuthExpired))
onUnmounted(() => window.removeEventListener('auth:expired', handleAuthExpired))
</script>

<style scoped>
.fab-cart {
  position: fixed;
  bottom: 32px;
  right: 32px;
  z-index: 1000;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #1976d2;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(25, 118, 210, 0.45);
  text-decoration: none;
  transition: background 0.2s, transform 0.15s, box-shadow 0.2s;
}

.fab-cart:hover {
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
</style>
