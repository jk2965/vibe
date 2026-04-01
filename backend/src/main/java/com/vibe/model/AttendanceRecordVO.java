package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 직원 출퇴근 기록 정보를 담는 Value Object 클래스.
 * AttendanceRecordMapper.java, AttendanceRecordService.java에서 사용되며,
 * username은 UserVO.java의 username 필드와 연결됨.
 * 검색 조건은 AttendanceRecordSearchParamVO.java 참조.
 */
@Getter            // 모든 필드 getter 자동 생성
@Setter            // 모든 필드 setter 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성 — MyBatis ResultMap 매핑에 필수
public class AttendanceRecordVO {

    /** 출퇴근 기록 고유 식별자 */
    private String id;
    /** 출퇴근 기록 대상 직원 이름 (UserVO.java의 username 참조) */
    private String username;
    /** 출근 시각 (문자열 형식, 예: "2026-03-19 09:00:00") */
    private String checkInTime;
    /** 퇴근 시각 (문자열 형식, 미퇴근 시 null) */
    private String checkOutTime;
}
