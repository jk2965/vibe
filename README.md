# Vibe — 사내 인트라넷 시스템

Spring Boot 3 + MyBatis + Vue.js 3 기반의 사내 인트라넷 시스템.
게시판, 공지사항, 자료실, Q&A, FAQ, 출퇴근, 휴가, 일정 관리 기능을 제공합니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 3.0, MyBatis, Oracle DB, Lombok |
| Frontend | Vue.js 3, Tiptap Editor |
| Build | Maven |

---

## 버전 히스토리

### v1.0.0 — 프로젝트 초기 구성 `2026-03-12`
- 프로젝트 기본 구조 생성 (Spring Boot + Vue.js 3)
- Oracle DB SQL 초기화 스크립트 추가

---

### v1.1.0 — 핵심 기능 추가 `2026-03-16`
- 홈 화면 구성
- 출퇴근 기록 기능 (출근/퇴근 시각 기록)
- 휴가 신청·조회 기능
- 인사 관리 (HR) 기능
- 자유게시판 기본 기능 (작성·조회·삭제)
- 댓글 기능

---

### v1.2.0 — 공지사항 게시판 추가 `2026-03-17`
- 전사 공지사항 게시판 (관리자 전용 작성)
- 팀 공지사항 게시판 (팀별 공지 분리)
- HR 권한 개선

---

### v1.3.0 — 자료실·에디터·파일 기능 추가 `2026-03-17`
- 전사 자료실 / 팀 자료실 게시판 추가
- Tiptap 리치 텍스트 에디터 적용
- 파일 첨부·다운로드 기능
- 파일 접근 권한 처리
- 컴포넌트 구조 리팩토링

---

### v1.4.0 — FAQ 게시판·에디터 확장 `2026-03-18`
- FAQ(자주 묻는 질문) 게시판 추가
- 에디터 코드 블록 문법 하이라이팅 지원
- 에디터 하이퍼링크 지원

---

### v1.5.0 — 캘린더·일정 관리 추가 `2026-03-18`
- 캘린더 UI 및 일정 등록·조회 기능
- 팀 공유 일정 / 개인 일정 구분
- 팀 기반 일정 필터링
- 프론트엔드 컴포넌트 구조 리팩토링

---

### v1.6.0 — 전체 주석 추가 `2026-03-19`
- 백엔드 전체 소스 한국어 주석 추가
- 프론트엔드 전체 소스 한국어 주석 추가

---

### v1.7.0 — 필독 기능·사이드바·게시글 작성 모달 `2026-03-19`
- 게시글 필독 설정 기능 추가
- 사이드바 네비게이션 개선
- 게시글 작성 모달 UI 추가
- 필독 설정 권한을 일반 사용자도 사용 가능하도록 변경

---

### v1.8.0 — 내 게시글 페이지 추가 `2026-03-19`
- 내가 작성한 게시글 목록 페이지 추가
- 사이드바 메뉴 구성 개선

---

### v1.9.0 — 태그 기능 추가 `2026-03-20`
- 자유게시판·공지사항·자료실·팀 게시판 전체에 태그 기능 추가
- 게시글 작성·수정·상세 화면에 태그 입력·표시 적용

---

### v1.10.0 — 검색·페이지네이션·UI 통일 `2026-03-20`
- 게시판 전체 검색 기능 추가 (제목·내용·태그 검색)
- 페이지네이션 적용
- 게시판 UI 스타일 통일

---

### v1.11.0 — FAQ 파일 첨부 기능 추가 `2026-03-23`
- FAQ 게시판 파일 첨부·다운로드 지원

---

### v1.12.0 — 테이블/차트 에디터·Q&A 게시판 추가 `2026-03-23`
- 에디터 테이블 삽입 기능 추가
- 에디터 차트 삽입 기능 추가
- Q&A 게시판 추가 (질문·답변 구조)
- 게시판 목록에 파일 첨부 정보 표시

---

### v1.13.0 — 백엔드 디자인 패턴 적용·리팩토링 `2026-03-31`
- **단일 테이블 상속**: 7개 게시판을 `vibe.boards` 단일 테이블로 통합 (`board_type` 컬럼으로 구분)
- **Template Method Pattern**: `AbstractBoardService<T>`, `AbstractBoardController<T>` 추상 클래스로 공통 CRUD 로직 통합
- **Builder Pattern (CRTP)**: 모든 게시판 VO에 계층형 Builder 적용
- **Facade Pattern**: `RequiredController`, `MyPostsController`, `SearchController` 도입
- **Shared Service Pattern**: `FileService`로 파일 처리 로직 중앙화
- 전체 Javadoc 주석 추가

---

### v1.14.0 — Lombok 적용 `2026-04-01`
- `pom.xml`에 Lombok 의존성 추가
- `AbstractBoardVO` + 7개 게시판 VO: `@SuperBuilder` `@Getter` `@Setter` `@NoArgsConstructor` 적용, 수동 Builder 클래스 제거
- 나머지 8개 VO (`QnaAnswerVO`, `ArchiveFileVO`, `CommentVO`, `UserVO`, `VacationVO`, `ScheduleVO`, `AttendanceRecordVO`, `AttendanceRecordSearchParamVO`): `@Getter` `@Setter` `@NoArgsConstructor` 적용, 수동 getter/setter 제거
- 보일러플레이트 코드 약 450줄 제거
- Lombok 어노테이션 설명 주석 추가

---

### v1.15.0 — Oracle DB 재구성 및 Q&A 사이드바 추가 `2026-04-27`
- Oracle `MAX_STRING_SIZE=EXTENDED` 마이그레이션 스크립트 추가 (`sql/extended_setup.sql`, `sql/migrate.sh`)
- `sql/init.sql` 재작성: 현재 Mapper 구조 기반 전체 테이블 스키마 정비, `VARCHAR2(32767)` 적용
- Q&A 게시판 경로(`/qna`)를 사이드바 표시 경로 목록에 추가
