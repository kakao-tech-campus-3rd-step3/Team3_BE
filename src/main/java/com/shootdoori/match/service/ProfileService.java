package com.shootdoori.match.service;

import com.shootdoori.match.dto.*;
import com.shootdoori.match.entity.User;
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

    /**
     * 새로운 프로필를 생성하고 데이터베이스에 저장합니다.
     * @param createRequest 프로필 생성을 위한 데이터가 담긴 DTO
     * @return 생성된 프로필 정보가 담긴 응답 DTO
     */
    public ProfileResponse createProfile(ProfileCreateRequest createRequest) {
        User saveProfile = profileRepository.save(new User(createRequest));
        return profileMapper.toProfileResponse(saveProfile);
    }

    /**
     * ID를 이용해 특정 프로필을 조회합니다.
     * @param id 조회할 프로필의 ID
     * @return 조회된 프로필 정보가 담긴 응답 DTO
     * @throws IllegalArgumentException 해당 ID의 프로필이 존재하지 않을 경우 발생
     */
    @Transactional(readOnly = true)
    public ProfileResponse findProfileById(Long id) {
        User profile = profileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 프로필을 찾을 수 없습니다."));
        return profileMapper.toProfileResponse(profile);
    }

    /**
     * 기존 프로필의 정보를 수정합니다.
     * @param id 수정할 프로필의 ID
     * @param updateRequest 수정할 정보가 담긴 DTO
     * @throws IllegalArgumentException 해당 ID의 프로필이 존재하지 않을 경우 발생
     */
    public void updateProfile(Long id, ProfileUpdateRequest updateRequest) {
        User profile = profileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 프로필을 찾을 수 없습니다."));
        profile.update(updateRequest);
    }

    /**
     * ID를 이용해 특정 프로필을 삭제합니다.
     * @param id 삭제할 프로필의 ID
     * @throws IllegalArgumentException 해당 ID의 프로필이 존재하지 않을 경우 발생
     */
    public void deleteProfile(Long id) {
        if (!profileRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 프로필이 존재하지 않습니다.");
        }
        profileRepository.deleteById(id);
    }
}
