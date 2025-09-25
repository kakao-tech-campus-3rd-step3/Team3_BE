package com.shootdoori.match.service;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileMapper;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedUserException;
import com.shootdoori.match.repository.ProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper, PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileResponse createProfile(ProfileCreateRequest createRequest) {
        if (profileRepository.existsByEmailOrUniversityEmail(
            createRequest.email(), createRequest.universityEmail())
        ) {
            throw new DuplicatedUserException();
        }

        String encodePassword = passwordEncoder.encode(createRequest.password());

        User user = User.create(
            createRequest.name(),
            createRequest.skillLevel(),
            createRequest.email(),
            createRequest.universityEmail(),
            encodePassword,
            createRequest.phoneNumber(),
            createRequest.position(),
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

    @Transactional
    public Optional<User> findByEmail(String email) {
        return profileRepository.findByEmail(email);
    }

    public void updateProfile(Long id, ProfileUpdateRequest updateRequest) {
        User profile = profileRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 프로필을 찾을 수 없습니다."));
        profile.update(updateRequest.skillLevel(), updateRequest.position(), updateRequest.bio());
    }

    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 프로필이 존재하지 않습니다.");
        }
        profileRepository.deleteById(id);
    }
}
