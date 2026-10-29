package com.sentineldesk.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByIncidentNumber(String incidentNumber);

    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.owner ORDER BY i.createdAt DESC")
    List<Incident> findAllWithDetails();

    long countByStatus(IncidentStatus status);
}
