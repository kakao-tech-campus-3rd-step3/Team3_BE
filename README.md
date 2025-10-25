# Shoot Doori BE ⚽

**혼자가 아닌 함께, 실력 맞춤 지역 스포츠 매칭 플랫폼**

## 🛠 기술 스택

- **Backend**: Java 17, Spring Boot, JPA
- **Database**: MySQL

## 📁 프로젝트 구조

```
match/
├── client/        # 외부 API 연동 클라이언트
├── config/        # CORS 설정, 환경변수 Record 클래스
├── controller/    # REST API 컨트롤러 계층
├── dto/           # 데이터 전송 객체
├── entity/        # JPA 엔티티 클래스
├── handler/       # 이벤트 핸들러
├── exception/     # 커스텀 예외 및 ErrorCode 정의
├── notification/  # 이메일 알림 관련 컴포넌트
├── repository/    # 데이터 액세스 계층
├── resolver/      # GraphQL 리졸버
├── service/       # 비즈니스 로직 계층
└── util/          # JWT, SHA256 등 공통 유틸리티 함수
```

## 🔧 실행

```bash
git clone https://github.com/kakao-tech-campus-3rd-step3/Team3_BE.git
cd Team3_BE

./gradlew bootRun
```
