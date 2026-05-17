<!--
  後台管理 Layout — 管理員後台的外框
  使用 Element Plus 的 Container 元件，包含：
  - 左側選單（Aside）
  - 右側內容區（Main），用 <router-view> 顯示子頁面
-->
<template>
  <el-container class="admin-layout">
    <!-- 左側選單 -->
    <el-aside width="220px" class="admin-aside">
      <div class="admin-logo">📊 後台管理</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>儀表板</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><ShoppingBag /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Grid /></el-icon>
          <span>分類管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>訂單管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>會員管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/news">
          <el-icon><Bell /></el-icon>
          <span>最新消息</span>
        </el-menu-item>
        <el-menu-item index="/admin/reviews">
          <el-icon><ChatDotRound /></el-icon>
          <span>評論管理</span>
        </el-menu-item>
      </el-menu>

      <!-- 回到前台按鈕 -->
      <div class="admin-footer">
        <router-link to="/">
          <el-button type="info" plain>← 回到前台</el-button>
        </router-link>
      </div>
    </el-aside>

    <!-- 右側內容區 -->
    <el-container>
      <el-header class="admin-header">
        <span>管理員：{{ authStore.username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">登出</el-button>
      </el-header>
      <el-main>
        <!-- 子路由頁面會渲染在這裡 -->
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-aside {
  background-color: #304156;
  overflow-y: auto;
}

.admin-logo {
  padding: 20px;
  color: white;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  border-bottom: 1px solid #3d4b5e;
}

.admin-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.admin-footer {
  padding: 16px;
  text-align: center;
  position: absolute;
  bottom: 0;
  width: 220px;
}

.admin-footer a {
  text-decoration: none;
}
</style>
