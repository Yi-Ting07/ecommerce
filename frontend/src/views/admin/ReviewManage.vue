<template>
  <div class="review-manage-page">
    <div class="toolbar">
      <h2>評論管理</h2>
      <span class="toolbar-hint">可刪除不當或違規評論</span>
    </div>

    <el-table :data="reviews" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="160">
        <template #default="scope">{{ scope.row.productName }}</template>
      </el-table-column>
      <el-table-column label="會員" width="120">
        <template #default="scope">{{ scope.row.username }}</template>
      </el-table-column>
      <el-table-column label="星評" width="160">
        <template #default="scope">
          <el-rate :model-value="scope.row.rating" disabled />
        </template>
      </el-table-column>
      <el-table-column label="評論內容" min-width="220">
        <template #default="scope">
          <span v-if="scope.row.content">{{ scope.row.content }}</span>
          <span v-else class="no-content">（無文字評論）</span>
        </template>
      </el-table-column>
      <el-table-column label="建立時間" width="160">
        <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
          <el-button size="small" type="danger" @click="removeReview(scope.row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分頁 -->
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const reviews = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

async function fetchReviews(page = 1) {
  loading.value = true
  try {
    const res = await api.get('/admin/reviews', {
      params: { page: page - 1, size: pageSize.value }
    })
    reviews.value = res.data.content
    total.value = res.data.totalElements
  } catch (err) {
    ElMessage.error('載入評論失敗')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchReviews(page)
}

async function removeReview(row) {
  try {
    await ElMessageBox.confirm(
      `確定要刪除 ${row.username} 對「${row.productName}」的評論嗎？`,
      '刪除評論',
      { type: 'warning', confirmButtonText: '確定刪除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  try {
    await api.delete(`/admin/reviews/${row.id}`)
    ElMessage.success('評論已刪除')
    fetchReviews(currentPage.value)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '刪除失敗')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

onMounted(() => fetchReviews())
</script>

<style scoped>
.review-manage-page {
  padding: 8px 0;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
}

.toolbar-hint {
  font-size: 13px;
  color: #909399;
}

.no-content {
  color: #c0c4cc;
  font-style: italic;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
