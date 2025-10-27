package com.shootdoori.match.controller;

import com.shootdoori.match.dto.RecruitmentCreateRequest;
import com.shootdoori.match.dto.RecruitmentResponse;
import com.shootdoori.match.dto.RecruitmentUpdateRequest;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.MercenaryRecruitmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mercenaries/recruitments")
public class MercenaryRecruitmentController {
    private final MercenaryRecruitmentService recruitmentService;

    public MercenaryRecruitmentController(MercenaryRecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }

    @PostMapping
    public ResponseEntity<RecruitmentResponse> create(@RequestBody RecruitmentCreateRequest createRequest,
                                                      @LoginUser Long loginUserId) {
        return new ResponseEntity<>(recruitmentService.create(createRequest, loginUserId), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<RecruitmentResponse>> findAll(
        @PageableDefault(
            page = 0,size = 10,
            sort = {"matchDate", "matchTime"},
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        Page<RecruitmentResponse> recruitments = recruitmentService.findAllPages(pageable);
        return new ResponseEntity<>(recruitments, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruitmentResponse> findById(@PathVariable Long id) {
        return new ResponseEntity<>(recruitmentService.findById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecruitmentResponse> update(
        @PathVariable Long id,
        @RequestBody RecruitmentUpdateRequest updateRequest,
        @LoginUser Long loginUserId
    ) {
        return new ResponseEntity<>(recruitmentService.update(id, updateRequest, loginUserId), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @LoginUser Long loginUserId) {
        recruitmentService.delete(id, loginUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
