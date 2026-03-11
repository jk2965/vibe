# 회원가입 시스템

Vue.js + Node.js(BFF) + Spring Boot + H2 DB로 구성된 회원가입 풀스택 애플리케이션입니다.

## 아키텍처

```
[Vue.js :5173] → [Node.js BFF :3000] → [Spring Boot :8080] → [H2 DB]
```

## 실행 방법

### 1. Spring Boot 백엔드 (포트 8080)
```bash
cd backend
./mvnw spring-boot:run
```
- H2 콘솔: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:registrationdb`
  - Username: `sa` / Password: (없음)

### 2. Node.js 서버 (포트 3000)
```bash
cd server
npm install
npm start
```

### 3. Vue.js 프론트엔드 (포트 5173)
```bash
cd frontend
npm install
npm run dev
```

브라우저에서 http://localhost:5173 접속

## API 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/users/register` | 회원가입 |
| GET | `/api/users/check-email?email=` | 이메일 중복 확인 |

## 기능

- 이름, 이메일, 비밀번호, 전화번호 입력 폼
- 실시간 입력 유효성 검사
- 이메일 중복 확인
- 비밀번호 강도 표시
- 회원가입 성공 화면
