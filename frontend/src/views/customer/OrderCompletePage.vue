<template>
  <div class="order-complete-page">
    <NavBar />
    <main class="page-content">
      <!-- 進度條 -->
      <div class="steps-bar">
        <el-steps :active="3" finish-status="success" align-center>
          <el-step title="確認訂單" />
          <el-step title="填寫資料" />
          <el-step title="訂單完成" />
        </el-steps>
      </div>

      <!-- 完成區塊 -->
      <div class="complete-card">
        <div class="success-icon">
          <el-icon class="icon-circle" color="#67c23a" :size="72"><CircleCheckFilled /></el-icon>
        </div>
        <h2 class="complete-title">訂單已送出！</h2>
        <p class="order-id-text">訂單編號：<strong>#{{ orderId }}</strong></p>

        <!-- 訂單摘要 -->
        <div v-if="order" class="order-summary-box">
          <div class="summary-line">
            <span class="label">訂單狀態</span>
            <el-tag type="warning">{{ order.statusLabel }}</el-tag>
          </div>
          <div class="summary-line">
            <span class="label">送貨方式</span>
            <span>{{ order.deliveryMethodLabel }}</span>
          </div>
          <div class="summary-line">
            <span class="label">收貨人</span>
            <span>{{ order.recipientName }}</span>
          </div>
          <template v-if="order.deliveryMethod === 'HOME_DELIVERY'">
            <div class="summary-line">
              <span class="label">收貨地址</span>
              <span>{{ order.recipientAddress }}</span>
            </div>
          </template>
          <template v-else>
            <div class="summary-line">
              <span class="label">門市名稱</span>
              <span>{{ order.storeName }}</span>
            </div>
            <div class="summary-line">
              <span class="label">店號</span>
              <span>{{ order.storeCode }}</span>
            </div>
          </template>
          <el-divider />
          <div class="summary-line total-line">
            <span class="label">訂單總金額</span>
            <span class="total-amount">{{ formatCurrency(order.totalAmount) }}</span>
          </div>
        </div>

        <div v-else-if="loading" class="loading-box">
          <el-skeleton :rows="3" animated />
        </div>

        <!-- 操作按鈕 -->
        <div class="action-buttons">
          <el-button type="primary" size="large" @click="$router.push(`/orders/${orderId}`)">
            查看訂單詳情
          </el-button>
          <el-button size="large" @click="$router.push('/orders')">
            我的訂單
          </el-button>
          <el-button size="large" plain @click="$router.push('/products')">
            繼續購物
          </el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import api from '../../api'
import NavBar from '../../components/NavBar.vue'

const route = useRoute()
const orderId = route.params.id
const order = ref(null)
const loading = ref(true)

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})
function formatCurrency(v) { return formatter.format(Number(v || 0)) }

onMounted(async () => {
  try {
    const res = await api.get(`/orders/${orderId}`)
    order.value = res.data
  } catch {
    // 靜默失敗，仍顯示完成畫面
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.order-complete-page { min-height: 100vh; background: #f5f7fa; }
.page-content { max-width: 700px; margin: 0 auto; padding: 32px 16px; }
.steps-bar {
  margin-bottom: 32px; padding: 20px 0;
  background: #fff; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.complete-card {
  background: #fff; border-radius: 16px;
  padding: 48px 40px; text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,.08);
}

.success-icon { margin-bottom: 20px; }
.icon-circle { display: block; margin: 0 auto; }
.complete-title { font-size: 28px; font-weight: 700; color: #303133; margin: 0 0 8px; }
.order-id-text { font-size: 15px; color: #606266; margin-bottom: 32px; }

.order-summary-box {
  text-align: left;
  background: #f5f7fa; border-radius: 10px;
  padding: 20px 24px; margin-bottom: 32px;
}
.summary-line {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; font-size: 14px; color: #606266;
}
.summary-line .label { font-weight: 500; color: #909399; }
.total-line { font-size: 16px; }
.total-line .label { font-size: 16px; font-weight: 600; color: #303133; }
.total-amount { font-size: 22px; font-weight: 700; color: #e1251b; }

.loading-box { margin-bottom: 32px; text-align: left; }

.action-buttons {
  display: flex; justify-content: center; gap: 12px; flex-wrap: wrap;
}
</style>
