package com.shootdoori.match.controller;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> postProfile(@Valid @RequestBody ProfileCreateRequest request) {
        return new ResponseEntity<>(profileService.createProfile(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long id) {
        return new ResponseEntity<>(profileService.findProfileById(id), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getProfile(@LoginUser User user) {
        ProfileResponse profile = profileService.findProfileById(user.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(@LoginUser User user, @Valid @RequestBody ProfileUpdateRequest request) {
        profileService.updateProfile(user.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile(@LoginUser User user) {
        profileService.deleteAccount(user.getId());
        return ResponseEntity.noContent().build();
    }
}