-- Oracle DB 초기화 스크립트 (현재 코드 기준 최신)
-- 사용자: VIBE / 비밀번호: vibe123
-- PDB: FREEPDB1
-- 주의: content VARCHAR2(32767) 사용을 위해 MAX_STRING_SIZE=EXTENDED 설정 필요

-- ── 통합 게시판 테이블 ───────────────────────────────────────────────
-- board_type: FREE(자유), NOTICE(전사공지), TEAM_NOTICE(팀공지),
--             ARCHIVE(전사자료실), TEAM_ARCHIVE(팀자료실), FAQ, QNA
CREATE TABLE vibe.boards (
    id          VARCHAR2(100)    NOT NULL PRIMARY KEY,
    board_type  VARCHAR2(30)     NOT NULL,
    title       VARCHAR2(500)    NOT NULL,
    content     VARCHAR2(32767),
    author_id   VARCHAR2(100)    NOT NULL,
    author_name VARCHAR2(150)    NOT NULL,
    created_at  VARCHAR2(30),
    views       NUMBER(10)       DEFAULT 0,
    is_required NUMBER(1)        DEFAULT 0,
    tags        VARCHAR2(500),
    team        VARCHAR2(100)
);

-- ── 댓글 테이블 ──────────────────────────────────────────────────────
-- parent_id: 대댓글인 경우 부모 댓글 ID, is_deleted: 소프트 딜리트
CREATE TABLE vibe.board_comment (
    id          VARCHAR2(100)    NOT NULL PRIMARY KEY,
    board_id    VARCHAR2(100)    NOT NULL,
    author_id   VARCHAR2(100)    NOT NULL,
    author_name VARCHAR2(150)    NOT NULL,
    content     VARCHAR2(32767)  NOT NULL,
    created_at  VARCHAR2(30),
    parent_id   VARCHAR2(100),
    is_deleted  NUMBER(1)        DEFAULT 0
);

-- ── 첨부파일 테이블 ──────────────────────────────────────────────────
-- 모든 게시판 유형 공유 (board_id로 연결)
CREATE TABLE vibe.archive_file (
    id            VARCHAR2(100) NOT NULL PRIMARY KEY,
    board_id      VARCHAR2(100) NOT NULL,
    original_name VARCHAR2(500),
    stored_name   VARCHAR2(500),
    file_size     NUMBER(20),
    created_at    VARCHAR2(30),
    uploader_id   VARCHAR2(100)
);

-- ── 사용자 테이블 ────────────────────────────────────────────────────
CREATE TABLE vibe.users (
    id                 VARCHAR2(100) NOT NULL PRIMARY KEY,
    username           VARCHAR2(100) NOT NULL,
    password           VARCHAR2(100) NOT NULL,
    position           VARCHAR2(50)  DEFAULT '사원',
    remaining_vacation NUMBER(5,1)   DEFAULT 15,
    is_admin           NUMBER(1)     DEFAULT 0,
    team               VARCHAR2(100),
    is_team_leader     NUMBER(1)     DEFAULT 0
);

-- ── Q&A 답변 테이블 ──────────────────────────────────────────────────
CREATE TABLE vibe.qna_answer (
    id          VARCHAR2(100)   NOT NULL PRIMARY KEY,
    qna_id      VARCHAR2(100)   NOT NULL,
    content     VARCHAR2(32767) NOT NULL,
    author_id   VARCHAR2(100)   NOT NULL,
    author_name VARCHAR2(150)   NOT NULL,
    created_at  VARCHAR2(30)
);

-- ── 휴가 테이블 ──────────────────────────────────────────────────────
-- VacationMapper.xml에서 prefix 없이 참조 (vibe 사용자 기본 스키마)
CREATE TABLE vacation (
    id         VARCHAR2(100) NOT NULL PRIMARY KEY,
    user_id    VARCHAR2(100) NOT NULL,
    start_date VARCHAR2(20)  NOT NULL,
    end_date   VARCHAR2(20)  NOT NULL,
    type       VARCHAR2(50)  NOT NULL,
    reason     VARCHAR2(500),
    created_at VARCHAR2(30)
);

-- ── 일정 테이블 ──────────────────────────────────────────────────────
CREATE TABLE vibe.schedule (
    id          VARCHAR2(100)  NOT NULL PRIMARY KEY,
    title       VARCHAR2(200)  NOT NULL,
    start_date  VARCHAR2(30)   NOT NULL,
    end_date    VARCHAR2(30),
    color       VARCHAR2(30),
    user_id     VARCHAR2(100)  NOT NULL,
    team        VARCHAR2(100),
    description VARCHAR2(1000),
    created_at  VARCHAR2(30)
);

-- ── 출퇴근 기록 테이블 ───────────────────────────────────────────────
CREATE TABLE vibe.attendance_records (
    id             VARCHAR2(100) NOT NULL,
    username       VARCHAR2(100) NOT NULL,
    check_in_time  VARCHAR2(30),
    check_out_time VARCHAR2(30)
);
