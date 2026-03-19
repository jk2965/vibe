package com.vibe.model;

/**
 * 직원 출퇴근 기록 정보를 담는 Value Object 클래스.
 * AttendanceRecordMapper.java, AttendanceRecordService.java에서 사용되며,
 * username은 UserVO.java의 username 필드와 연결됨.
 * 검색 조건은 AttendanceRecordSearchParamVO.java 참조.
 */
public class AttendanceRecordVO {

    /** 출퇴근 기록 고유 식별자 */
    private String id;
    /** 출퇴근 기록 대상 직원 이름 (UserVO.java의 username 참조) */
    private String username;
    /** 출근 시각 (문자열 형식, 예: "2026-03-19 09:00:00") */
    private String checkInTime;
    /** 퇴근 시각 (문자열 형식, 미퇴근 시 null) */
    private String checkOutTime;

    /** id 필드 반환 */
    public String getId() { return id; }
    /** id 필드 설정 */
    public void setId(String id) { this.id = id; }
    /** username 필드 반환 */
    public String getUsername() { return username; }
    /** username 필드 설정 */
    public void setUsername(String username) { this.username = username; }
    /** checkInTime 필드 반환 */
    public String getCheckInTime() { return checkInTime; }
    /** checkInTime 필드 설정 */
    public void setCheckInTime(String checkInTime) { this.checkInTime = checkInTime; }
    /** checkOutTime 필드 반환 */
    public String getCheckOutTime() { return checkOutTime; }
    /** checkOutTime 필드 설정 */
    public void setCheckOutTime(String checkOutTime) { this.checkOutTime = checkOutTime; }
}
