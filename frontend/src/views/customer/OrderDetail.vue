<template>
  <div class="order-detail-page">
    <NavBar />

    <main class="page-content">
      <!-- 返回按鈕 -->
      <div class="back-row">
        <el-button :icon="ArrowLeft" @click="$router.push('/orders')">返回訂單列表</el-button>
      </div>

      <!-- 載入中 -->
      <el-skeleton v-if="loading" :rows="8" animated />

      <!-- 錯誤 -->
      <el-result
        v-else-if="error"
        icon="error"
        title="無法載入訂單"
        :sub-title="error"
      >
        <template #extra>
          <el-button @click="$router.push('/orders')">返回訂單列表</el-button>
        </template>
      </el-result>

      <!-- 訂單詳情 -->
      <template v-else-if="order">
        <!-- 訂單標頭資訊 -->
        <el-card class="order-header-card" shadow="never">
          <div class="order-title-row">
            <h1 class="order-title">訂單 #{{ order.id }}</h1>
            <el-tag :type="statusTagType(order.status)" size="large">
              {{ order.statusLabel }}
            </el-tag>
          </div>

          <el-descriptions :column="2" border class="order-descriptions">
            <el-descriptions-item label="下單時間">
              {{ formatDate(order.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="最後更新">
              {{ formatDate(order.updatedAt) }}
            </el-descriptions-item>

            <!-- 訂購人 -->
            <el-descriptions-item label="訂購人姓名">{{ order.ordererName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="訂購人電話">{{ order.ordererPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="訂購人地址" :span="2">{{ order.ordererAddress || '-' }}</el-descriptions-item>

            <!-- 收貨人 -->
            <el-descriptions-item label="收貨人姓名">{{ order.recipientName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="收貨人電話">{{ order.recipientPhone || '-' }}</el-descriptions-item>

            <!-- 送貨方式 -->
            <el-descriptions-item label="送貨方式" :span="2">
              <el-tag :type="deliveryTagType(order.deliveryMethod)">{{ order.deliveryMethodLabel || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="order.deliveryMethod === 'HOME_DELIVERY'" label="收貨地址" :span="2">
              {{ order.recipientAddress || '-' }}
            </el-descriptions-item>
            <template v-if="order.deliveryMethod === 'FAMILY_MART' || order.deliveryMethod === 'SEVEN_ELEVEN'">
              <el-descriptions-item label="門市名稱">{{ order.storeName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="門市店號">{{ order.storeCode || '-' }}</el-descriptions-item>
            </template>

            <!-- 付款 & 金額 -->
            <el-descriptions-item label="付款方式">
              <el-tag type="info">模擬付款（信用卡）</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="訂單總額">
              <strong class="total-price">{{ formatCurrency(order.totalAmount) }}</strong>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 訂單商品明細 -->
        <el-card class="items-card" shadow="never">
          <template #header>
            <span class="card-header-title">📦 商品明細</span>
          </template>

          <el-table :data="order.items" border stripe>
            <el-table-column label="商品名稱" prop="productName" min-width="200" />
            <el-table-column label="單價" width="140">
              <template #default="scope">
                {{ formatCurrency(scope.row.price) }}
              </template>
            </el-table-column>
            <el-table-column label="數量" prop="quantity" width="100" align="center" />
            <el-table-column label="小計" width="150" align="right">
              <template #default="scope">
                <strong>{{ formatCurrency(scope.row.subtotal) }}</strong>
              </template>
            </el-table-column>
          </el-table>

          <!-- 合計列 -->
          <div class="total-row">
            <span>訂單總額：</span>
            <strong class="total-price-lg">{{ formatCurrency(order.totalAmount) }}</strong>
          </div>
        </el-card>

        <!-- 操作按鈕 -->
        <div class="action-row">
          <el-button
            v-if="order.status === 'PENDING'"
            type="danger"
            plain
            :loading="cancelling"
            @click="handleCancel"
          >
            取消訂單
          </el-button>
          <el-button type="primary" @click="$router.push('/products')">繼續購物</el-button>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import api from '../../api'
import NavBar from '../../components/NavBar.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const order = ref(null)
const loading = ref(true)
const error = ref('')
const cancelling = ref(false)

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})

function formatCurrency(value) {
  return formatter.format(Number(value || 0))
}

function formatDate(dateStr) {
  if (!dateStr) return '--'
  return new Date(dateStr).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

function statusTagType(status) {
  const map = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    SHIPPED: '',
    DELIVERED: 'success',
    CANCELLED: 'info'
  }
  return map[status] || ''
}

function deliveryTagType(method) {
  if (method === 'HOME_DELIVERY') return 'primary'
  if (method === 'FAMILY_MART') return 'success'
  if (method === 'SEVEN_ELEVEN') return 'danger'
  return ''
}

async function fetchOrder() {
  loading.value = true
  error.value = ''
  try {
    const response = await api.get(`/orders/${route.params.id}`)
    order.value = response.data
  } catch (err) {
    error.value = err.response?.data?.message || '訂單不存在或無權限查看'
  } finally {
    loading.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('確定要取消此訂單嗎？取消後庫存將自動回補。', '取消確認', {
      confirmButtonText: '確定取消',
      cancelButtonText: '保留訂單',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })

    cancelling.value = true
    const response = await api.post(`/orders/${order.value.id}/cancel`)
    order.value = response.data
    ElMessage.success('訂單已成功取消，庫存已回補')
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '取消失敗')
    }
  } finally {
    cancelling.value = false
  }
}

onMounted(fetchOrder)
</script>

<style scoped>
/* === 主內容 === */
.page-content {
  max-width: 900px;
  margin: 32px auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.back-row {
  display: flex;
  align-items: center;
}

/* === 訂單標頭卡片 === */
.order-header-card {
  border-radius: 8px;
}

.order-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.order-title {
  font-size: 22px;
  font-weight: bold;
  margin: 0;
  color: #303133;
}

.order-descriptions {
  margin-top: 4px;
}

/* === 商品明細卡片 === */
.items-card {
  border-radius: 8px;
}

.card-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 16px 0 4px;
  font-size: 16px;
  color: #606266;
}

.total-price {
  color: #e6a23c;
  font-size: 16px;
}

.total-price-lg {
  color: #e6a23c;
  font-size: 22px;
}

/* === 操作按鈕 === */
.action-row {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
