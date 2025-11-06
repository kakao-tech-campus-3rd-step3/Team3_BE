# Shoot Doori BE ⚽

**혼자가 아닌 함께, 실력 맞춤 지역 스포츠 매칭 플랫폼**

> 개인 실력과 지역 기반으로 축구 팀 매칭을 도와주는 서비스입니다.  
> 혼자서도, 친구와 함께도 쉽게 경기를 즐길 수 있도록 설계되었습니다.

---
## 👥 프로젝트 팀원

|                                                         Frontend                                                          |                                                         Frontend                                                          |                                                          Backend                                                          |                                                            Backend                                                            |                                                        Backend                                                        |                                                          Backend                                                          |
|:-------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------:|
| <img src="https://github.com/cheog0.png" width="150" height="150" alt="설유준"/> <br> **[설유준](https://github.com/cheog0)** | <img src="https://github.com/jskim3936.png" width="150" height="150" alt="김지수"/> <br> **[김지수](https://github.com/jskim3936)** | <img src="https://github.com/raminkim.png" width="150" height="150" alt="김상수"/> <br> **[김상수](https://github.com/raminkim)** | <img src="https://github.com/ManDu2001.png" width="150" height="150" alt="선원준"/> <br> **[선원준](https://github.com/ManDu2001)** | <img src="https://github.com/sweet-mine.png" width="150" height="150" alt="이시행"/> <br> **[이시행](https://github.com/sweet-mine)** | <img src="https://github.com/KKPASII.png" width="150" height="150" alt="서현주"/> <br> **[서현주](https://github.com/KKPASII)** |<img src="https://github.com/KKPASII.png" width="150" height="150" alt="서현주"/> <br> **[서현주](https://github.com/KKPASII)** | <img src="https://github.com/KKPASII.png" width="150" height="150" alt="서현주"/> <br> **[서현주](https://github.com/KKPASII)** |
|                                                          **메이커**                                                          |                                                        **FE 테크리더**                                                        |                                                        **BE 테크리더**                                                        |                                                           **팀 리더**                                                            |                                                        **플래너**                                                        |                                                          **메이커**                                                          |

**🎯 팀 리더 – “우리 팀장님 등장~!”**<br>
**💻 (FE/BE) 테크 리더 - “기술 담당은 나야 나.”**<br>
**⏰ 플래너 – “시간 요정 등장✨”**<br>
**📔 메이커 – “팀의 일지를 맡은 기록자!”**<br>

---
## 🎬 시연 영상
<img src="./src/main/resources/static/images/shootdoori_video.gif" alt="시연영상" width="1080"/>

---

## 🛠 기술 스택

### 💻 Language & Framework
![Java](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.5.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
### 🗄️ Database
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Railway](https://img.shields.io/badge/Railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)

### ☁️ AWS Infrastructure
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)
![AWS ELB](https://img.shields.io/badge/AWS%20ELB-FF9900?style=for-the-badge&logo=awselasticloadbalancing&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![AWS Route 53](https://img.shields.io/badge/AWS%20Route%2053-8C4FFF?style=for-the-badge&logo=amazonroute53&logoColor=white)
![AWS Certificate Manager](https://img.shields.io/badge/AWS%20Certificate%20Manager-DD344C?style=for-the-badge&logo=awscertificatemanager&logoColor=white)

### 🚀 CI/CD & Container
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

### 🔧 Tools
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

---

## 📁 프로젝트 구조

```
match/
├── client/        # 외부 API 연동 클라이언트
├── config/        # CORS 설정, 환경변수 Record 클래스
├── controller/    # REST API 컨트롤러 계층
├── dto/           # 데이터 전송 객체
├── entity/        # JPA 엔티티 클래스
├── exception/     # 커스텀 예외 및 ErrorCode 정의
├── handler/       # 이벤트 핸들러
├── notification/  # 이메일 알림 관련 컴포넌트
├── policy/        # 비즈니스 정책 및 규칙 관리
├── repository/    # 데이터 액세스 계층
├── resolver/      # GraphQL 리졸버
├── service/       # 비즈니스 로직 계층
├── util/          # JWT, SHA256 등 공통 유틸리티 함수
└── value/         # Value Object (불변 도메인 객체)
```

---

## 🔧 실행

```bash
git clone https://github.com/kakao-tech-campus-3rd-step3/Team3_BE.git
cd Team3_BE

./gradlew bootRun
```

---

## 📅 개발 일정

| 주차 | 기간 | 내용 |
|--|--|--|
| 1주차 | 2025.08.04 ~ 2025.08.10 | **🧠 서비스 기획 및 아이디어톤 진행**<br>- 서비스 콘셉트 구체화 및 핵심 타깃 정의<br>- 프로젝트 주제 및 기능 초안 구상<br>→ ✅ 서비스 아이디어 구체화 및 초기 기획 완료
| 2주차 | 2025.08.11 ~ 2025.08.17 | **🧩 서비스 기획안 고도화 및 기능 정**의<br>- 사용자 흐름 정의 (회원 → 팀 → 매칭 프로세스)<br>- 기술 스택 확정 및 ERD 초안 작성 준비<br>→ ✅ 서비스 흐름 정의 및 기술 스택 확정
| 3주차 | 2025.08.18 ~ 2025.08.24 | **📄 최종 기획안·테크스펙 작성**<br>- 백엔드 구조 설계 문서화<br>- 엔티티 관계 초안 확정 및 데이터 흐름 설계<br>→ ✅ 테크스펙 및 ERD 기반 시스템 설계 완료
| 4주차 | 2025.08.25 ~ 2025.08.31 | **🏗 깃허브 환경 셋업 및 ERD 설계**<br>- 레포 초기 세팅, 브랜치 전략 확립<br>- ERD 및 패키지 구조 설계 완료<br>→ ✅ 개발 환경 구축 및 브랜치 전략 정립
| 5주차 | 2025.09.01 ~ 2025.09.07 | **⚙️ 기초 도메인 구축 & CRUD 구현 시작**<br>- Team, Profile, Match 도메인 생성<br>- 팀 CRUD API 구현 (생성, 조회, 삭제)<br>- 프로필 CRUD + 유효성 검증 + 단위 테스트<br>- 매칭 요청/대기/확정 Entity 및 Repository 구축<br>→ ✅ 팀·프로필 CRUD 구축 및 매칭 기능 개발 시작
| 6주차 | 2025.09.08 ~ 2025.09.14 | **🤝 팀 멤버 & 리뷰 시스템 기초 구성**<br>- TeamMember, TeamReview, MercenaryReview Entity 추가<br>- 매치 생성·참여 기능 구현<br>- 용병 모집 CRUD + 단위 테스트<br>- 리뷰 서비스 CRUD 및 기본 구조 완성<br>→ ✅ 팀 멤버·리뷰 시스템 기초 구성 및 매치 기능 확장
| 7주차 | 2025.09.15 ~ 2025.09.21 | **🔐 인증·리뷰·매치 수락 흐름 완성**<br>- JWT 기반 로그인/회원가입, RefreshToken 도입<br>- 가입 요청(JoinQueue) 생성·승인·거절 기능 구현<br>- JPA Auditing 도입 및 리뷰 CRUD 완성<br>- Value Object (TeamName, UniversityName, Password) 도입<br>→ ✅ JWT 인증 + 매치 수락/거절 + VO 구조 도입 완료
| 8주차 | 2025.09.22 ~ 2025.09.28 | **🧪 도메인 안정화 및 테스트 강화**<br>- 팀 리더 위임 기능 구현 + 권한 테스트<br>- 로그아웃, RefreshToken 적용<br>- CI/CD 파이프라인 초기 구축<br>- 예외 구조 개선 및 NotFoundException 통합<br>- JPA 프록시 객체 equals() 문제 해결<br>→ ✅ 리더 권한·인증 안정화 및 도메인 테스트 강화
| 9주차 | 2025.09.29 ~ 2025.10.05 | **🚀 배포 및 고도화 단계 진입**<br>- GitHub Actions 기반 CI/CD 완성<br>- Venue(경기장) API 추가<br>- 비밀번호 찾기 기능 및 이메일 알림 도입<br>- 팀 삭제 → Soft Delete 전환<br>- HealthCheck 및 배포 헬스 모니터링 설정<br>→ ✅ CI/CD 구축 및 주요 서비스 고도화 완료
| 10주차 | 2025.10.06 ~ 2025.10.12 | **🧰 토큰·보안·Soft Delete 강화**<br>- TokenIssuer 도입으로 토큰 발급 로직 분리<br>- 대학 이메일 검증 API 보안 강화<br>- 팀 복구 API 및 테스트 코드 추가<br>- Soft Delete → 복원 가능한 구조로 확장<br>→ ✅ 보안 및 데이터 복구 구조 개선 완료
| 11주차 | 2025.10.13 ~ 2025.10.19 | **🧠 스케줄러 및 인증 안정화**<br>- 매치 스케줄러 도입 (3시간 후 자동 FINISHED)<br>- JoinWaiting 이메일 알림 고도화<br>- HttpOnly 쿠키 인증 전환<br>- Soft Delete 회원탈퇴 적용<br>- Actuator 기반 헬스체크 추가<br>→ ✅ 인증 안정화 및 운영 환경 점검 체계 완성
| 12주차 | 2025.10.20 ~ 2025.10.26 | **🧩 라인업 시스템 및 도메인 구조 개선**<br>- Lineup Entity 및 CRUD 구현<br>- Entity 상속 → 합성 구조로 리팩토링<br>- TeamMembers 일급 컬렉션 도입<br>- PasswordEncoder 구조 개선 및 주입 방식 통합<br>→ ✅ 라인업 시스템 구축 및 도메인 구조 리팩토링 완료
| 13주차 | 2025.10.27 ~ 2025.11.02 | **⚡ 성능 및 피드백 반영 주간**<br>- 커서 페이징 도입 (팀 멤버 조회 개선)<br>- 용병/매치 응답 구조 개선<br>- 피드백 보완 및 코드 품질 개선<br>- 코드 컨벤션 통일<br>→ ✅ 성능 최적화 및 피드백 반영 완료
| 14주차 | 2025.11.03 ~ 2025.11.09 | **🧾 프론트 연동 및 안정화 마무리**<br>- 프론트 피드백 반영 (라인업 Entity에 teamId, userName 추가)<br>- 팀 멤버 삭제 시 라인업 연관 문제 해결 (하드 삭제 적용)<br>- 전체 API 검증 및 버그 픽스<br>- 최종 안정화 및 발표 준비<br>→ ✅ 프론트 연동 및 최종 배포 완료

---

## 🧩 서비스 아키텍처

<img src="./src/main/resources/static/images/shootdoori_architecture.png" alt="서비스 아키텍처" width="1080"/>


---

## 📱 앱 다운로드

[![Google Play Store](https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps)

👉 **[앱 다운로드하기](https://play.google.com/store/apps/details?id=com.cheogo.shootdoori&hl=ko)**

<img src="./src/main/resources/static/images/shootdoori_playstore.png" alt="플레이스토어 이미지" width="300"/>