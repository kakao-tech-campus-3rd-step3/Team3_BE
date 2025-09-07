package com.shootdoori.match.service;

import com.shootdoori.match.dto.CreateTeamRequestDto;
import com.shootdoori.match.dto.CreateTeamResponseDto;

public interface TeamService {
    public CreateTeamResponseDto create(CreateTeamRequestDto requestDto);
}
