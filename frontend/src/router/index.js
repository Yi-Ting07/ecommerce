/**
 * Vue Router 路由設定
 *
 * 路由 = 定義「什麼 URL 對應什麼頁面」
 *
 * 分為三組：
 * 1. 公開路由 — 任何人都能看（首頁、商品列表、登入、註冊）
 * 2. 需登入路由 — 登入後才能用（購物車、訂單、個人資料）
 * 3. 管理員路由 — 只有 ADMIN 角色能進（後台管理）
 *
 * Navigation Guard（路由守衛）：
 * 在進入頁面前檢查權限，不符合就跳轉到登入頁或首頁
 */
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  // ========== 公開路由 ==========
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/customer/HomePage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/customer/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/customer/RegisterPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('../views/customer/ProductList.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('../views/customer/ProductDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/news',
    name: 'News',
    component: () => import('../views/customer/NewsList.vue'),
    meta: { requiresAuth: false }
  },

  // ========== 需要登入的路由 ==========
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/customer/CartPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/customer/OrderList.vue'),
    meta: { requiresAuth: true }
  },

  // ========== 管理員後台路由 ==========
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/DashboardPage.vue')
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/ProductManage.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/OrderManage.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManage.vue')
      },
      {
        path: 'news',
        name: 'AdminNews',
        component: () => import('../views/admin/NewsManage.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('../views/admin/CategoryManage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局路由守衛（Navigation Guard）
 * 每次切換頁面前都會執行這段邏輯
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  // 需要登入但沒有 Token → 跳到登入頁
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理員但角色不是 ADMIN → 跳到首頁
  if (to.meta.requiresAdmin && user?.role !== 'ROLE_ADMIN') {
    next({ name: 'Home' })
    return
  }

  // 已登入卻要去登入/註冊頁 → 跳到首頁
  if ((to.name === 'Login' || to.name === 'Register') && token) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
