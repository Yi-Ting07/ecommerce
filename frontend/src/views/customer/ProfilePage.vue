<template>
  <div class="profile-page">
    <NavBar />

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

              <el-form-item label="聯絡手機號碼">
                <div class="phone-row">
                  <el-select v-model="profileForm.countryCode" class="country-code-select" filterable>
                    <el-option v-for="c in countryCodes" :key="c.code" :value="c.code"
                      :label="`${c.flag} ${c.code}`">
                      <span>{{ c.flag }} {{ c.name }} {{ c.code }}</span>
                    </el-option>
                  </el-select>
                  <el-input v-model="profileForm.phoneNum" placeholder="912345678" maxlength="20" class="phone-num-input" />
                </div>
              </el-form-item>

              <el-form-item label="常用地址">
                <el-input v-model="profileForm.address" placeholder="請輸入常用地址" maxlength="300" />
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

      <!-- 常用地址 & 門市管理入口 -->
      <el-row :gutter="16" style="margin-bottom: 24px;">
        <el-col :xs="24" :sm="12">
          <el-card class="profile-card" shadow="hover" style="margin-bottom: 0;">
            <div class="manage-entry">
              <div class="manage-entry-info">
                <span class="manage-entry-icon">📍</span>
                <div>
                  <div class="manage-entry-title">常用收貨地址</div>
                  <div class="manage-entry-count">已儲存 {{ savedAddresses.length }} 筆</div>
                </div>
              </div>
              <el-button type="primary" plain @click="manageAddressVisible = true">管理</el-button>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-card class="profile-card" shadow="hover" style="margin-bottom: 0;">
            <div class="manage-entry">
              <div class="manage-entry-info">
                <span class="manage-entry-icon">🏪</span>
                <div>
                  <div class="manage-entry-title">常用超商門市</div>
                  <div class="manage-entry-count">已儲存 {{ savedStoresCombined.length }} 筆</div>
                </div>
              </div>
              <el-button type="primary" plain @click="manageStoreVisible = true">管理</el-button>
            </div>
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

    <!-- 管理常用地址 Dialog -->
    <el-dialog v-model="manageAddressVisible" title="📍 常用收貨地址管理" width="580px">
      <div style="margin-bottom: 16px; text-align: right;">
        <el-button type="primary" @click="openAddressForm(null)">+ 新增地址</el-button>
      </div>
      <el-skeleton v-if="loadingAddresses" :rows="3" animated />
      <el-empty v-else-if="savedAddresses.length === 0" description="尚無常用地址" />
      <div v-else class="saved-list">
        <div v-for="addr in savedAddresses" :key="addr.id" class="saved-item">
          <div class="saved-item-info">
            <el-tag size="small">{{ addr.label }}</el-tag>
            <span class="saved-name">{{ addr.recipientName }}</span>
            <span class="saved-phone">{{ addr.recipientPhone }}</span>
            <span class="saved-addr">{{ addr.address }}</span>
          </div>
          <div class="saved-item-actions">
            <el-button type="primary" link size="small" @click="openAddressForm(addr)">編輯</el-button>
            <el-button type="danger" link size="small" @click="deleteAddress(addr.id)">刪除</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 管理常用門市 Dialog -->
    <el-dialog v-model="manageStoreVisible" title="🏪 常用超商門市管理" width="540px">
      <div style="margin-bottom: 16px; text-align: right;">
        <el-button type="primary" @click="openStoreForm(null)">+ 新增門市</el-button>
      </div>
      <el-skeleton v-if="loadingStores" :rows="3" animated />
      <el-empty v-else-if="savedStoresCombined.length === 0" description="尚無常用門市" />
      <div v-else class="saved-list">
        <div v-for="store in savedStoresCombined" :key="store.id" class="saved-item">
          <div class="saved-item-info">
            <el-tag size="small" :type="store.storeType === 'FAMILY_MART' ? 'success' : 'danger'">
              {{ store.storeTypeLabel }}
            </el-tag>
            <span class="saved-name">{{ store.storeName }}</span>
            <span class="saved-phone">店號：{{ store.storeCode }}</span>
          </div>
          <div class="saved-item-actions">
            <el-button type="primary" link size="small" @click="openStoreForm(store)">編輯</el-button>
            <el-button type="danger" link size="small" @click="deleteStore(store.id)">刪除</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 新增/編輯常用地址 Dialog -->
    <el-dialog v-model="addressFormVisible" :title="editingAddressId ? '編輯常用地址' : '新增常用地址'" width="460px" append-to-body>
      <el-form :model="newAddress" label-position="top">
        <el-form-item label="標籤（如：家、公司）">
          <el-input v-model="newAddress.label" placeholder="家" maxlength="30" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="收貨人姓名">
              <el-input v-model="newAddress.recipientName" placeholder="請輸入姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收貨人手機">
              <div class="phone-row">
                <el-select v-model="newAddress.countryCode" class="country-code-select-sm" filterable>
                  <el-option v-for="c in countryCodes" :key="c.code" :value="c.code" :label="`${c.flag} ${c.code}`">
                    <span>{{ c.flag }} {{ c.name }} {{ c.code }}</span>
                  </el-option>
                </el-select>
                <el-input v-model="newAddress.phoneNum" placeholder="912345678" maxlength="20" class="phone-num-input" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址">
          <el-input v-model="newAddress.address" placeholder="請輸入完整地址" maxlength="300" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAddress" @click="handleSaveAddress">儲存</el-button>
      </template>
    </el-dialog>

    <!-- 新增/編輯常用門市 Dialog -->
    <el-dialog v-model="storeFormVisible" :title="editingStoreId ? '編輯常用門市' : '新增常用門市'" width="400px" append-to-body>
      <el-form :model="newStore" label-position="top">
        <el-form-item label="超商類型">
          <el-radio-group v-model="newStore.storeType">
            <el-radio value="FAMILY_MART">🟢 全家</el-radio>
            <el-radio value="SEVEN_ELEVEN">🔴 7-11</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="門市名稱">
          <el-input v-model="newStore.storeName" placeholder="例：台北信義門市" maxlength="100" />
        </el-form-item>
        <el-form-item label="門市店號">
          <el-input v-model="newStore.storeCode" placeholder="例：123456" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="storeFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingStore" @click="handleSaveStore">儲存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import api from '../../api'
import QRCode from 'qrcode'
import NavBar from '../../components/NavBar.vue'

const router = useRouter()
const authStore = useAuthStore()

// ===== 個人資料 =====
const loadingProfile = ref(false)
const savingProfile = ref(false)
const profileData = ref(null)
const profileFormRef = ref(null)

const profileForm = reactive({
  username: '',
  email: '',
  countryCode: '+886',
  phoneNum: '',
  address: ''
})

// 國碼清單
const countryCodes = [
  { code: '+886', name: '台灣', flag: '🇹🇼' },
  { code: '+1',   name: '美國/加拿大', flag: '🇺🇸' },
  { code: '+81',  name: '日本', flag: '🇯🇵' },
  { code: '+82',  name: '南韓', flag: '🇰🇷' },
  { code: '+86',  name: '中國', flag: '🇨🇳' },
  { code: '+852', name: '香港', flag: '🇭🇰' },
  { code: '+853', name: '澳門', flag: '🇲🇴' },
  { code: '+65',  name: '新加坡', flag: '🇸🇬' },
  { code: '+60',  name: '馬來西亞', flag: '🇲🇾' },
  { code: '+63',  name: '菲律賓', flag: '🇵🇭' },
  { code: '+66',  name: '泰國', flag: '🇹🇭' },
  { code: '+84',  name: '越南', flag: '🇻🇳' },
  { code: '+62',  name: '印尼', flag: '🇮🇩' },
  { code: '+44',  name: '英國', flag: '🇬🇧' },
  { code: '+49',  name: '德國', flag: '🇩🇪' },
  { code: '+33',  name: '法國', flag: '🇫🇷' },
  { code: '+61',  name: '澳洲', flag: '🇦🇺' },
]

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
    profileForm.address = res.data.address || ''
    if (res.data.phone) {
      const matched = countryCodes.find(c => res.data.phone.startsWith(c.code))
      if (matched) {
        profileForm.countryCode = matched.code
        profileForm.phoneNum = res.data.phone.slice(matched.code.length)
      } else {
        profileForm.phoneNum = res.data.phone
      }
    }
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
    const phone = profileForm.phoneNum.trim()
      ? profileForm.countryCode + profileForm.phoneNum.trim()
      : null
    const res = await api.put('/user/profile', {
      username: profileForm.username,
      email: profileForm.email,
      phone,
      address: profileForm.address.trim() || null
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
  loadSavedAddresses()
  loadSavedAllStores()
})

// ===== 常用地址管理 =====
const savedAddresses = ref([])
const loadingAddresses = ref(false)
const manageAddressVisible = ref(false)
const addressFormVisible = ref(false)
const savingAddress = ref(false)
const editingAddressId = ref(null)
const newAddress = reactive({
  label: '家', recipientName: '', countryCode: '+886', phoneNum: '', address: ''
})

async function loadSavedAddresses() {
  loadingAddresses.value = true
  try {
    const res = await api.get('/user/addresses')
    savedAddresses.value = res.data
  } catch { savedAddresses.value = [] } finally { loadingAddresses.value = false }
}

function openAddressForm(item = null) {
  if (item) {
    editingAddressId.value = item.id
    newAddress.label = item.label
    newAddress.recipientName = item.recipientName
    const matched = countryCodes.find(c => item.recipientPhone.startsWith(c.code))
    if (matched) {
      newAddress.countryCode = matched.code
      newAddress.phoneNum = item.recipientPhone.slice(matched.code.length)
    } else {
      newAddress.countryCode = '+886'
      newAddress.phoneNum = item.recipientPhone
    }
    newAddress.address = item.address
  } else {
    editingAddressId.value = null
    newAddress.label = '家'
    newAddress.recipientName = ''
    newAddress.countryCode = '+886'
    newAddress.phoneNum = ''
    newAddress.address = ''
  }
  addressFormVisible.value = true
}

async function handleSaveAddress() {
  if (!newAddress.recipientName.trim()) return ElMessage.warning('請填寫收貨人姓名')
  if (!newAddress.phoneNum.trim()) return ElMessage.warning('請填寫手機號碼')
  if (!newAddress.address.trim()) return ElMessage.warning('請填寫地址')
  savingAddress.value = true
  try {
    const payload = {
      label: newAddress.label || '常用地址',
      recipientName: newAddress.recipientName.trim(),
      recipientPhone: newAddress.countryCode + newAddress.phoneNum.trim(),
      address: newAddress.address.trim()
    }
    if (editingAddressId.value) {
      await api.put(`/user/addresses/${editingAddressId.value}`, payload)
      ElMessage.success('已更新常用地址')
    } else {
      await api.post('/user/addresses', payload)
      ElMessage.success('已新增常用地址')
    }
    await loadSavedAddresses()
    addressFormVisible.value = false
    editingAddressId.value = null
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失敗')
  } finally { savingAddress.value = false }
}

async function deleteAddress(id) {
  try {
    await api.delete(`/user/addresses/${id}`)
    await loadSavedAddresses()
    ElMessage.success('已刪除')
  } catch { ElMessage.error('刪除失敗') }
}

// ===== 常用門市管理 =====
const savedStoresCombined = ref([])
const loadingStores = ref(false)
const manageStoreVisible = ref(false)
const storeFormVisible = ref(false)
const savingStore = ref(false)
const editingStoreId = ref(null)
const newStore = reactive({ storeType: 'FAMILY_MART', storeName: '', storeCode: '' })

async function loadSavedAllStores() {
  loadingStores.value = true
  try {
    const [fm, se] = await Promise.all([
      api.get('/user/stores', { params: { type: 'FAMILY_MART' } }),
      api.get('/user/stores', { params: { type: 'SEVEN_ELEVEN' } })
    ])
    savedStoresCombined.value = [...fm.data, ...se.data]
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } catch { savedStoresCombined.value = [] } finally { loadingStores.value = false }
}

function openStoreForm(item = null) {
  if (item) {
    editingStoreId.value = item.id
    newStore.storeType = item.storeType
    newStore.storeName = item.storeName
    newStore.storeCode = item.storeCode
  } else {
    editingStoreId.value = null
    newStore.storeType = 'FAMILY_MART'
    newStore.storeName = ''
    newStore.storeCode = ''
  }
  storeFormVisible.value = true
}

async function handleSaveStore() {
  if (!newStore.storeName.trim()) return ElMessage.warning('請填寫門市名稱')
  if (!newStore.storeCode.trim()) return ElMessage.warning('請填寫門市店號')
  savingStore.value = true
  try {
    const payload = {
      storeType: newStore.storeType,
      storeName: newStore.storeName.trim(),
      storeCode: newStore.storeCode.trim()
    }
    if (editingStoreId.value) {
      await api.put(`/user/stores/${editingStoreId.value}`, payload)
      ElMessage.success('已更新常用門市')
    } else {
      await api.post('/user/stores', payload)
      ElMessage.success('已新增常用門市')
    }
    await loadSavedAllStores()
    storeFormVisible.value = false
    editingStoreId.value = null
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失敗')
  } finally { savingStore.value = false }
}

async function deleteStore(id) {
  try {
    await api.delete(`/user/stores/${id}`)
    await loadSavedAllStores()
    ElMessage.success('已刪除')
  } catch { ElMessage.error('刪除失敗') }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
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

/* 電話輸入 */
.phone-row { display: flex; gap: 8px; }
.country-code-select { width: 110px; flex-shrink: 0; }
.country-code-select-sm { width: 100px; flex-shrink: 0; }
.phone-num-input { flex: 1; }

/* 常用地址 / 門市管理入口 */
.manage-entry { display: flex; align-items: center; justify-content: space-between; }
.manage-entry-info { display: flex; align-items: center; gap: 12px; }
.manage-entry-icon { font-size: 28px; }
.manage-entry-title { font-weight: 600; font-size: 15px; color: #303133; }
.manage-entry-count { font-size: 12px; color: #909399; margin-top: 2px; }

/* 常用地址 / 門市清單 */
.saved-list { display: flex; flex-direction: column; gap: 10px; }
.saved-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 14px; border: 1px solid #e4e7ed; border-radius: 8px;
}
.saved-item-info { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; flex-wrap: wrap; }
.saved-item-actions { display: flex; gap: 4px; flex-shrink: 0; }
.saved-name { font-weight: 600; font-size: 14px; }
.saved-phone { color: #606266; font-size: 13px; }
.saved-addr { color: #909399; font-size: 13px; }
</style>
