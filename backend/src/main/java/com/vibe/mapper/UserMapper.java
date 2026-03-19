package com.vibe.mapper;

import com.vibe.model.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// UserMapper.xml과 연결됨 — 사용자 계정·팀 정보 관련 DB 접근 인터페이스
// UserService.java에서 호출됨
@Mapper
public interface UserMapper {
    // 사용자 ID로 단일 사용자 정보를 조회
    UserVO findById(String id);
    // 전체 사용자 목록을 조회
    List<UserVO> findAll();
    // 특정 팀에 속한 사용자 목록을 조회
    List<UserVO> findByTeam(String team);
    // 신규 사용자를 DB에 등록
    void insertUser(UserVO user);
    // 사용자의 잔여 연차 일수를 수정 (VacationService.java에서도 호출됨)
    void updateRemainingVacation(UserVO user);
    // 사용자 기본 정보(이름, 연락처 등)를 수정
    void updateUserInfo(UserVO user);
    // 사용자의 소속 팀을 변경
    void updateTeam(UserVO user);
    // 해당 팀에 팀장 역할을 가진 사용자 수를 반환 (팀장 중복 방지 용도)
    int countTeamLeaderByTeam(String team);
}
