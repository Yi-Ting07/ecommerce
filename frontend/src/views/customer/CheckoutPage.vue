<template>
  <div class="checkout-page">
    <NavBar />
    <main class="page-content">
      <div class="steps-bar">
        <el-steps :active="1" finish-status="success" align-center>
          <el-step title="確認訂單" />
          <el-step title="填寫資料" />
          <el-step title="訂單完成" />
        </el-steps>
      </div>
      <h1 class="page-title">填寫訂單資訊</h1>

      <div v-if="cartStore.items.length === 0 && !submitting" class="empty-tip">
        <el-empty description="購物車是空的">
          <el-button type="primary" @click="$router.push('/products')">去選購</el-button>
        </el-empty>
      </div>

      <div v-else class="checkout-layout">
        <!-- 左欄：表單 -->
        <div class="form-col">

          <!-- ① 訂購人資訊 -->
          <el-card class="section-card" shadow="hover">
            <template #header><span class="section-title">👤 訂購人資訊</span></template>
            <el-form :model="form" label-position="top">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="姓名" required>
                    <el-input v-model="form.ordererName" placeholder="請輸入姓名" maxlength="50" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手機號碼" required>
                    <div class="phone-row">
                      <el-select v-model="form.ordererCountryCode" class="country-code-select" filterable>
                        <el-option v-for="c in countryCodes" :key="c.code" :value="c.code"
                          :label="`${c.flag} ${c.code}`">
                          <span>{{ c.flag }} {{ c.name }} {{ c.code }}</span>
                        </el-option>
                      </el-select>
                      <el-input v-model="form.ordererPhoneNum" placeholder="912345678" maxlength="20" class="phone-input" />
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="地址" required>
                <el-input v-model="form.ordererAddress" placeholder="請輸入地址" maxlength="300" />
              </el-form-item>
            </el-form>
          </el-card>

          <!-- ② 送貨方式 -->
          <el-card class="section-card" shadow="hover">
            <template #header><span class="section-title">🚚 送貨方式</span></template>
            <el-radio-group v-model="form.deliveryMethod" class="delivery-group">
              <el-radio-button value="HOME_DELIVERY">🏠 宅配</el-radio-button>
              <el-radio-button value="FAMILY_MART">🟢 全家超商</el-radio-button>
              <el-radio-button value="SEVEN_ELEVEN">🔴 7-11</el-radio-button>
            </el-radio-group>
          </el-card>

          <!-- ③ 收貨人資訊 -->
          <el-card class="section-card" shadow="hover">
            <template #header>
              <div class="section-header-row">
                <span class="section-title">📦 收貨人資訊</span>
                <el-checkbox v-model="sameAsOrderer" @change="copySameAsOrderer">同訂購人</el-checkbox>
              </div>
            </template>

            <el-form :model="form" label-position="top">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="姓名" required>
                    <el-input v-model="form.recipientName" placeholder="請輸入姓名" maxlength="50" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手機號碼" required>
                    <div class="phone-row">
                      <el-select v-model="form.recipientCountryCode" class="country-code-select" filterable>
                        <el-option v-for="c in countryCodes" :key="c.code" :value="c.code"
                          :label="`${c.flag} ${c.code}`">
                          <span>{{ c.flag }} {{ c.name }} {{ c.code }}</span>
                        </el-option>
                      </el-select>
                      <el-input v-model="form.recipientPhoneNum" placeholder="912345678" maxlength="20" class="phone-input" />
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 宅配：地址 -->
              <template v-if="form.deliveryMethod === 'HOME_DELIVERY'">
                <el-form-item label="收貨地址" required>
                  <div class="address-input-row">
                    <el-input v-model="form.recipientAddress" placeholder="請輸入完整地址" maxlength="300" class="address-input" />
                    <el-button plain @click="openAddressDialog" :disabled="savedAddresses.length === 0">
                      常用地址 ({{ savedAddresses.length }})
                    </el-button>
                  </div>
                </el-form-item>
                <el-checkbox v-if="canSaveAddress" v-model="form.saveAddress" class="save-check">
                  新增至常用地址
                </el-checkbox>
              </template>

              <!-- 超商：門市資訊 -->
              <template v-else>
                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="門市名稱" required>
                      <div class="address-input-row">
                        <el-input v-model="form.storeName" placeholder="例：台北信義門市" maxlength="100" class="address-input" />
                        <el-button plain @click="openStoreDialog" :disabled="savedStores.length === 0">
                          常用門市 ({{ savedStores.length }})
                        </el-button>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="店號" required>
                      <el-input v-model="form.storeCode" placeholder="例：123456" maxlength="20" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-checkbox v-if="canSaveStore" v-model="form.saveStore" class="save-check">
                  新增至常用門市
                </el-checkbox>
              </template>
            </el-form>
          </el-card>

          <!-- ④ 付款方式 -->
          <el-card class="section-card" shadow="hover">
            <template #header><span class="section-title">💳 付款方式</span></template>
            <el-radio-group v-model="form.paymentMethod">
              <el-radio value="SIMULATED">
                <span class="payment-label">💳 信用卡（模擬付款）</span>
              </el-radio>
            </el-radio-group>

            <!-- 模擬信用卡表單 -->
            <div v-if="form.paymentMethod === 'SIMULATED'" class="credit-card-form">
              <div class="card-preview" :class="{ flipped: showCvv }">
                <div class="card-front">
                  <div class="card-chip">▬▬</div>
                  <div class="card-number">{{ formattedCardNumber }}</div>
                  <div class="card-bottom">
                    <div>
                      <div class="card-label">持卡人</div>
                      <div class="card-value">{{ form.cardHolder || 'YOUR NAME' }}</div>
                    </div>
                    <div>
                      <div class="card-label">有效期限</div>
                      <div class="card-value">{{ form.cardExpiry || 'MM/YY' }}</div>
                    </div>
                  </div>
                </div>
              </div>
              <el-form label-position="top" class="card-inputs">
                <el-form-item label="卡號">
                  <el-input v-model="form.cardNumber" placeholder="1234 5678 9012 3456"
                    maxlength="19" @input="formatCardNumber" />
                </el-form-item>
                <el-row :gutter="12">
                  <el-col :span="12">
                    <el-form-item label="持卡人姓名">
                      <el-input v-model="form.cardHolder" placeholder="NAME" maxlength="30" style="text-transform:uppercase" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item label="有效期限">
                      <el-input v-model="form.cardExpiry" placeholder="MM/YY" maxlength="5" @input="formatExpiry" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item label="安全碼">
                      <el-input v-model="form.cardCvv" placeholder="CVV" maxlength="3" type="password"
                        @focus="showCvv = true" @blur="showCvv = false" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </el-card>
        </div>

        <!-- 右欄：訂單摘要 -->
        <div class="summary-col">
          <el-card class="summary-card" shadow="always">
            <template #header><span class="section-title">🛒 訂單摘要</span></template>
            <div v-for="(item, index) in cartStore.items" :key="item.cartItemId" class="summary-item">
              <span class="summary-item-num">{{ index + 1 }}.</span>
              <span class="summary-item-name">{{ item.productName }}</span>
              <span class="summary-item-qty">x{{ item.quantity }}</span>
              <span class="summary-item-price">{{ formatCurrency(item.subtotal) }}</span>
            </div>
            <el-divider />
            <div class="summary-total">
              <span>合計</span>
              <span class="total-price">{{ formatCurrency(cartStore.totalAmount) }}</span>
            </div>
            <div class="order-buttons">
              <div>
                <el-button type="primary" size="large" style="width:100%" :loading="submitting" @click="handleSubmit">
                  確認下單
                </el-button>
              </div>
              <div>
                <el-button size="large" style="width:100%" @click="$router.push('/cart')">返回購物車</el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </main>

    <!-- 常用地址選擇 Dialog -->
    <el-dialog v-model="addressDialogVisible" title="選擇常用地址" width="500px">
      <div v-if="savedAddresses.length === 0" class="dialog-empty">尚無常用地址</div>
      <div v-else class="address-list">
        <div v-for="addr in savedAddresses" :key="addr.id" class="address-item"
          @click="selectAddress(addr)">
          <div class="addr-label-row">
            <el-tag size="small">{{ addr.label }}</el-tag>
            <el-button type="danger" link size="small" @click.stop="deleteAddress(addr.id)">刪除</el-button>
          </div>
          <div class="addr-name">{{ addr.recipientName }} / {{ addr.recipientPhone }}</div>
          <div class="addr-address">{{ addr.address }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 常用門市選擇 Dialog -->
    <el-dialog v-model="storeDialogVisible" :title="`選擇常用${currentStoreLabel}門市`" width="500px">
      <div v-if="savedStores.length === 0" class="dialog-empty">尚無常用門市</div>
      <div v-else class="address-list">
        <div v-for="store in savedStores" :key="store.id" class="address-item"
          @click="selectStore(store)">
          <div class="addr-label-row">
            <el-tag size="small" :type="store.storeType === 'FAMILY_MART' ? 'success' : 'danger'">
              {{ store.storeTypeLabel }}
            </el-tag>
            <el-button type="danger" link size="small" @click.stop="deleteStore(store.id)">刪除</el-button>
          </div>
          <div class="addr-name">{{ store.storeName }}</div>
          <div class="addr-address">店號：{{ store.storeCode }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import api from '../../api'
import NavBar from '../../components/NavBar.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

// ===== 常用地址 / 門市 =====
const savedAddresses = ref([])
const savedStores = ref([])
const addressDialogVisible = ref(false)
const storeDialogVisible = ref(false)

// ===== 表單 =====
const sameAsOrderer = ref(false)
const showCvv = ref(false)
const submitting = ref(false)

const form = ref({
  // 訂購人
  ordererName: '',
  ordererCountryCode: '+886',
  ordererPhoneNum: '',
  ordererAddress: '',
  // 收貨人
  recipientName: '',
  recipientCountryCode: '+886',
  recipientPhoneNum: '',
  // 送貨方式
  deliveryMethod: 'HOME_DELIVERY',
  recipientAddress: '',
  storeName: '',
  storeCode: '',
  // 付款
  paymentMethod: 'SIMULATED',
  cardNumber: '',
  cardHolder: '',
  cardExpiry: '',
  cardCvv: '',
  // 儲存選項
  saveAddress: false,
  saveStore: false
})

// ===== 國碼清單（靜態，不需 API）=====
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

// 格式化金額
const formatter = new Intl.NumberFormat('zh-TW', { style: 'currency', currency: 'TWD', maximumFractionDigits: 0 })
function formatCurrency(v) { return formatter.format(Number(v || 0)) }

// 信用卡號格式化（每4位加空格）
function formatCardNumber() {
  let v = form.value.cardNumber.replace(/\D/g, '').substring(0, 16)
  form.value.cardNumber = v.replace(/(.{4})/g, '$1 ').trim()
}
function formatExpiry() {
  let v = form.value.cardExpiry.replace(/\D/g, '').substring(0, 4)
  if (v.length > 2) v = v.substring(0, 2) + '/' + v.substring(2)
  form.value.cardExpiry = v
}
const formattedCardNumber = computed(() =>
  form.value.cardNumber || '•••• •••• •••• ••••'
)

// 顯示的超商名稱
const currentStoreLabel = computed(() =>
  form.value.deliveryMethod === 'FAMILY_MART' ? '全家' : '7-11'
)

// 是否顯示「新增至常用」checkbox
const canSaveAddress = computed(() => {
  if (!form.value.recipientAddress.trim()) return false
  return !savedAddresses.value.some(a =>
    a.recipientName === form.value.recipientName &&
    a.recipientPhone === (form.value.recipientCountryCode + form.value.recipientPhoneNum) &&
    a.address === form.value.recipientAddress
  )
})
const canSaveStore = computed(() => {
  if (!form.value.storeCode.trim()) return false
  return !savedStores.value.some(s =>
    s.storeType === form.value.deliveryMethod &&
    s.storeCode === form.value.storeCode
  )
})

// 同訂購人
function copySameAsOrderer(val) {
  if (val) {
    form.value.recipientName = form.value.ordererName
    form.value.recipientCountryCode = form.value.ordererCountryCode
    form.value.recipientPhoneNum = form.value.ordererPhoneNum
    if (form.value.deliveryMethod === 'HOME_DELIVERY') {
      form.value.recipientAddress = form.value.ordererAddress
    }
  }
}

// 載入個人資料以預填
onMounted(async () => {
  await cartStore.fetchCart()
  if (cartStore.items.length === 0) return
  try {
    const res = await api.get('/user/profile')
    const profile = res.data
    form.value.ordererName = profile.username || ''
    if (profile.phone) {
      const matched = countryCodes.find(c => profile.phone.startsWith(c.code))
      if (matched) {
        form.value.ordererCountryCode = matched.code
        form.value.ordererPhoneNum = profile.phone.slice(matched.code.length)
      } else {
        form.value.ordererPhoneNum = profile.phone
      }
    }
    form.value.ordererAddress = profile.address || ''
  } catch { /* 靜默失敗 */ }

  await loadSavedAddresses()
  await loadSavedStores()
})

// 切換送貨方式時重新載入常用門市
watch(() => form.value.deliveryMethod, async (method) => {
  if (method !== 'HOME_DELIVERY') {
    await loadSavedStores()
  }
})

async function loadSavedAddresses() {
  try {
    const res = await api.get('/user/addresses')
    savedAddresses.value = res.data
  } catch { savedAddresses.value = [] }
}

async function loadSavedStores() {
  try {
    const type = form.value.deliveryMethod
    if (type === 'HOME_DELIVERY') { savedStores.value = []; return }
    const res = await api.get('/user/stores', { params: { type } })
    savedStores.value = res.data
  } catch { savedStores.value = [] }
}

function openAddressDialog() {
  addressDialogVisible.value = true
}
function openStoreDialog() {
  storeDialogVisible.value = true
}
function selectAddress(addr) {
  form.value.recipientName = addr.recipientName
  const matched = countryCodes.find(c => addr.recipientPhone.startsWith(c.code))
  if (matched) {
    form.value.recipientCountryCode = matched.code
    form.value.recipientPhoneNum = addr.recipientPhone.slice(matched.code.length)
  } else {
    form.value.recipientPhoneNum = addr.recipientPhone
  }
  form.value.recipientAddress = addr.address
  addressDialogVisible.value = false
}
function selectStore(store) {
  form.value.storeName = store.storeName
  form.value.storeCode = store.storeCode
  storeDialogVisible.value = false
}

async function deleteAddress(id) {
  try {
    await api.delete(`/user/addresses/${id}`)
    await loadSavedAddresses()
    ElMessage.success('已刪除常用地址')
  } catch { ElMessage.error('刪除失敗') }
}
async function deleteStore(id) {
  try {
    await api.delete(`/user/stores/${id}`)
    await loadSavedStores()
    ElMessage.success('已刪除常用門市')
  } catch { ElMessage.error('刪除失敗') }
}

// ===== 送出訂單 =====
async function handleSubmit() {
  // 基本驗證
  if (!form.value.ordererName.trim()) return ElMessage.warning('請填寫訂購人姓名')
  if (!form.value.ordererPhoneNum.trim()) return ElMessage.warning('請填寫訂購人手機號碼')
  if (!form.value.ordererAddress.trim()) return ElMessage.warning('請填寫訂購人地址')
  if (!form.value.recipientName.trim()) return ElMessage.warning('請填寫收貨人姓名')
  if (!form.value.recipientPhoneNum.trim()) return ElMessage.warning('請填寫收貨人手機號碼')
  if (form.value.deliveryMethod === 'HOME_DELIVERY' && !form.value.recipientAddress.trim())
    return ElMessage.warning('宅配請填寫收貨地址')
  if (form.value.deliveryMethod !== 'HOME_DELIVERY') {
    if (!form.value.storeName.trim()) return ElMessage.warning('請填寫門市名稱')
    if (!form.value.storeCode.trim()) return ElMessage.warning('請填寫門市店號')
  }
  if (!form.value.cardNumber.trim()) return ElMessage.warning('請填寫信用卡卡號')
  if (!form.value.cardExpiry.trim()) return ElMessage.warning('請填寫信用卡有效期限')
  if (!form.value.cardCvv.trim()) return ElMessage.warning('請填寫信用卡安全碼')

  submitting.value = true
  try {
    const payload = {
      ordererName: form.value.ordererName.trim(),
      ordererPhone: form.value.ordererCountryCode + form.value.ordererPhoneNum.trim(),
      ordererAddress: form.value.ordererAddress.trim(),
      recipientName: form.value.recipientName.trim(),
      recipientPhone: form.value.recipientCountryCode + form.value.recipientPhoneNum.trim(),
      deliveryMethod: form.value.deliveryMethod,
      recipientAddress: form.value.deliveryMethod === 'HOME_DELIVERY' ? form.value.recipientAddress.trim() : null,
      storeName: form.value.deliveryMethod !== 'HOME_DELIVERY' ? form.value.storeName.trim() : null,
      storeCode: form.value.deliveryMethod !== 'HOME_DELIVERY' ? form.value.storeCode.trim() : null,
      paymentMethod: form.value.paymentMethod,
      saveAddress: form.value.saveAddress && form.value.deliveryMethod === 'HOME_DELIVERY',
      saveStore: form.value.saveStore && form.value.deliveryMethod !== 'HOME_DELIVERY'
    }
    const res = await api.post('/orders/checkout', payload)
    cartStore.reset()
    ElMessage.success('下單成功！')
    router.push(`/order-complete/${res.data.id}`)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '下單失敗，請稍後再試')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.checkout-page { min-height: 100vh; background: #f5f7fa; }
.page-content { max-width: 1200px; margin: 0 auto; padding: 32px 16px; }
.steps-bar { margin-bottom: 32px; padding: 20px 0; background: #fff; border-radius: 12px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.page-title { font-size: 24px; margin-bottom: 24px; }
.checkout-layout { display: flex; gap: 24px; align-items: flex-start; }
.form-col { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 20px; }
.summary-col { width: 320px; flex-shrink: 0; }
.section-card { border-radius: 12px; }
.section-title { font-size: 16px; font-weight: 600; display: block; text-align: left; }
.section-header-row { display: flex; align-items: center; justify-content: space-between; }
.phone-row { display: flex; gap: 8px; }
.country-code-select { width: 120px; flex-shrink: 0; }
.phone-input { flex: 1; }
.delivery-group { display: flex; gap: 12px; }
.address-input-row { display: flex; gap: 8px; }
.address-input { flex: 1; }
.save-check { margin-top: 8px; color: #606266; }
.payment-label { font-size: 15px; font-weight: 500; }

/* 信用卡預覽 */
.credit-card-form { margin-top: 20px; }
.card-preview {
  width: 320px; height: 185px; border-radius: 16px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: #fff; padding: 24px; margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3); position: relative; overflow: hidden;
}
.card-chip { font-size: 24px; margin-bottom: 20px; }
.card-number { font-size: 20px; letter-spacing: 3px; font-family: monospace; margin-bottom: 20px; }
.card-bottom { display: flex; justify-content: space-between; }
.card-label { font-size: 10px; opacity: 0.7; text-transform: uppercase; margin-bottom: 4px; }
.card-value { font-size: 14px; font-weight: 600; }
.card-inputs { margin-top: 4px; }

/* 訂單摘要 */
.summary-card { position: sticky; top: 24px; }
.summary-item { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; font-size: 14px; }
.summary-item-num { color: #909399; flex-shrink: 0; min-width: 20px; }
.summary-item-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; text-align: left; }
.summary-item-qty { color: #909399; flex-shrink: 0; }
.summary-item-price { font-weight: 600; flex-shrink: 0; color: #e1251b; }
.summary-total { display: flex; justify-content: space-between; font-size: 18px; font-weight: 700; margin-bottom: 16px; }
.total-price { color: #e1251b; }
.order-buttons { display: flex; flex-direction: column; gap: 10px; }
.order-buttons :deep(.el-button) { width: 100%; }

/* 常用地址/門市 Dialog */
.address-list { display: flex; flex-direction: column; gap: 12px; }
.address-item {
  padding: 12px 16px; border: 1px solid #e4e7ed; border-radius: 8px;
  cursor: pointer; transition: all 0.2s;
}
.address-item:hover { border-color: #409eff; background: #f0f7ff; }
.addr-label-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.addr-name { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.addr-address { font-size: 13px; color: #909399; }
.dialog-empty { text-align: center; color: #909399; padding: 20px 0; }

@media (max-width: 768px) {
  .checkout-layout { flex-direction: column; }
  .summary-col { width: 100%; }
  .card-preview { width: 100%; }
}
</style>
