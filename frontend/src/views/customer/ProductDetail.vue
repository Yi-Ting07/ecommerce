<template>
  <div class="product-detail-page">
    <router-link to="/products">← 回商品列表</router-link>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-row :gutter="24">
          <el-col :xs="24" :md="12">
            <el-skeleton-item variant="image" style="width: 100%; height: 320px" />
          </el-col>
          <el-col :xs="24" :md="12">
            <el-skeleton-item variant="h1" style="width: 70%; margin-bottom: 16px" />
            <el-skeleton-item variant="text" style="width: 90%; margin-bottom: 12px" />
            <el-skeleton-item variant="text" style="width: 50%;" />
          </el-col>
        </el-row>
      </template>

      <template #default>
        <el-empty v-if="!product" description="商品不存在或已下架" />

        <el-row v-else :gutter="24">
          <el-col :xs="24" :md="12">
            <img class="product-image" :src="product.imageUrl || fallbackImage" :alt="product.name" />
          </el-col>

          <el-col :xs="24" :md="12">
            <h1 class="title">{{ product.name }}</h1>
            <p class="category">分類：{{ product.categoryName }}</p>
            <p class="price">{{ formatCurrency(product.price) }}</p>
            <p class="stock">庫存：{{ product.stock }}</p>
            <p class="desc">{{ product.description || '此商品目前沒有描述。' }}</p>

            <div class="add-to-cart-row">
              <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
              <el-button
                type="primary"
                size="large"
                :loading="addingToCart"
                :disabled="product.stock === 0"
                @click="handleAddToCart"
              >
                {{ product.stock === 0 ? '已售完' : '加入購物車' }}
              </el-button>
            </div>
          </el-col>
        </el-row>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import api from '../../api'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const addingToCart = ref(false)

const fallbackImage =
  'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80'

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})

function formatCurrency(value) {
  return formatter.format(Number(value || 0))
}

async function fetchProduct(id) {
  loading.value = true
  product.value = null
  try {
    const response = await api.get(`/products/${id}`)
    product.value = response.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '商品載入失敗')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProduct(route.params.id)
})

watch(
  () => route.params.id,
  (newId) => {
    fetchProduct(newId)
  }
)

async function handleAddToCart() {
  if (!authStore.isLoggedIn) {
    ElMessage.warning('請先登入才能加入購物車')
    router.push('/login')
    return
  }
  addingToCart.value = true
  try {
    await cartStore.addItem(product.value.id, quantity.value)
    ElMessage.success(`已將「${product.value.name}」加入購物車`)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加入購物車失敗')
  } finally {
    addingToCart.value = false
  }
}
</script>

<style scoped>
.product-detail-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.product-image {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
  max-height: 420px;
}

.title {
  margin-top: 0;
  margin-bottom: 12px;
}

.price {
  font-size: 28px;
  color: #e1251b;
  font-weight: 700;
  margin: 12px 0;
}

.stock,
.category,
.desc {
  margin: 10px 0;
  line-height: 1.6;
}

.add-to-cart-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
}
</style>
