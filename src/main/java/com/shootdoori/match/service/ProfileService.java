package com.shootdoori.match.service;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileMapper;
import com.shootdoori.match.dto.ProfileResponse;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedUserException;
import com.shootdoori.match.exception.ProfileNotFoundException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper, PasswordEncoder passwordEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
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
            createRequest.kakaoTalkId(),
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
            .orElseThrow(ProfileNotFoundException::new);
        return profileMapper.toProfileResponse(profile);
    }

    @Transactional
    public Optional<User> findByEmail(String email) {
        return profileRepository.findByEmail(email);
    }

    public void updateProfile(Long id, ProfileUpdateRequest updateRequest) {
        User profile = profileRepository.findById(id)
            .orElseThrow(ProfileNotFoundException::new);
        profile.update(updateRequest.skillLevel(), updateRequest.position(), updateRequest.bio());
    }

    public void deleteAccount(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new ProfileNotFoundException();
        }

        refreshTokenRepository.deleteAllByUserId(id);

        profileRepository.deleteById(id);
    }
}
