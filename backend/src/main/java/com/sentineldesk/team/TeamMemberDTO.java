package com.sentineldesk.team;

import com.sentineldesk.user.UserDTO;

public class TeamMemberDTO {
    private Long id;
    private Long teamId;
    private UserDTO user;
    private boolean isTeamLead;

    public TeamMemberDTO() {}

    public TeamMemberDTO(TeamMember tm) {
        this.id = tm.getId();
        this.teamId = tm.getTeam().getId();
        this.user = UserDTO.fromEntity(tm.getUser());
        this.isTeamLead = tm.isTeamLead();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public boolean isTeamLead() {
        return isTeamLead;
    }

    public void setTeamLead(boolean teamLead) {
        isTeamLead = teamLead;
    }
}
