<template>
  <div class="orders-page">
    <!-- 導覽列 -->
    <header class="page-header">
      <nav class="nav-bar">
        <div class="nav-brand">🛒 E-Commerce</div>
        <div class="nav-links">
          <router-link to="/">首頁</router-link>
          <router-link to="/products">商品</router-link>
          <router-link to="/cart">購物車</router-link>
          <router-link to="/orders">我的訂單</router-link>
          <router-link v-if="authStore.isAdmin" to="/admin">後台管理</router-link>
          <span class="nav-user">{{ authStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">登出</el-button>
        </div>
      </nav>
    </header>

    <main class="page-content">
      <h1 class="page-title">📋 我的訂單</h1>

      <el-skeleton v-if="loading" :rows="5" animated />

      <el-empty v-else-if="orders.length === 0" description="目前沒有任何訂單">
        <el-button type="primary" @click="$router.push('/products')">去購物</el-button>
      </el-empty>

      <div v-else>
        <!-- 訂單卡片列表 -->
        <el-card
          v-for="order in orders"
          :key="order.id"
          class="order-card"
          shadow="hover"
        >
          <!-- 訂單標頭 -->
          <div class="order-header">
            <div class="order-meta">
              <span class="order-id">訂單 #{{ order.id }}</span>
              <span class="order-date">{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="order-right">
              <el-tag :type="statusTagType(order.status)" size="large">
                {{ order.statusLabel }}
              </el-tag>
            </div>
          </div>

          <!-- 訂單商品列表（折疊） -->
          <el-collapse accordion class="order-items-collapse">
            <el-collapse-item name="1">
              <template #title>
                <span class="collapse-title">
                  共 {{ order.items.length }} 種商品 —
                  <strong class="order-total">{{ formatCurrency(order.totalAmount) }}</strong>
                </span>
              </template>
              <div class="order-items">
                <div
                  v-for="item in order.items"
                  :key="item.orderItemId"
                  class="order-item-row"
                >
                  <span class="item-name">{{ item.productName }}</span>
                  <span class="item-detail">
                    {{ formatCurrency(item.price) }} × {{ item.quantity }} =
                    <strong>{{ formatCurrency(item.subtotal) }}</strong>
                  </span>
                </div>
              </div>
              <div class="shipping-address">
                <el-icon><Location /></el-icon>
                收貨地址：{{ order.shippingAddress }}
              </div>
            </el-collapse-item>
          </el-collapse>

          <!-- 訂單底部操作 -->
          <div class="order-footer">
            <span class="order-total-label">
              訂單總額：<strong class="total-price">{{ formatCurrency(order.totalAmount) }}</strong>
            </span>
            <div class="order-actions">
              <el-button
                size="small"
                @click="$router.push(`/orders/${order.id}`)"
              >
                查看詳情
              </el-button>
              <el-button
                v-if="order.status === 'PENDING'"
                type="danger"
                plain
                size="small"
                :loading="cancellingId === order.id"
                @click="handleCancel(order.id)"
              >
                取消訂單
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 分頁 -->
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchOrders"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import api from '../../api'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const cancellingId = ref(null)

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})

function formatCurrency(value) {
  return formatter.format(Number(value || 0))
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function statusTagType(status) {
  const map = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    SHIPPED: '',
    DELIVERED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] ?? 'info'
}

async function fetchOrders(page = 1) {
  loading.value = true
  try {
    const response = await api.get('/orders', {
      params: { page: page - 1, size: pageSize.value }
    })
    const data = response.data
    orders.value = data.content
    total.value = data.totalElements
    currentPage.value = page
  } catch (error) {
    ElMessage.error('載入訂單失敗')
  } finally {
    loading.value = false
  }
}

async function handleCancel(orderId) {
  try {
    await ElMessageBox.confirm('確定要取消此訂單嗎？取消後庫存將退回。', '確認取消', {
      type: 'warning',
      confirmButtonText: '確定取消',
      cancelButtonText: '保留訂單'
    })
    cancellingId.value = orderId
    await api.post(`/orders/${orderId}/cancel`)
    ElMessage.success('訂單已取消')
    fetchOrders(currentPage.value)
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '取消失敗')
    }
  } finally {
    cancellingId.value = null
  }
}

function handleLogout() {
  authStore.logout()
  cartStore.reset()
  router.push('/')
}

onMounted(() => fetchOrders())
</script>

<style scoped>
.orders-page { min-height: 100vh; background: #f5f7fa; }
.page-header { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,.08); position: sticky; top: 0; z-index: 100; }
.nav-bar { max-width: 1200px; margin: 0 auto; padding: 0 16px; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.nav-brand { font-size: 20px; font-weight: 700; color: #409eff; }
.nav-links { display: flex; align-items: center; gap: 16px; }
.nav-links a { color: #606266; text-decoration: none; font-size: 14px; }
.nav-links a:hover, .nav-links a.router-link-active { color: #409eff; }
.nav-user { color: #409eff; font-weight: 600; font-size: 14px; }
.page-content { max-width: 900px; margin: 0 auto; padding: 32px 16px; }
.page-title { font-size: 24px; margin-bottom: 24px; }
.order-card { margin-bottom: 16px; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-meta { display: flex; flex-direction: column; gap: 4px; }
.order-id { font-size: 16px; font-weight: 700; }
.order-date { font-size: 12px; color: #909399; }
.collapse-title { font-size: 14px; }
.order-total { color: #e1251b; }
.order-items { padding: 8px 0; }
.order-item-row { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px dashed #eee; font-size: 14px; }
.order-item-row:last-child { border-bottom: none; }
.item-name { color: #303133; }
.item-detail { color: #606266; }
.shipping-address { font-size: 13px; color: #909399; margin-top: 10px; display: flex; align-items: center; gap: 4px; }
.order-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.order-total-label { font-size: 14px; }
.order-actions { display: flex; gap: 8px; align-items: center; }
.total-price { font-size: 18px; color: #e1251b; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }
</style>
