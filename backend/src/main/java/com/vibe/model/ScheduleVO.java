package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일정(캘린더) 정보를 담는 Value Object 클래스.
 * ScheduleMapper.java, ScheduleService.java에서 사용되며,
 * userId는 UserVO.java의 id 필드와 연결됨.
 * team 필드로 팀 공유 일정과 개인 일정을 구분하며,
 * UserVO.java의 team 필드로 팀 필터링이 적용됨.
 */
@Getter            // 모든 필드 getter 자동 생성
@Setter            // 모든 필드 setter 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성 — MyBatis ResultMap 매핑에 필수
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
}
