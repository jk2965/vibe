-- Oracle DB 초기화 스크립트
-- 사용자: VIBE / 비밀번호: vibe123
-- PDB: FREEPDB1

-- 유저 테이블
CREATE TABLE vibe.users (
    id                 VARCHAR2(100) NOT NULL PRIMARY KEY,
    username           VARCHAR2(100) NOT NULL,
    password           VARCHAR2(100) NOT NULL,
    position           VARCHAR2(50)  DEFAULT '사원',
    remaining_vacation NUMBER(5,1)   DEFAULT 15,
    is_admin           NUMBER(1)     DEFAULT 0
);

-- 기존 테이블에 컬럼 추가 (이미 테이블이 있는 경우)
-- ALTER TABLE vibe.users ADD (position VARCHAR2(50) DEFAULT '사원');
-- ALTER TABLE vibe.users ADD (remaining_vacation NUMBER(5,1) DEFAULT 15);
-- ALTER TABLE vibe.users ADD (is_admin NUMBER(1) DEFAULT 0);
-- 관리자 계정 설정: UPDATE vibe.users SET is_admin = 1 WHERE id = '관리자아이디';

-- 출퇴근 기록 테이블
CREATE TABLE vibe.attendance_records (
    id             VARCHAR2(100) NOT NULL,
    username       VARCHAR2(100) NOT NULL,
    check_in_time  VARCHAR2(30),
    check_out_time VARCHAR2(30)
);

-- 댓글 테이블
CREATE TABLE vibe.board_comment (
    id          VARCHAR2(100)  NOT NULL PRIMARY KEY,
    board_id    VARCHAR2(100)  NOT NULL,
    author_id   VARCHAR2(100)  NOT NULL,
    author_name VARCHAR2(150)  NOT NULL,
    content     VARCHAR2(1000) NOT NULL,
    created_at  VARCHAR2(30)
);

-- 게시판 테이블
CREATE TABLE vibe.board (
    id          VARCHAR2(100)  NOT NULL PRIMARY KEY,
    title       VARCHAR2(200)  NOT NULL,
    content     CLOB           NOT NULL,
    author_id   VARCHAR2(100)  NOT NULL,
    author_name VARCHAR2(150)  NOT NULL,
    created_at  VARCHAR2(30),
    views       NUMBER(10)     DEFAULT 0
);

-- 휴가 테이블
CREATE TABLE vibe.vacation (
    id         VARCHAR2(100) NOT NULL PRIMARY KEY,
    user_id    VARCHAR2(100) NOT NULL,
    start_date VARCHAR2(20)  NOT NULL,
    end_date   VARCHAR2(20)  NOT NULL,
    type       VARCHAR2(50)  NOT NULL,
    reason     VARCHAR2(500),
    created_at VARCHAR2(30)
);

-- FAQ 테이블 (title=질문, content=답변)
CREATE TABLE vibe.faq (
    id           VARCHAR2(100)    NOT NULL PRIMARY KEY,
    title        VARCHAR2(500)    NOT NULL,
    content      VARCHAR2(32767),
    author_id    VARCHAR2(100)    NOT NULL,
    author_name  VARCHAR2(150)    NOT NULL,
    created_at   VARCHAR2(30),
    views        NUMBER(10)       DEFAULT 0
);

-- 일정 테이블
CREATE TABLE vibe.schedule (
    id          VARCHAR2(100)   NOT NULL PRIMARY KEY,
    title       VARCHAR2(200)   NOT NULL,
    start_date  VARCHAR2(30)    NOT NULL,
    end_date    VARCHAR2(30),
    color       VARCHAR2(30),
    user_id     VARCHAR2(100)   NOT NULL,
    description VARCHAR2(1000),
    created_at  VARCHAR2(30)
);

-- schedule 테이블 team 컬럼 추가 (기존 테이블 있는 경우)
-- ALTER TABLE vibe.schedule ADD (team VARCHAR2(100));
