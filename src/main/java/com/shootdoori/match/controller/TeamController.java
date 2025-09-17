package com.shootdoori.match.controller;

import com.shootdoori.match.dto.CreateTeamResponseDto;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.TeamDetailResponseDto;
import com.shootdoori.match.dto.TeamRequestDto;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<CreateTeamResponseDto> create(
        @RequestBody TeamRequestDto requestDto) {

        // TODO: JWT 토큰에서 유저 데이터를 가져와 captain 변수에 넣어야 한다.
        ProfileCreateRequest createRequest = new ProfileCreateRequest(
            "김학생",
            "아마추어",
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

    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> findById(@PathVariable Long id) {

        return new ResponseEntity<>(teamService.findById(id), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<TeamDetailResponseDto>> findAllByUniversity(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam String university
    ) {

        return new ResponseEntity<>(teamService.findAllByUniversity(page, size, university),
            HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> update(
        @PathVariable Long id,
        @RequestBody TeamRequestDto requestDto
    ) {
        return new ResponseEntity<TeamDetailResponseDto>(teamService.update(id, requestDto),
            HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}