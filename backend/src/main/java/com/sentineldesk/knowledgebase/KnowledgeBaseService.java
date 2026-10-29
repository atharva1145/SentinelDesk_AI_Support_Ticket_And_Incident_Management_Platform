package com.sentineldesk.knowledgebase;

import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.common.response.PagedResponse;
import com.sentineldesk.security.SecurityUtils;
import com.sentineldesk.ticket.TicketCategory;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private final KnowledgeArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final AtomicLong codeSequence = new AtomicLong(1007);

    public KnowledgeBaseService(KnowledgeArticleRepository articleRepository,
                                UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<KnowledgeArticleDTO> searchArticles(String query, TicketCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "helpfulCount"));
        Page<KnowledgeArticle> articlePage = articleRepository.searchArticles(query, category, pageable);

        List<KnowledgeArticleDTO> content = articlePage.getContent().stream()
                .map(KnowledgeArticleDTO::new)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                articlePage.getNumber(),
                articlePage.getSize(),
                articlePage.getTotalElements(),
                articlePage.getTotalPages(),
                articlePage.isLast()
        );
    }

    @Transactional
    public KnowledgeArticleDTO getArticleById(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge article not found with id: " + id));

        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);

        return new KnowledgeArticleDTO(article);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleDTO> suggestArticles(String query, TicketCategory category) {
        String searchTerm = (query != null && !query.trim().isEmpty()) ? query.trim().toLowerCase() : "";

        List<KnowledgeArticle> all = articleRepository.findAll();

        return all.stream()
                .sorted(Comparator.comparingInt((KnowledgeArticle a) -> {
                    int score = 0;
                    if (category != null && a.getCategory() == category) {
                        score += 50;
                    }
                    if (!searchTerm.isEmpty()) {
                        String combined = (a.getTitle() + " " + a.getSummary() + " " + a.getTags()).toLowerCase();
                        for (String word : searchTerm.split("\\s+")) {
                            if (word.length() > 2 && combined.contains(word)) {
                                score += 25;
                            }
                        }
                    }
                    score += (a.getHelpfulCount() != null ? a.getHelpfulCount() : 0);
                    return score;
                }).reversed())
                .limit(4)
                .map(KnowledgeArticleDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public KnowledgeArticleDTO createArticle(CreateArticleRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User author = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;

        String code = "KB-" + codeSequence.getAndIncrement();
        KnowledgeArticle article = new KnowledgeArticle(
                code,
                request.getTitle(),
                request.getSummary(),
                request.getContent(),
                request.getCategory(),
                request.getTags(),
                author
        );

        KnowledgeArticle saved = articleRepository.save(article);
        return new KnowledgeArticleDTO(saved);
    }

    @Transactional
    public KnowledgeArticleDTO voteHelpful(Long id) {
        KnowledgeArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge article not found: " + id));

        article.setHelpfulCount(article.getHelpfulCount() + 1);
        KnowledgeArticle saved = articleRepository.save(article);
        return new KnowledgeArticleDTO(saved);
    }
}
