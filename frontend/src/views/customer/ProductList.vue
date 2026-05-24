<template>
  <div class="product-list-page-wrapper">
    <NavBar />
    <div class="product-list-page">
    <h1>商品列表</h1>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="關鍵字">
          <el-input v-model="filters.keyword" placeholder="搜尋商品名稱" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分類">
          <el-select v-model="filters.categoryId" placeholder="全部分類" clearable style="width: 180px">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜尋</el-button>
          <el-button @click="handleReset">重設</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-skeleton :loading="loading" animated :count="6">
      <template #template>
        <el-row :gutter="16">
          <el-col v-for="index in 6" :key="index" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card>
              <el-skeleton-item variant="image" style="width: 100%; height: 160px" />
              <el-skeleton-item variant="h3" style="width: 70%; margin-top: 10px" />
              <el-skeleton-item variant="text" style="width: 50%; margin-top: 10px" />
            </el-card>
          </el-col>
        </el-row>
      </template>

      <template #default>
        <el-empty v-if="products.length === 0" description="目前沒有商品" />

        <el-row v-else :gutter="16">
          <el-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
            <el-card class="product-card" shadow="hover">
              <img
                class="product-image"
                :src="product.imageUrl || fallbackImage"
                :alt="product.name"
                @click="goToDetail(product.id)"
              />
              <div class="product-content">
                <h3 class="product-name" @click="goToDetail(product.id)">{{ product.name }}</h3>
                <p class="category">分類：{{ product.categoryName }}</p>
                <p class="price">{{ formatCurrency(product.price) }}</p>
                <p class="stock">庫存：{{ product.stock }}</p>
                <el-button type="primary" plain @click="goToDetail(product.id)">查看詳細</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="pagination-wrap" v-if="total > pageSize">
          <el-pagination
            background
            layout="prev, pager, next, total"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </el-skeleton>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api'
import NavBar from '../../components/NavBar.vue'

const router = useRouter()

const loading = ref(false)
const categories = ref([])
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const filters = ref({
  keyword: '',
  categoryId: null
})

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

async function fetchCategories() {
  const response = await api.get('/categories')
  categories.value = response.data
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'id,desc'
    }

    if (filters.value.keyword) {
      params.keyword = filters.value.keyword
    }
    if (filters.value.categoryId) {
      params.categoryId = filters.value.categoryId
    }

    const response = await api.get('/products', { params })
    products.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '商品載入失敗')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchProducts()
}

function handleReset() {
  filters.value.keyword = ''
  filters.value.categoryId = null
  currentPage.value = 1
  fetchProducts()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchProducts()
}

function goToDetail(id) {
  router.push(`/products/${id}`)
}

onMounted(async () => {
  await fetchCategories()
  await fetchProducts()
})
</script>

<style scoped>
.product-list-page-wrapper {
  min-height: 100vh;
  background: #f5f7fa;
}

.product-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.filter-card {
  margin-bottom: 20px;
}

.product-card {
  margin-bottom: 16px;
}

.product-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.product-content {
  margin-top: 12px;
}

.product-name {
  cursor: pointer;
  margin: 0;
  font-size: 16px;
}

.category,
.price,
.stock {
  margin: 8px 0;
}

.price {
  color: #e1251b;
  font-size: 18px;
  font-weight: 700;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
