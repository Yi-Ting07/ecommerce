package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.NewsResponse;
import com.ecommerce.backend.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台公開新聞公告 Controller（不需登入）
 *
 * Base URL: /api/news
 * 由 SecurityConfig 設定：GET /api/news/** 為公開路由
 *
 * GET /api/news        — 分頁取已發佈公告（置頂優先）
 * GET /api/news/{id}   — 取得單篇已發佈公告
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public ResponseEntity<Page<NewsResponse>> getNews(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(newsService.getPublicNews(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getPublicNewsById(id));
    }
}
