package com.vibe.model;

/**
 * 직원 휴가 신청 정보를 담는 Value Object 클래스.
 * VacationMapper.java, VacationService.java에서 사용되며,
 * userId는 UserVO.java의 id 필드와 연결됨.
 * 휴가 승인/차감은 UserVO.java의 remainingVacation 필드와 연동됨.
 */
public class VacationVO {
    /** 휴가 신청 고유 식별자 */
    private String id;
    /** 휴가 신청 직원의 사용자 ID (UserVO.java의 id 참조) */
    private String userId;
    /** 휴가 시작일 (문자열 형식, 예: "2026-03-20") */
    private String startDate;
    /** 휴가 종료일 (문자열 형식, 예: "2026-03-21") */
    private String endDate;
    /** 휴가 유형 (예: 연차, 반차, 병가 등) */
    private String type;
    /** 휴가 신청 사유 */
    private String reason;
    /** 휴가 신청 일시 (문자열 형식) */
    private String createdAt;

    /** id 필드 반환 */
    public String getId() { return id; }
    /** id 필드 설정 */
    public void setId(String id) { this.id = id; }
    /** userId 필드 반환 */
    public String getUserId() { return userId; }
    /** userId 필드 설정 */
    public void setUserId(String userId) { this.userId = userId; }
    /** startDate 필드 반환 */
    public String getStartDate() { return startDate; }
    /** startDate 필드 설정 */
    public void setStartDate(String startDate) { this.startDate = startDate; }
    /** endDate 필드 반환 */
    public String getEndDate() { return endDate; }
    /** endDate 필드 설정 */
    public void setEndDate(String endDate) { this.endDate = endDate; }
    /** type 필드 반환 */
    public String getType() { return type; }
    /** type 필드 설정 */
    public void setType(String type) { this.type = type; }
    /** reason 필드 반환 */
    public String getReason() { return reason; }
    /** reason 필드 설정 */
    public void setReason(String reason) { this.reason = reason; }
    /** createdAt 필드 반환 */
    public String getCreatedAt() { return createdAt; }
    /** createdAt 필드 설정 */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
