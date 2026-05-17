# E-Commerce Portfolio Project

使用 Java Spring Boot + Vue 3 的前後端分離電商作品集專案。

## Tech Stack

- Backend: Java 21, Spring Boot 4, Spring Security, Spring Data JPA, Maven
- Frontend: Vue 3, Vite, Pinia, Vue Router, Axios, Element Plus
- Database: PostgreSQL
- IDE: VS Code

## Project Structure

```text
project/
├─ backend/   # Spring Boot API
└─ frontend/  # Vue 3 SPA
```

## Current Progress

- [x] Phase 1：認證系統（Register / Login API、JWT、ROLE_USER / ROLE_ADMIN、前端整合）
- [x] Phase 2：商品與分類（Category + Product Entity/Service/Controller、公開與後台 CRUD、前台商品列表 & 詳細頁、後台商品管理 & 分類管理）
- [x] Phase 3：購物車與訂單
- [x] Phase 4：新聞公告管理（News Entity/Service/Controller、公開列表 & 全文彈窗、後台 CRUD 含草稿/置頂）
- [x] Phase 5：圖片上傳、結帳流程強化、報表
  - 圖片上傳：`POST /api/admin/upload`，儲存於 `backend/uploads/`，`/uploads/**` 公開存取
  - 結帳強化：結帳成功後直接跳轉至訂單詳情頁 `/orders/:id`
  - 訂單詳情頁：顧客可查看商品明細、收貨地址、訂單狀態，並在 PENDING 狀態取消訂單
  - 後台儀表板：顯示真實統計（商品數、訂單數、會員數、總營收、各狀態訂單數）
- [x] Phase 6：會員個人中心 & 商品評論
  - 顧客修改個人資料（暱稱、Email）& 更改密碼（`/profile`）
  - 商品評論 / 星評系統（Review Entity、顧客新增/刪除、前台商品詳情頁展示）
  - 後台管理員可刪除不當評論（`/admin/reviews`）
  - 啟動成功訊息：後端啟動時在終端機顯示 API 位址與健康檢查連結
- [ ] Phase 7：部署上線
  - Docker Compose（後端 + PostgreSQL 容器化）
  - 前端部署至 Vercel / Netlify（靜態 SPA）
  - 後端部署至 Railway / Render（免費 PaaS）
  - 設定生產環境 application-prod.properties（環境變數）
  - GitHub Actions CI/CD 流程（自動測試 + 部署）

## Test Accounts

### Admin
- Email: admin@ecommerce.local
- Password: Admin1234!
- Role: ROLE_ADMIN

### Customer
- Email: testuser1@example.com
- Password: Test1234!
- Role: ROLE_USER

## How To Run

### 1. Start Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend default URL: `http://localhost:7687`

Health Check API:
- `GET http://localhost:7687/api/health`

### 2. Start Frontend

Open another terminal:

```powershell
# Windows PowerShell 需先允許執行腳本（每次新開終端機執行一次）
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

cd frontend
npm install   # 首次執行
npm run dev
```

Frontend URL: `http://localhost:5173`

## Database Setup

`backend/src/main/resources/application.properties` uses:

- Database: `ecommerce_db`
- Username: `ecommerce_user`
- Password: `ecommerce_pass`

If not created yet, run in PostgreSQL:

```sql
CREATE USER ecommerce_user WITH PASSWORD 'ecommerce_pass';
CREATE DATABASE ecommerce_db OWNER ecommerce_user;
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;
```

### Cart APIs（需登入）

- `GET /api/cart`
- `POST /api/cart/items`
- `PUT /api/cart/items/{id}`
- `DELETE /api/cart/items/{id}`
- `DELETE /api/cart`

### Order APIs（需登入）

- `POST /api/orders/checkout`
- `GET /api/orders`
- `GET /api/orders/{id}`
- `POST /api/orders/{id}/cancel`

### Admin Order APIs

- `GET /api/admin/orders`
- `GET /api/admin/orders/{id}`
- `PUT /api/admin/orders/{id}/status`

### News APIs（公開，不需登入）

- `GET /api/news` — 分頁取已發佈公告（置頂優先）
- `GET /api/news/{id}` — 取得單篇已發佈公告

### Admin News APIs（需 ROLE_ADMIN）

- `GET    /api/admin/news` — 查詢所有公告（含草稿）
- `GET    /api/admin/news/{id}` — 取得單篇
- `POST   /api/admin/news` — 新增公告
- `PUT    /api/admin/news/{id}` — 更新公告
- `DELETE /api/admin/news/{id}` — 刪除公告

## API Summary

### Auth APIs

- `POST /api/auth/register`
- `POST /api/auth/login`

### Category APIs

Public:
- `GET /api/categories`

Admin:
- `GET /api/admin/categories`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `DELETE /api/admin/categories/{id}`

### Product APIs

Public:
- `GET /api/products`
- `GET /api/products/{id}`

Admin:
- `GET /api/admin/products`
- `GET /api/admin/products/{id}`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`

## Important Auth Flow

1. Login success returns JWT token.
2. Frontend stores token in localStorage.
3. Axios interceptor automatically adds:
   - `Authorization: Bearer <token>`
4. Backend JWT filter validates token and role.
5. `/api/admin/**` only allows `ROLE_ADMIN`.

## Upload To GitHub

1. Create a new empty repository on GitHub (for example: `ecommerce-portfolio`).
2. In local project root:

```powershell
cd c:\Users\n8906\Desktop\worksapce\project
git init
git add .
git commit -m "feat: phase1 auth + phase2 products/categories"
git branch -M main
git remote add origin https://github.com/<your-account>/<your-repo>.git
git push -u origin main
```

If your repo already exists locally:

```powershell
git add .
git commit -m "feat: update project"
git push
```

## Interview Highlights (Suggested)

- Frontend and backend are fully separated and communicate via REST API.
- Authentication uses JWT with role-based authorization.
- Passwords are hashed with BCrypt, never stored as plain text.
- Backend follows layered architecture:
  - Controller -> Service -> Repository -> Entity
- Admin and customer routes are protected by router guards and backend security rules.
- Product module supports search, category filter, pagination, and admin CRUD.
- Image upload uses UUID-renamed files + MIME validation to prevent path traversal attacks.
- Dashboard uses JPQL aggregate queries (`SUM`, `GROUP BY`) for real-time statistics.

## Phase 5 新增 API

### Upload API（需 ROLE_ADMIN）

- `POST /api/admin/upload` — 上傳商品圖片（multipart/form-data，欄位名 `file`）
  - 回應：`{ "url": "/uploads/uuid.ext" }`
  - 限制：jpg / png / webp，最大 5MB

### Dashboard API（需 ROLE_ADMIN）

- `GET /api/admin/dashboard/stats` — 取得儀表板統計數據

### Order Detail（需登入）

- `GET /api/orders/{id}` — 查詢單筆訂單詳情（前端新增 `/orders/:id` 頁面）

