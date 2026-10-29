package com.sentineldesk.knowledgebase;

import com.sentineldesk.ticket.TicketCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Optional<KnowledgeArticle> findByArticleCode(String articleCode);

    List<KnowledgeArticle> findByCategory(TicketCategory category);

    @Query("SELECT k FROM KnowledgeArticle k WHERE " +
           "(:category IS NULL OR k.category = :category) AND " +
           "(:query IS NULL OR LOWER(k.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(k.summary) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(k.tags) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(k.articleCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<KnowledgeArticle> searchArticles(@Param("query") String query,
                                          @Param("category") TicketCategory category,
                                          Pageable pageable);

    @Query("SELECT k FROM KnowledgeArticle k WHERE " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(k.tags) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "k.category = :category")
    List<KnowledgeArticle> findSuggestions(@Param("term") String term,
                                           @Param("category") TicketCategory category);
}
