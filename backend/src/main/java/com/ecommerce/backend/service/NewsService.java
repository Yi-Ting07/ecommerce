package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.NewsRequest;
import com.ecommerce.backend.dto.NewsResponse;
import com.ecommerce.backend.model.News;
import com.ecommerce.backend.repository.NewsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/**
 * 新聞公告服務層
 *
 * 層次職責：
 *   Controller  →  Service（這裡）  →  Repository  →  DB
 *
 * 業務邏輯集中於此，Controller 只負責接收 HTTP 請求並呼叫 Service。
 */
@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    // ── 公開 API ────────────────────────────────────────────

    /**
     * 前台列表：只回傳「已發佈」的公告。
     * 排序：置頂 → 建立時間由新到舊
     */
    @Transactional(readOnly = true)
    public Page<NewsResponse> getPublicNews(Pageable pageable) {
        return newsRepository
                .findByPublishedTrueOrderByPinnedDescCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    /**
     * 前台取得單篇：必須是已發佈才能看
     */
    @Transactional(readOnly = true)
    public NewsResponse getPublicNewsById(Long id) {
        News news = findEntityById(id);
        if (!news.isPublished()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到公告");
        }
        return toResponse(news);
    }

    // ── 後台管理 API ─────────────────────────────────────────

    /**
     * 後台列表：回傳全部（含草稿），按建立時間降序
     */
    @Transactional(readOnly = true)
    public Page<NewsResponse> getAdminNews(Pageable pageable) {
        return newsRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * 後台取單篇（含草稿）
     */
    @Transactional(readOnly = true)
    public NewsResponse getAdminNewsById(Long id) {
        return toResponse(findEntityById(id));
    }

    /**
     * 新增公告
     * pinned / published 預設 false（若前端未傳值）
     */
    @Transactional
    public NewsResponse create(NewsRequest request) {
        LocalDateTime now = LocalDateTime.now();
        News news = News.builder()
                .title(request.title().trim())
                .content(request.content())
                .pinned(request.pinned() != null && request.pinned())
                .published(request.published() != null && request.published())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toResponse(newsRepository.save(news));
    }

    /**
     * 更新公告
     */
    @Transactional
    public NewsResponse update(Long id, NewsRequest request) {
        News news = findEntityById(id);
        news.setTitle(request.title().trim());
        news.setContent(request.content());
        news.setPinned(request.pinned() != null && request.pinned());
        news.setPublished(request.published() != null && request.published());
        news.setUpdatedAt(LocalDateTime.now());
        return toResponse(newsRepository.save(news));
    }

    /**
     * 刪除公告
     */
    @Transactional
    public void delete(Long id) {
        News news = findEntityById(id);
        newsRepository.delete(news);
    }

    // ── 私有工具方法 ─────────────────────────────────────────

    /** 依 ID 查 Entity，找不到丟 404 */
    public News findEntityById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到公告 id=" + id));
    }

    /** Entity → Response DTO 轉換 */
    private NewsResponse toResponse(News news) {
        return new NewsResponse(
                news.getId(),
                news.getTitle(),
                news.getContent(),
                news.isPinned(),
                news.isPublished(),
                news.getCreatedAt(),
                news.getUpdatedAt()
        );
    }
}
