package com.sentineldesk.team;

import com.sentineldesk.common.exception.BadRequestException;
import com.sentineldesk.common.exception.ResourceNotFoundException;
import com.sentineldesk.user.User;
import com.sentineldesk.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToDTOWithMembers)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamDTO getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return mapToDTOWithMembers(team);
    }

    @Transactional
    public TeamDTO createTeam(String name, String description, String categoryFocus) {
        if (teamRepository.existsByName(name)) {
            throw new BadRequestException("Team name already exists: " + name);
        }
        Team team = new Team(name, description, categoryFocus);
        team = teamRepository.save(team);
        return new TeamDTO(team);
    }

    @Transactional
    public TeamMemberDTO addMember(Long teamId, Long userId, boolean isTeamLead) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (teamMemberRepository.findByTeamIdAndUserId(teamId, userId).isPresent()) {
            throw new BadRequestException("User is already a member of this team");
        }

        TeamMember member = new TeamMember(team, user, isTeamLead);
        member = teamMemberRepository.save(member);
        return new TeamMemberDTO(member);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
        teamMemberRepository.delete(member);
    }

    private TeamDTO mapToDTOWithMembers(Team team) {
        TeamDTO dto = new TeamDTO(team);
        List<TeamMember> members = teamMemberRepository.findByTeamIdWithUsers(team.getId());
        dto.setMembers(members.stream().map(TeamMemberDTO::new).collect(Collectors.toList()));
        return dto;
    }
}
