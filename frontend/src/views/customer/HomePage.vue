<!--
  首頁 — 顧客進入網站的第一個頁面
  顯示歡迎訊息，並透過呼叫後端 /api/health 確認前後端串接是否正常
-->
<template>
  <div class="home-page">
    <header class="home-header">
      <nav class="nav-bar">
        <div class="nav-brand">🛒 E-Commerce</div>
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

    <main class="home-content">
      <h1>歡迎來到 E-Commerce 電商平台</h1>
      <p>這是一個使用 Vue 3 + Spring Boot 建立的全端電商網站</p>

      <!-- 後端連線狀態 -->
      <el-card class="status-card">
        <template #header>後端連線狀態</template>
        <div v-if="healthStatus">
          <el-tag type="success">✅ {{ healthStatus.message }}</el-tag>
          <p>時間：{{ healthStatus.timestamp }}</p>
        </div>
        <div v-else-if="healthError">
          <el-tag type="danger">❌ 無法連線到後端</el-tag>
          <p>{{ healthError }}</p>
        </div>
        <div v-else>
          <el-tag type="info">⏳ 檢查中...</el-tag>
        </div>
      </el-card>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import api from '../../api'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const healthStatus = ref(null)
const healthError = ref(null)

onMounted(async () => {
  try {
    const response = await api.get('/health')
    healthStatus.value = response.data
  } catch (error) {
    healthError.value = '請確認後端是否已啟動 (localhost:7687)'
  }
  // 若已登入，拉取購物車數量以顯示 Badge
  if (authStore.isLoggedIn) {
    cartStore.fetchCart()
  }
})

function handleLogout() {
  authStore.logout()
  cartStore.reset()
  router.push('/')
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
  background-color: #409eff;
  color: white;
}

.nav-brand {
  font-size: 20px;
  font-weight: bold;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-links a {
  color: white;
  text-decoration: none;
}

.nav-links a:hover {
  text-decoration: underline;
}

.nav-user {
  color: #e6f7ff;
  font-weight: bold;
}

.home-content {
  max-width: 800px;
  margin: 40px auto;
  text-align: center;
  padding: 0 16px;
}

.status-card {
  margin-top: 32px;
}
</style>
