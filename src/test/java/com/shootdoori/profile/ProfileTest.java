package com.shootdoori.profile;

import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.dto.ProfileUpdateRequest;
import com.shootdoori.match.entity.User;
import com.shootdoori.match.exception.DuplicatedDataException;
import com.shootdoori.match.repository.ProfileRepository;
import com.shootdoori.match.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileTest {
    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;

    @Test
    @DisplayName("프로필 정보 수정 성공")
    void updateProfile_Success() {
        // given
        Long userId = 1L;
        ProfileCreateRequest createRequest = new ProfileCreateRequest(
            "jam",
            "test@email.com",
            "test@email.co.kr",
            "010-0000-0000",
            "knu",
            "cs",
            "202500000",
            "hello, world"
        );
        User user = new User(createRequest);
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest("변경된이름");

        when(profileRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        profileService.updateProfile(userId, updateRequest);

        // then
        assertThat(user.getName()).isEqualTo("변경된이름");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 프로필 생성 시 DuplicatedDataException 예외 발생")
    void createProfile_WithDuplicateEmail_ThrowsException() {
        // given
        ProfileCreateRequest createRequest = new ProfileCreateRequest(
            "jam",
            "duplicate@email.com",
            "test@email.co.kr",
            "010-0000-0000",
            "knu",
            "cs",
            "202500000",
            "hello, world"
        );

        when(profileRepository.existsByEmailOrUniversityEmail(
            createRequest.email(),
            createRequest.universityEmail()
        )).thenReturn(true);

        // when & then
        assertThrows(DuplicatedDataException.class, () -> {
            profileService.createProfile(createRequest);
        });
    }
}
