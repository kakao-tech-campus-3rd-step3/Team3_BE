package com.shootdoori.match.controller;

import com.shootdoori.match.dto.CreateTeamRequestDto;
import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.entity.Team;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<CreateTeamResponseDto> create(@RequestBody CreateTeamRequestDto requestDto) {

        // TODO: JWT 토큰에서 유저 데이터를 가져와 captain 변수에 넣어야 한다.
        ProfileCreateRequest createRequest = new ProfileCreateRequest(
            "김학생",
            "student@example.com",
            "student@kangwon.ac.kr",
            "010-1234-5678",
            "강원대학교",
            "컴퓨터공학과",
            "20",
            "안녕하세요! 축구를 좋아하는 대학생입니다."
        );
        User captain = new User(createRequest);

        return new ResponseEntity<>(teamService.create(requestDto, captain), HttpStatus.CREATED);
    }
}