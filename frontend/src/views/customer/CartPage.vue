<template>
  <div class="cart-page">
    <NavBar />

    <main class="page-content">
      <h1 class="page-title">🛒 我的購物車</h1>

      <!-- 載入中 -->
      <div v-if="cartStore.loading" class="loading-center">
        <el-skeleton :rows="4" animated />
      </div>

      <!-- 購物車是空的 -->
      <el-empty
        v-else-if="cartStore.items.length === 0"
        description="購物車是空的，快去選購商品吧！"
      >
        <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
      </el-empty>

      <!-- 購物車有內容 -->
      <div v-else class="cart-layout">
        <!-- 左側：商品列表 -->
        <div class="cart-items">
          <el-card
            v-for="item in cartStore.items"
            :key="item.cartItemId"
            class="cart-item-card"
            shadow="hover"
          >
            <div class="item-row">
              <!-- 商品圖片 -->
              <img
                :src="item.productImageUrl || fallbackImage"
                :alt="item.productName"
                class="item-image"
              />

              <!-- 商品資訊 -->
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-price">單價：{{ formatCurrency(item.unitPrice) }}</div>
              </div>

              <!-- 數量控制 -->
              <div class="item-qty">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="999"
                  size="small"
                  :disabled="updatingId === item.cartItemId"
                  @change="(val) => handleUpdateQty(item.cartItemId, val)"
                />
              </div>

              <!-- 小計 -->
              <div class="item-subtotal">
                {{ formatCurrency(item.subtotal) }}
              </div>

              <!-- 刪除 -->
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                :loading="deletingId === item.cartItemId"
                @click="handleRemove(item.cartItemId)"
              />
            </div>
          </el-card>

          <!-- 清空購物車 -->
          <div class="cart-actions">
            <el-button type="warning" plain @click="handleClearCart">清空購物車</el-button>
            <el-button @click="$router.push('/products')">繼續購物</el-button>
          </div>
        </div>

        <!-- 右側：結帳摘要 -->
        <div class="checkout-panel">
          <el-card shadow="always">
            <template #header>
              <span class="checkout-title">訂單摘要</span>
            </template>

            <div class="summary-row">
              <span>商品種類</span>
              <span>{{ cartStore.cartCount }} 種</span>
            </div>
            <div class="summary-row total">
              <span>總金額</span>
              <span class="total-price">{{ formatCurrency(cartStore.totalAmount) }}</span>
            </div>

            <el-divider />

            <!-- 收貨地址 -->
            <div class="address-section">
              <div class="address-label">收貨地址 <span class="required">*</span></div>
              <el-input
                v-model="shippingAddress"
                type="textarea"
                :rows="3"
                placeholder="請輸入完整收貨地址"
                maxlength="300"
                show-word-limit
              />
            </div>

            <el-button
              type="primary"
              size="large"
              class="checkout-btn"
              :loading="checkingOut"
              :disabled="!shippingAddress.trim()"
              @click="handleCheckout"
            >
              立即結帳
            </el-button>
          </el-card>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import api from '../../api'
import NavBar from '../../components/NavBar.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

const shippingAddress = ref('')
const checkingOut = ref(false)
const updatingId = ref(null)
const deletingId = ref(null)

const fallbackImage =
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=200&q=60'

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})

function formatCurrency(value) {
  return formatter.format(Number(value || 0))
}

onMounted(() => {
  cartStore.fetchCart()
})

async function handleUpdateQty(itemId, newQty) {
  if (!newQty || newQty < 1) return
  updatingId.value = itemId
  try {
    await cartStore.updateItem(itemId, newQty)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '更新數量失敗')
    cartStore.fetchCart()
  } finally {
    updatingId.value = null
  }
}

async function handleRemove(itemId) {
  deletingId.value = itemId
  try {
    await cartStore.removeItem(itemId)
    ElMessage.success('已移除商品')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '移除失敗')
  } finally {
    deletingId.value = null
  }
}

async function handleClearCart() {
  try {
    await ElMessageBox.confirm('確定要清空整個購物車嗎？', '確認清空', {
      type: 'warning',
      confirmButtonText: '確定清空',
      cancelButtonText: '取消'
    })
    await cartStore.clearCart()
    ElMessage.success('購物車已清空')
  } catch {
    // 使用者取消
  }
}

async function handleCheckout() {
  if (!shippingAddress.value.trim()) {
    ElMessage.warning('請填寫收貨地址')
    return
  }
  checkingOut.value = true
  try {
    const response = await api.post('/orders/checkout', {
      shippingAddress: shippingAddress.value.trim()
    })
    cartStore.reset()
    ElMessage.success('結帳成功！訂單已建立，正在跳轉至訂單詳情...')
    // 結帳成功後直接跳轉到該筆訂單的詳情頁
    router.push(`/orders/${response.data.id}`)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '結帳失敗，請稍後再試')
  } finally {
    checkingOut.value = false
  }
}

function handleLogout() {
  authStore.logout()
  cartStore.reset()
  router.push('/')
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.page-content { max-width: 1200px; margin: 0 auto; padding: 32px 16px; }
.page-title { font-size: 24px; margin-bottom: 24px; }
.loading-center { padding: 40px 0; }
.cart-layout { display: flex; gap: 24px; align-items: flex-start; }
.cart-items { flex: 1; min-width: 0; }
.cart-item-card { margin-bottom: 12px; }
.item-row { display: flex; align-items: center; gap: 16px; }
.item-image { width: 80px; height: 80px; object-fit: cover; border-radius: 8px; flex-shrink: 0; }
.item-info { flex: 1; min-width: 0; }
.item-name { font-size: 15px; font-weight: 600; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-price { font-size: 13px; color: #909399; }
.item-qty { flex-shrink: 0; }
.item-subtotal { font-size: 16px; font-weight: 600; color: #e1251b; min-width: 100px; text-align: right; flex-shrink: 0; }
.cart-actions { display: flex; gap: 12px; margin-top: 16px; }
.checkout-panel { width: 320px; flex-shrink: 0; }
.checkout-title { font-size: 16px; font-weight: 600; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px; color: #606266; }
.summary-row.total { font-size: 16px; font-weight: 600; color: #303133; }
.total-price { color: #e1251b; font-size: 20px; }
.address-section { margin-bottom: 16px; }
.address-label { font-size: 14px; font-weight: 600; margin-bottom: 8px; }
.required { color: #f56c6c; }
.checkout-btn { width: 100%; height: 44px; font-size: 16px; }
@media (max-width: 768px) {
  .cart-layout { flex-direction: column; }
  .checkout-panel { width: 100%; }
  .item-image { width: 60px; height: 60px; }
}
</style>
