package com.ecommerce.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 圖片上傳 Controller
 *
 * POST /api/admin/upload
 *   - 接收 multipart/form-data，欄位名稱為 "file"
 *   - 驗證副檔名（只允許 jpg / jpeg / png / webp）
 *   - 將檔案儲存到 app.upload.dir 設定的目錄
 *   - 回傳可直接使用的圖片 URL，例如 "/uploads/abc123.jpg"
 *
 * SecurityConfig 已設定 /uploads/** 為公開路徑，任何人都能讀取圖片。
 * 上傳 API 本身在 /api/admin/** 之下，只有 ROLE_ADMIN 才能呼叫。
 */
@RestController
@RequestMapping("/api/admin/upload")
public class UploadController {

    // 允許上傳的圖片副檔名（全小寫比對）
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    // 從 application.properties 讀取儲存目錄
    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * 上傳單張圖片
     *
     * 請求範例（multipart/form-data）：
     *   POST /api/admin/upload
     *   file = (binary image data)
     *
     * 回應範例：
     *   { "url": "/uploads/3f7a2b1c-xxxx.png" }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        // 1. 驗證不能是空檔案
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請選擇要上傳的圖片檔案");
        }

        // 2. 驗證 MIME type（不能只依賴副檔名，還要看 Content-Type）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只允許上傳圖片格式的檔案");
        }

        // 3. 取得原始副檔名並驗證
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無效的檔案名稱");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "不支援的圖片格式，請上傳 JPG / PNG / WebP");
        }

        // 4. 產生不重複的檔名（UUID），防止使用者透過檔名注入路徑
        String newFilename = UUID.randomUUID() + "." + ext;

        // 5. 確保目錄存在
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "建立上傳目錄失敗");
        }

        // 6. 解析目標路徑並確認不超出目錄（防止 Path Traversal 攻擊）
        Path targetPath = uploadPath.resolve(newFilename).normalize();
        if (!targetPath.startsWith(uploadPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法的檔案路徑");
        }

        // 7. 寫入檔案
        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "圖片儲存失敗，請稍後再試");
        }

        // 8. 回傳公開可存取的 URL
        String fileUrl = "/uploads/" + newFilename;
        return ResponseEntity.ok(Map.of("url", fileUrl));
    }
}
