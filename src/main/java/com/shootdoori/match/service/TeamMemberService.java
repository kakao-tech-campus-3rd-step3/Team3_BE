package com.shootdoori.match.service;

import com.shootdoori.match.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;

    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }
}
