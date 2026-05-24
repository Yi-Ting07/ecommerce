<!-- 統一導覽列元件 — 所有顧客頁面共用 -->
<template>
  <header class="page-header">
    <nav class="nav-bar">
      <router-link to="/" class="nav-brand">🛒 E-Commerce</router-link>
      <div class="nav-links">
        <router-link to="/">首頁</router-link>
        <router-link to="/products">商品</router-link>
        <router-link to="/news">最新消息</router-link>
        <template v-if="authStore.isLoggedIn">
          <router-link to="/cart">
            <el-badge :value="cartStore.cartCount" :hidden="cartStore.cartCount === 0" type="danger">
              購物車
            </el-badge>
          </router-link>
          <router-link to="/orders">我的訂單</router-link>
          <router-link to="/profile">個人中心</router-link>
          <router-link v-if="authStore.isAdmin" to="/admin">後台管理</router-link>
          <span class="nav-user">{{ authStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">登出</el-button>
        </template>
        <template v-else>
          <router-link to="/login">登入</router-link>
          <router-link to="/register">註冊</router-link>
        </template>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

function handleLogout() {
  authStore.logout()
  cartStore.reset()
  router.push('/')
}
</script>

<style scoped>
.page-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-bar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
  text-decoration: none;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-links a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #409eff;
}

.nav-user {
  color: #409eff;
  font-weight: 600;
  font-size: 14px;
}
</style>
