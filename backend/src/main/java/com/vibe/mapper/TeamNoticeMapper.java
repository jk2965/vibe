package com.vibe.mapper;

import com.vibe.model.TeamNoticeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// TeamNoticeMapper.xml과 연결됨 — 팀별 공지사항 관련 DB 접근 인터페이스
// TeamNoticeService.java에서 호출됨 / UserMapper.java와 연계하여 팀 정보 사용
@Mapper
public interface TeamNoticeMapper {
    // 전체 팀 공지사항 목록을 조회 (관리자용)
    List<TeamNoticeVO> findAll();
    // 특정 팀의 공지사항 목록만 조회 (팀원 목록 화면에서 호출)
    List<TeamNoticeVO> findByTeam(String team);
    // 팀 공지사항 ID로 단일 공지사항을 조회
    TeamNoticeVO findById(String id);
    // 새 팀 공지사항을 DB에 등록
    void insert(TeamNoticeVO notice);
    // 기존 팀 공지사항 내용을 수정
    void update(TeamNoticeVO notice);
    // 팀 공지사항 조회수를 1 증가 (상세 조회 시 호출)
    void incrementViews(String id);
    // 팀 공지사항을 DB에서 삭제
    void delete(String id);
    // 팀 공지사항 필독 여부를 설정 (1=필독, 0=해제)
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    // 필독 팀 공지사항 목록을 조회
    List<TeamNoticeVO> findRequired();
    // 특정 작성자의 팀 공지사항 목록을 조회
    List<TeamNoticeVO> findByAuthor(String authorId);
}
