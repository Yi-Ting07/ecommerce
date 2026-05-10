<template>
  <div class="category-manage-page">
    <div class="toolbar">
      <h2>分類管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增分類</el-button>
    </div>

    <el-table :data="categories" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分類名稱" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="productCount" label="商品數量" width="100" align="center" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <el-button size="small" type="primary" :icon="Edit" @click="openEditDialog(row)">編輯</el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增 / 編輯 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '編輯分類' : '新增分類'"
      width="480px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分類名稱" prop="name">
          <el-input v-model="form.name" placeholder="請輸入分類名稱" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="選填描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">確定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import api from '@/api/index.js'

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = ref({ name: '', description: '' })

const rules = {
  name: [
    { required: true, message: '分類名稱不能為空', trigger: 'blur' },
    { max: 100, message: '不可超過 100 字元', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '不可超過 500 字元', trigger: 'blur' }
  ]
}

async function fetchCategories() {
  loading.value = true
  try {
    const res = await api.get('/admin/categories')
    categories.value = res.data
  } catch {
    ElMessage.error('載入分類失敗')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '', description: '' }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEditing.value = true
  editingId.value = row.id
  form.value = { name: row.name, description: row.description || '' }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEditing.value) {
      await api.put(`/admin/categories/${editingId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await api.post('/admin/categories', form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchCategories()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失敗')
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `確定要刪除分類「${row.name}」嗎？刪除後無法復原。`,
      '刪除確認',
      { type: 'warning', confirmButtonText: '確定刪除', cancelButtonText: '取消' }
    )
    await api.delete(`/admin/categories/${row.id}`)
    ElMessage.success('刪除成功')
    fetchCategories()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '刪除失敗')
    }
  }
}

onMounted(fetchCategories)
</script>

<style scoped>
.category-manage-page {
  padding: 16px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
