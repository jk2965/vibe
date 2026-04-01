package com.vibe.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자(직원) 정보를 담는 Value Object 클래스.
 * UserMapper.java, UserService.java에서 사용되며,
 * VacationVO.java, AttendanceRecordVO.java, ScheduleVO.java 등에서 userId/username으로 참조됨.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserVO {

    /** 사용자 고유 식별자 (로그인 ID) */
    private String id;
    /** 사용자 이름 (표시 이름) */
    private String username;
    /** 암호화된 비밀번호 */
    private String password;
    /** 직급 (예: 사원, 대리, 과장 등) */
    private String position;
    /** 잔여 휴가 일수 (소수점 지원, 반차 고려) */
    private Double remainingVacation;
    /** 관리자 여부 (1: 관리자, 0: 일반 사용자) */
    private Integer isAdmin;
    /** 소속 팀 이름 (TeamNoticeVO.java, TeamArchiveVO.java, ScheduleVO.java 참조) */
    private String team;
    /** 팀장 여부 (1: 팀장, 0: 일반 팀원) */
    private Integer isTeamLeader;
}
