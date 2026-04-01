package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 직원 휴가 신청 정보를 담는 Value Object 클래스.
 * VacationMapper.java, VacationService.java에서 사용되며,
 * userId는 UserVO.java의 id 필드와 연결됨.
 * 휴가 승인/차감은 UserVO.java의 remainingVacation 필드와 연동됨.
 */
@Getter
@Setter
@NoArgsConstructor
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
}
