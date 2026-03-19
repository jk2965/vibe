package com.vibe.mapper;

import com.vibe.model.TeamArchiveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// TeamArchiveMapper.xml과 연결됨 — 팀별 자료실 게시글 관련 DB 접근 인터페이스
// TeamArchiveService.java에서 호출됨 / UserMapper.java와 연계하여 팀 정보 사용
@Mapper
public interface TeamArchiveMapper {
    // 특정 팀의 자료실 게시글 목록을 조회 (팀원만 접근 가능)
    List<TeamArchiveVO> findByTeam(String team);
    // 팀 자료실 게시글 ID로 단일 게시글을 조회
    TeamArchiveVO findById(String id);
    // 새 팀 자료실 게시글을 DB에 등록
    void insert(TeamArchiveVO archive);
    // 기존 팀 자료실 게시글 내용을 수정
    void update(TeamArchiveVO archive);
    // 팀 자료실 게시글 조회수를 1 증가 (상세 조회 시 호출)
    void incrementViews(String id);
    // 팀 자료실 게시글을 DB에서 삭제
    void delete(String id);
    // 팀 자료실 게시글 필독 여부를 설정 (1=필독, 0=해제)
    void setRequired(@Param("id") String id, @Param("isRequired") int isRequired);
    // 필독 팀 자료실 게시글 목록을 조회
    List<TeamArchiveVO> findRequired();
}
