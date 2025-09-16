package com.shootdoori.match.service;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileMapper;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedUserException;
import com.shootdoori.match.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
    }

    public ProfileResponse createProfile(ProfileCreateRequest createRequest) {
        if (profileRepository.existsByEmailOrUniversityEmail(
            createRequest.email(), createRequest.universityEmail())
        ) {
            throw new DuplicatedUserException();
        }

        User user = User.create(
            createRequest.name(),
            createRequest.email(),
            createRequest.universityEmail(),
            createRequest.phoneNumber(),
            createRequest.university(),
            createRequest.department(),
            createRequest.studentYear(),
            createRequest.bio()
        );

        User saveProfile = profileRepository.save(user);
        return profileMapper.toProfileResponse(saveProfile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse findProfileById(Long id) {
        User profile = profileRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 프로필을 찾을 수 없습니다."));
        return profileMapper.toProfileResponse(profile);
    }

    public void updateProfile(Long id, ProfileUpdateRequest updateRequest) {
        User profile = profileRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 프로필을 찾을 수 없습니다."));
        profile.update(updateRequest);
    }

    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 프로필이 존재하지 않습니다.");
        }
        profileRepository.deleteById(id);
    }
}
