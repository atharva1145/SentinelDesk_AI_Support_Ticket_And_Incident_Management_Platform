package com.sentineldesk.history;

import com.sentineldesk.team.TeamDTO;
import com.sentineldesk.user.UserDTO;
import java.time.LocalDateTime;

public class TicketAssignmentHistoryDTO {
    private Long id;
    private Long ticketId;
    private UserDTO changedBy;
    private UserDTO oldAgent;
    private UserDTO newAgent;
    private TeamDTO oldTeam;
    private TeamDTO newTeam;
    private String reason;
    private LocalDateTime createdAt;

    public TicketAssignmentHistoryDTO() {}

    public TicketAssignmentHistoryDTO(TicketAssignmentHistory tah) {
        this.id = tah.getId();
        this.ticketId = tah.getTicket().getId();
        this.changedBy = UserDTO.fromEntity(tah.getChangedBy());
        this.oldAgent = UserDTO.fromEntity(tah.getOldAgent());
        this.newAgent = UserDTO.fromEntity(tah.getNewAgent());
        this.oldTeam = tah.getOldTeam() != null ? new TeamDTO(tah.getOldTeam()) : null;
        this.newTeam = tah.getNewTeam() != null ? new TeamDTO(tah.getNewTeam()) : null;
        this.reason = tah.getReason();
        this.createdAt = tah.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public UserDTO getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UserDTO changedBy) {
        this.changedBy = changedBy;
    }

    public UserDTO getOldAgent() {
        return oldAgent;
    }

    public void setOldAgent(UserDTO oldAgent) {
        this.oldAgent = oldAgent;
    }

    public UserDTO getNewAgent() {
        return newAgent;
    }

    public void setNewAgent(UserDTO newAgent) {
        this.newAgent = newAgent;
    }

    public TeamDTO getOldTeam() {
        return oldTeam;
    }

    public void setOldTeam(TeamDTO oldTeam) {
        this.oldTeam = oldTeam;
    }

    public TeamDTO getNewTeam() {
        return newTeam;
    }

    public void setNewTeam(TeamDTO newTeam) {
        this.newTeam = newTeam;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
