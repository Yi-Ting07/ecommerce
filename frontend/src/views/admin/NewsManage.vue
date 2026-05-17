<template>
  <div class="news-manage-page">
    <div class="toolbar">
      <h2>最新消息管理</h2>
      <el-button type="primary" @click="openCreateDialog">新增公告</el-button>
    </div>

    <el-table :data="newsList" border stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="標題" min-width="200">
        <template #default="scope">
          <el-tag v-if="scope.row.pinned" type="danger" size="small" style="margin-right: 6px">置頂</el-tag>
          {{ scope.row.title }}
        </template>
      </el-table-column>
      <el-table-column label="狀態" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.published ? 'success' : 'info'">
            {{ scope.row.published ? '已發佈' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="建立時間" width="170">
        <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="最後更新" width="170">
        <template #default="scope">{{ formatDate(scope.row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEditDialog(scope.row)">編輯</el-button>
          <el-button size="small" type="danger" @click="removeNews(scope.row)">刪除</el-button>
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

    <!-- 新增 / 編輯 Dialog -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '編輯公告' : '新增公告'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="標題" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="內容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            maxlength="5000"
            show-word-limit
            placeholder="支援換行，直接按 Enter 即可"
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="置頂">
              <el-switch v-model="form.pinned" active-text="置頂" inactive-text="一般" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="發佈狀態">
              <el-switch v-model="form.published" active-text="已發佈" inactive-text="草稿" />
            </el-form-item>
          </el-col>
        </el-row>
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
import api from '../../api'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const newsList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const form = reactive({
  title: '',
  content: '',
  pinned: false,
  published: false
})

const rules = {
  title: [{ required: true, message: '請輸入標題', trigger: 'blur' }],
  content: [{ required: true, message: '請輸入內容', trigger: 'blur' }]
}

async function fetchNews(page = 0) {
  loading.value = true
  try {
    const res = await api.get('/admin/news', {
      params: { page, size: pageSize.value, sort: 'createdAt,desc' }
    })
    newsList.value = res.data.content
    total.value = res.data.totalElements
  } catch {
    ElMessage.error('載入失敗')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchNews(page - 1)
}

function openCreateDialog() {
  editingId.value = null
  Object.assign(form, { title: '', content: '', pinned: false, published: false })
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    content: row.content,
    pinned: row.pinned,
    published: row.published
  })
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await api.put(`/admin/news/${editingId.value}`, payload)
      ElMessage.success('公告已更新')
    } else {
      await api.post('/admin/news', payload)
      ElMessage.success('公告已新增')
    }
    dialogVisible.value = false
    fetchNews(currentPage.value - 1)
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失敗')
  } finally {
    saving.value = false
  }
}

async function removeNews(row) {
  await ElMessageBox.confirm(`確定要刪除「${row.title}」？`, '刪除確認', {
    type: 'warning',
    confirmButtonText: '刪除',
    cancelButtonText: '取消'
  })
  try {
    await api.delete(`/admin/news/${row.id}`)
    ElMessage.success('已刪除')
    fetchNews(currentPage.value - 1)
  } catch {
    ElMessage.error('刪除失敗')
  }
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
.news-manage-page {
  padding: 16px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar h2 {
  margin: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
