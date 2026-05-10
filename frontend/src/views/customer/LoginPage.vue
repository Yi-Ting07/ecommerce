<!-- 登入頁面 — 使用者輸入 Email 和密碼登入 -->
<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <h2>會員登入</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="請輸入 Email" />
        </el-form-item>

        <el-form-item label="密碼" prop="password">
          <el-input v-model="form.password" type="password" placeholder="請輸入密碼" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登入</el-button>
          <router-link to="/register" class="register-link">還沒有帳號？立即註冊</router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  email: '',
  password: ''
})

const rules = {
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: 'Email 格式不正確', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼至少 6 個字元', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form.value.email, form.value.password)
    ElMessage.success('登入成功！')

    // 管理員登入後直接進後台，其餘使用者依重導向或回首頁
    if (authStore.isAdmin) {
      router.push('/admin')
      return
    }

    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登入失敗，請確認帳號密碼')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-card {
  width: 420px;
}

.login-card h2 {
  margin: 0;
  text-align: center;
}

.register-link {
  margin-left: 16px;
  color: #409eff;
  text-decoration: none;
}
</style>
