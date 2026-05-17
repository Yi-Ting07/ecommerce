<template>
  <div class="news-list-page">
    <div class="header-row">
      <h1>最新消息</h1>
      <router-link to="/">← 回首頁</router-link>
    </div>

    <!-- 載入中 Skeleton -->
    <el-skeleton :loading="loading" animated :count="3">
      <template #template>
        <el-card v-for="i in 3" :key="i" class="news-card" shadow="never" style="margin-bottom: 16px">
          <el-skeleton-item variant="h3" style="width: 60%" />
          <el-skeleton-item variant="text" style="width: 30%; margin-top: 8px" />
          <el-skeleton-item variant="text" style="width: 100%; margin-top: 12px" />
          <el-skeleton-item variant="text" style="width: 80%" />
        </el-card>
      </template>

      <template #default>
        <el-empty v-if="newsList.length === 0" description="目前沒有公告" />

        <template v-else>
          <el-card
            v-for="news in newsList"
            :key="news.id"
            class="news-card"
            :class="{ 'is-pinned': news.pinned }"
            shadow="hover"
          >
            <div class="news-header">
              <el-tag v-if="news.pinned" type="danger" size="small" style="margin-right: 8px">置頂</el-tag>
              <h2 class="news-title" @click="openDetail(news)">{{ news.title }}</h2>
            </div>
            <div class="news-meta">
              <span>{{ formatDate(news.createdAt) }}</span>
            </div>
            <!-- 預覽前 120 字 -->
            <p class="news-preview">{{ preview(news.content) }}</p>
            <el-button text type="primary" @click="openDetail(news)">閱讀全文 →</el-button>
          </el-card>
        </template>

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

    <!-- 全文 Dialog -->
    <el-dialog
      v-model="detailVisible"
      :title="detailNews?.title"
      width="640px"
      top="6vh"
      destroy-on-close
    >
      <p class="detail-meta">{{ formatDate(detailNews?.createdAt) }}</p>
      <div class="detail-content">{{ detailNews?.content }}</div>
      <template #footer>
        <el-button @click="detailVisible = false">關閉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const loading = ref(false)
const newsList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const detailNews = ref(null)

async function fetchNews(page = 0) {
  loading.value = true
  try {
    const res = await api.get('/news', {
      params: { page, size: pageSize.value }
    })
    newsList.value = res.data.content
    total.value = res.data.totalElements
  } catch {
    ElMessage.error('無法載入公告，請稍後再試')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchNews(page - 1)
}

function openDetail(news) {
  detailNews.value = news
  detailVisible.value = true
}

function preview(content) {
  if (!content) return ''
  return content.length > 120 ? content.slice(0, 120) + '…' : content
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => fetchNews())
</script>

<style scoped>
.news-list-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 16px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.header-row h1 {
  margin: 0;
  font-size: 1.8rem;
}

.news-card {
  margin-bottom: 16px;
  border-radius: 8px;
  transition: box-shadow 0.2s;
}

.news-card.is-pinned {
  border-left: 4px solid var(--el-color-danger);
}

.news-header {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.news-title {
  font-size: 1.1rem;
  margin: 0;
  cursor: pointer;
  color: var(--el-color-primary);
}

.news-title:hover {
  text-decoration: underline;
}

.news-meta {
  font-size: 0.82rem;
  color: #888;
  margin-bottom: 10px;
}

.news-preview {
  color: #555;
  line-height: 1.7;
  margin: 0 0 12px;
  white-space: pre-line;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-meta {
  color: #888;
  font-size: 0.85rem;
  margin-bottom: 16px;
}

.detail-content {
  white-space: pre-line;
  line-height: 1.8;
  color: #333;
  font-size: 1rem;
}
</style>
