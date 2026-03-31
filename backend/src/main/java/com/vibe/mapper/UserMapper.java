package com.vibe.mapper;

import com.vibe.model.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 사용자 계정·팀 정보 관련 DB 접근 인터페이스.
 * UserMapper.xml과 연결됨. UserService.java에서 호출됨.
 */
@Mapper
public interface UserMapper {
    /** 사용자 ID로 단건 조회 */
    UserVO findById(String id);
    /** 전체 사용자 목록 조회 */
    List<UserVO> findAll();
    /** 특정 팀에 속한 사용자 목록 조회 */
    List<UserVO> findByTeam(String team);
    /** 신규 사용자 등록 */
    void insertUser(UserVO user);
    /** 잔여 연차 일수 수정 (VacationService.java에서도 호출) */
    void updateRemainingVacation(UserVO user);
    /** 사용자 기본 정보(이름·직급 등) 수정 */
    void updateUserInfo(UserVO user);
    /** 사용자 소속 팀 변경 */
    void updateTeam(UserVO user);
    /** 특정 팀의 팀장 수 반환 (팀장 중복 방지용) */
    int countTeamLeaderByTeam(String team);
}
