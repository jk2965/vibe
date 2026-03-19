package com.vibe.model;

/**
 * 출퇴근 기록 조회 시 검색 조건을 담는 Value Object 클래스.
 * AttendanceRecordMapper.java에서 검색 쿼리의 파라미터로 사용되며,
 * AttendanceRecordService.java에서 조건 생성 후 전달됨.
 * 조회 결과는 AttendanceRecordVO.java 리스트로 반환됨.
 */
public class AttendanceRecordSearchParamVO {

    /** 검색 대상 직원 이름 (UserVO.java의 username 참조, null이면 전체 조회) */
    private String username;
    /** 조회 시작 날짜 (문자열 형식, 예: "2026-03-01") */
    private String fromDate;

    /** username 필드 반환 */
    public String getUsername() { return username; }
    /** username 필드 설정 */
    public void setUsername(String username) { this.username = username; }
    /** fromDate 필드 반환 */
    public String getFromDate() { return fromDate; }
    /** fromDate 필드 설정 */
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }
}
