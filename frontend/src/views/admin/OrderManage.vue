<template>
  <div class="order-manage">
    <div class="page-title-row">
      <h2>訂單管理</h2>
    </div>

    <!-- 過濾列 -->
    <el-card class="filter-card" shadow="never">
      <el-select
        v-model="statusFilter"
        placeholder="所有狀態"
        clearable
        @change="fetchOrders(1)"
        style="width: 160px"
      >
        <el-option label="待確認" value="PENDING" />
        <el-option label="已確認" value="CONFIRMED" />
        <el-option label="已出貨" value="SHIPPED" />
        <el-option label="已完成" value="DELIVERED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </el-card>

    <!-- 訂單表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="orders"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="訂單 #" width="80" />
        <el-table-column prop="username" label="使用者" width="120" />
        <el-table-column label="商品數" width="80">
          <template #default="{ row }">
            {{ row.items.length }} 種
          </template>
        </el-table-column>
        <el-table-column label="總金額" width="120">
          <template #default="{ row }">
            <span class="price">{{ formatCurrency(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="狀態" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shippingAddress" label="收貨地址" show-overflow-tooltip />
        <el-table-column label="下單時間" width="160">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">詳情</el-button>
            <el-button
              v-if="canUpdateStatus(row.status)"
              size="small"
              type="primary"
              @click="openStatusDialog(row)"
            >
              更新狀態
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>

    <!-- 訂單詳情 Dialog -->
    <el-dialog v-model="detailVisible" title="訂單詳情" width="600px">
      <div v-if="selectedOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="訂單 #">{{ selectedOrder.id }}</el-descriptions-item>
          <el-descriptions-item label="使用者">{{ selectedOrder.username }}</el-descriptions-item>
          <el-descriptions-item label="狀態">
            <el-tag :type="statusTagType(selectedOrder.status)">{{ selectedOrder.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="總金額">
            <span class="price">{{ formatCurrency(selectedOrder.totalAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="收貨地址" :span="2">
            {{ selectedOrder.shippingAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="下單時間" :span="2">
            {{ formatDate(selectedOrder.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">商品明細</h4>
        <el-table :data="selectedOrder.items" stripe size="small">
          <el-table-column prop="productName" label="商品名稱" />
          <el-table-column prop="price" label="單價" width="100">
            <template #default="{ row }">{{ formatCurrency(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="數量" width="70" />
          <el-table-column label="小計" width="110">
            <template #default="{ row }">{{ formatCurrency(row.subtotal) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 更新狀態 Dialog -->
    <el-dialog v-model="statusDialogVisible" title="更新訂單狀態" width="400px">
      <div v-if="selectedOrder">
        <p>訂單 <strong>#{{ selectedOrder.id }}</strong> 目前狀態：
          <el-tag :type="statusTagType(selectedOrder.status)">{{ selectedOrder.statusLabel }}</el-tag>
        </p>
        <el-select v-model="newStatus" placeholder="選擇新狀態" style="width: 100%; margin-top: 12px">
          <el-option label="已確認" value="CONFIRMED" />
          <el-option label="已出貨" value="SHIPPED" />
          <el-option label="已完成" value="DELIVERED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="handleUpdateStatus">確認更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const statusFilter = ref('')

const detailVisible = ref(false)
const statusDialogVisible = ref(false)
const selectedOrder = ref(null)
const newStatus = ref('')
const updating = ref(false)

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
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

function statusTagType(status) {
  const map = { PENDING: 'warning', CONFIRMED: 'primary', SHIPPED: '', DELIVERED: 'success', CANCELLED: 'danger' }
  return map[status] ?? 'info'
}

function canUpdateStatus(status) {
  return status !== 'CANCELLED' && status !== 'DELIVERED'
}

async function fetchOrders(page = 1) {
  loading.value = true
  try {
    const params = { page: page - 1, size: pageSize.value }
    if (statusFilter.value) params.status = statusFilter.value
    const response = await api.get('/admin/orders', { params })
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

function openDetail(order) {
  selectedOrder.value = order
  detailVisible.value = true
}

function openStatusDialog(order) {
  selectedOrder.value = order
  newStatus.value = ''
  statusDialogVisible.value = true
}

async function handleUpdateStatus() {
  if (!newStatus.value) {
    ElMessage.warning('請選擇新狀態')
    return
  }
  updating.value = true
  try {
    await api.put(`/admin/orders/${selectedOrder.value.id}/status`, { status: newStatus.value })
    ElMessage.success('訂單狀態已更新')
    statusDialogVisible.value = false
    fetchOrders(currentPage.value)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新失敗')
  } finally {
    updating.value = false
  }
}

onMounted(() => fetchOrders())
</script>

<style scoped>
.page-title-row { margin-bottom: 16px; }
.filter-card { margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
.price { color: #e1251b; font-weight: 600; }
</style>
