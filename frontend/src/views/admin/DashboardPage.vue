<!-- 後台儀表板 — Phase 5 實作真實統計數據 -->
<template>
  <div class="dashboard-page">
    <h2 class="page-title">📊 儀表板</h2>

    <el-skeleton v-if="loading" :rows="6" animated />

    <template v-else>
      <!-- ===== 數字統計卡片 ===== -->
      <el-row :gutter="20" class="stat-row">
        <el-col :xs="12" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon stat-icon--blue">📦</div>
            <div class="stat-number">{{ stats.totalProducts }}</div>
            <div class="stat-label">商品總數</div>
            <div class="stat-sub">上架中 {{ stats.activeProducts }} 件</div>
          </el-card>
        </el-col>

        <el-col :xs="12" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon stat-icon--green">🛒</div>
            <div class="stat-number">{{ stats.totalOrders }}</div>
            <div class="stat-label">訂單總數</div>
            <div class="stat-sub">
              待確認 {{ stats.ordersByStatus?.PENDING ?? 0 }} 筆
            </div>
          </el-card>
        </el-col>

        <el-col :xs="12" :sm="12" :md="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon stat-icon--orange">👥</div>
            <div class="stat-number">{{ stats.totalUsers }}</div>
            <div class="stat-label">會員總數</div>
            <div class="stat-sub">含管理員帳號</div>
          </el-card>
        </el-col>

        <el-col :xs="12" :sm="12" :md="6">
          <el-card class="stat-card stat-card--highlight" shadow="hover">
            <div class="stat-icon stat-icon--red">💰</div>
            <div class="stat-number revenue">{{ formatCurrency(stats.totalRevenue) }}</div>
            <div class="stat-label">總營收</div>
            <div class="stat-sub">不含已取消訂單</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- ===== 訂單狀態分佈 ===== -->
      <el-card class="status-card" shadow="never">
        <template #header>
          <span class="card-title">📋 訂單狀態分佈</span>
        </template>

        <el-row :gutter="16">
          <el-col
            v-for="item in statusItems"
            :key="item.key"
            :xs="12" :sm="8" :md="4"
          >
            <div class="status-item">
              <el-tag :type="item.tagType" size="large" class="status-tag">
                {{ item.label }}
              </el-tag>
              <div class="status-count">{{ stats.ordersByStatus?.[item.key] ?? 0 }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- ===== 快速導航 ===== -->
      <el-card class="quick-nav-card" shadow="never">
        <template #header>
          <span class="card-title">⚡ 快速功能</span>
        </template>
        <div class="quick-nav-buttons">
          <el-button type="primary" @click="$router.push('/admin/products')">管理商品</el-button>
          <el-button type="success" @click="$router.push('/admin/orders')">管理訂單</el-button>
          <el-button type="warning" @click="$router.push('/admin/categories')">管理分類</el-button>
          <el-button type="info" @click="$router.push('/admin/news')">管理公告</el-button>
          <el-button @click="$router.push('/admin/users')">管理會員</el-button>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'

const loading = ref(true)
const stats = ref({
  totalProducts: 0,
  activeProducts: 0,
  totalOrders: 0,
  totalUsers: 0,
  totalRevenue: 0,
  ordersByStatus: {}
})

const statusItems = [
  { key: 'PENDING',   label: '待確認', tagType: 'warning' },
  { key: 'CONFIRMED', label: '已確認', tagType: 'primary' },
  { key: 'SHIPPED',   label: '已出貨', tagType: ''        },
  { key: 'DELIVERED', label: '已完成', tagType: 'success' },
  { key: 'CANCELLED', label: '已取消', tagType: 'info'    }
]

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})

function formatCurrency(value) {
  return formatter.format(Number(value || 0))
}

async function fetchStats() {
  loading.value = true
  try {
    const response = await api.get('/admin/dashboard/stats')
    stats.value = response.data
  } catch {
    // 載入失敗時保持預設值
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-title {
  font-size: 22px;
  margin: 0 0 4px;
  color: #303133;
}

/* === 統計卡片 === */
.stat-row {
  margin-bottom: 4px;
}

.stat-card {
  text-align: center;
  padding: 8px 0;
  border-radius: 10px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card--highlight {
  background: linear-gradient(135deg, #fff7e6, #fff);
}

.stat-icon {
  font-size: 32px;
  margin-bottom: 6px;
}

.stat-number {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-number.revenue {
  font-size: 24px;
  color: #e6a23c;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
}

.stat-sub {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}

/* === 訂單狀態卡片 === */
.status-card,
.quick-nav-card {
  border-radius: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
}

.status-tag {
  width: 100%;
  text-align: center;
  justify-content: center;
}

.status-count {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

/* === 快速導航 === */
.quick-nav-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
