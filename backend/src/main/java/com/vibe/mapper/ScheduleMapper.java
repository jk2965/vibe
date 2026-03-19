package com.vibe.mapper;

import com.vibe.model.ScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

// ScheduleMapper.xml과 연결됨 — 일정(캘린더) 관련 DB 접근 인터페이스
// ScheduleService.java에서 호출됨 / UserMapper.java와 연계하여 사용자·팀 정보 사용
@Mapper
public interface ScheduleMapper {
    // 전체 일정 목록을 조회 (관리자용 또는 공용 일정 포함)
    List<ScheduleVO> findAll();
    // 특정 팀의 일정 목록을 조회 (팀 캘린더 필터링 시 호출)
    List<ScheduleVO> findByTeam(String team);
    // 특정 사용자(userId)의 개인 일정 목록을 조회
    List<ScheduleVO> findByUserId(String userId);
    // 일정 ID로 단일 일정을 조회
    ScheduleVO findById(String id);
    // 새 일정을 DB에 등록
    void insert(ScheduleVO schedule);
    // 기존 일정 내용을 수정 (날짜, 제목, 설명 등)
    void update(ScheduleVO schedule);
    // 일정을 DB에서 삭제
    void delete(String id);
}
