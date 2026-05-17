package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 新聞公告 Repository
 *
 * 繼承 JpaRepository 自動擁有基本 CRUD（save, findById, delete, findAll...）。
 * 自訂查詢方法：
 * - findByPublishedTrue  : 只查已發佈（前台用）
 * - findByPublished...OrderByPinnedDesc... : 置頂排最前，再按時間排
 */
public interface NewsRepository extends JpaRepository<News, Long> {

    /**
     * 前台查詢：只取已發佈的公告，
     * 先依 pinned 降序（置頂在前），再依 createdAt 降序（最新在前）
     */
    Page<News> findByPublishedTrueOrderByPinnedDescCreatedAtDesc(Pageable pageable);
}
