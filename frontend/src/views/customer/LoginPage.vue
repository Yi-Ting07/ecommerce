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

        <el-form-item label="驗證碼" prop="captchaCode">
          <div class="captcha-row">
            <el-input
              v-model="form.captchaCode"
              placeholder="請輸入圖中數字"
              maxlength="4"
              class="captcha-input"
              @keyup.enter="handleLogin"
            />
            <div class="captcha-img-wrap" title="點擊刷新驗證碼">
              <img
                v-if="captchaImage"
                :src="`data:image/png;base64,${captchaImage}`"
                alt="驗證碼"
                class="captcha-img"
                @click="refreshCaptcha"
              />
              <div v-else class="captcha-placeholder" @click="refreshCaptcha">載入中…</div>
            </div>
          </div>
          <div class="captcha-hint">驗證碼有效期 5 分鐘，點擊圖片可刷新</div>
        </el-form-item>

        <el-form-item>
          <div class="bottom-row">
            <el-checkbox v-model="rememberMe">記住帳號</el-checkbox>
            <router-link to="/register" class="register-link">還沒有帳號？立即註冊</router-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">登入</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 雙重驗證對話框 -->
    <el-dialog
      v-model="twoFactorVisible"
      :title="twoFactorDialogTitle"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @closed="handleTwoFactorCancel"
    >
      <div class="twofa-dialog">
        <!-- TOTP 提示 -->
        <template v-if="twoFactorMethod === 'TOTP'">
          <p class="twofa-hint">請開啟 <strong>Google Authenticator</strong> App，輸入對應帳號的 6 位數驗證碼。</p>
        </template>

        <!-- Email OTP 提示 -->
        <template v-else>
          <p class="twofa-hint">驗證碼已寄至您的信箱，請輸入 6 位數字驗證碼。</p>
          <p class="twofa-sub-hint">若未收到信件，請檢查垃圾郵件，或點擊下方「重新寄送」。</p>
        </template>

        <el-input
          v-model="twoFactorCode"
          placeholder="輸入 6 位數驗證碼"
          maxlength="6"
          size="large"
          class="twofa-input"
          @keyup.enter="handleTwoFactorSubmit"
          autofocus
        />

        <div class="twofa-actions">
          <el-button
            type="primary"
            :loading="twoFactorLoading"
            @click="handleTwoFactorSubmit"
            style="flex: 1"
          >
            確認驗證
          </el-button>
          <el-button
            v-if="twoFactorMethod === 'EMAIL'"
            :loading="resendLoading"
            :disabled="resendCountdown > 0"
            @click="handleResendEmail"
          >
            {{ resendCountdown > 0 ? `重新寄送 (${resendCountdown}s)` : '重新寄送' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { ElMessage } from 'element-plus'
import api from '../../api'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

// 驗證碼狀態
const captchaId = ref('')
const captchaImage = ref('')

const form = ref({
  email: '',
  password: '',
  captchaCode: ''
})

const rules = {
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: 'Email 格式不正確', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼至少 6 個字元', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '請輸入驗證碼', trigger: 'blur' },
    { len: 4, message: '驗證碼為 4 位數字', trigger: 'blur' }
  ]
}

// ===== 2FA 狀態 =====
const twoFactorVisible = ref(false)
const twoFactorMethod = ref('') // 'TOTP' | 'EMAIL' | 'SMS'
const twoFactorCode = ref('')
const twoFactorLoading = ref(false)
const resendLoading = ref(false)
const resendCountdown = ref(0)
let countdownTimer = null

const twoFactorDialogTitle = computed(() => {
  if (twoFactorMethod.value === 'TOTP') return '🔐 Google Authenticator 驗證'
  if (twoFactorMethod.value === 'SMS')  return '📱 手機簡訊驗證'
  return '📧 Email 驗證碼'
})

// ===== 記住我 =====
const rememberMe = ref(false)

async function refreshCaptcha() {
  try {
    const res = await api.get('/auth/captcha')
    captchaId.value = res.data.captchaId
    captchaImage.value = res.data.captchaImage
    form.value.captchaCode = ''
  } catch {
    ElMessage.error('驗證碼載入失敗，請重試')
  }
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const result = await authStore.login(
      form.value.email, form.value.password,
      captchaId.value, form.value.captchaCode
    )

    if (result.requiresTwoFactor) {
      // 需要 2FA 驗證 → 顯示對話框
      twoFactorMethod.value = result.method
      twoFactorCode.value = ''
      twoFactorVisible.value = true
      // Email / SMS 方式：啟動重新寄送倒計時（後端已自動送出第一封/條）
      if (result.method === 'EMAIL' || result.method === 'SMS') {
        startResendCountdown()
      }
    } else {
      // 直接登入成功
      navigateAfterLogin()
    }
  } catch (error) {
    const msg = error.response?.data?.message || '登入失敗，請確認帳號密碼'
    ElMessage.error(msg)
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

async function handleTwoFactorSubmit() {
  if (!twoFactorCode.value || twoFactorCode.value.length !== 6) {
    ElMessage.warning('請輸入 6 位數驗證碼')
    return
  }

  twoFactorLoading.value = true
  try {
    await authStore.verifyTwoFactor(twoFactorCode.value)
    twoFactorVisible.value = false
    ElMessage.success('驗證成功，登入中…')
    navigateAfterLogin()
  } catch (error) {
    const msg = error.response?.data?.message || '驗證碼錯誤，請重試'
    ElMessage.error(msg)
    twoFactorCode.value = ''
  } finally {
    twoFactorLoading.value = false
  }
}

async function handleResendEmail() {
  resendLoading.value = true
  try {
    await authStore.resendEmailOtp()
    ElMessage.success('驗證碼已重新寄出')
    startResendCountdown()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '重新寄送失敗')
  } finally {
    resendLoading.value = false
  }
}

function handleTwoFactorCancel() {
  authStore.cancelTwoFactor()
  clearInterval(countdownTimer)
  resendCountdown.value = 0
  refreshCaptcha()
}

function startResendCountdown(seconds = 60) {
  resendCountdown.value = seconds
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    resendCountdown.value--
    if (resendCountdown.value <= 0) clearInterval(countdownTimer)
  }, 1000)
}

function navigateAfterLogin() {
  // 處理《記住我》：登入成功後才寫入 email
  if (rememberMe.value) {
    localStorage.setItem('rememberedEmail', form.value.email)
  } else {
    localStorage.removeItem('rememberedEmail')
  }
  ElMessage.success('登入成功！')
  if (authStore.isAdmin) {
    router.push('/admin')
    return
  }
  const redirect = route.query.redirect || '/'
  router.push(redirect)
}

onMounted(() => {
  // 自動從 localStorage 恢復儲存的 email
  const saved = localStorage.getItem('rememberedEmail')
  if (saved) {
    form.value.email = saved
    rememberMe.value = true
  }
  refreshCaptcha()
})
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
  width: 440px;
}

.login-card h2 {
  margin: 0;
  text-align: center;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.captcha-img-wrap {
  flex-shrink: 0;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  height: 44px;
}

.captcha-img {
  display: block;
  height: 44px;
  width: 130px;
  object-fit: cover;
  transition: opacity 0.2s;
}

.captcha-img:hover {
  opacity: 0.8;
}

.captcha-placeholder {
  width: 130px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  background: #f5f5f5;
}

.captcha-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}

.bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-size: 13px;
}

/* 2FA 對話框樣式 */
.twofa-dialog {
  padding: 4px 0;
}

.twofa-hint {
  color: #333;
  margin: 0 0 8px;
  line-height: 1.6;
}

.twofa-sub-hint {
  color: #888;
  font-size: 13px;
  margin: 0 0 16px;
}

.twofa-input {
  font-size: 20px;
  letter-spacing: 8px;
  text-align: center;
  margin-bottom: 20px;
}

.twofa-actions {
  display: flex;
  gap: 8px;
}
</style>
