package com.sentineldesk.team;

import com.sentineldesk.user.UserDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TeamDTO {
    private Long id;
    private String name;
    private String description;
    private String categoryFocus;
    private List<TeamMemberDTO> members = new ArrayList<>();
    private LocalDateTime createdAt;

    public TeamDTO() {}

    public TeamDTO(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.description = team.getDescription();
        this.categoryFocus = team.getCategoryFocus();
        this.createdAt = team.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryFocus() {
        return categoryFocus;
    }

    public void setCategoryFocus(String categoryFocus) {
        this.categoryFocus = categoryFocus;
    }

    public List<TeamMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMemberDTO> members) {
        this.members = members;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
