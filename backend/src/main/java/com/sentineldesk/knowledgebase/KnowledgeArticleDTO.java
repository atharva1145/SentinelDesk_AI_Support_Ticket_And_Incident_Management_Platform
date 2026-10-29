package com.sentineldesk.knowledgebase;

import com.sentineldesk.ticket.TicketCategory;
import java.time.LocalDateTime;

public class KnowledgeArticleDTO {

    private Long id;
    private String articleCode;
    private String title;
    private String summary;
    private String content;
    private TicketCategory category;
    private String tags;
    private Integer viewCount;
    private Integer helpfulCount;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeArticleDTO() {}

    public KnowledgeArticleDTO(KnowledgeArticle article) {
        this.id = article.getId();
        this.articleCode = article.getArticleCode();
        this.title = article.getTitle();
        this.summary = article.getSummary();
        this.content = article.getContent();
        this.category = article.getCategory();
        this.tags = article.getTags();
        this.viewCount = article.getViewCount() != null ? article.getViewCount() : 0;
        this.helpfulCount = article.getHelpfulCount() != null ? article.getHelpfulCount() : 0;
        this.authorName = article.getAuthor() != null ? article.getAuthor().getFullName() : "IT Operations";
        this.createdAt = article.getCreatedAt();
        this.updatedAt = article.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getArticleCode() { return articleCode; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }
    public TicketCategory getCategory() { return category; }
    public String getTags() { return tags; }
    public Integer getViewCount() { return viewCount; }
    public Integer getHelpfulCount() { return helpfulCount; }
    public String getAuthorName() { return authorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
