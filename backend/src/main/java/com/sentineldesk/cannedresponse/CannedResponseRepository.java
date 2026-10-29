package com.sentineldesk.cannedresponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CannedResponseRepository extends JpaRepository<CannedResponse, Long> {

    Optional<CannedResponse> findByShortcut(String shortcut);

    List<CannedResponse> findByCategoryIgnoreCase(String category);
}
