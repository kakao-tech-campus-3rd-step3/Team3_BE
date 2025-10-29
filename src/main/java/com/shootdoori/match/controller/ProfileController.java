package com.shootdoori.match.controller;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.resolver.LoginUser;
import com.shootdoori.match.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ProfileResponse> postProfile(
        @Valid @RequestBody ProfileCreateRequest request
    ) {
        return new ResponseEntity<>(profileService.createProfile(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getAllProfiles() {
        return new ResponseEntity<>(profileService.getProfilesWithDeleted(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long id) {
        return new ResponseEntity<>(profileService.findProfileById(id), HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@LoginUser Long userId) {
        return new ResponseEntity<>(profileService.findProfileById(userId), HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
        @LoginUser Long userId,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileResponse updatedProfile = profileService.updateProfile(userId, request);
        return new ResponseEntity<>(updatedProfile, HttpStatus.OK);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteProfile(@LoginUser Long userId) {
        profileService.deleteAccount(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}