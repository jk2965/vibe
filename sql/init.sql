-- Oracle DB 초기화 스크립트
-- 사용자: VIBE / 비밀번호: vibe123
-- PDB: FREEPDB1

-- 유저 테이블
CREATE TABLE vibe.users (
    id       VARCHAR2(100) NOT NULL PRIMARY KEY,
    username VARCHAR2(100) NOT NULL,
    password VARCHAR2(100) NOT NULL
);

-- 출퇴근 기록 테이블
CREATE TABLE vibe.attendance_records (
    id             VARCHAR2(100) NOT NULL,
    username       VARCHAR2(100) NOT NULL,
    check_in_time  VARCHAR2(30),
    check_out_time VARCHAR2(30)
);
