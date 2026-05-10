/**
 * Vue 3 應用程式進入點
 *
 * 在這裡載入並註冊所有全局套件：
 * - Router: 頁面路由
 * - Pinia: 狀態管理
 * - Element Plus: UI 元件庫
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

// 註冊 Pinia（狀態管理）
app.use(createPinia())

// 註冊 Router（路由）
app.use(router)

// 註冊 Element Plus（UI 元件庫）
app.use(ElementPlus)

// 註冊所有 Element Plus 圖示為全局元件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 掛載到 HTML 的 #app 節點
app.mount('#app')
