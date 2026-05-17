<template>
  <div class="profile-page">
    <!-- 導覽列 -->
    <header class="page-header">
      <nav class="nav-bar">
        <div class="nav-brand">🛒 E-Commerce</div>
        <div class="nav-links">
          <router-link to="/">首頁</router-link>
          <router-link to="/products">商品</router-link>
          <router-link to="/cart">購物車</router-link>
          <router-link to="/orders">我的訂單</router-link>
          <router-link v-if="authStore.isAdmin" to="/admin">後台管理</router-link>
          <span class="nav-user">{{ authStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">登出</el-button>
        </div>
      </nav>
    </header>

    <main class="page-content">
      <h1 class="page-title">👤 個人中心</h1>

      <el-row :gutter="24">
        <!-- 左側：修改個人資料 -->
        <el-col :xs="24" :md="12">
          <el-card class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><User /></el-icon>
                <span>個人資料</span>
              </div>
            </template>

            <el-skeleton v-if="loadingProfile" :rows="3" animated />

            <el-form
              v-else
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-position="top"
            >
              <el-form-item label="暱稱" prop="username">
                <el-input v-model="profileForm.username" placeholder="請輸入暱稱" />
              </el-form-item>

              <el-form-item label="電子郵件" prop="email">
                <el-input v-model="profileForm.email" placeholder="請輸入 Email" />
              </el-form-item>

              <el-form-item label="角色">
                <el-tag :type="profileData?.role === 'ROLE_ADMIN' ? 'danger' : 'success'">
                  {{ profileData?.role === 'ROLE_ADMIN' ? '管理員' : '一般會員' }}
                </el-tag>
              </el-form-item>

              <el-form-item label="註冊時間">
                <span class="info-text">{{ formatDate(profileData?.createdAt) }}</span>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="savingProfile"
                  @click="handleUpdateProfile"
                >
                  儲存變更
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右側：更改密碼 -->
        <el-col :xs="24" :md="12">
          <el-card class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Lock /></el-icon>
                <span>更改密碼</span>
              </div>
            </template>

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-position="top"
            >
              <el-form-item label="目前密碼" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  show-password
                  placeholder="請輸入目前密碼"
                />
              </el-form-item>

              <el-form-item label="新密碼" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="至少 8 字元，含大小寫與數字"
                />
              </el-form-item>

              <el-form-item label="確認新密碼" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="再次輸入新密碼"
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="warning"
                  :loading="savingPassword"
                  @click="handleChangePassword"
                >
                  更改密碼
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import api from '../../api'

const router = useRouter()
const authStore = useAuthStore()

// ===== 個人資料 =====
const loadingProfile = ref(false)
const savingProfile = ref(false)
const profileData = ref(null)
const profileFormRef = ref(null)

const profileForm = reactive({
  username: '',
  email: ''
})

const profileRules = {
  username: [
    { required: true, message: '請輸入暱稱', trigger: 'blur' },
    { min: 2, max: 50, message: '暱稱長度須為 2~50 字元', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: '請輸入合法的 Email 格式', trigger: 'blur' }
  ]
}

async function fetchProfile() {
  loadingProfile.value = true
  try {
    const res = await api.get('/user/profile')
    profileData.value = res.data
    profileForm.username = res.data.username
    profileForm.email = res.data.email
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '載入個人資料失敗')
  } finally {
    loadingProfile.value = false
  }
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingProfile.value = true
  try {
    const res = await api.put('/user/profile', {
      username: profileForm.username,
      email: profileForm.email
    })
    profileData.value = res.data

    // 同步更新 auth store（暱稱可能改變了）
    authStore.user.username = res.data.username
    authStore.user.email = res.data.email
    localStorage.setItem('user', JSON.stringify(authStore.user))

    ElMessage.success('個人資料已更新')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '更新失敗')
  } finally {
    savingProfile.value = false
  }
}

// ===== 更改密碼 =====
const savingPassword = ref(false)
const passwordFormRef = ref(null)

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  currentPassword: [
    { required: true, message: '請輸入目前密碼', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '請輸入新密碼', trigger: 'blur' },
    { min: 8, message: '新密碼至少 8 個字元', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/
        if (!regex.test(value)) {
          callback(new Error('新密碼必須包含大寫字母、小寫字母與數字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '請再次輸入新密碼', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('兩次輸入的密碼不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  savingPassword.value = true
  try {
    await api.put('/user/password', {
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密碼已成功更改，請重新登入')

    // 清空表單
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.resetFields()

    // 密碼更改後登出，要求重新登入
    authStore.logout()
    router.push('/login')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '更改密碼失敗')
  } finally {
    savingPassword.value = false
  }
}

// ===== 工具 =====
function formatDate(dateStr) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-bar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-brand {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-links a {
  text-decoration: none;
  color: #606266;
  font-size: 14px;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #409eff;
}

.nav-user {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

.page-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-title {
  font-size: 24px;
  margin-bottom: 28px;
  color: #303133;
}

.profile-card {
  border-radius: 12px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.info-text {
  color: #909399;
  font-size: 14px;
}
</style>
