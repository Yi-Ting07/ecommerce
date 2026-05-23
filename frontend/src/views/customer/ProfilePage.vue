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

      <!-- 雙重驗證管理 -->
      <el-card class="profile-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Key /></el-icon>
            <span>雙重驗證（2FA）</span>
            <el-tag v-if="twoFactorStatus?.enabled" type="success" size="small" style="margin-left: auto;">已啟用</el-tag>
            <el-tag v-else type="info" size="small" style="margin-left: auto;">未啟用</el-tag>
          </div>
        </template>

        <el-skeleton v-if="loadingTwoFactor" :rows="2" animated />

        <template v-else>
          <!-- 已啟用狀態 -->
          <template v-if="twoFactorStatus?.enabled">
            <el-alert
              :title="`目前使用：${twoFactorStatus.method === 'TOTP' ? 'Google Authenticator' : 'Email 驗證碼'}`"
              type="success"
              :closable="false"
              style="margin-bottom: 16px;"
            />
            <el-button type="danger" plain @click="handleDisable2FA">停用雙重驗證</el-button>
          </template>

          <!-- 未啟用狀態 -->
          <template v-else>
            <p class="twofa-desc">啟用雙重驗證後，每次登入需額外輸入驗證碼，有效保護帳號安全。</p>
            <div class="twofa-options">
              <el-card class="twofa-option-card" shadow="hover" @click="handleSetupTotp">
                <div class="twofa-option">
                  <span class="twofa-icon">🔐</span>
                  <div>
                    <div class="twofa-option-title">Google Authenticator</div>
                    <div class="twofa-option-desc">使用 TOTP App 生成的 30 秒驗證碼（建議）</div>
                  </div>
                </div>
              </el-card>
              <el-card class="twofa-option-card" shadow="hover" @click="handleSetupEmail">
                <div class="twofa-option">
                  <span class="twofa-icon">📧</span>
                  <div>
                    <div class="twofa-option-title">Email 驗證碼</div>
                    <div class="twofa-option-desc">每次登入寄送 OTP 至您的信箱</div>
                  </div>
                </div>
              </el-card>
              <el-card class="twofa-option-card" shadow="hover" @click="handleSetupSms">
                <div class="twofa-option">
                  <span class="twofa-icon">📱</span>
                  <div>
                    <div class="twofa-option-title">手機簡訊</div>
                    <div class="twofa-option-desc">每次登入寄送 OTP 至您的手機號碼</div>
                  </div>
                </div>
              </el-card>
            </div>
          </template>
        </template>
      </el-card>
    </main>

    <!-- TOTP 設定對話框 -->
    <el-dialog v-model="totpSetupVisible" title="🔐 設定 Google Authenticator" width="480px" :close-on-click-modal="false" @opened="renderQrCode">
      <div v-if="totpSetupData" class="totp-setup">
        <p>請使用 Google Authenticator 或其他 TOTP App 掃描下方 QR Code：</p>
        <div class="qr-container">
          <canvas ref="qrCanvas"></canvas>
        </div>
        <el-divider>或手動輸入密鑰</el-divider>
        <el-input :value="totpSetupData.secret" readonly>
          <template #append>
            <el-button @click="copySecret">複製</el-button>
          </template>
        </el-input>
        <p style="margin-top: 20px;">掃描完成後，請輸入 App 顯示的 <strong>6 位數驗證碼</strong> 確認綁定：</p>
        <el-input
          v-model="totpConfirmCode"
          placeholder="輸入 6 位數驗證碼"
          maxlength="6"
          size="large"
          style="margin-bottom: 12px;"
          @keyup.enter="handleConfirmTotp"
        />
      </div>
      <template #footer>
        <el-button @click="totpSetupVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingTotp" @click="handleConfirmTotp">確認啟用</el-button>
      </template>
    </el-dialog>

    <!-- SMS 設定對話框 -->
    <el-dialog v-model="smsSetupVisible" title="📱 設定手機簡訊驗證" width="420px" :close-on-click-modal="false">
      <p>請輸入您的手機號碼，登入時將發送 6 位數驗證碼至此號碼。</p>
      <el-input
        v-model="smsPhone"
        placeholder="請輸入手機號碼（如：0912345678 或 +886912345678）"
        maxlength="20"
        size="large"
        @keyup.enter="handleConfirmSms"
      />
      <p style="font-size: 12px; color: #999; margin-top: 8px;">※ 目前為開發模式，驗證碼將顯示在後端 log 中</p>
      <template #footer>
        <el-button @click="smsSetupVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingSms" @click="handleConfirmSms">確認啟用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import api from '../../api'
import QRCode from 'qrcode'

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

// ===== 雙重驗證（2FA）管理 =====
const loadingTwoFactor = ref(false)
const twoFactorStatus = ref(null) // { enabled, method }

// TOTP 設定對話框
const totpSetupVisible = ref(false)
const totpSetupData = ref(null)  // { secret, qrUri, setupToken }
const totpConfirmCode = ref('')
const savingTotp = ref(false)
const qrCanvas = ref(null)

// SMS 設定狀態
const smsSetupVisible = ref(false)
const smsPhone = ref('')
const savingSms = ref(false)

async function fetchTwoFactorStatus() {
  loadingTwoFactor.value = true
  try {
    const res = await api.get('/user/2fa/status')
    twoFactorStatus.value = res.data
  } catch {
    // 靜默失敗
  } finally {
    loadingTwoFactor.value = false
  }
}

async function handleSetupTotp() {
  try {
    const res = await api.post('/user/2fa/setup/totp')
    totpSetupData.value = res.data
    totpConfirmCode.value = ''
    totpSetupVisible.value = true
    // QR Code 在 dialog opened 事件中渲染（等 dialog 動畫完成）
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '載入設定失敗')
  }
}

async function renderQrCode() {
  if (qrCanvas.value && totpSetupData.value?.qrUri) {
    await QRCode.toCanvas(qrCanvas.value, totpSetupData.value.qrUri, { width: 200 })
  }
}

async function handleConfirmTotp() {
  if (!totpConfirmCode.value || totpConfirmCode.value.length !== 6) {
    ElMessage.warning('請輸入 App 顯示的 6 位數驗證碼')
    return
  }
  savingTotp.value = true
  try {
    await api.post('/user/2fa/confirm/totp', {
      setupToken: totpSetupData.value.setupToken,
      code: totpConfirmCode.value
    })
    ElMessage.success('Google Authenticator 已成功啟用！')
    totpSetupVisible.value = false
    fetchTwoFactorStatus()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '驗證碼錯誤，請重試')
    totpConfirmCode.value = ''
  } finally {
    savingTotp.value = false
  }
}

async function handleSetupEmail() {
  try {
    await ElMessageBox.confirm(
      '啟用後，每次登入將寄送 6 位數 OTP 至您的信箱。確定要啟用嗎？',
      '啟用 Email 雙重驗證',
      { confirmButtonText: '確認啟用', cancelButtonText: '取消', type: 'info' }
    )
    await api.post('/user/2fa/setup/email')
    ElMessage.success('Email 雙重驗證已成功啟用！')
    fetchTwoFactorStatus()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '啟用失敗')
    }
  }
}

function handleSetupSms() {
  smsPhone.value = ''
  smsSetupVisible.value = true
}

async function handleConfirmSms() {
  if (!smsPhone.value.trim()) {
    ElMessage.warning('請輸入手機號碼')
    return
  }
  savingSms.value = true
  try {
    // 正規化：台灣 09xx 格式自動轉 +886
    let phone = smsPhone.value.trim()
    if (phone.startsWith('09')) phone = '+886' + phone.slice(1)
    await api.post('/user/2fa/setup/sms', { phone })
    smsSetupVisible.value = false
    ElMessage.success('手機簡訊雙重驗證已成功啟用！')
    fetchTwoFactorStatus()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '啟用失敗')
  } finally {
    savingSms.value = false
  }
}

async function handleDisable2FA() {
  try {
    await ElMessageBox.confirm(
      '停用後，登入將不再需要雙重驗證。確定要停用嗎？',
      '停用雙重驗證',
      { confirmButtonText: '確認停用', cancelButtonText: '取消', type: 'warning' }
    )
    await api.delete('/user/2fa')
    ElMessage.success('雙重驗證已停用')
    fetchTwoFactorStatus()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.response?.data?.message || '操作失敗')
    }
  }
}

function copySecret() {
  if (totpSetupData.value?.secret) {
    navigator.clipboard.writeText(totpSetupData.value.secret)
    ElMessage.success('已複製到剪貼簿')
  }
}

onMounted(() => {
  fetchProfile()
  fetchTwoFactorStatus()
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

/* 2FA 管理樣式 */
.twofa-desc {
  color: #606266;
  margin: 0 0 16px;
}

.twofa-options {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.twofa-option-card {
  flex: 1;
  min-width: 200px;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.twofa-option-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.twofa-option {
  display: flex;
  align-items: center;
  gap: 14px;
}

.twofa-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.twofa-option-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.twofa-option-desc {
  font-size: 12px;
  color: #909399;
}

/* TOTP QR 設定對話框 */
.totp-setup p {
  color: #606266;
  margin-bottom: 16px;
}

.qr-container {
  display: flex;
  justify-content: center;
  padding: 16px 0;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 4px;
}
</style>
