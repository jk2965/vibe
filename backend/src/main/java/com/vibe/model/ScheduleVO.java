package com.vibe.model;

/**
 * 일정(캘린더) 정보를 담는 Value Object 클래스.
 * ScheduleMapper.java, ScheduleService.java에서 사용되며,
 * userId는 UserVO.java의 id 필드와 연결됨.
 * team 필드로 팀 공유 일정과 개인 일정을 구분하며,
 * UserVO.java의 team 필드로 팀 필터링이 적용됨.
 */
public class ScheduleVO {
    /** 일정 고유 식별자 */
    private String id;
    /** 일정 제목 */
    private String title;
    /** 일정 시작 날짜 (문자열 형식, 예: "2026-03-20") */
    private String startDate;
    /** 일정 종료 날짜 (문자열 형식, 예: "2026-03-21") */
    private String endDate;
    /** 캘린더에 표시할 색상 코드 (예: "#FF5733") */
    private String color;
    /** 일정 등록자 사용자 ID (UserVO.java의 id 참조) */
    private String userId;
    /** 일정 공유 대상 팀 이름 (UserVO.java의 team 필드와 일치, 개인 일정은 null) */
    private String team;
    /** 일정 상세 설명 */
    private String description;
    /** 일정 생성 일시 (문자열 형식) */
    private String createdAt;

    /** id 필드 반환 */
    public String getId() { return id; }
    /** id 필드 설정 */
    public void setId(String id) { this.id = id; }
    /** title 필드 반환 */
    public String getTitle() { return title; }
    /** title 필드 설정 */
    public void setTitle(String title) { this.title = title; }
    /** startDate 필드 반환 */
    public String getStartDate() { return startDate; }
    /** startDate 필드 설정 */
    public void setStartDate(String startDate) { this.startDate = startDate; }
    /** endDate 필드 반환 */
    public String getEndDate() { return endDate; }
    /** endDate 필드 설정 */
    public void setEndDate(String endDate) { this.endDate = endDate; }
    /** color 필드 반환 */
    public String getColor() { return color; }
    /** color 필드 설정 */
    public void setColor(String color) { this.color = color; }
    /** userId 필드 반환 */
    public String getUserId() { return userId; }
    /** userId 필드 설정 */
    public void setUserId(String userId) { this.userId = userId; }
    /** team 필드 반환 */
    public String getTeam() { return team; }
    /** team 필드 설정 */
    public void setTeam(String team) { this.team = team; }
    /** description 필드 반환 */
    public String getDescription() { return description; }
    /** description 필드 설정 */
    public void setDescription(String description) { this.description = description; }
    /** createdAt 필드 반환 */
    public String getCreatedAt() { return createdAt; }
    /** createdAt 필드 설정 */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
