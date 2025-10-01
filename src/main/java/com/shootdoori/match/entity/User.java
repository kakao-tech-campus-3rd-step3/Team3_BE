package com.shootdoori.match.entity;

import com.shootdoori.match.value.Password;
import com.shootdoori.match.value.UniversityName;
import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Entity
public class User extends DateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "SKILL_LEVEL", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT '아마추어'")
    private SkillLevel skillLevel = SkillLevel.AMATEUR;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "university_email", nullable = false, unique = true, length = 255)
    private String universityEmail;

    @Embedded
    @AttributeOverride(name = "password", column = @Column(name = "PASSWORD", nullable = false, length = 255))
    private Password password;

    @Column(name = "kakao_user_id", nullable = false, length = 20)
    private String kakaoTalkId;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "UNIVERSITY", nullable = false, length = 100))
    private UniversityName university;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "student_year", nullable = false, length = 2)
    private String studentYear;

    @Column(length = 500)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "POSITION", nullable = false, length = 2)
    private UserPosition position;

    protected User() {

    }

    private User(String name, SkillLevel skillLevel, String email, String universityEmail, String password, String kakaoTalkId,
        UserPosition position, String university, String department, String studentYear, String bio) {
        validate(name, skillLevel.getDisplayName(), email, universityEmail, password, kakaoTalkId, position.getDisplayName(), university, department, studentYear, bio);
        this.name = name;
        this.skillLevel = skillLevel;
        this.email = email;
        this.universityEmail = universityEmail;
        this.password = Password.of(password);
        this.kakaoTalkId = kakaoTalkId;
        this.position = position;
        this.university = UniversityName.of(university);
        this.department = department;
        this.studentYear = studentYear;
        this.bio = bio;
    }

    public static User create(String name, String skillLevelName, String email, String universityEmail, String encodedPassword, String kakaoTalkId,
                              String positionName, String university, String department, String studentYear, String bio) {
        UserPosition position = UserPosition.fromDisplayName(positionName);
        SkillLevel skillLevel = SkillLevel.fromDisplayName(skillLevelName);
        return new User(name, skillLevel, email, universityEmail, encodedPassword, kakaoTalkId, position, university, department,
            studentYear, bio);
    }

    private void validate(String name, String skillLevel, String email, String universityEmail, String password, String kakaoTalkId,
        String position, String university, String department, String studentYear, String bio) {
        validateName(name);
        validateSkillLevel(skillLevel);
        validateEmail(email);
        validateUniversityEmail(universityEmail);
        validatePassword(password);
        validateKakaoTalkId(kakaoTalkId);
        validatePosition(position);
        validateUniversity(university);
        validateDepartment(department);
        validateStudentYear(studentYear);
        validateBio(bio);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수 입력 값입니다.");
        }
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("이름은 2자 이상 100자 이하로 입력해주세요.");
        }
    }

    private void validateSkillLevel(String skillLevel) {
        if (skillLevel == null || skillLevel.isBlank()) {
            throw new IllegalArgumentException("스킬 레벨은 필수 입력 값입니다.");
        }

        try {
            SkillLevel.fromDisplayName(skillLevel);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 스킬 레벨입니다: " + skillLevel);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수 입력 값입니다.");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("이메일 주소는 255자를 초과할 수 없습니다.");
        }
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    private void validateUniversityEmail(String universityEmail) {
        if (universityEmail == null || universityEmail.isBlank()) {
            throw new IllegalArgumentException("학교 이메일은 필수 입력 값입니다.");
        }
        if (universityEmail.length() > 255) {
            throw new IllegalArgumentException("학교 이메일 주소는 255자를 초과할 수 없습니다.");
        }
        if (!universityEmail.endsWith("ac.kr")) {
            throw new IllegalArgumentException("학교 이메일은 'ac.kr' 도메인이어야 합니다.");
        }
    }

    private void validateKakaoTalkId(String kakaoTalkId) {
        if (kakaoTalkId == null || kakaoTalkId.isBlank()) {
            throw new IllegalArgumentException("카카오톡 채널(친구추가) 아이디는 필수 입력 값입니다.");
        }
        if (!kakaoTalkId.matches("^[a-zA-Z0-9_.-]{4,20}$")) {
            throw new IllegalArgumentException("아이디의 형식이 올바르지 않습니다. 영문, 숫자, 특수문자(-, _, .)를 포함하여 4~20자이어야 합니다.");
        }
    }

    private void validatePosition(String position) {
        if (position == null || position.isBlank()) {
            throw new IllegalArgumentException("포지션은 필수 입력 값입니다.");
        }

        try {
            UserPosition.fromDisplayName(position);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 포지션입니다: " + position);
        }
    }

    private void validateUniversity(String university) {
        if (university == null || university.isBlank()) {
            throw new IllegalArgumentException("대학교 이름은 필수 입력 값입니다.");
        }
        if (university.length() > 100) {
            throw new IllegalArgumentException("대학교 이름은 100자를 초과할 수 없습니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력 값입니다.");
        }
    }

    private void validateDepartment(String department) {
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("학과 이름은 필수 입력 값입니다.");
        }
        if (department.length() > 100) {
            throw new IllegalArgumentException("학과 이름은 100자를 초과할 수 없습니다.");
        }
    }

    private void validateStudentYear(String studentYear) {
        if (studentYear == null || studentYear.isBlank()) {
            throw new IllegalArgumentException("입학년도는 필수 입력 값입니다.");
        }
        if (!studentYear.matches("\\d{2}")) {
            throw new IllegalArgumentException("입학년도는 2자리 숫자로 입력해주세요. (예: 25)");
        }
    }

    private void validateBio(String bio) {
        if (bio.length() > 500) {
            throw new IllegalArgumentException("자기소개는 500자를 초과할 수 없습니다.");
        }
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public SkillLevel getSkillLevel() {
        return this.skillLevel;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUniversityEmail() {
        return this.universityEmail;
    }

    public String getKakaoTalkId() {
        return this.kakaoTalkId;
    }

    public UniversityName getUniversity() {
        return this.university;
    }

    public String getDepartment() {
        return this.department;
    }

    public String getStudentYear() {
        return this.studentYear;
    }

    public String getBio() {
        return this.bio;
    }

    public UserPosition getPosition() {
        return this.position;
    }

    public void update(String skillLevel, String position, String bio) {
        validateSkillLevel(skillLevel);
        validatePosition(position);
        validateBio(bio);
        this.skillLevel = SkillLevel.fromDisplayName(skillLevel);
        this.position = UserPosition.fromDisplayName(position);
        this.bio = bio;
    }

    public void validatePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        this.password.validate(rawPassword, passwordEncoder);
    }

    public void changePassword(String encodedPassword) {
        this.password = Password.of(encodedPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
