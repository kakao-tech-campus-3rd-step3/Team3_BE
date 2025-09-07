package com.shootdoori.match.service;

import com.shootdoori.match.dto.CreateTeamRequestDto;
import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.TeamDetailResponseDto;
import com.shootdoori.match.entity.User;

public interface TeamService {

    public CreateTeamResponseDto create(CreateTeamRequestDto requestDto, User captain);

    public TeamDetailResponseDto findById(Long id);

    public void delete(Long id);
}
