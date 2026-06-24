# 📚 도서관리시스템 서버 개발



## 프로젝트 개요

**걸어서 서재 속으로** 백엔드는 Spring Boot 기반의 도서 관리 시스템 API 서버입니다.

Spring Data JPA를 활용하여 도서 CRUD, 회원 관리, 좋아요, 댓글, AI 이미지 생성, AI 한줄평, 사용자 통계 기능을 제공하는 REST API를 구현하였습니다.

## 기술 스택

### Frontend
- React 19 · Vite ·fetchBackend

### Backend
- java 17.0
- Spring Boot 4.0
- Spring MVC 
- Spring Data JPA
- Lombok
- MySQL 8.0

### AI
- OpenAI API (GPT Image 모델)

### Tools
- IntelliJ IDEA
- GitHub 
- Postman
- Notion
- Swagger



## 팀원 구성

- 박유경 : PM·기획, 도서 검색/필터 구현, ERD 작성 및 연관관계 매핑

- 김완수 : PPT 작성, AI 한줄평 생성, 통합 예외 처리
  
- 박선호 : 발표, 댓글 CRUD 구현, 댓글 예외 처리
  
- 박형우 : 검토 담당자, AI 이미지 생성, 요청/응답 DTO 적용
  
- 신가람 : 검토 담당자, Swagger 적용, 도서 생성/조회 구현
  
- 심유리 : 도서 등록/통계 대시보드 구현, BookService 예외 처리
  
- 윤빈 : PPT 작성, 로그인/회원가입 프론트 구현, 마이페이지 구현
  
- 최지흠 : MySQL 연동, Front/Back QA, 도서 수정/삭제 구현

- 한승연 : 서기, 로그인/회원가입 API 구현, Book Entity 설계 및 ERD 작성



## 기능 소개

### 회원 기능
- 회원가입
- 로그인
- 로그아웃
- 마이페이지
- 관리자/회원/비회원 역할 분리

### 도서 관리
- 새 도서 등록
- 도서 목록 조회
- 도서 수정
- 도서 삭제 (휴지통으로 이동)

### 후기 관리
- 도서 후기 댓글 등록
- 도서 후기 댓글 수정
- 도서 후기 댓글 삭제
- 도서 좋아요 수 증가

### AI 
- AI 표지 이미지 생성
- AI 내용 요약 한줄평 생성

### 통계 조회
- 도서 수 기반 장르/태그 통계 
- 좋아요 수 기반 장르/태그 통계
- 도서 월간/일간 등록 수 통계

### 휴지통 관리
- 삭제된 도서 관리
- 휴지통 도서 영구 삭제

### 예외 처리
- 회원가입 및 로그인 관련 예외 처리
  - 존재하지 않는 이메일
  - 중복 이메일
  - 비밀번호 불일치
- 필수 입력값 검증
- 잘못된 요청 파라미터 검증
- 존재하지 않는 도서 조회/수정/삭제 예외처리
- 존재하지 않는 댓글 조회/수정 예외처리
- OpenAI API 호출 및 이미지 생성 예외처리

## Screenshot

- 도서 메인페이지 UI

<img width="640" height="349" alt="Image" src="https://github.com/user-attachments/assets/4f1f84a4-3ab2-4571-9b84-30f1f856b754" />

- 도서 등록페이지 UI

<img width="428" height="625" alt="Image" src="https://github.com/user-attachments/assets/0757bdc4-77ea-4f90-a97c-61e0590ca517" />

- 도서 목록페이지 UI

<img width="640" height="349" alt="Image" src="https://github.com/user-attachments/assets/e2cf5065-a57a-42c5-b99c-ef65f72ca5b4" />

- 도서 수정페이지 UI

<img width="640" height="334" alt="Image" src="https://github.com/user-attachments/assets/14ce77f1-c4f3-449c-b7a0-098b07e529d4" />

- 도서 삭제 페이지 UI

<img width="640" height="362" alt="Image" src="https://github.com/user-attachments/assets/4d0b9076-9a6c-4bef-93b8-8f8d78c8cfb6" />

- 도서 통계 페이지 UI

<img width="640" height="313" alt="Image" src="https://github.com/user-attachments/assets/1501908b-4357-4df4-b23e-c50df0bcdf4d" />

- 휴지통 UI

<img width="640" height="338" alt="Image" src="https://github.com/user-attachments/assets/3d6c192d-c34f-4fb0-821a-5b675119be30" />

## 설치 방법

git clone https://github.com/AIVLE-9th-mini-project-book-service/BackEnd.git

cd BackEnd

## 실행 방법

### IntelliJ IDEA
- IntelliJ IDEA 실행
- BackEnd 프로젝트 열기
- Gradle 의존성 다운로드 완료 대기
- BookappApplication.java 실행

### 터미널 실행
gradlew.bat bootRun

### API 서버
http://localhost:8080



## 프로젝트 구조

```text
src/
├── main/
│   ├── java/
│   │   └── com/aivle/bookapp/
│   │       ├── config/
│   │       │   ├── SecurityConfig.java
│   │       │   ├── SwaggerConfig.java
│   │       │   └── WebConfig.java
│   │       ├── controller/
│   │       │   ├── AdminController.java
│   │       │   ├── BookController.java
│   │       │   ├── CommentController.java
│   │       │   └── MemberController.java
│   │       ├── domain/
│   │       │   ├── Book.java
│   │       │   ├── BookTag.java
│   │       │   ├── Comment.java
│   │       │   └── Member.java
│   │       ├── dto/
│   │       │   ├── AiBookSummaryRequest.java
│   │       │   ├── AiBookSummaryResponse.java
│   │       │   ├── BookCountStatisticsResponse.java
│   │       │   ├── BookCreateRequest.java
│   │       │   ├── BookResponse.java
│   │       │   ├── BookSearchRequest.java
│   │       │   ├── BookSearchResponse.java
│   │       │   ├── BookUpdateRequest.java
│   │       │   ├── CommentCreateRequest.java
│   │       │   ├── CommentResponse.java
│   │       │   ├── CommentUpdateRequest.java
│   │       │   ├── CoverImageUpdateRequest.java
│   │       │   ├── GenerateCoverRequest.java
│   │       │   ├── GenerateCoverResponse.java
│   │       │   ├── LikesStatisticsResponse.java
│   │       │   ├── LoginRequest.java
│   │       │   ├── LoginResponse.java
│   │       │   ├── MessageResponse.java
│   │       │   ├── SignupRequest.java
│   │       │   ├── SignupResponse.java
│   │       │   └── SummaryUpdateRequest.java
│   │       ├── exception/
│   │       │   ├── BookNotFoundException.java
│   │       │   ├── CommentNotFoundException.java
│   │       │   ├── DuplicateEmailException.java
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── InvalidPasswordException.java
│   │       │   ├── MemberNotFoundException.java
│   │       │   └── OpenAiException.java
│   │       ├── filter/
│   │       │   └── JwtFilter.java
│   │       ├── repository/
│   │       │   ├── BookRepository.java
│   │       │   ├── CommentRepository.java
│   │       │   └── MemberRepository.java
│   │       ├── service/
│   │       │   ├── BookCoverImage.java
│   │       │   ├── BookService.java
│   │       │   ├── CommentService.java
│   │       │   └── MemberService.java
│   │       ├── util/
│   │       │   └── JwtUtil.java
│   │       └── BookappApplication.java
│   └── resources/
│       ├── application.yaml
│       ├── data.sql
│       └── mysql_mini5.sql
└── test/
```

### 주요 디렉토리 설명
- config : 보안 및 환경 설정
- controller : API 요청 처리
- domain : Entity 관리
- dto : 요청 및 응답 객체
- exception : 예외처리
- repository : 데이터베이스 접근
- service : 비즈니스 로직 처리

## 시스템 아키텍처

<img width="677" height="240" alt="Image" src="https://github.com/user-attachments/assets/68e60a5d-0305-4ec6-a3f1-4cbcfe4a7a72" />


## 트러블 슈팅
### 1. 프론트엔드/백엔드 Path 중복 문제

- Before

  <img width="1279" height="764" alt="Image" src="https://github.com/user-attachments/assets/ff08b0db-19d2-4f6f-9402-9ba586909da6" />

  - 기존에는 프론트엔드와 백엔드가 같은 path를 사용
  - 새로고침 시 백엔드와 직접 통신
  - Controller가 요청을 받아 JSON 반환
  - 프론트엔드 화면이 아닌 JSON 문자열이 그대로 출력되는 문제 발생

- After

  <img width="2539" height="1451" alt="Image" src="https://github.com/user-attachments/assets/f2eaeba5-d866-47c7-a39a-f20777079340" />

  - yaml에 servelt.path: /api 설정 -> /api prefix 설정
  - vite.config에 '/api': 'http://localhost:8080' 설정
  - 프록시를 통한 CORS 오류 해결


### 2. 댓글 접근 시 관리자 인증 문제
- Before

  <img width="443" height="236" alt="Image" src="https://github.com/user-attachments/assets/e31e6825-e78b-43d4-a3fa-741e682f7fb8" />

  - 기존에는 관리자와 일반 유저 구분 없이 동일한 로직 사용
  - 모든 수정/삭제 요청에 checkOwner() 적용
  - 관리자 토큰의 email이 admin이라 본인 소유 아님으로 판단
  - 관리자도 403 Forbidden 에러 발생

- After

  <img width="452" height="278" alt="Image" src="https://github.com/user-attachments/assets/0b917374-71cf-40ec-93a4-d4a983816aa1" />

  - JWT claim에 role: ADMIN 추가
  - 관리자 전용 API 별도 생성 (/admin/books/{id}, /admin/comments/{id})
  - checkOwner() 없이 모든 도서/댓글 수정삭제 가능
  - 일반 유저 API는 기존 checkOwner() 로직 유지
