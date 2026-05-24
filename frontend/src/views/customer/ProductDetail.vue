<template>
  <div class="product-detail-wrapper">
    <NavBar />
    <div class="product-detail-page">

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

    <!-- ===== 評論區塊 ===== -->
    <div v-if="product" class="reviews-section">
      <el-divider />
      <h2 class="reviews-title">💬 商品評論</h2>

      <!-- 新增評論表單（需登入，且尚未評論過） -->
      <el-card v-if="authStore.isLoggedIn && !myReview" class="review-form-card" shadow="never">
        <template #header>
          <span>撰寫評論</span>
        </template>
        <el-form :model="reviewForm" label-position="top">
          <el-form-item label="星評">
            <el-rate v-model="reviewForm.rating" :max="5" show-text />
          </el-form-item>
          <el-form-item label="評論內容（選填）">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="3"
              maxlength="1000"
              show-word-limit
              placeholder="分享您對此商品的看法…"
            />
          </el-form-item>
          <el-button
            type="primary"
            :loading="submittingReview"
            @click="handleSubmitReview"
          >
            送出評論
          </el-button>
        </el-form>
      </el-card>

      <!-- 已評論提示 -->
      <el-alert
        v-if="authStore.isLoggedIn && myReview"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      >
        <template #title>您已對此商品留過評論。若要修改，請先刪除後再重新撰寫。</template>
      </el-alert>

      <!-- 未登入提示 -->
      <el-alert
        v-if="!authStore.isLoggedIn"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 16px"
      >
        <template #title>
          <router-link to="/login">登入</router-link> 後即可留下您的評論。
        </template>
      </el-alert>

      <!-- 評論列表 -->
      <el-skeleton v-if="loadingReviews" :rows="4" animated />

      <el-empty v-else-if="reviews.length === 0" description="目前還沒有評論，快來搶頭香！" />

      <div v-else class="review-list">
        <el-card
          v-for="review in reviews"
          :key="review.id"
          class="review-card"
          shadow="never"
        >
          <div class="review-header">
            <div class="review-meta">
              <span class="review-user">{{ review.username }}</span>
              <el-rate :model-value="review.rating" disabled show-score />
            </div>
            <div class="review-actions">
              <span class="review-date">{{ formatDate(review.createdAt) }}</span>
              <!-- 自己的評論才能刪除 -->
              <el-button
                v-if="authStore.username === review.username"
                type="danger"
                size="small"
                text
                :loading="deletingReviewId === review.id"
                @click="handleDeleteReview(review.id)"
              >
                刪除
              </el-button>
            </div>
          </div>
          <p v-if="review.content" class="review-content">{{ review.content }}</p>
          <p v-else class="review-no-content">（僅給星評，無文字內容）</p>
        </el-card>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import api from '../../api'
import NavBar from '../../components/NavBar.vue'

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
  fetchReviews(route.params.id)
})

watch(
  () => route.params.id,
  (newId) => {
    fetchProduct(newId)
    fetchReviews(newId)
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

// ===== 評論相關 =====
const reviews = ref([])
const loadingReviews = ref(false)
const submittingReview = ref(false)
const deletingReviewId = ref(null)

const reviewForm = ref({ rating: 5, content: '' })

// 目前登入使用者的評論（用來判斷是否已評論過）
const myReview = computed(() =>
  reviews.value.find(r => r.username === authStore.username)
)

async function fetchReviews(productId) {
  loadingReviews.value = true
  try {
    const res = await api.get(`/products/${productId}/reviews`)
    reviews.value = res.data
  } catch (err) {
    // 評論載入失敗不顯示錯誤訊息（不影響主流程）
  } finally {
    loadingReviews.value = false
  }
}

async function handleSubmitReview() {
  if (reviewForm.value.rating < 1) {
    ElMessage.warning('請選擇至少 1 顆星')
    return
  }
  submittingReview.value = true
  try {
    const res = await api.post(`/products/${route.params.id}/reviews`, {
      rating: reviewForm.value.rating,
      content: reviewForm.value.content || null
    })
    reviews.value.unshift(res.data)
    reviewForm.value = { rating: 5, content: '' }
    ElMessage.success('評論發佈成功！')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '評論發佈失敗')
  } finally {
    submittingReview.value = false
  }
}

async function handleDeleteReview(reviewId) {
  try {
    await ElMessageBox.confirm('確定要刪除這則評論嗎？', '刪除確認', {
      type: 'warning',
      confirmButtonText: '確定刪除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  deletingReviewId.value = reviewId
  try {
    await api.delete(`/reviews/${reviewId}`)
    reviews.value = reviews.value.filter(r => r.id !== reviewId)
    ElMessage.success('評論已刪除')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '刪除失敗')
  } finally {
    deletingReviewId.value = null
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-TW', {
    year: 'numeric', month: 'long', day: 'numeric'
  })
}
</script>

<style scoped>
.product-detail-wrapper {
  min-height: 100vh;
  background: #f5f7fa;
}

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

/* ===== 評論區塊 ===== */
.reviews-section {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 16px 40px;
}

.reviews-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #303133;
}

.review-form-card {
  margin-bottom: 24px;
  border-radius: 10px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-card {
  border-radius: 10px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-user {
  font-weight: 600;
  color: #303133;
}

.review-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-date {
  font-size: 12px;
  color: #909399;
}

.review-content {
  color: #606266;
  line-height: 1.7;
  margin: 0;
}

.review-no-content {
  color: #c0c4cc;
  font-style: italic;
  margin: 0;
}
</style>
