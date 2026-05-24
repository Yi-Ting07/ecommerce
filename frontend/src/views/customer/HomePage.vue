<!--
  首頁 — 顧客進入網站的第一個頁面
  顯示歡迎訊息，並透過呼叫後端 /api/health 確認前後端串接是否正常
-->
<template>
  <div class="home-page">
    <NavBar />

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
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import api from '../../api'
import NavBar from '../../components/NavBar.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()
const healthStatus = ref(null)
const healthError = ref(null)

onMounted(async () => {
  try {
    const response = await api.get('/health')
    healthStatus.value = response.data
  } catch (error) {
    healthError.value = '請確認後端是否已啟動 (localhost:7687)'
  }
  if (authStore.isLoggedIn) {
    cartStore.fetchCart()
  }
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
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
