<template>
  <div class="product-manage-page">
    <div class="toolbar">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openCreateDialog">新增商品</el-button>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="關鍵字">
          <el-input v-model="filters.keyword" placeholder="商品名稱" clearable @keyup.enter="search" />
        </el-form-item>
        <el-form-item label="分類">
          <el-select v-model="filters.categoryId" clearable placeholder="全部分類" style="width: 180px">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="狀態">
          <el-select v-model="filters.active" clearable placeholder="全部狀態" style="width: 180px">
            <el-option label="上架" :value="true" />
            <el-option label="下架" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜尋</el-button>
          <el-button @click="reset">重設</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="products" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名稱" min-width="160" />
      <el-table-column prop="categoryName" label="分類" width="130" />
      <el-table-column label="價格" width="130">
        <template #default="scope">
          {{ formatCurrency(scope.row.price) }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="庫存" width="100" />
      <el-table-column label="狀態" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.active ? 'success' : 'info'">
            {{ scope.row.active ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEditDialog(scope.row)">編輯</el-button>
          <el-button size="small" type="danger" @click="removeProduct(scope.row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <el-dialog v-model="dialogVisible" :title="editingId ? '編輯商品' : '新增商品'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="商品名稱" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="商品分類" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="請選擇分類" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="價格" prop="price">
              <el-input-number v-model="form.price" :min="0" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="庫存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品圖片">
          <!-- 圖片上傳區塊 -->
          <div class="image-upload-area">
            <!-- 預覽目前圖片 -->
            <div v-if="form.imageUrl" class="image-preview">
              <img :src="resolveImageUrl(form.imageUrl)" alt="商品圖片" class="preview-img" />
              <el-button
                type="danger"
                size="small"
                circle
                class="remove-img-btn"
                @click="form.imageUrl = ''"
              >✕</el-button>
            </div>

            <!-- 上傳按鈕（僅在沒有圖片時顯示） -->
            <el-upload
              v-if="!form.imageUrl"
              class="upload-trigger"
              :show-file-list="false"
              :before-upload="handleImageUpload"
              accept="image/jpeg,image/png,image/webp"
            >
              <div class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">點擊上傳圖片</div>
                <div class="upload-hint">JPG / PNG / WebP，最大 5MB</div>
                <el-icon v-if="uploadingImage" class="loading-icon"><Loading /></el-icon>
              </div>
            </el-upload>
          </div>

          <!-- 或輸入圖片網址 -->
          <div class="url-input-wrap">
            <span class="url-label">或輸入圖片網址：</span>
            <el-input
              v-model="form.imageUrl"
              placeholder="https://example.com/image.jpg"
              clearable
              style="flex: 1"
            />
          </div>
        </el-form-item>

        <el-form-item label="商品狀態">
          <el-switch
            v-model="form.active"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">儲存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Loading } from '@element-plus/icons-vue'
import api from '../../api'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const uploadingImage = ref(false)

const products = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filters = reactive({
  keyword: '',
  categoryId: null,
  active: null
})

const form = reactive({
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  imageUrl: '',
  active: true,
  description: ''
})

const rules = {
  name: [
    { required: true, message: '請輸入商品名稱', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '請選擇商品分類', trigger: 'change' }
  ],
  price: [
    { required: true, message: '請輸入商品價格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '請輸入庫存', trigger: 'blur' }
  ]
}

const formatter = new Intl.NumberFormat('zh-TW', {
  style: 'currency',
  currency: 'TWD',
  maximumFractionDigits: 0
})

function formatCurrency(value) {
  return formatter.format(Number(value || 0))
}

function buildQueryParams() {
  const params = {
    page: currentPage.value - 1,
    size: pageSize.value,
    sort: 'id,desc'
  }

  if (filters.keyword) {
    params.keyword = filters.keyword
  }
  if (filters.categoryId) {
    params.categoryId = filters.categoryId
  }
  if (filters.active !== null && filters.active !== undefined) {
    params.active = filters.active
  }

  return params
}

async function fetchCategories() {
  const response = await api.get('/admin/categories')
  categories.value = response.data
}

async function fetchProducts() {
  loading.value = true
  try {
    const response = await api.get('/admin/products', { params: buildQueryParams() })
    products.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '商品資料載入失敗')
  } finally {
    loading.value = false
  }
}

function search() {
  currentPage.value = 1
  fetchProducts()
}

function reset() {
  filters.keyword = ''
  filters.categoryId = null
  filters.active = null
  currentPage.value = 1
  fetchProducts()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchProducts()
}

function resetForm() {
  form.name = ''
  form.categoryId = null
  form.price = 0
  form.stock = 0
  form.imageUrl = ''
  form.active = true
  form.description = ''
}

function openCreateDialog() {
  if (categories.value.length === 0) {
    ElMessage.warning('目前沒有分類，請先透過分類 API 建立分類')
    return
  }

  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(product) {
  editingId.value = product.id
  form.name = product.name
  form.categoryId = product.categoryId
  form.price = Number(product.price)
  form.stock = product.stock
  form.imageUrl = product.imageUrl || ''
  form.active = product.active
  form.description = product.description || ''
  dialogVisible.value = true
}

function buildPayload() {
  return {
    name: form.name,
    categoryId: form.categoryId,
    price: Number(form.price),
    stock: Number(form.stock),
    imageUrl: form.imageUrl || null,
    active: form.active,
    description: form.description || null
  }
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await api.put(`/admin/products/${editingId.value}`, payload)
      ElMessage.success('商品更新成功')
    } else {
      await api.post('/admin/products', payload)
      ElMessage.success('商品新增成功')
    }

    dialogVisible.value = false
    fetchProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '儲存失敗')
  } finally {
    saving.value = false
  }
}

async function removeProduct(product) {
  try {
    await ElMessageBox.confirm(`確定要刪除「${product.name}」嗎？`, '刪除確認', {
      confirmButtonText: '刪除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await api.delete(`/admin/products/${product.id}`)
    ElMessage.success('商品刪除成功')
    fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '刪除失敗')
    }
  }
}

/**
 * 解析圖片 URL
 * - 若是 /uploads/ 開頭的相對路徑 → 補上後端 baseURL
 * - 若已是完整 URL（http/https）→ 直接使用
 */
function resolveImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('/uploads/')) {
    return 'http://localhost:7687' + url
  }
  return url
}

/**
 * 圖片上傳處理（el-upload 的 before-upload hook）
 * 回傳 false 可阻止 el-upload 自動上傳，改由我們自行控制
 */
async function handleImageUpload(file) {
  // 驗證大小（5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('圖片大小不能超過 5MB')
    return false
  }

  uploadingImage.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)

    // 呼叫後端上傳 API（注意：不用 JSON，要用 multipart/form-data）
    const response = await api.post('/admin/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    // 將回傳的 URL 填入表單
    form.imageUrl = response.data.url
    ElMessage.success('圖片上傳成功')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '圖片上傳失敗')
  } finally {
    uploadingImage.value = false
  }

  // 回傳 false 阻止 el-upload 自動上傳（我們已手動處理）
  return false
}

onMounted(async () => {
  await fetchCategories()
  await fetchProducts()
})
</script>

<style scoped>
.product-manage-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar h2 {
  margin: 0;
}

.search-card {
  margin-bottom: 4px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}

/* ===== 圖片上傳區塊樣式 ===== */
.image-upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.preview-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
}

.remove-img-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 22px;
  height: 22px;
  min-height: 22px;
  padding: 0;
  font-size: 11px;
}

.upload-trigger :deep(.el-upload) {
  display: block;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  transition: border-color 0.2s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.upload-text {
  font-size: 13px;
}

.upload-hint {
  font-size: 11px;
  margin-top: 2px;
  color: #c0c4cc;
}

.url-input-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.url-label {
  white-space: nowrap;
  font-size: 13px;
  color: #606266;
}
</style>
